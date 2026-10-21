package com.campus.dispatch.modules.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户状态变更参数")
public class UserStatusDTO {

    @Schema(description = "状态编码")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "封禁截止时间")
    private LocalDateTime banUntil;
}