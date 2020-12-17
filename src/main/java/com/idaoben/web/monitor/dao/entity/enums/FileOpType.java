package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum FileOpType implements DescribedValuedEnum<Integer> {

    READ(1, "读"),
    WRITE(2,"写"),
    DELETE(3, "删除");

    private final int value;
    private final String description;

    FileOpType(int value, String description) {
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
