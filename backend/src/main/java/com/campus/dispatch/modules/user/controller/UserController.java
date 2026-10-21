package com.campus.dispatch.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.user.dto.UserCreateDTO;
import com.campus.dispatch.modules.user.dto.UserQueryDTO;
import com.campus.dispatch.modules.user.dto.UserStatusDTO;
import com.campus.dispatch.modules.user.dto.UserVO;
import com.campus.dispatch.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "02 用户管理")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @io.swagger.v3.oas.annotations.Operation(summary = "用户分页查询")
    @GetMapping("/page")
    public Result<IPage<UserVO>> page(UserQueryDTO dto) {
        return Result.success(userService.page(dto));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "新建用户（管理员）")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "修改用户状态（封禁/解封）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody UserStatusDTO dto) {
        userService.updateStatus(id, dto);
        return Result.success();
    }
}