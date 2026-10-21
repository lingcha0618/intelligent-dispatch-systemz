package com.campus.dispatch.modules.forecast.client;

import com.campus.dispatch.modules.dashboard.entity.ResourceStatistics;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "forecast.mock", havingValue = "true", matchIfMissing = true)
public class MockForecastClient implements ForecastClient {

    private final Random random = new Random();

    @Override
    public List<ResourceForecast> predict(List<ResourceStatistics> history) {
        Set<Long> resourceIds = history.stream()
                .map(ResourceStatistics::getResourceId)
                .collect(Collectors.toSet());
        LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<ResourceForecast> results = new ArrayList<>();
        for (Long resourceId : resourceIds) {
            for (int h = 1; h <= 24; h++) {
                ResourceForecast forecast = new ResourceForecast();
                forecast.setResourceId(resourceId);
                forecast.setForecastTime(base.plusHours(h));
                forecast.setHeatValue(random.nextInt(101));
                results.add(forecast);
            }
        }
        return results;
    }
}