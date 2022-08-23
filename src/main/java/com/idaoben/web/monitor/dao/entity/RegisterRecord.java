package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import io.swagger.models.auth.In;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author hezifeng
 * @create 2022/8/23 14:04
 */
@Entity
@Table(name = "t_register_record")
@Description("注册记录")
public class RegisterRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Description("公司名称")
    @Column
    private String companyName;

    @Description("注册文件-建立时间")
    @Column
    private String creationTime;

    @Description("修改时间")
    @Column
    private LocalDateTime modifyTime;

    @Description("总共使用次数")
    @Column
    private Integer count;

    @Description("当前使用次数")
    @Column
    private Integer number;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "RegisterRecord{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", modifyTime=" + modifyTime +
                ", count=" + count +
                ", number=" + number +
                '}';
    }
}
