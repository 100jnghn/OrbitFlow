package com.finalproj.orbitflow.approval.form.template.group.entity;

import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.category.entity.TemplateCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FormTemplateGroup
 * <p>
 * 여러 개의 결재 양식(FormTemplate)을 하나의 기준으로 묶기 위한
 * 결재 양식 그룹 엔티티이다.
 * <p>
 * 양식 그룹은 회사 단위로 관리되며,
 * 동일한 회사 내에서는 그룹 이름이 중복될 수 없도록
 * (company_id + name) 기준의 유니크 제약을 가진다.
 * <p>
 * 하나의 FormTemplateGroup은
 * - 어떤 카테고리에 속하는지(templateCategory)
 * - 해당 그룹이 가지는 문서의 기본 성격(baseRole)
 * - 현재 사용 여부(active)
 * 와 같은 메타 정보를 함께 관리한다.
 * <p>
 * baseRole은 해당 양식 그룹이
 * 휴가, 출장, 외근, 회사 일정 등
 * 특정 도메인 역할을 가지는지 여부를 나타내며,
 * AI 양식 생성이나 일정/근태 연동 정책의 기준으로 사용된다.
 * <p>
 * 이 엔티티는 양식의 실제 구조나 버전 정보를 직접 가지지 않고,
 * 개별 FormTemplate 엔티티들이 이 그룹을 기준으로
 * 버전 관리되도록 설계되어 있다.
 * <p>
 * 활성/비활성 여부(active)는
 * 그룹 단위로 노출 여부를 제어하기 위한 용도로 사용되며,
 * 비활성화된 그룹에 대해서는 신규 양식 생성이 제한된다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateGroup
 * @since 2025. 12. 15.
 */


@Entity
@Table(
        name = "form_template_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_form_template_group_company_name",
                        columnNames = {"company_id", "name"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplateGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_category_id", nullable = false)
    private TemplateCategory templateCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_role", length = 30)
    private BaseRole baseRole;               // ✅ 추가 (nullable)


    @Column(nullable = false)
    private Boolean active;


    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
