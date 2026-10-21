package com.campus.dispatch.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.dashboard.entity.ResourceStatistics;
import com.campus.dispatch.modules.dashboard.mapper.StatisticsMapper;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import com.campus.dispatch.modules.resource.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日资源分时统计任务：聚合前一日各资源各小时的预约量与使用率，
 * upsert 到 resource_statistics（依赖 uk_resource_date_hour 唯一键），
 * 为 AI 人流预测提供「数据采集 → 统计 → 预测」闭环中的统计数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsJob {

    private final ResourceMapper resourceMapper;
    private final BookingMapper bookingMapper;
    private final StatisticsMapper statisticsMapper;

    @Scheduled(cron = "0 30 0 * * ?")
    public void run() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.plusDays(1).atStartOfDay();

        List<ResourceBooking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<ResourceBooking>()
                .ge(ResourceBooking::getStartTime, start)
                .lt(ResourceBooking::getStartTime, end));

        Map<Long, Map<Integer, Integer>> counter = new HashMap<>();
        for (ResourceBooking booking : bookings) {
            LocalDateTime cursor = booking.getStartTime();
            LocalDateTime effectiveEnd = booking.getEndTime().isAfter(end) ? end : booking.getEndTime();
            while (cursor.isBefore(effectiveEnd)) {
                int hour = cursor.getHour();
                counter.computeIfAbsent(booking.getResourceId(), k -> new HashMap<>())
                        .merge(hour, 1, Integer::sum);
                cursor = cursor.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            }
        }

        List<CampusResource> resources = resourceMapper.selectList(null);
        for (CampusResource resource : resources) {
            for (int hour = 0; hour < 24; hour++) {
                int count = counter.getOrDefault(resource.getId(), Collections.emptyMap())
                        .getOrDefault(hour, 0);
                BigDecimal rate = calcRate(count, resource.getCapacity());
                upsert(resource.getId(), yesterday, hour, count, rate);
            }
        }
        log.info("每日资源分时统计完成，覆盖 {} 个资源，预约 {} 条", resources.size(), bookings.size());
    }

    private BigDecimal calcRate(int count, Integer capacity) {
        if (capacity == null || capacity <= 0) {
            return BigDecimal.ZERO;
        }
        double rate = Math.min(count * 100.0 / capacity, 100.0);
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private void upsert(Long resourceId, LocalDate date, int hour, int count, BigDecimal rate) {
        ResourceStatistics existing = statisticsMapper.selectOne(new LambdaQueryWrapper<ResourceStatistics>()
                .eq(ResourceStatistics::getResourceId, resourceId)
                .eq(ResourceStatistics::getStatDate, date)
                .eq(ResourceStatistics::getHour, hour));
        if (existing == null) {
            ResourceStatistics statistics = new ResourceStatistics();
            statistics.setResourceId(resourceId);
            statistics.setStatDate(date);
            statistics.setHour(hour);
            statistics.setBookingCount(count);
            statistics.setUtilizationRate(rate);
            statisticsMapper.insert(statistics);
        } else {
            existing.setBookingCount(count);
            existing.setUtilizationRate(rate);
            statisticsMapper.updateById(existing);
        }
    }
}