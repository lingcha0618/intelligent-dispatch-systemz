package com.campus.dispatch.modules.violation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dispatch.common.enums.BookingStatus;
import com.campus.dispatch.common.enums.ViolationType;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.config.service.ConfigService;
import com.campus.dispatch.modules.notice.mq.NoticeMessage;
import com.campus.dispatch.modules.notice.mq.NoticeProducer;
import com.campus.dispatch.modules.user.entity.SysUser;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import com.campus.dispatch.modules.violation.dto.ViolationCreateDTO;
import com.campus.dispatch.modules.violation.dto.ViolationQueryDTO;
import com.campus.dispatch.modules.violation.dto.ViolationVO;
import com.campus.dispatch.modules.violation.entity.UserViolation;
import com.campus.dispatch.modules.violation.mapper.ViolationMapper;
import com.campus.dispatch.modules.violation.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private final ViolationMapper violationMapper;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final ConfigService configService;
    private final NoticeProducer noticeProducer;

    @Override
    public IPage<ViolationVO> page(ViolationQueryDTO dto) {
        LambdaQueryWrapper<UserViolation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getUserId() != null, UserViolation::getUserId, dto.getUserId())
                .eq(dto.getType() != null, UserViolation::getType, dto.getType())
                .orderByDesc(UserViolation::getCreateTime);
        Page<UserViolation> page = violationMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        Page<ViolationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ViolationCreateDTO dto) {
        ResourceBooking booking = bookingMapper.selectById(dto.getBookingId());
        if (booking == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约不存在");
        }
        ViolationType type = toType(dto.getType());
        if (type != ViolationType.EARLY_LEAVE && type != ViolationType.MALICIOUS_OCCUPY) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "人工判定仅支持早退或恶意占座");
        }
        if (booking.getStatus() == BookingStatus.IN_USE || booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.VIOLATED);
            bookingMapper.updateById(booking);
        }
        recordViolation(booking.getUserId(), booking.getId(), type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordViolation(Long userId, Long bookingId, ViolationType type) {
        UserViolation violation = new UserViolation();
        violation.setUserId(userId);
        violation.setBookingId(bookingId);
        violation.setType(type);
        violationMapper.insert(violation);

        int threshold = configService.getInt("booking.ban_threshold", 3);
        Long count = violationMapper.selectCount(new LambdaQueryWrapper<UserViolation>()
                .eq(UserViolation::getUserId, userId));
        if (count != null && count >= threshold) {
            int banDays = configService.getInt("booking.ban_days", 7);
            SysUser user = userMapper.selectById(userId);
            if (user != null) {
                LocalDateTime base = (user.getBanUntil() != null && user.getBanUntil().isAfter(LocalDateTime.now()))
                        ? user.getBanUntil() : LocalDateTime.now();
                user.setStatus(1);
                user.setBanUntil(base.plusDays(banDays));
                userMapper.updateById(user);
            }
        }

        noticeProducer.send(new NoticeMessage(userId, "违约通知",
                "您有一条违约记录（" + type.getDesc() + "），请遵守预约规则", 3));
    }

    private ViolationType toType(Integer type) {
        if (type == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "违规类型不能为空");
        }
        for (ViolationType t : ViolationType.values()) {
            if (t.getValue() == type) {
                return t;
            }
        }
        throw new BusinessException(ResultCode.PARAM_ERROR, "非法的违规类型");
    }

    private ViolationVO toVO(UserViolation violation) {
        ViolationVO vo = new ViolationVO();
        BeanUtils.copyProperties(violation, vo);
        vo.setType(violation.getType() == null ? null : violation.getType().getValue());
        vo.setTypeDesc(violation.getType() == null ? null : violation.getType().getDesc());
        return vo;
    }
}