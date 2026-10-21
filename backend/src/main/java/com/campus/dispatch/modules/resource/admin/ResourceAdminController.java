package com.campus.dispatch.modules.resource.admin;

import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.resource.dto.ResourceDTO;
import com.campus.dispatch.modules.resource.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "03 资源管理（管理员）")
@RestController
@RequestMapping("/api/admin/resource")
@RequiredArgsConstructor
public class ResourceAdminController {

    private final ResourceService resourceService;

    @io.swagger.v3.oas.annotations.Operation(summary = "新增资源")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ResourceDTO dto) {
        resourceService.add(dto);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "修改资源信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ResourceDTO dto) {
        resourceService.update(id, dto);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "删除资源")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return Result.success();
    }
}