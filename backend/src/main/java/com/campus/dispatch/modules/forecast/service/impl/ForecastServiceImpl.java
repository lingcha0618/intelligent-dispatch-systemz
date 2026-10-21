package com.campus.dispatch.modules.forecast.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.modules.dashboard.entity.ResourceStatistics;
import com.campus.dispatch.modules.dashboard.mapper.StatisticsMapper;
import com.campus.dispatch.modules.forecast.client.ForecastClient;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import com.campus.dispatch.modules.forecast.mapper.ForecastMapper;
import com.campus.dispatch.modules.forecast.service.ForecastService;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import com.campus.dispatch.modules.resource.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private final ForecastClient forecastClient;
    private final ForecastMapper forecastMapper;
    private final StatisticsMapper statisticsMapper;
    private final ResourceMapper resourceMapper;

    @Override
    public void runPredict() {
        LocalDate start = LocalDate.now().minusDays(30);
        List<ResourceStatistics> history = statisticsMapper.selectList(new LambdaQueryWrapper<ResourceStatistics>()
                .ge(ResourceStatistics::getStatDate, start));

        if (history.isEmpty()) {
            List<CampusResource> resources = resourceMapper.selectList(null);
            history = resources.stream().map(resource -> {
                ResourceStatistics statistics = new ResourceStatistics();
                statistics.setResourceId(resource.getId());
                statistics.setStatDate(LocalDate.now());
                statistics.setHour(0);
                statistics.setBookingCount(0);
                return statistics;
            }).toList();
        }

        try {
            List<ResourceForecast> forecasts = forecastClient.predict(history);
            if (forecasts == null || forecasts.isEmpty()) {
                log.warn("预测服务返回空结果，保留上次预测");
                return;
            }
            forecastMapper.delete(null);
            for (ResourceForecast forecast : forecasts) {
                forecastMapper.insert(forecast);
            }
            log.info("人流预测完成，共 {} 条", forecasts.size());
        } catch (Exception ex) {
            log.warn("预测调用失败，保留上次结果", ex);
        }
    }
}