package com.finalproj.orbitflow.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : OpenApiConfig
 * @since : 26. 1. 22. 목요일
 **/


@OpenAPIDefinition(
        info = @Info(
                title = "OrbitFlow API",
                description = "OrbitFlow 그룹웨어 백엔드 API 문서",
                version = "v1"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {
}