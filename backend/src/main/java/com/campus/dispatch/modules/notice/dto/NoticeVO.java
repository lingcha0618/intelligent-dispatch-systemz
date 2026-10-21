package com.campus.dispatch.modules.notice.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "通知公告信息")
public class NoticeVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知正文内容")
    private String content;

    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeDesc;

    @Schema(description = "是否已读")
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}