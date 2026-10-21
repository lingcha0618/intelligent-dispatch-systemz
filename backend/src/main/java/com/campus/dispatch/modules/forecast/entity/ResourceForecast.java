package com.campus.dispatch.modules.forecast.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_forecast")
public class ResourceForecast {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resourceId;

    private LocalDateTime forecastTime;

    private Integer heatValue;

    private LocalDateTime createTime;
}