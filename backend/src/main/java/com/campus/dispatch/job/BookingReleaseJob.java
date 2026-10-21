package com.campus.dispatch.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.common.enums.BookingStatus;
import com.campus.dispatch.common.enums.NoticeType;
import com.campus.dispatch.common.enums.ViolationType;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.config.service.ConfigService;
import com.campus.dispatch.modules.notice.mq.NoticeMessage;
import com.campus.dispatch.modules.notice.mq.NoticeProducer;
import com.campus.dispatch.modules.violation.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingReleaseJob {

    private static final String REMIND_KEY_PREFIX = "booking:remind:";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final BookingMapper bookingMapper;
    private final ViolationService violationService;
    private final ConfigService configService;
    private final NoticeProducer noticeProducer;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 * * * * ?")
    public void release() {
        int overtimeMinutes = configService.getInt("booking.overtime_minutes", 15);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime overdueThreshold = now.minusMinutes(overtimeMinutes);

        List<ResourceBooking> overdue = bookingMapper.selectList(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getStatus, BookingStatus.PENDING)
                .lt(ResourceBooking::getStartTime, overdueThreshold));
        for (ResourceBooking booking : overdue) {
            booking.setStatus(BookingStatus.VIOLATED);
            bookingMapper.updateById(booking);
            violationService.recordViolation(booking.getUserId(), booking.getId(), ViolationType.OVERTIME);
            log.info("预约 {} 超时未签到，标记违约", booking.getId());
        }

        List<ResourceBooking> finished = bookingMapper.selectList(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getStatus, BookingStatus.IN_USE)
                .lt(ResourceBooking::getEndTime, now));
        for (ResourceBooking booking : finished) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingMapper.updateById(booking);
            log.info("预约 {} 到期，标记完成", booking.getId());
        }

        remindExpiring(now);
    }

    private void remindExpiring(LocalDateTime now) {
        int remindMinutes = configService.getInt("booking.remind_minutes", 30);
        LocalDateTime remindThreshold = now.plusMinutes(remindMinutes);
        List<ResourceBooking> expiring = bookingMapper.selectList(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getStatus, BookingStatus.IN_USE)
                .ge(ResourceBooking::getEndTime, now)
                .le(ResourceBooking::getEndTime, remindThreshold));
        for (ResourceBooking booking : expiring) {
            String key = REMIND_KEY_PREFIX + booking.getId();
            Boolean first = redisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofMinutes(remindMinutes + 1L));
            if (Boolean.TRUE.equals(first)) {
                noticeProducer.send(new NoticeMessage(booking.getUserId(), "到期提醒",
                        "您的预约将于 " + booking.getEndTime().format(TIME_FORMATTER)
                                + " 到期，请合理安排使用时间", NoticeType.EXPIRE_REMIND.getValue()));
                log.info("预约 {} 即将到期，已发送提醒", booking.getId());
            }
        }
    }
}