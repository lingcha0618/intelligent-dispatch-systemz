package com.campus.dispatch.modules.dashboard.mapper;

import com.campus.dispatch.modules.dashboard.vo.OccupancyVO;
import com.campus.dispatch.modules.dashboard.vo.TrendPointVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardMapper {

    long countResource();

    long countInUseBooking();

    long countTodayBooking();

    long countViolation();

    long countTotalBooking();

    List<TrendPointVO> selectTrend(@Param("days") int days);

    List<OccupancyVO> selectOccupancy(@Param("resourceId") Long resourceId);
}