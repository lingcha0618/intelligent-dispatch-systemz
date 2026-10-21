package com.campus.dispatch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("校园资源智能预约调度系统 API")
                .description("教室/图书馆座位/实验室/会议室预约与调度后端接口")
                .version("1.0.0"));
    }
}