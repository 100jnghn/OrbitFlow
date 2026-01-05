package com.finalproj.orbitflow.chatbot.manualCategory.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

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