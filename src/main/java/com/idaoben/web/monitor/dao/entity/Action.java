package com.idaoben.web.monitor.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.idaoben.web.common.entity.Description;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "t_action")
@Description("应用行为")
public class Action {

    @Id
    @Column(name = "uuid", length = 50, unique = true, nullable = false)
    @Description("唯一标识符")
    private String uuid;

    @Description("日志时间戳")
    @Column(nullable = false)
    private ZonedDateTime timestamp;

    @Description("本日志行是否有对应的附件")
    @Column
    private Boolean withAttachment;

    @Description("关联的任务ID")
    @Column(nullable = false)
    private Long taskId;

    @JsonIgnore
    @Description("进程ID")
    @Column(nullable = false)
    private String pid;

    @Description("分类")
    @Type(type = "com.idaoben.utils.valued_enum.hibernate.ValuedEnumType")
    @Column
    private ActionGroup actionGroup;

    @Description("行为类型")
    @Column(length = 10, nullable = false)
    private Integer type;

    @Description("网络套接字描述符")
    @Column
    private Integer socketFd;

    @Description("目标主机")
    @Column
    private String host;

    @Description("目标端口")
    @Column
    private Integer port;

    @Description("参考字段")
    @Column
    private Integer ref;

    @Description("数据包大小")
    @Column
    private Long bytes;

    @Description("多个写入数据时的包大小集合，逗号分隔")
    @Column(name = "write_bytes", columnDefinition = "MEDIUMTEXT COMMENT '数据集'")
    private String writeBytes;

    @Description("文件路径/如果创建远程线程时执行的thread")
    @Column
    private String path;

    @Description("文件名称")
    @Column
    private String fileName;

    @Description("文件敏感度")
    @Type(type = "com.idaoben.utils.valued_enum.hibernate.ValuedEnumType")
    @Column
    private FileSensitivity sensitivity;

    @Description("打开文件时要求的访问权限")
    @Column
    private Long access;

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
    @Transient
    private Long offset;

    @Description("多个写入数据时的偏移量集合，逗号分隔")
    @Column(name = "write_offsets", columnDefinition = "MEDIUMTEXT COMMENT '偏移量'")
    private String writeOffsets;

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

    @Description("值键值/发送数据(HEX编码)")
    @Column(name = "data", columnDefinition = "MEDIUMTEXT COMMENT '数据'")
    private String data;

    @Description("值键原类型")
    @Column
    private String oldValueType;

    @Description("值键原有值")
    @Column(name = "old_data", columnDefinition = "MEDIUMTEXT COMMENT '原数据'")
    private String oldData;

    @Description("启动进程的完整命令行数据")
    @Column(length = 2000)
    private String cmdLine;

    @JsonProperty("pid")
    @Description("执行命令的PID")
    @Column
    private String cmdPid;

    @Description("执行的命令")
    @Transient
    private String cmd;

    @Description("执行命令时的参数（数组）")
    @Transient
    private String[] args;

    @Description("创建远程线程时提供的入口函数地址 ")
    @Column
    private String threadEntryAddress;

    @Description("消息发送目标窗口的句柄")
    @Column
    private Integer destHwnd;

    @Description("消息发送源窗口的句柄")
    @Column
    private Integer srcHwnd;

    @Description("设备名称")
    @Column
    private String deviceName;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSocketFd() {
        return socketFd;
    }

    public void setSocketFd(Integer socketFd) {
        this.socketFd = socketFd;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getRef() {
        return ref;
    }

    public void setRef(Integer ref) {
        this.ref = ref;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getWriteBytes() {
        return writeBytes;
    }

    public void setWriteBytes(String writeBytes) {
        this.writeBytes = writeBytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileSensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(FileSensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public Long getAccess() {
        return access;
    }

    public void setAccess(Long access) {
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

    public String getWriteOffsets() {
        return writeOffsets;
    }

    public void setWriteOffsets(String writeOffsets) {
        this.writeOffsets = writeOffsets;
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

    public String getCmdLine() {
        return cmdLine;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }

    public String getCmdPid() {
        return cmdPid;
    }

    public void setCmdPid(String cmdPid) {
        this.cmdPid = cmdPid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getThreadEntryAddress() {
        return threadEntryAddress;
    }

    public void setThreadEntryAddress(String threadEntryAddress) {
        this.threadEntryAddress = threadEntryAddress;
    }

    public Integer getDestHwnd() {
        return destHwnd;
    }

    public void setDestHwnd(Integer destHwnd) {
        this.destHwnd = destHwnd;
    }

    public Integer getSrcHwnd() {
        return srcHwnd;
    }

    public void setSrcHwnd(Integer srcHwnd) {
        this.srcHwnd = srcHwnd;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
