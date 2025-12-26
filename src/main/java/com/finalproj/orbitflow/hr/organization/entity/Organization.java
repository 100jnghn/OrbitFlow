package com.finalproj.orbitflow.hr.organization.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조직 엔티티
 * 트리 구조 (self join)
 *
 * @author : seunga03
 * @filename : Organization
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "organization")
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;    // 조직 ID (PK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;    // 회사 ID (FK)

    @Column(name = "category_id", nullable = false)
    private Long categoryId;   // 조직 카테고리 ID (FK)

    @Column(name = "parent_org_id")
    private Long parentOrgId;  // 상위 조직 ID (Self Join)

    @Column(nullable = false, length = 100)
    private String name;       // 조직명

    @Column(name = "order_index")
    private Integer orderIndex; // 정렬 순서

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;  // 사용 여부 (소프트 삭제)

    public static Organization createRoot(
            Company company,
            Long companyCategoryId   // ← "회사" 카테고리 ID
    ) {
        Organization org = new Organization();
        org.companyId = company.getId();
        org.categoryId = companyCategoryId;
        org.parentOrgId = null;     // 루트
        org.name = company.getName(); // 또는 "회사"
        org.orderIndex = 1;
        org.isActive = true;
        return org;
    }

    public static Organization create(
            Long companyId,
            Long categoryId,
            Long parentOrgId,
            String name,
            Integer orderIndex
    ) {
        Organization org = new Organization();
        org.companyId = companyId;
        org.categoryId = categoryId;
        org.parentOrgId = parentOrgId;
        org.name = name;
        org.orderIndex = orderIndex;
        org.isActive = true;
        return org;
    }

    public void update(Long parentOrgId, String name) {
        this.parentOrgId = parentOrgId;
        this.name = name;
    }

    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void deactivate() {
        this.isActive = false;
        this.orderIndex = null;
    }

    public void activate(Integer newOrderIndex) {
        this.isActive = true;
        this.orderIndex = newOrderIndex;
    }
}
