package com.finalproj.orbitflow.hr.orgPositionUsage.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조직별 직책 사용 정책 (화이트리스트)
 *
 * - 레코드 존재 = 해당 조직에서 해당 직책 사용 가능
 * - 레코드 없음 = 사용 불가
 *
 * @author : seunga03
 * @filename : OrgPositionUsage
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "org_position_usage",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "org_id", "position_category_id"})
        }
)
public class OrgPositionUsage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 정책 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_category_id", nullable = false)
    private PositionCategory positionCategory;

    /* ========= Factory ========= */
    public static OrgPositionUsage create(
            Company company,
            Organization organization,
            PositionCategory positionCategory
    ) {
        OrgPositionUsage usage = new OrgPositionUsage();
        usage.company = company;
        usage.organization = organization;
        usage.positionCategory = positionCategory;
        return usage;
    }
}