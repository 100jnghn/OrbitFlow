package com.finalproj.orbitflow.hr.position.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : Position
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "position",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "name"})
        }
)
public class Position extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 직책 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PositionCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_position_id")
    private Position parentPosition;

    @Column(nullable = false, length = 50)
    private String name; // 개발팀장 등

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /* ===== 생성 ===== */
    public static Position create(
            Company company,
            PositionCategory category,
            Position parent,
            String name,
            int orderIndex
    ) {
        Position p = new Position();
        p.company = company;
        p.category = category;
        p.parentPosition = parent;
        p.name = name;
        p.orderIndex = orderIndex;
        p.isActive = true;
        return p;
    }

    /* ===== 수정 ===== */
    public void update(String name, Boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void updateParent(Position parent) {
        this.parentPosition = parent;
    }

    public void changeCategory(PositionCategory category) {
        this.category = category;
    }

}
