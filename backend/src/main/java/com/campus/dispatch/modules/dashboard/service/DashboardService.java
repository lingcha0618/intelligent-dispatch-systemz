package com.campus.dispatch.modules.dashboard.service;

import com.campus.dispatch.modules.dashboard.vo.ForecastPointVO;
import com.campus.dispatch.modules.dashboard.vo.OccupancyVO;
import com.campus.dispatch.modules.dashboard.vo.OverviewVO;
import com.campus.dispatch.modules.dashboard.vo.TrendPointVO;

import java.util.List;

public interface DashboardService {

    OverviewVO overview();

    List<TrendPointVO> trend(int days);

    List<ForecastPointVO> forecast(Long resourceId, int hours);

    List<OccupancyVO> occupancy(Long resourceId);
}