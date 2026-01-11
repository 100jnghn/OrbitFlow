package com.finalproj.orbitflow.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RestTemplateConfig
 * @since : 26. 1. 6. 화요일
 **/


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}