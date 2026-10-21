package com.campus.dispatch.modules.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "用户分页查询参数")
public class UserQueryDTO {

    @Schema(description = "页码（从1开始）")
    private long page = 1;

    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "角色编码")
    private Integer role;
}