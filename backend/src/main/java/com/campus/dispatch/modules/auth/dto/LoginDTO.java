package com.campus.dispatch.modules.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求参数")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "学号/用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    private String password;
}