package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum ActionProcessType implements DescribedValuedEnum<Integer> {
    PROCESS_OPEN(16384, "启动进程"),
    PROCESS_OPEN_LINUX(16385,"启动进程"),
    PROCESS_INJECT(20480, "进程注入"),
    PROCESS_MESSAGE_SEND(20481, "消息通讯"),
    PROCESS_SHARE_MEMORY(20482, "内存共享");

    private final int value;
    private final String description;

    ActionProcessType(int value, String description) {
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