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
import com.idaoben.web.monitor.web.dto.ProcessJsonDto;
import com.idaoben.web.monitor.web.dto.ProcessListJsonDto;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, SoftwareDto> softwareMap;

    private Map<String, String> exeNameSoftwareIdMap = new HashMap<>();

    private Map<String, List<ProcessJsonDto>> processMaps = new HashMap();

    public List<SoftwareDto> getSystemSoftware(){
        List<Favorite> favorites = favoriteService.findAll();
        String startMenuHome = SystemUtils.getOsHome() + "ProgramData\\Microsoft\\Windows\\Start Menu\\Programs";
        File startMenu = new File(startMenuHome);
        List<SoftwareDto> softwares = new ArrayList<>();
        if(startMenu.exists() && startMenu.isDirectory()){
            for(File file : startMenu.listFiles()){
                if(!file.isDirectory()){
                    if(checklnkFile(file)){
                        softwares.add(getSoftwareInfo(file, favorites));
                    }
                } else {
                    //只搜索一层文件夹
                    for(File fileChild : file.listFiles()){
                        if(!fileChild.isDirectory()){
                            if(checklnkFile(fileChild)){
                                softwares.add(getSoftwareInfo(fileChild, favorites));
                            }
                        }
                    }
                }
            }
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

    private boolean checklnkFile(File file){
        return file.getName().endsWith(".lnk");
    }

    private SoftwareDto getSoftwareInfo(File lnkFile, List<Favorite> favorites){
        SoftwareDto softwareDto = new SoftwareDto();
        softwareDto.setId(lnkFile.getPath());
        softwareDto.setLnkPath(softwareDto.getId());
        softwareDto.setFavorite(favorites.contains(softwareDto.getId()));
        softwareDto.setSoftwareName(lnkFile.getName().replace(".lnk", ""));
        try {
            //TODO: 解析lnk文件, 先写死一个firefox的做测试用
            String content = FileUtils.readFileToString(lnkFile);
            if(lnkFile.getName().contains("Firefox")){
                softwareDto.setExeName("Firefox.exe");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return softwareDto;
    }

    private SoftwareDto getSoftwareInfo(Software software, List<Favorite> favorites){
        SoftwareDto softwareDto = new SoftwareDto();
        BeanUtils.copyProperties(software, softwareDto, "id");
        softwareDto.setId(String.valueOf(software.getId()));
        softwareDto.setFavorite(favorites.contains(softwareDto.getId()));
        return softwareDto;
    }

    public SoftwareDto getSoftwareInfo(String id){
        if(softwareMap == null){
            getSystemSoftware();
        }
        return softwareMap.get(id);
    }

    public List<ProcessJsonDto> getProcessPids(String softwareId){
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
            ProcessListJsonDto processListJson = objectMapper.readValue(processContent, ProcessListJsonDto.class);
            Map<String, List<ProcessJsonDto>> tempProcessMaps = new HashMap<>();
            if(processListJson.getProcesses() != null){
                Map<Integer, ProcessJsonDto> pidProcessMap = new HashMap<>();
                processListJson.getProcesses().forEach(processJsonDto -> pidProcessMap.put(processJsonDto.getPid(), processJsonDto));
                //把所有进程组装到对应的软件中
                for(ProcessJsonDto processJson : processListJson.getProcesses()){
                    //先找父对象是否存在，存在父对象时直接挂到父对象的进程列表中
                    ProcessJsonDto parentProcess = pidProcessMap.get(processJson.getParentPid());
                    String softwareId;
                    if(parentProcess != null){
                        softwareId = getSoftwareIdFromImageName(parentProcess.getImageName());
                    } else {
                        //无父对象，则单独添加
                        softwareId = getSoftwareIdFromImageName(processJson.getImageName());
                    }
                    if(softwareId != null){
                        List<ProcessJsonDto> softwareProcesses = tempProcessMaps.get(softwareId);
                        if(softwareProcesses == null){
                            softwareProcesses = new ArrayList<>();
                            tempProcessMaps.put(softwareId, softwareProcesses);
                        }
                        softwareProcesses.add(processJson);
                    }
                }
            }
            processMaps = tempProcessMaps;
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSoftwareIdFromImageName(String imageName){
        return exeNameSoftwareIdMap.get(imageName.toLowerCase());
    }
}
