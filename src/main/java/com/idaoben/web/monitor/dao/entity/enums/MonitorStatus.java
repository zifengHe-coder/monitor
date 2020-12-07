package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum MonitorStatus implements DescribedValuedEnum<Integer> {

    NOT_MONITOR(1, "未监听"),
    MONITORING(2,"监听中"),
    ERROR(3,"监听失败");

    private final int value;
    private final String description;

    MonitorStatus(int value, String description) {
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