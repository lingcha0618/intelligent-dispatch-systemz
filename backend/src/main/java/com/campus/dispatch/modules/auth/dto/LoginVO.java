package com.campus.dispatch.modules.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.campus.dispatch.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {

    @Schema(description = "JWT登录令牌")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色编码")
    private Role role;
}