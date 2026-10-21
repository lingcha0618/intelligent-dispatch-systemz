package com.campus.dispatch.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    SYSTEM_ERROR(500, "系统异常，请稍后再试"),

    LOGIN_FAILED(40001, "账号或密码错误"),
    USER_BANNED(40002, "账号已被封禁"),
    LOGIN_LOCKED(40003, "登录尝试过于频繁，请稍后再试"),
    RESOURCE_NOT_FOUND(40004, "资源不存在"),
    USERNAME_EXISTS(40010, "用户名已存在"),
    PASSWORD_INVALID(40011, "密码不合规"),
    RESOURCE_HAS_BOOKING(40030, "资源存在有效预约，无法删除"),
    DUPLICATE_SUBMIT(40040, "请勿重复提交"),
    SLOT_CONFLICT(40041, "该时段已被预约"),
    BOOKING_LIMIT_EXCEEDED(40042, "今日预约次数已达上限"),
    OUT_OF_OPEN_TIME(40043, "超出资源开放时间"),
    RESOURCE_MAINTENANCE(40044, "资源维护中，暂不可预约"),
    BOOKING_STATUS_ERROR(40050, "预约状态不符"),
    NOT_OWNER(40051, "无权限操作他人预约"),
    OUT_OF_CHECKIN_WINDOW(40052, "不在可签到时间范围内"),
    CANNOT_CANCEL(40053, "该预约不可取消");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}