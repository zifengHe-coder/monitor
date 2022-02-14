package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class ActionSyncCommand {

    @ApiModelProperty("上次同步时间，为空时取全部")
    private ZonedDateTime lastSyncTime;

    @ApiModelProperty("任务ID")
    private Long taskId;

    public ZonedDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(ZonedDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
