package com.campus.dispatch.modules.forecast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ForecastMapper extends BaseMapper<ResourceForecast> {

    @Select("SELECT * FROM resource_forecast WHERE resource_id = #{resourceId} ORDER BY forecast_time DESC LIMIT 1")
    ResourceForecast selectLatest(@Param("resourceId") Long resourceId);
}