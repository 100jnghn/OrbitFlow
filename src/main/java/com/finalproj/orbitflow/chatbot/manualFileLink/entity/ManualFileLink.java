package com.finalproj.orbitflow.chatbot.manualFileLink.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manual_link")
public class ManualFileLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;      // 연결 고유 ID (PK)

    @Column(name = "file_id", nullable = false)
    private Long fileId;            // 파일 ID (FK)

    @Column(name = "category_id", nullable = false)
    private Long categoryId;        // 카테고리 ID (FK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;         // 회사 ID (FK)

    @Column(name = "status", length = 50)
    private String status;          // 파일 처리 상태 (예: PENDING, PROCESSED)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 사용 활성화 여부 (DB default=true 반영)

    @Column(name = "vectorized_at")
    private LocalDateTime vectorizedAt; // 벡터화 처리 시각 (DATETIME)

    // created_at, updated_at은 BaseEntity에서 관리
}