package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BookingStatus {

    PENDING(0, "待签到"),
    IN_USE(1, "使用中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消"),
    VIOLATED(4, "违约");

    @EnumValue
    private final int value;
    private final String desc;

    BookingStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}