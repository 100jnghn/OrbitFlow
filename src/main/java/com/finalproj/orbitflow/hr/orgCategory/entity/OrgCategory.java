package com.finalproj.orbitflow.hr.orgCategory.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조직 카테고리 엔티티
 * 예) 회사 / 본부 / 부서 / 팀
 *
 * @author : seunga03
 * @filename : OrgCategory
 * @since : 2025-12-16 화요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "org_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "name"})
        }
)
public class OrgCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;                            // 조직 카테고리 ID (PK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;                             // 회사 ID (FK)

    @Column(nullable = false, length = 50)
    private String name;                                // 조직 유형명 (회사/본부/부서/팀)

    @Column(name = "order_index")
    private Integer orderIndex;                             // 정렬 순서 (null 허용)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;                           // 사용 여부 (소프트 삭제)

    @Column(name = "is_root", nullable = false)
    private Boolean isRoot;

    public static OrgCategory create(
            Long companyId,
            String name,
            Integer orderIndex,
            Boolean isRoot
    ) {
        OrgCategory category = new OrgCategory();
        category.companyId = companyId;
        category.name = name;
        category.orderIndex = orderIndex;
        category.isActive = true;
        category.isRoot = isRoot;
        return category;
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
}
