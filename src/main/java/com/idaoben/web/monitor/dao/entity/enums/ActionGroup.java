package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum ActionGroup implements DescribedValuedEnum<Integer> {
    FILE(1, "文件读写"),
    NETWORK(2,"网络访问"),
    REGISTRY(3, "注册表"),
    PROCESS(4, "进程调用"),
    DEVICE(5, "设备控制"),
    SECURITY(6, "对象权限");

    private final int value;
    private final String description;

    ActionGroup(int value, String description) {
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