package com.idaoben.web.monitor.web.dto;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class SoftwareDto {

    @ApiModelProperty("唯一识别码")
    private String id;

    @ApiModelProperty("快捷方式路径")
    private String lnkPath;

    @ApiModelProperty("软件名称")
    private String softwareName;

    @ApiModelProperty("启动命令行")
    private String commandLine;

    @ApiModelProperty("启动目录")
    private String executePath;

    @ApiModelProperty("可执行程序名称")
    private String exeName;

    @ApiModelProperty("可执行文件路径")
    private String exePath;

    @ApiModelProperty("BASE64格式的icon")
    private String base64Icon;

    @ApiModelProperty("是否常用")
    private boolean favorite;

    @ApiModelProperty("文件大小")
    private BigDecimal fileSize;

    @ApiModelProperty("文件创建时间")
    private ZonedDateTime fileCreationTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLnkPath() {
        return lnkPath;
    }

    public void setLnkPath(String lnkPath) {
        this.lnkPath = lnkPath;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }

    public String getExeName() {
        return exeName;
    }

    public void setExeName(String exeName) {
        this.exeName = exeName;
    }

    public String getBase64Icon() {
        return base64Icon;
    }

    public void setBase64Icon(String base64Icon) {
        this.base64Icon = base64Icon;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public BigDecimal getFileSize() {
        return fileSize;
    }

    public void setFileSize(BigDecimal fileSize) {
        this.fileSize = fileSize;
    }

    public ZonedDateTime getFileCreationTime() {
        return fileCreationTime;
    }

    public void setFileCreationTime(ZonedDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
    }
}
