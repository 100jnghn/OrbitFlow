package com.finalproj.orbitflow.hr.positionCategory.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategory
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "position_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "name"})
        }
)
public class PositionCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 카테고리 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String name; // 본부장 / 팀장 등

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /* ========= 생성 ========= */
    public static PositionCategory create(
            Company company,
            String name,
            int orderIndex
    ) {
        PositionCategory category = new PositionCategory();
        category.company = company;
        category.name = name;
        category.orderIndex = orderIndex;
        category.isActive = true;
        return category;
    }

    /* ========= 수정 ========= */
    public void update(String name, Boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}