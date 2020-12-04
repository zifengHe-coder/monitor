package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.IdentifiableObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "t_task")
@Description("任务")
public class Task extends IdentifiableObject {

    @Description("启动时间")
    @Column(nullable = false)
    private ZonedDateTime startTime;

    @Description("结束时间")
    @Column(nullable = false)
    private ZonedDateTime endTime;

    @Description("软件ID")
    @Column(nullable = false)
    private String softwareId;

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

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
