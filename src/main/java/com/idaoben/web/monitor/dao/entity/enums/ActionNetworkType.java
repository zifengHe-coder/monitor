package com.idaoben.web.monitor.dao.entity.enums;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;

public enum ActionNetworkType implements DescribedValuedEnum<Integer> {
    NETWORK_OPEN(4096, "发起网络连接"),
    NETWORK_TCP_SEND(4097,"TCP数据发送"),
    NETWORK_TCP_RECEIVE(4098, "TCP数据接收"),
    NETWORK_UDP_SEND(4099, "UDP数据发送"),
    NETWORK_UDP_RECEIVE(4100, "UDP数据接收");

    private final int value;
    private final String description;

    ActionNetworkType(int value, String description) {
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
