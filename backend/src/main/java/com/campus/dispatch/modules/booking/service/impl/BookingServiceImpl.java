package com.campus.dispatch.modules.booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dispatch.common.constant.RedisKeyConstants;
import com.campus.dispatch.common.enums.BookingStatus;
import com.campus.dispatch.common.enums.ResourceStatus;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.lock.RedisDistributedLock;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.common.security.SecurityUtils;
import com.campus.dispatch.modules.booking.dto.BookingCreateDTO;
import com.campus.dispatch.modules.booking.dto.BookingQueryDTO;
import com.campus.dispatch.modules.booking.dto.BookingVO;
import com.campus.dispatch.modules.booking.dto.IdempotentTokenVO;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.booking.service.BookingService;
import com.campus.dispatch.modules.config.service.ConfigService;
import com.campus.dispatch.modules.notice.mq.NoticeMessage;
import com.campus.dispatch.modules.notice.mq.NoticeProducer;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import com.campus.dispatch.modules.resource.mapper.ResourceMapper;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final int IDEMPOTENT_TTL_SECONDS = 300;

    private final BookingMapper bookingMapper;
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final ConfigService configService;
    private final RedisDistributedLock redisDistributedLock;
    private final StringRedisTemplate redisTemplate;
    private final PlatformTransactionManager transactionManager;
    private final NoticeProducer noticeProducer;

    @Override
    public IdempotentTokenVO getIdempotentToken() {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisKeyConstants.idempotentKey(requestId), "pending",
                Duration.ofSeconds(IDEMPOTENT_TTL_SECONDS));
        return new IdempotentTokenVO(requestId);
    }

    @Override
    public BookingVO create(BookingCreateDTO dto) {
        Long userId = requireLogin();

        if (dto.getStartTime() == null || dto.getEndTime() == null
                || !dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约开始时间必须早于结束时间");
        }

        Boolean consumed = redisTemplate.delete(RedisKeyConstants.idempotentKey(dto.getRequestId()));
        if (!Boolean.TRUE.equals(consumed)) {
            throw new BusinessException(ResultCode.DUPLICATE_SUBMIT);
        }

        CampusResource resource = resourceMapper.selectById(dto.getResourceId());
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        if (resource.getStatus() == ResourceStatus.MAINTENANCE) {
            throw new BusinessException(ResultCode.RESOURCE_MAINTENANCE);
        }
        checkOpenTime(resource, dto);

        String slotKey = dto.getStartTime().format(SLOT_FORMATTER) + "-" + dto.getEndTime().format(SLOT_FORMATTER);
        String lockKey = RedisKeyConstants.bookingLockKey(resource.getId(), slotKey);
        String lockValue = UUID.randomUUID().toString();
        if (!redisDistributedLock.tryLock(lockKey, lockValue, 10)) {
            throw new BusinessException(ResultCode.SLOT_CONFLICT, "系统繁忙，请稍后重试");
        }

        try {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            BookingVO vo = template.execute(status -> {
                CampusResource lockedResource = resourceMapper.selectByIdForUpdate(dto.getResourceId());
                if (lockedResource == null) {
                    throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
                }
                if (lockedResource.getStatus() == ResourceStatus.MAINTENANCE) {
                    throw new BusinessException(ResultCode.RESOURCE_MAINTENANCE);
                }
                if (userMapper.selectByIdForUpdate(userId) == null) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在或已被删除");
                }
                checkDailyLimit(userId);

                List<ResourceBooking> conflicts = bookingMapper.selectConflicts(
                        resource.getId(), dto.getStartTime(), dto.getEndTime());
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw new BusinessException(ResultCode.SLOT_CONFLICT);
                }
                ResourceBooking booking = new ResourceBooking();
                booking.setUserId(userId);
                booking.setResourceId(resource.getId());
                booking.setRequestId(dto.getRequestId());
                booking.setStartTime(dto.getStartTime());
                booking.setEndTime(dto.getEndTime());
                booking.setStatus(BookingStatus.PENDING);
                bookingMapper.insert(booking);
                return toVO(booking, resource);
            });

            sendNotice(userId, "预约成功",
                    "您已成功预约「" + resource.getName() + "」" + dto.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                            + " 至 " + dto.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")), 1);
            return vo;
        } finally {
            redisDistributedLock.unlock(lockKey, lockValue);
        }
    }

    @Override
    public void checkin(Long id) {
        Long userId = requireLogin();
        ResourceBooking booking = getOrThrow(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_OWNER);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ResultCode.BOOKING_STATUS_ERROR);
        }
        int windowMinutes = configService.getInt("booking.overtime_minutes", 15);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(booking.getStartTime().minusMinutes(windowMinutes))
                || now.isAfter(booking.getStartTime().plusMinutes(windowMinutes))) {
            throw new BusinessException(ResultCode.OUT_OF_CHECKIN_WINDOW);
        }
        booking.setStatus(BookingStatus.IN_USE);
        booking.setCheckInTime(now);
        bookingMapper.updateById(booking);
    }

    @Override
    public void cancel(Long id) {
        Long userId = requireLogin();
        ResourceBooking booking = getOrThrow(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_OWNER);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ResultCode.CANNOT_CANCEL);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingMapper.updateById(booking);
    }

    @Override
    public IPage<BookingVO> my(BookingQueryDTO dto) {
        Long userId = requireLogin();
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceBooking::getUserId, userId)
                .eq(dto.getStatus() != null, ResourceBooking::getStatus, dto.getStatus())
                .orderByDesc(ResourceBooking::getCreateTime);
        return queryBookings(dto, wrapper);
    }

    @Override
    public IPage<BookingVO> adminPage(BookingQueryDTO dto) {
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getStatus() != null, ResourceBooking::getStatus, dto.getStatus())
                .eq(dto.getResourceId() != null, ResourceBooking::getResourceId, dto.getResourceId())
                .eq(dto.getUserId() != null, ResourceBooking::getUserId, dto.getUserId())
                .orderByDesc(ResourceBooking::getCreateTime);
        return queryBookings(dto, wrapper);
    }

    private IPage<BookingVO> queryBookings(BookingQueryDTO dto, LambdaQueryWrapper<ResourceBooking> wrapper) {
        Page<ResourceBooking> page = bookingMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        Page<BookingVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    private void checkDailyLimit(Long userId) {
        int maxDaily = configService.getInt("booking.max_daily_per_user", 3);
        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        Long count = bookingMapper.selectCount(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getUserId, userId)
                .ne(ResourceBooking::getStatus, BookingStatus.CANCELLED)
                .ge(ResourceBooking::getCreateTime, dayStart)
                .lt(ResourceBooking::getCreateTime, dayEnd));
        if (count != null && count >= maxDaily) {
            throw new BusinessException(ResultCode.BOOKING_LIMIT_EXCEEDED);
        }
    }

    private void checkOpenTime(CampusResource resource, BookingCreateDTO dto) {
        if (!dto.getStartTime().toLocalDate().equals(dto.getEndTime().toLocalDate())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约时段不可跨天");
        }
        LocalTime start = dto.getStartTime().toLocalTime();
        LocalTime end = dto.getEndTime().toLocalTime();
        if (resource.getOpenStart() != null && start.isBefore(resource.getOpenStart())) {
            throw new BusinessException(ResultCode.OUT_OF_OPEN_TIME);
        }
        if (resource.getOpenEnd() != null && end.isAfter(resource.getOpenEnd())) {
            throw new BusinessException(ResultCode.OUT_OF_OPEN_TIME);
        }
    }

    private ResourceBooking getOrThrow(Long id) {
        ResourceBooking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约不存在");
        }
        return booking;
    }

    private Long requireLogin() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void sendNotice(Long userId, String title, String content, Integer type) {
        noticeProducer.send(new NoticeMessage(userId, title, content, type));
    }

    private BookingVO toVO(ResourceBooking booking) {
        CampusResource resource = resourceMapper.selectById(booking.getResourceId());
        return toVO(booking, resource);
    }

    private BookingVO toVO(ResourceBooking booking, CampusResource resource) {
        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setUserId(booking.getUserId());
        vo.setResourceId(booking.getResourceId());
        if (resource != null) {
            vo.setResourceName(resource.getName());
            vo.setResourceType(resource.getType() == null ? null : resource.getType().getValue());
            vo.setResourceTypeDesc(resource.getType() == null ? null : resource.getType().getDesc());
        }
        vo.setRequestId(booking.getRequestId());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setStatus(booking.getStatus() == null ? null : booking.getStatus().getValue());
        vo.setStatusDesc(booking.getStatus() == null ? null : booking.getStatus().getDesc());
        vo.setCheckInTime(booking.getCheckInTime());
        vo.setCreateTime(booking.getCreateTime());
        return vo;
    }
}