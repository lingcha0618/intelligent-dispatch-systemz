package com.campus.dispatch.modules.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "学生注册请求参数")
public class RegisterDTO {

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号/用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号码")
    private String phone;
}