package com.campus.dispatch.job;

import com.campus.dispatch.modules.forecast.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForecastJob {

    private final ForecastService forecastService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void run() {
        log.info("每日人流预测任务开始");
        forecastService.runPredict();
    }
}