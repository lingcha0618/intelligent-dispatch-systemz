package com.campus.dispatch.modules.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新建用户参数")
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "学号/用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色编码")
    private Integer role;

    @Schema(description = "手机号码")
    private String phone;
}