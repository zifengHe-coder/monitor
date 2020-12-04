package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.IdentifiableObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_software")
@Description("软件")
public class Software extends IdentifiableObject {

    @Description("软件名称")
    @Column(nullable = false)
    private String softwareName;

    @Description("启动命令行")
    @Column(nullable = false)
    private String commandLine;

    @Description("启动目录")
    @Column(nullable = false)
    private String executePath;

    @Description("可执行程序名称")
    @Column(nullable = false)
    private String exeName;

    @Description("可执行文件路径")
    @Column
    private String exePath;

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

    public String getExeName() {
        return exeName;
    }

    public void setExeName(String exeName) {
        this.exeName = exeName;
    }

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }
}
