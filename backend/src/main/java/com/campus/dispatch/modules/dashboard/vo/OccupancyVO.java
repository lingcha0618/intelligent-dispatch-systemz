package com.campus.dispatch.modules.dashboard.vo;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "资源使用率排行项")
public class OccupancyVO {

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "数量")
    private Long count;
}