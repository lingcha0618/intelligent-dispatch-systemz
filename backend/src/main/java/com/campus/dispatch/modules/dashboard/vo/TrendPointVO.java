package com.campus.dispatch.modules.dashboard.vo;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "预约趋势数据点")
public class TrendPointVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "数量")
    private Long count;
}