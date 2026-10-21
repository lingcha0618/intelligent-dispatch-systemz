package com.campus.dispatch.common.constant;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    /** 预约分布式锁 key 前缀，格式 booking:lock:{resourceId}:{slotKey} */
    public static final String BOOKING_LOCK_PREFIX = "booking:lock:";

    /** 预约幂等 key 前缀，格式 booking:idempotent:{requestId} */
    public static final String BOOKING_IDEMPOTENT_PREFIX = "booking:idempotent:";

    /** 登录限流 key 前缀(用户名维度)，格式 login:limit:user:{username} */
    public static final String LOGIN_LIMIT_USER_PREFIX = "login:limit:user:";

    /** 登录限流 key 前缀(IP 维度)，格式 login:limit:ip:{ip} */
    public static final String LOGIN_LIMIT_IP_PREFIX = "login:limit:ip:";

    public static String bookingLockKey(Long resourceId, String slotKey) {
        return BOOKING_LOCK_PREFIX + resourceId + ":" + slotKey;
    }

    public static String idempotentKey(String requestId) {
        return BOOKING_IDEMPOTENT_PREFIX + requestId;
    }

    public static String loginLimitUserKey(String username) {
        return LOGIN_LIMIT_USER_PREFIX + username;
    }

    public static String loginLimitIpKey(String ip) {
        return LOGIN_LIMIT_IP_PREFIX + ip;
    }
}