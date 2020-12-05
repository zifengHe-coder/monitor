package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.monitor.dao.entity.Favorite;
import com.idaoben.web.monitor.dao.entity.Software;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.FavoriteService;
import com.idaoben.web.monitor.service.SoftwareService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.SoftwareAddCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import net.sf.ehcache.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SoftwareApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SoftwareApplicationService.class);

    @Resource
    private SoftwareService softwareService;

    @Resource
    private FavoriteService favoriteService;

    private Map<String, SoftwareDto> softwareMap = new ConcurrentHashMap<>();

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
                            if(checklnkFile(file)){
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
        Map<String, SoftwareDto> tempMap = new ConcurrentHashMap<>();
        softwares.forEach(softwareDto -> {tempMap.put(softwareDto.getId(), softwareDto);});
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
        try {
            String content = FileUtils.readFileToString(lnkFile);
            logger.info(content);
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
        return softwareMap.get(id);
    }
}
