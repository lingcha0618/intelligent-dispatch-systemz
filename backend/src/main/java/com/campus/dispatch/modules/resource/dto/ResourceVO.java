package com.campus.dispatch.modules.resource.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "资源列表信息")
public class ResourceVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "资源名称")
    private String name;

    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeDesc;

    @Schema(description = "所在位置")
    private String location;

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

    @Schema(description = "状态名称")
    private String statusDesc;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "当前人流热度(0-100，越低越不拥堵)")
    private Integer heatValue;
}