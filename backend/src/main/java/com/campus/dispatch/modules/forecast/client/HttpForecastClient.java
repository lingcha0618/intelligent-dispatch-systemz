package com.campus.dispatch.modules.forecast.client;

import com.campus.dispatch.modules.dashboard.entity.ResourceStatistics;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@ConditionalOnProperty(name = "forecast.mock", havingValue = "false")
public class HttpForecastClient implements ForecastClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${forecast.service.url:http://localhost:8000}")
    private String baseUrl;

    @Override
    public List<ResourceForecast> predict(List<ResourceStatistics> history) {
        ForecastResponse response = restTemplate.postForObject(baseUrl + "/predict", history, ForecastResponse.class);
        return response == null || response.getForecasts() == null ? List.of() : response.getForecasts();
    }
}