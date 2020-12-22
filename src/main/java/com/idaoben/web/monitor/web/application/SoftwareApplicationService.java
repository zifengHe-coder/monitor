package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DateTimeUtils;
import com.idaoben.web.monitor.dao.entity.Favorite;
import com.idaoben.web.monitor.dao.entity.Software;
import com.idaoben.web.monitor.dao.entity.enums.MonitorStatus;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.*;
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
import java.util.concurrent.ConcurrentHashMap;
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

    @Resource
    private JniService jniService;

    @Resource
    private ObjectMapper objectMapper;

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

    private Map<String, List<ProcessJson>> processMaps = new HashMap();

    private Map<Integer, String> pidUserMap = new ConcurrentHashMap<>();

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
                process.setUser(processJson.getUser());
                process.setName(processJson.getImageName());
                process.setCpu(processJson.getCpuTime());
                process.setMemory(Long.parseLong(processJson.getWsPrivateBytes()) / 1024);
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
                    if(monitoringTask != null){
                        if(!monitoringTask.getErrorPids().isEmpty()){
                            process.setMonitorStatus(MonitorStatus.ERROR);
                        } else {
                            process.setMonitorStatus(MonitorStatus.MONITORING);
                        }
                    } else {
                        process.setMonitorStatus(MonitorStatus.NOT_MONITOR);
                    }
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

            //Windows平台移除pidUserMap中已经关闭的进程
            if(SystemUtils.isWindows()) {
                Set<Integer> pids = pidUserMap.keySet();
                for (Integer pid : pids) {
                    if (!pidProcessMap.containsKey(pid)) {
                        pidProcessMap.remove(pid);
                    }
                }
            }

            //把所有进程组装到对应的软件中
            List<Integer> checkUserPids = new ArrayList<>();
            for(ProcessJson processJson : processJsons){
                //Windows平台需要再次去查询对应进程的用户ID
                if(SystemUtils.isWindows()){
                    String user = pidUserMap.get(processJson.getPid());
                    if(user == null){
                        checkUserPids.add(processJson.getPid());
                    } else {
                        processJson.setUser(user);
                    }
                }

                //先找父对象是否存在，存在父对象时直接挂到父对象的进程列表中
                ProcessJson parentProcess = pidProcessMap.get(processJson.getParentPid());
                String softwareId = null;
                if(parentProcess != null){
                    softwareId = getSoftwareIdFromImageName(parentProcess.getImageName(), true);
                }
                if(softwareId == null){
                    //无父对象，或父对象找不到的，则单独添加
                    softwareId = getSoftwareIdFromImageName(processJson.getImageName(), false);
                }
                if(softwareId == null) {
                    //找不到softwareId的，现从正在监听的id列表中反向查询
                    softwareId = monitoringService.getMonitoringSoftwareIdByPid(String.valueOf(processJson.getPid()));
                }
                if(softwareId != null){
                    List<ProcessJson> softwareProcesses = tempProcessMaps.computeIfAbsent(softwareId, key -> new ArrayList<>());
                    softwareProcesses.add(processJson);

                    if(systemOsService.isAutoMonitorChildProcess()){
                        //判断是否当前软件正在监听，但是当前进程未在监控中，这时尝试重新监听，已监控失败的不再重试
                        String pidStr = String.valueOf(processJson.getPid());
                        //Only Windows Need to auto add monitor
                        if(SystemUtils.isWindows() && monitoringService.isMonitoring(softwareId)
                                && !monitoringService.isPidMonitoringError(softwareId, pidStr) && !monitoringService.isPidMonitoring(softwareId, pidStr)){
                            String user = processJson.getUser();
                            //Windows平台如果当前processJson无user信息，则重新获取
                            if(user == null && SystemUtils.isWindows()){
                                List<ProcessJson> pidUsers = getPidUsers(Collections.singletonList(processJson.getPid()));
                                if(!CollectionUtils.isEmpty(pidUsers)){
                                    user = pidUsers.get(0).getUser();
                                }
                            }
                            monitorApplicationService.startMonitorPid(softwareId, pidStr, user);
                        }
                    }
                }
            }

            //Windows平台把需要重新查询用户的进程再次查询用户信息
            if(SystemUtils.isWindows() && !checkUserPids.isEmpty()){
                List<ProcessJson> pidUsers = getPidUsers(checkUserPids);
                for(ProcessJson process : pidUsers){
                    ProcessJson processJson = pidProcessMap.get(process.getPid());
                    if(processJson != null){
                        processJson.setUser(process.getUser());
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

    private List<ProcessJson> getPidUsers(List<Integer> pids){
        String processDetailContent = jniService.queryProcessDetails(pids);
        try {
            ProcessDetailsJson processDetailsJson = objectMapper.readValue(processDetailContent, ProcessDetailsJson.class);
            if(processDetailsJson != null){
                return processDetailsJson.getDetails();
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private String getSoftwareIdFromImageName(String imageName, boolean isParent){
        if(SystemUtils.isWindows()){
            if(isParent && "explorer.exe".equals(imageName)){
                //如果父进程是explorer.exe的，不把他当作explorer.exe的软件
                return null;
            }
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
