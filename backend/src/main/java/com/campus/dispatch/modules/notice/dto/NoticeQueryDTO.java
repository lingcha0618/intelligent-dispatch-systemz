package com.campus.dispatch.modules.notice.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "通知公告查询参数")
public class NoticeQueryDTO {

    @Schema(description = "页码（从1开始）")
    private long page = 1;

    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "是否已读")
    private Integer isRead;
}