package com.campus.dispatch.modules.dashboard.vo;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "大屏总览统计")
public class OverviewVO {

    @Schema(description = "资源总数")
    private Long resourceTotal;

    @Schema(description = "当前已占用数量")
    private Long occupiedCount;

    @Schema(description = "今日预约总数")
    private Long todayBookingCount;

    @Schema(description = "违约率(百分比)")
    private BigDecimal violationRate;
}