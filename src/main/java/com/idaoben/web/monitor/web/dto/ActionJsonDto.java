package com.idaoben.web.monitor.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.idaoben.web.monitor.dao.entity.Action;

import java.time.ZonedDateTime;

public class ActionJsonDto {

    private String uuid;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private ZonedDateTime timestamp;

    private Boolean withAttachment;

    private Action action;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getWithAttachment() {
        return withAttachment;
    }

    public void setWithAttachment(Boolean withAttachment) {
        this.withAttachment = withAttachment;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
