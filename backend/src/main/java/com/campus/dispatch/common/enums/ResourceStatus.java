package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ResourceStatus {

    IDLE(0, "空闲"),
    OCCUPIED(1, "占用"),
    MAINTENANCE(2, "维护中");

    @EnumValue
    private final int value;
    private final String desc;

    ResourceStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}