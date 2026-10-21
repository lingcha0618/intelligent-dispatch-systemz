package com.campus.dispatch.modules.forecast.client;

import com.campus.dispatch.modules.dashboard.entity.ResourceStatistics;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;

import java.util.List;

public interface ForecastClient {

    List<ResourceForecast> predict(List<ResourceStatistics> history);
}