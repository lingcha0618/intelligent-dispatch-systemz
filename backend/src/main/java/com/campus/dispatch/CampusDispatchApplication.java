package com.campus.dispatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.campus.dispatch.modules.*.mapper")
public class CampusDispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusDispatchApplication.class, args);
    }
}