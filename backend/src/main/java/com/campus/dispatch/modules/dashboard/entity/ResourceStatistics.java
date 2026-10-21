package com.campus.dispatch.modules.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("resource_statistics")
public class ResourceStatistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resourceId;

    private LocalDate statDate;

    private Integer hour;

    private Integer bookingCount;

    private BigDecimal utilizationRate;
}