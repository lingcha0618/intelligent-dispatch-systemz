package com.campus.dispatch.modules.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.resource.dto.ResourceDetailVO;
import com.campus.dispatch.modules.resource.dto.ResourceQueryDTO;
import com.campus.dispatch.modules.resource.dto.ResourceVO;
import com.campus.dispatch.modules.resource.dto.SlotVO;
import com.campus.dispatch.modules.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "04 资源浏览（学生端）")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @io.swagger.v3.oas.annotations.Operation(summary = "资源分页查询（教室/座位/实验室）")
    @GetMapping("/page")
    public Result<IPage<ResourceVO>> page(ResourceQueryDTO dto) {
        return Result.success(resourceService.page(dto));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "查询资源详情")
    @GetMapping("/{id}")
    public Result<ResourceDetailVO> detail(@PathVariable Long id) {
        return Result.success(resourceService.detail(id));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "查询资源可预约时段")
    @GetMapping("/{id}/slots")
    public Result<List<SlotVO>> slots(@PathVariable Long id) {
        return Result.success(resourceService.slots(id));
    }
}