package com.campus.dispatch.modules.violation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.violation.dto.ViolationCreateDTO;
import com.campus.dispatch.modules.violation.dto.ViolationQueryDTO;
import com.campus.dispatch.modules.violation.dto.ViolationVO;
import com.campus.dispatch.modules.violation.service.ViolationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "09 违规管理")
@RestController
@RequestMapping("/api/admin/violation")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @io.swagger.v3.oas.annotations.Operation(summary = "违规记录分页查询")
    @GetMapping("/page")
    public Result<IPage<ViolationVO>> page(ViolationQueryDTO dto) {
        return Result.success(violationService.page(dto));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "登记违规记录")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ViolationCreateDTO dto) {
        violationService.create(dto);
        return Result.success();
    }
}