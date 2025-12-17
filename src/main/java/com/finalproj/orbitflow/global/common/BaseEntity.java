package com.finalproj.orbitflow.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 필드를 정의하는 추상 클래스.
 * - 생성일시 / 수정일시를 Auditing 기반으로 자동 관리
 * - 생성자 / 수정자 사원 ID를 Spring Security + AuditorAware를 통해 자동 주입
 * 모든 JPA 엔티티는 해당 클래스를 상속하여 변경 이력 추적 및 감사 로그 기반을 공통으로 사용한다.
 *
 * @author : seunga03
 * @filename : BaseEntity
 * @since : 2025-12-15 월요일
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedBy
    private Long modifiedBy;
}
