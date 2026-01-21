package com.finalproj.orbitflow.chatbot.manual.entity;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualMetadata
 * @since : 2025. 12. 30. 화요일
 */

import com.finalproj.orbitflow.chatbot.manualcategory.entity.ManualCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ManualCategory category;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "READY";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void updateStatus(String status) {
        this.status = status;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }


}