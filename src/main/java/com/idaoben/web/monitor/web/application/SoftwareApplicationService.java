package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.monitor.dao.entity.Favorite;
import com.idaoben.web.monitor.dao.entity.Software;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.FavoriteService;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.service.SoftwareService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.SoftwareAddCommand;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SoftwareApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SoftwareApplicationService.class);

    @Resource
    private SoftwareService softwareService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private JniService jniService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private MonitorApplicationService monitorApplicationService;

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

    private Map<String, List<ProcessJson>> processMaps = new HashMap();

    public List<SoftwareDto> getSystemSoftware(){
        List<Favorite> favorites = favoriteService.findAll();
        String startMenuHome = SystemUtils.getOsHome() + "ProgramData\\Microsoft\\Windows\\Start Menu\\Programs";
        File startMenu = new File(startMenuHome);
        List<SoftwareDto> softwares = new ArrayList<>();
        List<File> linkFiles = new ArrayList<>();
        if(startMenu.exists() && startMenu.isDirectory()){
            for(File file : startMenu.listFiles()){
                if(!file.isDirectory()){
                    if(checklnkFile(file)){
                        linkFiles.add(file);
                    }
                } else {
                    //只搜索一层文件夹
                    for(File fileChild : file.listFiles()){
                        if(!fileChild.isDirectory()){
                            if(checklnkFile(fileChild)){
                                linkFiles.add(fileChild);
                            }
                        }
                    }
                }
            }
        }
        List<String> linkPaths = linkFiles.stream().map(File::getPath).collect(Collectors.toList());
        String content = jniService.queryLinkInfos(linkPaths);
        Map<String, LinkFileJson> linkFileJsonMap = new HashMap<>();
        try {
            LinkListJson linkListJson = objectMapper.readValue(content, LinkListJson.class);
            linkListJson.getDetails().forEach(linkFileJson -> linkFileJsonMap.put(linkFileJson.getLinkPath(), linkFileJson));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        for(File linkFile : linkFiles){
            softwares.add(getSoftwareInfo(linkFile, favorites, linkFileJsonMap.get(linkFile.getPath())));
        }

        //增加添加到数据库的软件
        List<Software> softwareDbs = softwareService.findAll();
        for(Software software : softwareDbs){
            softwares.add(getSoftwareInfo(software, favorites));
        }
        Map<String, SoftwareDto> tempSoftwareMap = new HashMap<>();
        Map<String, String> tempExeNameSoftwareIdMap = new HashMap<>();
        softwares.forEach(softwareDto -> {
            tempSoftwareMap.put(softwareDto.getId(), softwareDto);
            if(StringUtils.isNotEmpty(softwareDto.getExeName())){
                tempExeNameSoftwareIdMap.put(softwareDto.getExeName().toLowerCase(), softwareDto.getId());
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
        Software software = new Software();
        //TODO: 参数需要正确设置
        software.setSoftwareName(command.getExePath());
        software.setCommandLine(command.getExePath());
        software.setExeName(command.getExePath());
        software.setExePath(command.getExePath());
        software.setExeName(command.getExePath());
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
        List<ProcessJson> processJsons = processMaps.get(softwareDto.getId());
        if(!CollectionUtils.isEmpty(processJsons)){
            List<ProcessDto> processes = new ArrayList<>();
            for(ProcessJson processJson : processJsons){
                ProcessDto process = new ProcessDto();
                process.setPid(String.valueOf(processJson.getPid()));
                process.setName(processJson.getImageName());
                //TODO：CPU使用时间待补充
                process.setMemory(Float.parseFloat(processJson.getWsPrivateBytes()) / 1000);
                process.setMonitoring(monitorApplicationService.isPidMonitoring(softwareDto.getId(), process.getPid()));
                processes.add(process);
            }
            detail.setProcesses(processes);
        }
        return detail;
    }

    private boolean checklnkFile(File file){
        return file.getName().endsWith(".lnk");
    }

    private SoftwareDto getSoftwareInfo(File lnkFile, List<Favorite> favorites, LinkFileJson linkFileJson){
        SoftwareDto softwareDto = new SoftwareDto();
        softwareDto.setId(lnkFile.getPath());
        softwareDto.setLnkPath(softwareDto.getId());
        softwareDto.setFavorite(favorites.contains(softwareDto.getId()));
        softwareDto.setSoftwareName(lnkFile.getName().replace(".lnk", ""));
        if(linkFileJson != null){
            //TODO: 命令行需要exe+参数结合，需要具体测试确认
            File file = new File(linkFileJson.getPath());
            softwareDto.setCommandLine(linkFileJson.getPath());
            //TODO: 图标需要再处理
            softwareDto.setBase64Icon(linkFileJson.getIconLocation());
            softwareDto.setExecutePath(linkFileJson.getWorkingDirectory());
            softwareDto.setExeName(file.getName());
        }
        //判断当前是否监控中
        softwareDto.setMonitoring(monitorApplicationService.isMonitoring(softwareDto.getId()));
        return softwareDto;
    }

    private SoftwareDto getSoftwareInfo(Software software, List<Favorite> favorites){
        SoftwareDto softwareDto = new SoftwareDto();
        BeanUtils.copyProperties(software, softwareDto, "id");
        softwareDto.setId(String.valueOf(software.getId()));
        softwareDto.setFavorite(favorites.contains(softwareDto.getId()));
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
        String processContent = jniService.listAllProcesses();
        try {
            ProcessListJson processListJson = objectMapper.readValue(processContent, ProcessListJson.class);
            Map<String, List<ProcessJson>> tempProcessMaps = new HashMap<>();
            if(processListJson != null && processListJson.getProcesses() != null){
                Map<Integer, ProcessJson> pidProcessMap = new HashMap<>();
                processListJson.getProcesses().forEach(processJsonDto -> pidProcessMap.put(processJsonDto.getPid(), processJsonDto));
                //把所有进程组装到对应的软件中
                for(ProcessJson processJson : processListJson.getProcesses()){
                    //先找父对象是否存在，存在父对象时直接挂到父对象的进程列表中
                    ProcessJson parentProcess = pidProcessMap.get(processJson.getParentPid());
                    String softwareId;
                    if(parentProcess != null){
                        softwareId = getSoftwareIdFromImageName(parentProcess.getImageName());
                    } else {
                        //无父对象，则单独添加
                        softwareId = getSoftwareIdFromImageName(processJson.getImageName());
                    }
                    if(softwareId != null){
                        List<ProcessJson> softwareProcesses = tempProcessMaps.get(softwareId);
                        if(softwareProcesses == null){
                            softwareProcesses = new ArrayList<>();
                            tempProcessMaps.put(softwareId, softwareProcesses);
                        }
                        softwareProcesses.add(processJson);

                        //判断是否当前软件正在监听，但是当前进程未在监控中，这时尝试重新监听，已监控失败的不再重试
                        String pidStr = String.valueOf(processJson.getPid());
                        if(monitorApplicationService.isMonitoring(softwareId) && !monitorApplicationService.isPidMonitoringError(softwareId, pidStr) && !monitorApplicationService.isPidMonitoring(softwareId, pidStr)){
                            monitorApplicationService.startMonitorPid(softwareId, pidStr);
                        }
                    }
                }
                processMaps = tempProcessMaps;
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSoftwareIdFromImageName(String imageName){
        return exeNameSoftwareIdMap.get(imageName.toLowerCase());
    }
}
