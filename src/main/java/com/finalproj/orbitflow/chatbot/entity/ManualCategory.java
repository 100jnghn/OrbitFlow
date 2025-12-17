package com.finalproj.orbitflow.chatbot.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manual_category",
        uniqueConstraints = {
                // 회사와 카테고리 이름 조합은 유일해야 할 가능성이 높음
                @UniqueConstraint(columnNames = {"company_id", "category_name"})
        })
public class ManualCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;        // 카테고리 ID (PK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;         // 회사 ID (FK)

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;    // 카테고리 이름

    @Column(name = "description", length = 255)
    private String description;     // 카테고리 설명

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 카테고리 사용 여부 (DB default=true 반영)

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;      // 정렬 순서 (INT)

    // created_at, updated_at은 BaseEntity에서 관리
}
