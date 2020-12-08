package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum FileSensitivity implements DescribedValuedEnum<Integer> {

    LOW(1, "低"),
    MIDDLE(2,"中"),
    HIGH(3,"高");

    private final int value;
    private final String description;

    FileSensitivity(int value, String description) {
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