package com.finalproj.orbitflow;

import com.finalproj.orbitflow.global.time.calendar.service.impl.props.HolidayApiProperties;
import com.finalproj.orbitflow.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.finalproj.orbitflow")
@EnableConfigurationProperties({JwtProperties.class, HolidayApiProperties.class})
public class OrbitflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrbitflowApplication.class, args);
    }

}
