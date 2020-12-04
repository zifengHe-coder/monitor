package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.IdentifiableObject;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_favorite")
@Description("软件")
public class Favorite extends IdentifiableObject {

    @ApiModelProperty("常用软件ID")
    @Column(nullable = false)
    private String softwareId;

    public String getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }
}
