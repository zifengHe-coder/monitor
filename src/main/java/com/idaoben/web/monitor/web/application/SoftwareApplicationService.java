package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DateTimeUtils;
import com.idaoben.web.monitor.dao.entity.Favorite;
import com.idaoben.web.monitor.dao.entity.Software;
import com.idaoben.web.monitor.dao.entity.enums.MonitorStatus;
import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.FavoriteService;
import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.service.SoftwareService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.impl.MonitoringTask;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.FileListCommand;
import com.idaoben.web.monitor.web.command.SoftwareAddCommand;
import com.idaoben.web.monitor.web.command.SoftwareCmdAddCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SoftwareApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SoftwareApplicationService.class);

    @Resource
    private SoftwareService softwareService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private MonitorApplicationService monitorApplicationService;

    @Resource
    private SystemOsService systemOsService;

    @Resource
    private MonitoringService monitoringService;

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

    private Map<String, List<ProcessJson>> processMaps = new HashMap();

    public List<SoftwareDto> getSystemSoftware(){
        List<String> favoriteSoftwareIds = favoriteService.findAll().stream().map(Favorite::getSoftwareId).collect(Collectors.toList());

        List<SoftwareDto> softwares = systemOsService.getSystemSoftware();
        //增加添加到数据库的软件
        List<Software> softwareDbs = softwareService.findAll();
        for(Software software : softwareDbs){
            softwares.add(getSoftwareInfo(software));
        }
        Map<String, SoftwareDto> tempSoftwareMap = new HashMap<>();
        Map<String, String> tempExeNameSoftwareIdMap = new HashMap<>();
        softwares.forEach(software -> {
            software.setFavorite(favoriteSoftwareIds.contains(software.getId()));
            if(StringUtils.isNotEmpty(software.getExePath())){
                File exeFile = new File(software.getExePath());
                if(exeFile.exists()){
                    software.setFileSize(new BigDecimal(exeFile.length()).divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP));
                    software.setFileCreationTime(DateTimeUtils.convertZonedDateTime(new Date(exeFile.lastModified())));
                }
            }
            tempSoftwareMap.put(software.getId(), software);
            if(StringUtils.isNotEmpty(software.getExeName())){
                tempExeNameSoftwareIdMap.put(software.getExeName().toLowerCase(), software.getId());
            }
        });
        softwareMap = tempSoftwareMap;
        exeNameSoftwareIdMap = tempExeNameSoftwareIdMap;
        return softwares;
    }

    public void addSoftware(SoftwareAddCommand command){
        if(softwareService.exists(Filters.query().eq(Software::getExePath, command.getExePath()))){
            throw ServiceException.of(ErrorCode.SOFTWARE_ALREADY_EXISTS);
        }
        File file = new File(command.getExePath());
        Software software = new Software();
        software.setSoftwareName(file.getName());
        software.setCommandLine(file.getPath());
        software.setExecutePath(file.getParent());
        software.setExeName(file.getName());
        software.setExePath(file.getPath());
        softwareService.save(software);
    }

    public void addCmdSoftware(SoftwareCmdAddCommand command){
        if(softwareService.exists(Filters.query().eq(Software::getExePath, command.getCommandLine()))){
            throw ServiceException.of(ErrorCode.SOFTWARE_ALREADY_EXISTS);
        }
        //Get the exe file
        String exePath = StringUtils.substringBefore(command.getCommandLine(), " ");
        File file = new File(exePath);
        Software software = new Software();
        software.setSoftwareName(file.getName());
        software.setCommandLine(command.getCommandLine());
        software.setExecutePath(file.getParent());
        software.setExeName(file.getName());
        software.setExePath(file.getPath());
        softwareService.save(software);
    }

    public void addFavorite(SoftwareIdCommand command){
        if(!favoriteService.exists(Filters.query().eq(Favorite::getSoftwareId, command.getId()))){
            Favorite favorite = new Favorite();
            favorite.setSoftwareId(command.getId());
            favoriteService.save(favorite);
        }
    }

    public void removeFavorite(SoftwareIdCommand command){
        List<Favorite> favorites = favoriteService.findList(Filters.query().eq(Favorite::getSoftwareId, command.getId()), null);
        favoriteService.deleteInBatch(favorites);
    }

    public SoftwareDetailDto detailSoftware(SoftwareIdCommand command){
        SoftwareDto softwareDto = getSoftwareInfo(command.getId());
        if(softwareDto == null){
            throw  ServiceException.of(ErrorCode.SYSTEM_ERROR);
        }
        SoftwareDetailDto detail = new SoftwareDetailDto();
        BeanUtils.copyProperties(softwareDto, detail);
        MonitoringTask monitoringTask = monitoringService.getMonitoringTask(softwareDto.getId());
        detail.setMonitoring(monitoringTask == null ? false : true);
        if(monitoringTask != null){
            detail.setTaskId(monitoringTask.getTaskId());
        }
        List<ProcessJson> processJsons = processMaps.get(softwareDto.getId());
        if(!CollectionUtils.isEmpty(processJsons)){
            List<ProcessDto> processes = new ArrayList<>();
            for(ProcessJson processJson : processJsons){
                ProcessDto process = new ProcessDto();
                process.setPid(String.valueOf(processJson.getPid()));
                process.setName(processJson.getImageName());
                //TODO：CPU使用时间待补充
                process.setMemory(Float.parseFloat(processJson.getWsPrivateBytes()) / 1024);
                if(systemOsService.isAutoMonitorChildProcess()){
                    //需要自动监听子进程的，则根据具体的当前监听进程判断
                    MonitorStatus monitorStatus;
                    if(monitoringService.isPidMonitoring(softwareDto.getId(), process.getPid())){
                        monitorStatus = MonitorStatus.MONITORING;
                    } else if(monitoringService.isPidMonitoringError(softwareDto.getId(), process.getPid())){
                        monitorStatus = MonitorStatus.ERROR;
                    } else {
                        monitorStatus = MonitorStatus.NOT_MONITOR;
                    }
                    process.setMonitorStatus(monitorStatus);
                } else {
                    //只要判断当前主进程是否正在监听即可
                    process.setMonitorStatus(detail.isMonitoring() ? MonitorStatus.MONITORING : MonitorStatus.NOT_MONITOR);
                }
                processes.add(process);
            }
            detail.setProcesses(processes);
        }
        return detail;
    }

    private SoftwareDto getSoftwareInfo(Software software){
        SoftwareDto softwareDto = new SoftwareDto();
        BeanUtils.copyProperties(software, softwareDto, "id");
        softwareDto.setId(String.valueOf(software.getId()));
        softwareDto.setBase64Icon(systemOsService.getIconBase64(new File(software.getExePath())));
        return softwareDto;
    }

    public SoftwareDto getSoftwareInfo(String softwareId){
        if(softwareMap == null){
            getSystemSoftware();
        }
        return softwareMap.get(softwareId);
    }

    public List<ProcessJson> getProcessPids(String softwareId){
        return processMaps.get(softwareId);
    }

    @Scheduled(cron = "*/1 * * * * ?")
    public void refreshProcess() {
        if(softwareMap == null){
            //先做一次软件列表初始化
            getSystemSoftware();
        }
        List<ProcessJson> processJsons = systemOsService.listAllProcesses();
        Map<String, List<ProcessJson>> tempProcessMaps = new HashMap<>();
        if(!CollectionUtils.isEmpty(processJsons)){
            Map<Integer, ProcessJson> pidProcessMap = new HashMap<>();
            processJsons.forEach(processJsonDto -> pidProcessMap.put(processJsonDto.getPid(), processJsonDto));
            //把所有进程组装到对应的软件中
            for(ProcessJson processJson : processJsons){
                //先找父对象是否存在，存在父对象时直接挂到父对象的进程列表中
                ProcessJson parentProcess = pidProcessMap.get(processJson.getParentPid());
                String softwareId = null;
                if(parentProcess != null){
                    softwareId = getSoftwareIdFromImageName(parentProcess.getImageName());
                }
                if(softwareId == null){
                    //无父对象，或父对象找不到的，则单独添加
                    softwareId = getSoftwareIdFromImageName(processJson.getImageName());
                }
                if(softwareId == null) {
                    //找不到softwareId的，现从正在监听的id列表中反向查询
                    softwareId = monitoringService.getMonitoringSoftwareIdByPid(String.valueOf(processJson.getPid()));
                }
                if(softwareId != null){
                    List<ProcessJson> softwareProcesses = tempProcessMaps.get(softwareId);
                    if(softwareProcesses == null){
                        softwareProcesses = new ArrayList<>();
                        tempProcessMaps.put(softwareId, softwareProcesses);
                    }
                    softwareProcesses.add(processJson);

                    if(systemOsService.isAutoMonitorChildProcess()){
                        //判断是否当前软件正在监听，但是当前进程未在监控中，这时尝试重新监听，已监控失败的不再重试
                        String pidStr = String.valueOf(processJson.getPid());
                        if(monitoringService.isMonitoring(softwareId) && !monitoringService.isPidMonitoringError(softwareId, pidStr) && !monitoringService.isPidMonitoring(softwareId, pidStr)){
                            monitorApplicationService.startMonitorPid(softwareId, pidStr);
                        }
                    }
                }
            }

            //判断当前是否有正在监控的进程已关闭的，如果已经关闭主动结束监听
            Set<String> monitoringSoftwareIds = monitoringService.getMonitoringSoftwareIds();
            for(String monitoringSoftwareId : monitoringSoftwareIds){
                if(!tempProcessMaps.containsKey(monitoringSoftwareId)){
                    monitorApplicationService.stopMonitor(monitoringSoftwareId, false);
                }
            }

            processMaps = tempProcessMaps;
        }
    }

    private String getSoftwareIdFromImageName(String imageName){
        if(SystemUtils.getSystemOs() == SystemOs.WINDOWS){
            return exeNameSoftwareIdMap.get(imageName.toLowerCase());
        } else {
            //For linux
            for(SoftwareDto software : softwareMap.values()){
                if(imageName.contains(software.getExeName())){
                    return software.getId();
                }
            }
        }
        return null;
    }

    public List<FileDto> listFiles(FileListCommand command){
        List<FileDto> files = new ArrayList<>();
        File[] fileChildren;
        if(StringUtils.isEmpty(command.getPath())){
            fileChildren = File.listRoots();
        } else {
            File folder = new File(command.getPath());
            if(!folder.exists() || !folder.isDirectory()){
                throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
            }
            fileChildren = folder.listFiles();
        }
        if(fileChildren != null){
            for(File file : fileChildren){
                //过滤文件后缀
                if(!file.isDirectory() && !systemOsService.isExeFile(file)){
                    continue;
                }
                FileDto fileDto = new FileDto();
                fileDto.setName(file.getName());
                fileDto.setPath(file.getPath());
                fileDto.setDirectory(file.isDirectory());
                files.add(fileDto);
            }
        }
        Collections.sort(files, (f1, f2) -> {
            if(f1.isDirectory() && !f2.isDirectory()){
                return -1;
            } else if(!f1.isDirectory() && f2.isDirectory()){
                return 1;
            }
            return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        });
        return files;
    }
}
