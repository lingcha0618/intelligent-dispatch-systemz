package com.campus.dispatch.modules.forecast.client;

import lombok.Data;

import java.util.List;

@Data
public class ForecastResponse {

    private List<com.campus.dispatch.modules.forecast.entity.ResourceForecast> forecasts;
}