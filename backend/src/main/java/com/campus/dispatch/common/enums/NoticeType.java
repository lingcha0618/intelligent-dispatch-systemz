package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NoticeType {

    BOOKING_SUCCESS(1, "预约成功"),
    EXPIRE_REMIND(2, "到期提醒"),
    VIOLATION(3, "违约通知"),
    ANNOUNCEMENT(4, "系统公告");

    @EnumValue
    private final int value;
    private final String desc;

    NoticeType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}