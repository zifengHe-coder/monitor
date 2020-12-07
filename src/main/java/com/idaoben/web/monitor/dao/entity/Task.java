package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.IdentifiableObject;
import org.apache.tomcat.util.buf.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "t_task")
@Description("任务")
public class Task extends IdentifiableObject {

    @Description("启动时间")
    @Column(nullable = false)
    private ZonedDateTime startTime;

    @Description("结束时间")
    @Column
    private ZonedDateTime endTime;

    @Description("软件ID")
    @Column(nullable = false)
    private String softwareId;

    @Description("软件名称")
    @Column
    private String softwareName;

    @Description("路径")
    @Column
    private String exePath;

    @Description("pid列表，逗号分隔")
    @Column
    private String pids;

    @Description("是否已完成")
    @Column(nullable = false)
    private boolean complete;

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getPids() {
        return pids;
    }

    public void setPids(String pids) {
        this.pids = pids;
    }

    public void setPids(List<String> pids){
        this.pids = StringUtils.join(pids, ',');
    }

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
