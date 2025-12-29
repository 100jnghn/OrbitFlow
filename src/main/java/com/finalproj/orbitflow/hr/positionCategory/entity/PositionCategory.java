package com.finalproj.orbitflow.hr.positionCategory.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
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
                @UniqueConstraint(columnNames = {"company_id", "org_category_id", "name"})
        }
)
public class PositionCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_category_id", nullable = false)
    private OrgCategory orgCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_position_id")
    private PositionCategory parent;

    @Column(nullable = false, length = 50)
    private String name; // 본부장 / 팀장 등

    @Column(name = "order_index")
    private Integer orderIndex;

    @Getter
    @Column(name = "is_head", nullable = false)
    private Boolean isHead;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /* ========= 생성 ========= */
    public static PositionCategory create(
            Company company,
            OrgCategory orgCategory,
            PositionCategory parent,
            String name,
            Integer orderIndex,
            boolean isHead
    ) {
        PositionCategory pc = new PositionCategory();
        pc.company = company;
        pc.orgCategory = orgCategory;
        pc.parent = parent;
        pc.name = name;
        pc.orderIndex = orderIndex;
        pc.isHead = isHead;
        pc.isActive = true;
        return pc;
    }

    /* ========= 상태 변경 ========= */
    public void activate(Integer newOrderIndex) {
        this.isActive = true;
        this.orderIndex = newOrderIndex;
    }

    public void deactivate() {
        this.isActive = false;
        this.orderIndex = null;
    }

    /* ========= 수정 ========= */
    public void updateName(String name) {
        this.name = name;
    }

    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void updateParent(PositionCategory parent) {
        this.parent = parent;
    }
}