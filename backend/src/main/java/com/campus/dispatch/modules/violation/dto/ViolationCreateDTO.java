package com.campus.dispatch.modules.violation.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "违规登记参数")
public class ViolationCreateDTO {

    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约记录ID")
    private Long bookingId;

    @NotNull(message = "违规类型不能为空")
    @Schema(description = "类型编码")
    private Integer type;
}