package com.finalproj.orbitflow;

import com.finalproj.orbitflow.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class OrbitflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrbitflowApplication.class, args);
    }

}
