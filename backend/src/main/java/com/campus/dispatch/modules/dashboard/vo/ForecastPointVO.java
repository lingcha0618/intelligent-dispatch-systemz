package com.campus.dispatch.modules.dashboard.vo;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI人流预测数据点")
public class ForecastPointVO {

    @Schema(description = "资源ID")
    private Long resourceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预测时间点")
    private LocalDateTime forecastTime;

    @Schema(description = "人流热度预测值")
    private Integer heatValue;
}