package com.campus.dispatch.modules.config.controller;

import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.config.dto.ConfigItemDTO;
import com.campus.dispatch.modules.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@io.swagger.v3.oas.annotations.tags.Tag(name = "07 系统配置")
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @io.swagger.v3.oas.annotations.Operation(summary = "获取全部系统配置项")
    @GetMapping
    public Result<Map<String, String>> getAll() {
        return Result.success(configService.getAll());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "批量修改系统配置项")
    @PutMapping
    public Result<Void> update(@RequestBody List<ConfigItemDTO> items) {
        Map<String, String> configMap = new LinkedHashMap<>();
        for (ConfigItemDTO item : items) {
            configMap.put(item.getKey(), item.getValue());
        }
        configService.update(configMap);
        return Result.success();
    }
}