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
import com.idaoben.web.monitor.service.impl.LinuxSystemOsServiceImpl;
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
    private TaskService taskService;

    @Resource
    private JniService jniService;

    @Resource
    private ObjectMapper objectMapper;

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

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
        File file = getExeFileFromCmd(command.getCommandLine());
        Software software = new Software();
        software.setSoftwareName(file.getName());
        software.setCommandLine(command.getCommandLine());
        String executePath = file.getParent();
        software.setExecutePath(executePath == null ? "" : executePath);
        software.setExeName(file.getName());
        software.setExePath(file.getPath());
        softwareService.save(software);
    }

    public void removeSoftware(SoftwareIdCommand command){
        taskService.deleteTaskAndActionBySoftwareId(command.getId());
        softwareService.delete(Long.parseLong(command.getId()));
    }

    private File getExeFileFromCmd(String cmd){
        String exePath = StringUtils.substringBefore(cmd, " ");
        return new File(exePath);
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
        List<ProcessJson> processJsons = monitoringService.getProcessPids(softwareDto.getId());
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
        softwareDto.setAddByUser(true);
        return softwareDto;
    }

    public SoftwareDto getSoftwareInfo(String softwareId){
        if(softwareMap == null){
            getSystemSoftware();
        }
        return softwareMap.get(softwareId);
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
            Iterator<ProcessJson> processIt = processJsons.iterator();
            while (processIt.hasNext()){
                ProcessJson process = processIt.next();
                //忽略本应用和Linux下tracer的进程，避免这些进程进行监控和下面的进程树状处理（本软件打开的软件会被归类到本软件下）
                if(SystemUtils.getCurrentPid().equals(String.valueOf(process.getPid())) || (SystemUtils.isLinux() && process.getImageName().startsWith(LinuxSystemOsServiceImpl.TRACER_CMD))){
                    processIt.remove();
                } else {
                    pidProcessMap.put(process.getPid(), process);
                }
            }

            //Windows平台移除pidUserMap中已经关闭的进程
            if(SystemUtils.isWindows()) {
                Set<Integer> pids = pidUserMap.keySet();
                for (Integer pid : pids) {
                    if (!pidProcessMap.containsKey(pid)) {
                        pidProcessMap.remove(pid);
                    }
                }
            }

            //把进程组装成树状结构
            List<ProcessJson> processTree = new ArrayList<>();
            processJsons.forEach(process -> {
                if(process.getParentPid() != null && process.getParentPid() != 0 && pidProcessMap.containsKey(process.getParentPid())){
                    ProcessJson processParent = pidProcessMap.get(process.getParentPid());
                    if(processParent.getChildren() == null){
                        processParent.setChildren(new ArrayList<>());
                    }
                    processParent.getChildren().add(process);
                } else {
                    processTree.add(process);
                }
            });

            //把所有进程组装到对应的软件中
            List<Integer> checkUserPids = new ArrayList<>();
            handleProcessJson(processTree, null, checkUserPids, tempProcessMaps);

            //Windows平台把需要重新查询用户的进程再次查询用户信息
            if(SystemUtils.isWindows() && !checkUserPids.isEmpty()){
                List<ProcessJson> pidUsers = getPidUsers(checkUserPids);
                if(!CollectionUtils.isEmpty(pidUsers)){
                    for(ProcessJson process : pidUsers){
                        ProcessJson processJson = pidProcessMap.get(process.getPid());
                        if(processJson != null){
                            processJson.setUser(process.getUser());
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

            monitoringService.setProcessMaps(tempProcessMaps);
        }
    }

    private void handleProcessJson(List<ProcessJson> processJsons, ProcessJson parentProcess, List<Integer> checkUserPids, Map<String, List<ProcessJson>> tempProcessMaps){
        for(ProcessJson processJson : processJsons){
            //Windows平台需要再次去查询对应进程的用户ID
            checkAndAddUserPidsForWindows(processJson, checkUserPids);

            //通过进程名称查询对应的软件
            String softwareId = getSoftwareIdFromImageName(processJson, parentProcess == null);
            if(softwareId == null) {
                //找不到softwareId的，现从正在监听的id列表中反向查询
                softwareId = monitoringService.getMonitoringSoftwareIdByPid(String.valueOf(processJson.getPid()));
            }
            if(softwareId != null){
                //找到对应的softwareId了，做对应的设置，并且把所有子进程都设置到当前软件中
                List<ProcessJson> softwareProcesses = tempProcessMaps.computeIfAbsent(softwareId, key -> new ArrayList<>());
                addProcessToSoftware(softwareId, processJson, softwareProcesses, checkUserPids);
            } else {
                //对子进程再进行处理
                if(processJson.getChildren() != null){
                    handleProcessJson(processJson.getChildren(), processJson, checkUserPids, tempProcessMaps);
                }

            }
        }
    }

    private void addProcessToSoftware(String softwareId, ProcessJson processJson, List<ProcessJson> softwareProcesses, List<Integer> checkUserPids){
        softwareProcesses.add(processJson);

        //对需要自动加入监听的程序，进行自动监听
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

        if(processJson.getChildren() != null){
            for(ProcessJson child : processJson.getChildren()){
                //Windows平台下对所有子进程进行用户设置
                checkAndAddUserPidsForWindows(child, checkUserPids);

                addProcessToSoftware(softwareId, child, softwareProcesses, checkUserPids);
            }
        }
    }

    private List<ProcessJson> getPidUsers(List<Integer> pids){
        String processDetailContent = jniService.queryProcessDetails(pids);
        try {
            ProcessDetailsJson processDetailsJson = objectMapper.readValue(processDetailContent, ProcessDetailsJson.class);
            if(processDetailsJson != null){
                List<ProcessJson> processes = processDetailsJson.getDetails();
                for(ProcessJson process : processes){
                    pidUserMap.put(process.getPid(), process.getUser());
                }
                return processes;
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private String getSoftwareIdFromImageName(ProcessJson process, boolean isRootNode){
        String imageName = process.getImageName();
        if(SystemUtils.isWindows()){
            if(isRootNode && "explorer.exe".equals(imageName)){
                //根进程为explorer.exe的不认为是系统对应软件
                return null;
            }
            return exeNameSoftwareIdMap.get(imageName.toLowerCase());
        } else {
            //For linux
            if(imageName.contains(LinuxSystemOsServiceImpl.TRACER_CMD)){
                //Don't show the tracer process
                return null;
            }
            //Get exeName, the same method for addCmdSoftware
            File exeFile = getExeFileFromCmd(imageName);
            return exeNameSoftwareIdMap.get(exeFile.getName());
        }
    }

    private void checkAndAddUserPidsForWindows(ProcessJson processJson, List<Integer> checkUserPids){
        if(SystemUtils.isWindows()){
            String user = pidUserMap.get(processJson.getPid());
            if(user == null){
                checkUserPids.add(processJson.getPid());
            } else {
                processJson.setUser(user);
            }
        }
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
