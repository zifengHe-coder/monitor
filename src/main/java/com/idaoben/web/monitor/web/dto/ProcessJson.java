package com.idaoben.web.monitor.web.dto;

public class ProcessJson {

    private String cpuTime;

    private String imageName;

    private Integer parentPid;

    private Integer pid;

    private String user;

    private String wsPrivateBytes;

    public String getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(String cpuTime) {
        this.cpuTime = cpuTime;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Integer getParentPid() {
        return parentPid;
    }

    public void setParentPid(Integer parentPid) {
        this.parentPid = parentPid;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWsPrivateBytes() {
        return wsPrivateBytes;
    }

    public void setWsPrivateBytes(String wsPrivateBytes) {
        this.wsPrivateBytes = wsPrivateBytes;
    }

    @Override
    public String toString() {
        return "ProcessJson{" +
                "cpuTime='" + cpuTime + '\'' +
                ", imageName='" + imageName + '\'' +
                ", parentPid=" + parentPid +
                ", pid=" + pid +
                ", user='" + user + '\'' +
                ", wsPrivateBytes='" + wsPrivateBytes + '\'' +
                '}';
    }
}
