package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum ActionType implements DescribedValuedEnum<Integer> {

    START(0, "监控开始"),
    STOP(4095,"监控结束"),
    NETWORK_OPEN(4096,"发起网络连接"),
    NETWORK_TCP_SEND(4097, "TCP数据发送"),
    FILE_OPEN(8192, "文件打开"),
    FILE_WRIT(8193, "文件写入"),
    REGISTRY_OPEN_KEY(12288,"注册表打开或创建键"),
    REGISTRY_DELETE_KEY(12290, "注册表删除键"),
    REGISTRY_DELETE_VALUE(12291, "注册表删除值键"),
    REGISTRY_SET_VALUE(12294, "注册表设置值键"),
    PROCESS_OPEN(16384, "启动进程");

    private final int value;
    private final String description;

    ActionType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Integer value() {
        return value;
    }
}