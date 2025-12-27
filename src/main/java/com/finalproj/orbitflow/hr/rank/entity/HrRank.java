package com.finalproj.orbitflow.hr.rank.entity;

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
 * @filename : HrRank
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "hr_rank",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "name"})
        }
)
public class HrRank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 직급 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_hr_rank_id")
    private HrRank parentHrRank;

    @Column(nullable = false, length = 50)
    private String name; // 대리 / 과장 등

    @Column(name = "order_index")
    private Integer orderIndex; // null 허용

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // ====== 팩토리 ======
    public static HrRank create(Company company, HrRank parentHrRank, String name, Integer orderIndex) {
        HrRank rank = new HrRank();
        rank.company = company;
        rank.parentHrRank = parentHrRank;
        rank.name = name;
        rank.orderIndex = orderIndex;
        rank.isActive = true;
        return rank;
    }

    // ====== 도메인 메서드 ======
    public void update(String name, HrRank parentHrRank) {
        this.name = name;
        this.parentHrRank = parentHrRank;
    }

    public void changeOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void activate(Integer newOrderIndex) {
        this.isActive = true;
        this.orderIndex = newOrderIndex;
    }

    public void deactivate() {
        this.isActive = false;
        this.orderIndex = null;
    }
}