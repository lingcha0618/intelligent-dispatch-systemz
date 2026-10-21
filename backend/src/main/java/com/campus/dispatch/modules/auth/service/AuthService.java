package com.campus.dispatch.modules.auth.service;

import com.campus.dispatch.modules.auth.dto.LoginDTO;
import com.campus.dispatch.modules.auth.dto.LoginVO;
import com.campus.dispatch.modules.auth.dto.RegisterDTO;

public interface AuthService {

    LoginVO login(LoginDTO dto, String ip);

    void register(RegisterDTO dto);
}