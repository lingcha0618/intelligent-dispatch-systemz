package com.campus.dispatch.modules.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.modules.dashboard.mapper.DashboardMapper;
import com.campus.dispatch.modules.dashboard.service.DashboardService;
import com.campus.dispatch.modules.dashboard.vo.ForecastPointVO;
import com.campus.dispatch.modules.dashboard.vo.OccupancyVO;
import com.campus.dispatch.modules.dashboard.vo.OverviewVO;
import com.campus.dispatch.modules.dashboard.vo.TrendPointVO;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import com.campus.dispatch.modules.forecast.mapper.ForecastMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final ForecastMapper forecastMapper;

    @Override
    public OverviewVO overview() {
        OverviewVO vo = new OverviewVO();
        vo.setResourceTotal(dashboardMapper.countResource());
        vo.setOccupiedCount(dashboardMapper.countInUseBooking());
        vo.setTodayBookingCount(dashboardMapper.countTodayBooking());
        long totalBooking = dashboardMapper.countTotalBooking();
        long violation = dashboardMapper.countViolation();
        if (totalBooking == 0) {
            vo.setViolationRate(BigDecimal.ZERO);
        } else {
            vo.setViolationRate(BigDecimal.valueOf(violation * 100.0 / totalBooking)
                    .setScale(2, RoundingMode.HALF_UP));
        }
        return vo;
    }

    @Override
    public List<TrendPointVO> trend(int days) {
        return dashboardMapper.selectTrend(days);
    }

    @Override
    public List<ForecastPointVO> forecast(Long resourceId, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(hours);
        List<ResourceForecast> forecasts = forecastMapper.selectList(new LambdaQueryWrapper<ResourceForecast>()
                .eq(resourceId != null, ResourceForecast::getResourceId, resourceId)
                .ge(ResourceForecast::getForecastTime, now)
                .le(ResourceForecast::getForecastTime, end)
                .orderByAsc(ResourceForecast::getForecastTime));
        return forecasts.stream().map(this::toForecastVO).toList();
    }

    @Override
    public List<OccupancyVO> occupancy(Long resourceId) {
        return dashboardMapper.selectOccupancy(resourceId);
    }

    private ForecastPointVO toForecastVO(ResourceForecast forecast) {
        ForecastPointVO vo = new ForecastPointVO();
        BeanUtils.copyProperties(forecast, vo);
        return vo;
    }
}