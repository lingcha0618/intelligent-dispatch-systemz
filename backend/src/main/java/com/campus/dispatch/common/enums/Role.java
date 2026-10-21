package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Role {

    STUDENT(0, "学生"),
    ADMIN(1, "管理员"),
    SUPER_ADMIN(2, "超级管理员");

    @EnumValue
    private final int value;
    private final String desc;

    Role(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}