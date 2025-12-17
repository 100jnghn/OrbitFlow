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

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}