package com.campus.dispatch.modules.forecast.controller;

import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.forecast.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "10 AI人流预测")
@RestController
@RequestMapping("/api/admin/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @io.swagger.v3.oas.annotations.Operation(summary = "手动触发AI人流预测")
    @PostMapping("/trigger")
    public Result<Void> trigger() {
        forecastService.runPredict();
        return Result.success();
    }
}