package com.campus.dispatch.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ResourceType {

    CLASSROOM(1, "教室"),
    LIBRARY_SEAT(2, "图书馆座位"),
    LAB(3, "实验室"),
    MEETING_ROOM(4, "会议室");

    @EnumValue
    private final int value;
    private final String desc;

    ResourceType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}