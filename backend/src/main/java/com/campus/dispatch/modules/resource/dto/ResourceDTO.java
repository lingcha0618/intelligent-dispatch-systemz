package com.campus.dispatch.modules.resource.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "资源信息录入参数")
public class ResourceDTO {

    @NotBlank(message = "资源名称不能为空")
    @Schema(description = "资源名称")
    private String name;

    @NotNull(message = "资源类型不能为空")
    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "所在位置")
    private String location;

    @NotNull(message = "容量不能为空")
    @Schema(description = "容纳人数")
    private Integer capacity;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "每日开放开始时间")
    private LocalTime openStart;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "每日开放结束时间")
    private LocalTime openEnd;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "资源描述")
    private String description;
}