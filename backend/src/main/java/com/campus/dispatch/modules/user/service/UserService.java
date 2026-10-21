package com.campus.dispatch.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.modules.user.dto.UserCreateDTO;
import com.campus.dispatch.modules.user.dto.UserQueryDTO;
import com.campus.dispatch.modules.user.dto.UserStatusDTO;
import com.campus.dispatch.modules.user.dto.UserVO;

public interface UserService {

    IPage<UserVO> page(UserQueryDTO dto);

    void create(UserCreateDTO dto);

    void updateStatus(Long id, UserStatusDTO dto);
}