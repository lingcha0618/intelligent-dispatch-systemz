package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ViolationType {

    OVERTIME(1, "超时未签到"),
    EARLY_LEAVE(2, "早退"),
    MALICIOUS_OCCUPY(3, "恶意占座");

    @EnumValue
    private final int value;
    private final String desc;

    ViolationType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}