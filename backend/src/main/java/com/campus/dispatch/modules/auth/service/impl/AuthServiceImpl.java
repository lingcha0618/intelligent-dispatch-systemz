package com.campus.dispatch.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.common.constant.RedisKeyConstants;
import com.campus.dispatch.common.enums.Role;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.common.util.JwtUtil;
import com.campus.dispatch.modules.auth.dto.LoginDTO;
import com.campus.dispatch.modules.auth.dto.LoginVO;
import com.campus.dispatch.modules.auth.dto.RegisterDTO;
import com.campus.dispatch.modules.auth.service.AuthService;
import com.campus.dispatch.modules.user.entity.SysUser;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAIL_COUNT = 5;
    private static final int MAX_IP_FAIL_COUNT = 20;
    private static final long FAIL_WINDOW_SECONDS = 60;
    private static final long LOCK_SECONDS = 600;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        String failKey = RedisKeyConstants.loginLimitUserKey(dto.getUsername());
        String lockKey = failKey + ":lock";
        String ipFailKey = RedisKeyConstants.loginLimitIpKey(ip);
        String ipLockKey = ipFailKey + ":lock";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))
                || Boolean.TRUE.equals(redisTemplate.hasKey(ipLockKey))) {
            throw new BusinessException(ResultCode.LOGIN_LOCKED);
        }

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            recordFail(failKey, lockKey, MAX_FAIL_COUNT);
            recordFail(ipFailKey, ipLockKey, MAX_IP_FAIL_COUNT);
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        if (user.getStatus() != null && user.getStatus() == 1) {
            if (user.getBanUntil() == null || user.getBanUntil().isAfter(LocalDateTime.now())) {
                String message = user.getBanUntil() == null
                        ? "账号已被封禁"
                        : "账号已被封禁至 " + user.getBanUntil().format(DATE_TIME_FORMATTER);
                throw new BusinessException(ResultCode.USER_BANNED, message);
            }
            user.setStatus(0);
            user.setBanUntil(null);
            userMapper.updateById(user);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordFail(failKey, lockKey, MAX_FAIL_COUNT);
            recordFail(ipFailKey, ipLockKey, MAX_IP_FAIL_COUNT);
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        redisTemplate.delete(failKey);
        redisTemplate.delete(ipFailKey);
        String token = jwtUtil.generateToken(user.getId(), user.getRole().getValue());
        return new LoginVO(token, user.getId(), user.getRealName(), user.getRole());
    }

    @Override
    public void register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        if (!isValidPassword(dto.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_INVALID);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(Role.STUDENT);
        user.setStatus(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
    }

    private void recordFail(String failKey, String lockKey, int maxFailCount) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, Duration.ofSeconds(FAIL_WINDOW_SECONDS));
        }
        if (count != null && count >= maxFailCount) {
            redisTemplate.opsForValue().set(lockKey, "1", Duration.ofSeconds(LOCK_SECONDS));
            redisTemplate.delete(failKey);
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
}