package com.campus.dispatch.modules.booking.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "预约提交参数")
public class BookingCreateDTO {

    @NotBlank(message = "幂等凭证不能为空")
    @Schema(description = "防重请求ID（幂等令牌）")
    private String requestId;

    @NotNull(message = "资源ID不能为空")
    @Schema(description = "资源ID")
    private Long resourceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}