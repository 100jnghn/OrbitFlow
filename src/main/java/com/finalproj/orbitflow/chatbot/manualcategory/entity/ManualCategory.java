package com.finalproj.orbitflow.chatbot.manualcategory.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

/**
 * 매뉴얼 카테고리 엔티티
 *
 * @author : rlagkdus
 * @filename : ManualCategory
 * @since : 2025. 12. 30. 화요일
 */

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manual_category")
public class ManualCategory extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String categoryName;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private Integer sortOrder;


    public void update(String categoryName, String description, Integer sortOrder) {
        if (categoryName != null && !categoryName.isBlank()) {
            this.categoryName = categoryName;
        }
        this.description = description;
        this.sortOrder = sortOrder;
    }
    
}