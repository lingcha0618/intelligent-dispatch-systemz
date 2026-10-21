package com.campus.dispatch.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dispatch.common.enums.Role;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.modules.user.dto.UserCreateDTO;
import com.campus.dispatch.modules.user.dto.UserQueryDTO;
import com.campus.dispatch.modules.user.dto.UserStatusDTO;
import com.campus.dispatch.modules.user.dto.UserVO;
import com.campus.dispatch.modules.user.entity.SysUser;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import com.campus.dispatch.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> page(UserQueryDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(dto.getKeyword() != null && !dto.getKeyword().isBlank(), w -> w
                        .like(SysUser::getUsername, dto.getKeyword())
                        .or()
                        .like(SysUser::getRealName, dto.getKeyword()))
                .eq(dto.getRole() != null, SysUser::getRole, dto.getRole())
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public void create(UserCreateDTO dto) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(toRole(dto.getRole()));
        user.setStatus(0);
        userMapper.insert(user);
    }

    @Override
    public void updateStatus(Long id, UserStatusDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        user.setStatus(dto.getStatus());
        user.setBanUntil(dto.getBanUntil());
        userMapper.updateById(user);
    }

    private Role toRole(Integer role) {
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不能为空");
        }
        for (Role r : Role.values()) {
            if (r.getValue() == role) {
                return r;
            }
        }
        throw new BusinessException(ResultCode.PARAM_ERROR, "非法的角色");
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole() == null ? null : user.getRole().getValue());
        vo.setRoleDesc(user.getRole() == null ? null : user.getRole().getDesc());
        return vo;
    }
}