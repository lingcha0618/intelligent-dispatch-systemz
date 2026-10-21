package com.campus.dispatch.modules.violation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.dispatch.common.enums.ViolationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_violation")
public class UserViolation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookingId;

    private ViolationType type;

    private LocalDateTime createTime;
}