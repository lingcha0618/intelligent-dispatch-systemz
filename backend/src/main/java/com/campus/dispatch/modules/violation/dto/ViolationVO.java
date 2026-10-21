package com.campus.dispatch.modules.violation.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "违规记录信息")
public class ViolationVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "预约记录ID")
    private Long bookingId;

    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}