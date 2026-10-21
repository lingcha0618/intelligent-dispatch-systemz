package com.campus.dispatch.modules.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.dispatch.common.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_booking")
public class ResourceBooking {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long resourceId;

    private String requestId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BookingStatus status;

    private LocalDateTime checkInTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}