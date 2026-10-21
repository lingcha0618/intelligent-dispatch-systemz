package com.campus.dispatch.modules.violation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.enums.ViolationType;
import com.campus.dispatch.modules.violation.dto.ViolationCreateDTO;
import com.campus.dispatch.modules.violation.dto.ViolationQueryDTO;
import com.campus.dispatch.modules.violation.dto.ViolationVO;

public interface ViolationService {

    IPage<ViolationVO> page(ViolationQueryDTO dto);

    void create(ViolationCreateDTO dto);

    void recordViolation(Long userId, Long bookingId, ViolationType type);
}