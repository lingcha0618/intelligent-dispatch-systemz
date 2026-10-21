package com.campus.dispatch.modules.dashboard.controller;

import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.dashboard.service.DashboardService;
import com.campus.dispatch.modules.dashboard.vo.ForecastPointVO;
import com.campus.dispatch.modules.dashboard.vo.OccupancyVO;
import com.campus.dispatch.modules.dashboard.vo.OverviewVO;
import com.campus.dispatch.modules.dashboard.vo.TrendPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "11 数据大屏统计")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @io.swagger.v3.oas.annotations.Operation(summary = "大屏总览统计（KPI数据）")
    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.success(dashboardService.overview());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "预约趋势统计（近N天）")
    @GetMapping("/trend")
    public Result<List<TrendPointVO>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.trend(days));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "AI人流预测曲线")
    @GetMapping("/forecast")
    public Result<List<ForecastPointVO>> forecast(@RequestParam(required = false) Long resourceId,
                                                  @RequestParam(defaultValue = "24") int hours) {
        return Result.success(dashboardService.forecast(resourceId, hours));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "资源使用率排行")
    @GetMapping("/occupancy")
    public Result<List<OccupancyVO>> occupancy(@RequestParam(required = false) Long resourceId) {
        return Result.success(dashboardService.occupancy(resourceId));
    }
}