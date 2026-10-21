package com.campus.dispatch.modules.booking.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "预约防重令牌")
public class IdempotentTokenVO {

    @Schema(description = "防重请求ID（幂等令牌）")
    private String requestId;
}