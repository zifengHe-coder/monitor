package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private String socketFd;

    @Description("目标主机")
    @Column
    private String host;

    @Description("目标端口")
    @Column
    private Integer port;

    @Description("参考字段")
    @Column
    private String ref;

    @Description("数据包大小")
    @Column
    private Long bytes;

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

    @Description("值键值/发送数据(HEX编码)")
    @Column(name = "data", columnDefinition = "MEDIUMTEXT COMMENT '数据'")
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

    @Description("创建远程线程时提供的入口函数地址 ")
    @Column
    private String threadEntryAddress;

    @Description("消息发送目标窗口的句柄")
    @Column
    private String destHwnd;

    @Description("消息发送源窗口的句柄")
    @Column
    private String srcHwnd;

    public Boolean getWithAttachment() {
        return withAttachment;
    }

    public void setWithAttachment(Boolean withAttachment) {
        this.withAttachment = withAttachment;
    }

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
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

    public String getThreadEntryAddress() {
        return threadEntryAddress;
    }

    public void setThreadEntryAddress(String threadEntryAddress) {
        this.threadEntryAddress = threadEntryAddress;
    }

    public String getDestHwnd() {
        return destHwnd;
    }

    public void setDestHwnd(String destHwnd) {
        this.destHwnd = destHwnd;
    }

    public String getSrcHwnd() {
        return srcHwnd;
    }

    public void setSrcHwnd(String srcHwnd) {
        this.srcHwnd = srcHwnd;
    }
}
