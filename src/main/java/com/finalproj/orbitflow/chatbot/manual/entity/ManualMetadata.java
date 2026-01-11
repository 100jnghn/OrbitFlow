package com.finalproj.orbitflow.chatbot.manual.entity;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualMetadata
 * @since : 2025. 12. 30. 화요일
 */

import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

/**
 * 매뉴얼 메타데이터 엔티티
 * - 업로드된 매뉴얼 파일과 카테고리를 연결하고 상태를 관리한다.
 * - BaseEntity 상속을 통해 생성/수정 정보(Audit)를 자동 기록한다.
 */

/**
 * 매뉴얼 메타데이터 엔티티
 * - manual_metadata 테이블과 매핑되며 BaseEntity를 통해 Audit 필드(created_by 등)를 관리합니다.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manual_metadata")
public class ManualMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 1. 회사 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 2. 실제 파일 정보 (file 테이블 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    // 3. 카테고리 정보 (제공해주신 ManualCategory 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ManualCategory category;

    // 4. 수동 입력 필드 (필요 시 파일 정보 외에 별도 관리용)
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    // 5. 상태 및 활성화 여부
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "READY"; // READY, PROCESSING, SUCCESS, FAILED

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    /**
     * 벡터화 처리 상태 업데이트
     */
    public void updateStatus(String status) {
        this.status = status;
    }

    /**
     * 매뉴얼 활성화 상태 변경
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }
}