package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.IdentifiableObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_action")
@Description("应用行为")
public class Action extends IdentifiableObject {

    @Description("关联的任务ID")
    @Column(nullable = false)
    private Long taskId;

    @Description("行为类型")
    @Column(length = 10, nullable = false)
    private String type;

    @Description("网络套接字描述符")
    @Column
    private String socketFd;

    @Description("目标主机")
    @Column
    private String host;

    @Description("目标端口")
    @Column
    private String port;

    @Description("参考字段")
    @Column
    private String ref;

    @Description("数据包大小")
    @Column
    private Long bytes;

    @Description("文件路径")
    @Column
    private String path;

    @Description("打开文件时要求的访问权限")
    @Column
    private String access;

    @Description("文件是否普通文件")
    @Column
    private Boolean generalFile;

    @Description("备份目录 ")
    @Column
    private String backup;

    @Description("文件描述符")
    @Column
    private String fd;

    @Description("写入数据偏移量")
    @Column(name = "write_offset")
    private Long offset;

    @Description("注册表父键")
    @Column
    private String parent;

    @Description("注册表目标")
    @Column
    private String key;

    @Description("值键")
    @Column
    private String valueName;

    @Description("值键类型")
    @Column
    private String valueType;

    @Description("值键值")
    @Column
    private String data;

    @Description("值键原类型")
    @Column
    private String oldValueType;

    @Description("值键原有值")
    @Column
    private String oldData;

    @Description("启动进程的完整命令行数据")
    @Column
    private String commandLine;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSocketFd() {
        return socketFd;
    }

    public void setSocketFd(String socketFd) {
        this.socketFd = socketFd;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public Boolean getGeneralFile() {
        return generalFile;
    }

    public void setGeneralFile(Boolean generalFile) {
        this.generalFile = generalFile;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }

    public String getFd() {
        return fd;
    }

    public void setFd(String fd) {
        this.fd = fd;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOldValueType() {
        return oldValueType;
    }

    public void setOldValueType(String oldValueType) {
        this.oldValueType = oldValueType;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }
}
