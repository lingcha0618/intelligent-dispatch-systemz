package com.campus.dispatch.modules.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "学号/用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色编码")
    private Integer role;

    @Schema(description = "角色名称")
    private String roleDesc;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "状态编码")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "封禁截止时间")
    private LocalDateTime banUntil;
}