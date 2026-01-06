package com.finalproj.orbitflow.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FastJacksonConfig
 * @since : 26. 1. 6. 화요일
 **/


@Configuration
public class FastJacksonConfig {

    @Bean
    public ObjectMapper holidayObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}