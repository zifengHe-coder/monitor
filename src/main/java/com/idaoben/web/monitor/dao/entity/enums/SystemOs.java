package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum SystemOs implements DescribedValuedEnum<Integer> {

    WINDOWS(1, "Windows"),
    LINUX(2,"Linux");

    private final int value;
    private final String description;

    SystemOs(int value, String description) {
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
