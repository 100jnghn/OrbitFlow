package com.finalproj.orbitflow.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화를 위한 설정 클래스.
 * AuditorAware 구현체를 Bean으로 등록하여 엔티티의 생성자/수정자 정보가 자동으로 관리되도록 한다.
 *
 * @author : seunga03
 * @filename : JpaAuditingConfig
 * @since : 2025-12-15 월요일
 */

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new AuditorAwareImpl();
    }
}
