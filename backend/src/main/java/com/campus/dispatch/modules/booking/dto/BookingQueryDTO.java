package com.campus.dispatch.modules.booking.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "预约记录查询参数")
public class BookingQueryDTO {

    @Schema(description = "页码（从1开始）")
    private long page = 1;

    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "用户ID")
    private Long userId;
}