package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum ActionRegistryType implements DescribedValuedEnum<Integer> {
    REGISTRY_OPEN_KEY(12288, "注册表打开或创建键"),
    REGISTRY_DELETE_KEY(12290,"注册表删除键"),
    REGISTRY_DELETE_VALUE(12291, "注册表删除值键"),
    REGISTRY_SET_VALUE(12294, "注册表设置值键");

    private final int value;
    private final String description;

    ActionRegistryType(int value, String description) {
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
