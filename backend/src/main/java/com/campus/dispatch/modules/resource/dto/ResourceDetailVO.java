package com.campus.dispatch.modules.resource.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "资源详情（含时段）")
public class ResourceDetailVO extends ResourceVO {

    @Schema(description = "可预约时段列表")
    private List<SlotVO> slots;
}