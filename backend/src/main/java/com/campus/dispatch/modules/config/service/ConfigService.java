package com.campus.dispatch.modules.config.service;

import java.util.Map;

public interface ConfigService {

    Map<String, String> getAll();

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    void update(Map<String, String> configMap);
}