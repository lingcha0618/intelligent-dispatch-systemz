package com.campus.dispatch.modules.resource.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "资源分页查询参数")
public class ResourceQueryDTO {

    @Schema(description = "页码（从1开始）")
    private long page = 1;

    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "状态编码")
    private Integer status;
}