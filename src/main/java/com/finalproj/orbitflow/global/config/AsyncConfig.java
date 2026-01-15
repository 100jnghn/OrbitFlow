package com.finalproj.orbitflow.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AsyncConfig
 * @since : 26. 1. 5. 월요일
 **/


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pdfTaskExecutor")
    public Executor pdfTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("pdf-");
        executor.initialize();
        return executor;
    }
}
