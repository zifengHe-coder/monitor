package com.idaoben.web.monitor.web.application;

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
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "monitor.software")
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
    private ObjectMapper objectMapper;

    private Map<String, String> map;

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

    public List<SoftwareDto> getSystemSoftware(){
        List<String> favoriteSoftwareIds = favoriteService.findAll().stream().map(Favorite::getSoftwareId).collect(Collectors.toList());

        List<SoftwareDto> softwares = systemOsService.getSystemSoftware();
        //?????????????????????????????????
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
                    //???????????????????????????????????????????????????????????????????????????
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
                    //???????????????????????????????????????????????????
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
            //?????????????????????????????????
            getSystemSoftware();
        }
        List<ProcessJson> processJsons = systemOsService.listAllProcesses();

        Map<String, List<ProcessJson>> tempProcessMaps = new HashMap<>();
        if(!CollectionUtils.isEmpty(processJsons)){

            Map<Integer, ProcessJson> pidProcessMap = new HashMap<>();
            Iterator<ProcessJson> processIt = processJsons.iterator();
            while (processIt.hasNext()){
                ProcessJson process = processIt.next();
                //??????????????????Linux???tracer?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if(SystemUtils.getCurrentPid().equals(String.valueOf(process.getPid())) || (SystemUtils.isLinux() && process.getImageName().startsWith(LinuxSystemOsServiceImpl.TRACER_CMD))){
                    processIt.remove();
                } else {
                    pidProcessMap.put(process.getPid(), process);
                }
            }

            //??????????????????????????????
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

            //??????????????????????????????????????????
            handleProcessJson(processTree, null, tempProcessMaps);

            //?????????????????????????????????????????????????????????????????????????????????????????????
            Set<String> monitoringSoftwareIds = monitoringService.getMonitoringSoftwareIds();
            for(String monitoringSoftwareId : monitoringSoftwareIds){
                if(!tempProcessMaps.containsKey(monitoringSoftwareId)){
                    monitorApplicationService.stopMonitor(monitoringSoftwareId, false);
                }
            }

            monitoringService.setProcessMaps(tempProcessMaps);
        }
    }

    private void handleProcessJson(List<ProcessJson> processJsons, ProcessJson parentProcess, Map<String, List<ProcessJson>> tempProcessMaps){
        for(ProcessJson processJson : processJsons){

            //???????????????????????????????????????
            String softwareId = getSoftwareIdFromImageName(processJson, parentProcess == null);
            if(softwareId == null) {
                //?????????softwareId???????????????????????????id?????????????????????
                softwareId = monitoringService.getMonitoringSoftwareIdByPid(String.valueOf(processJson.getPid()));
            }
            if(softwareId != null){
                //???????????????softwareId??????????????????????????????????????????????????????????????????????????????
                List<ProcessJson> softwareProcesses = tempProcessMaps.computeIfAbsent(softwareId, key -> new ArrayList<>());
                addProcessToSoftware(softwareId, processJson, softwareProcesses);
            } else {
                //???????????????????????????
                if(processJson.getChildren() != null){
                    handleProcessJson(processJson.getChildren(), processJson, tempProcessMaps);
                }

            }
        }
    }

    private void addProcessToSoftware(String softwareId, ProcessJson processJson, List<ProcessJson> softwareProcesses){
        softwareProcesses.add(processJson);

        //?????????????????????????????????????????????????????????
        if(systemOsService.isAutoMonitorChildProcess()){
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            String pidStr = String.valueOf(processJson.getPid());
            //Only Windows Need to auto add monitor
            if(SystemUtils.isWindows() && monitoringService.isMonitoring(softwareId)
                    && !monitoringService.isPidMonitoringError(softwareId, pidStr) && !monitoringService.isPidMonitoring(softwareId, pidStr)){
                String user = processJson.getUser();
                //Windows??????????????????processJson???user????????????????????????(??????win7?????????????????????)
                if(StringUtils.isEmpty(user) && SystemUtils.isWindows()){
                    List<ProcessJson> pidUsers = systemOsService.getProcessByPids(Collections.singletonList(processJson.getPid()));
                    if(!CollectionUtils.isEmpty(pidUsers)){
                        user = pidUsers.get(0).getUser();
                    }
                }
                monitorApplicationService.startMonitorPid(softwareId, pidStr, user);
            }
        }

        if(processJson.getChildren() != null){
            for(ProcessJson child : processJson.getChildren()){
                addProcessToSoftware(softwareId, child, softwareProcesses);
            }
        }
    }

    private final static List<String> IGNORE_SYSTEM_IMAGE_NAMES = Arrays.asList("csrss.exe", "wininit.exe", "explorer.exe", "winlogon.exe");

    private String getSoftwareIdFromImageName(ProcessJson process, boolean isRootNode){
        String imageName = process.getImageName();
        if(SystemUtils.isWindows()){
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            //???firefox?????????????????????????????????\Device\HarddiskVolume4\Program Files\Mozilla Firefox\firefox.exe?????????????????????firefox.exe
            String exeName = StringUtils.substringAfterLast(imageName, "\\");
            imageName = StringUtils.isNotEmpty(exeName) ? exeName.toLowerCase() : imageName.toLowerCase();
            if(isRootNode && (StringUtils.isEmpty(imageName) || IGNORE_SYSTEM_IMAGE_NAMES.contains(imageName)) || !imageName.endsWith(".exe")){
                //???????????????????????????????????????????????????
                return null;
            }
            String softwareId = exeNameSoftwareIdMap.get(imageName);
            //??????????????????????????????softwareId??????????????????????????????????????????????????????????????????????????????imageName????????????????????????????????????????????????????????????????????????
            //??????Windows???TIM???????????????????????????????????????????????????????????????????????????????????????????????????
            if(softwareId == null && isRootNode){
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                Set<Map.Entry<String, SoftwareDto>> entries = softwareMap.entrySet();
                for(Map.Entry<String, SoftwareDto> entry : entries){
                    //imageName ??????.exe???????????????
                    SoftwareDto software = entry.getValue();
                    String imageNameWithoutExe = imageName.replace(".exe", "");
                    //?????????????????????????????????exe???????????????map????????????????????????????????????????????????
                    if(imageNameWithoutExe.equalsIgnoreCase(software.getSoftwareName()) || imageNameWithoutExe.equalsIgnoreCase(software.getExeName()) || Objects.equals(map.get(imageName), entry.getValue().getSoftwareName())){
                        softwareId = entry.getKey();
                        break;
                    }
                }
            }
            return softwareId;
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
                //??????????????????
                if(!file.isDirectory() && !systemOsService.isExeFile(file)){
                    continue;
                }
                FileDto fileDto = new FileDto();
                fileDto.setName(StringUtils.isEmpty(file.getName()) ? file.getPath() : file.getName());
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

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
