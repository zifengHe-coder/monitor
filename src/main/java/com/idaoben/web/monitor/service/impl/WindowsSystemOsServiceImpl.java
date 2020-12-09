package com.idaoben.web.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.LinkFileJson;
import com.idaoben.web.monitor.web.dto.LinkListJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WindowsSystemOsServiceImpl implements SystemOsService {

    private static final Logger logger = LoggerFactory.getLogger(WindowsSystemOsServiceImpl.class);

    @Resource
    private JniService jniService;

    @Resource
    private ObjectMapper objectMapper;

    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    @Override
    public List<SoftwareDto> getSystemSoftware() {
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
            softwares.add(getSoftwareInfo(linkFile, linkFileJsonMap.get(linkFile.getPath())));
        }
        return softwares;
    }

    private boolean checklnkFile(File file){
        return file.getName().endsWith(".lnk");
    }

    private SoftwareDto getSoftwareInfo(File lnkFile, LinkFileJson linkFileJson){
        SoftwareDto softwareDto = new SoftwareDto();
        softwareDto.setId(lnkFile.getPath());
        softwareDto.setLnkPath(softwareDto.getId());
        softwareDto.setSoftwareName(lnkFile.getName().replace(".lnk", ""));
        if(linkFileJson != null){
            File file = new File(linkFileJson.getPath());
            softwareDto.setCommandLine(linkFileJson.getPath() + linkFileJson.getArguments());
            softwareDto.setExePath(linkFileJson.getPath());
            softwareDto.setBase64Icon(getIconBase64(file));
            softwareDto.setExecutePath(linkFileJson.getWorkingDirectory());
            softwareDto.setExeName(file.getName());
        }
        return softwareDto;
    }

    public String getIconBase64(File file){
        //图标处理
        ImageIcon icon = (ImageIcon) fileSystemView.getSystemIcon(file);
        BufferedImage image = (BufferedImage) icon.getImage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            return String.format("data:image/png;base64,%s", Base64.getEncoder().encodeToString(os.toByteArray()));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(os);
        }
        return null;
    }
}
