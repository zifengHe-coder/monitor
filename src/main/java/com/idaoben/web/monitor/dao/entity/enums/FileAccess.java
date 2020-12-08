package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum FileAccess implements DescribedValuedEnum<Integer> {

    READ(1, "读"),
    WRITE(2,"写"),
    READ_AND_WRITE(3, "读写");

    private final int value;
    private final String description;

    FileAccess(int value, String description) {
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
