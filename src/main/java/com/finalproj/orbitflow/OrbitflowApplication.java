package com.finalproj.orbitflow;

import com.finalproj.orbitflow.approval.calendarDay.service.impl.props.HolidayApiProperties;
import com.finalproj.orbitflow.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, HolidayApiProperties.class})

public class OrbitflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrbitflowApplication.class, args);
    }

}
