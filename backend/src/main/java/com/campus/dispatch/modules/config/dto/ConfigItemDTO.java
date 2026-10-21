package com.campus.dispatch.modules.config.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "系统配置项")
public class ConfigItemDTO {

    @Schema(description = "配置项键名")
    private String key;

    @Schema(description = "配置项取值")
    private String value;
}