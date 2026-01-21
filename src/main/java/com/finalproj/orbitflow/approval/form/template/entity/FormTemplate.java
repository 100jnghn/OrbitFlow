package com.finalproj.orbitflow.approval.form.template.entity;

import com.finalproj.orbitflow.approval.form.template.enums.AffectTag;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 결재 문서를 작성할 때 사용되는
 * 개별 결재 양식(Form Template)을 표현하는 엔티티이다.
 * <p>
 * 하나의 FormTemplate은 특정 회사(company)에 속하며,
 * FormTemplateGroup을 기준으로 버전 관리된다.
 * 동일한 양식 그룹 내에서 여러 버전의 양식이 존재할 수 있고,
 * 그중 하나만이 활성(ACTIVE) 상태로 사용된다.
 * <p>
 * 이 엔티티는 다음과 같은 정보를 함께 관리한다.
 * - 양식 버전 정보(version)
 * - 현재 활성 버전(activeVersion, 조회용)
 * - 양식 상태(DRAFT / ACTIVE / INACTIVE)
 * - 양식 구조 JSON(templateJson)
 * - 결재선 규칙 JSON(approvalRuleJson)
 * - 근태/일정 연동 여부를 나타내는 affectTags
 * <p>
 * 양식 구조와 결재선 규칙은
 * 잦은 변경과 유연한 확장을 고려하여 JSON 형태로 저장하며,
 * affectTags 역시 다중 값을 가지므로 JSON 컬럼으로 관리한다.
 * <p>
 * 이 엔티티는 순수하게 “양식 데이터”만을 책임지며,
 * 버전 증가, 활성화/비활성화 정책 판단과 같은
 * 비즈니스 로직은 서비스 계층에서 처리한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplate
 * @since 2025. 12. 15.
 */


@Entity
@Table(name = "form_template")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_group_id", nullable = false)
    private FormTemplateGroup templateGroup;

    @Column(nullable = false)
    private int version;

    @Column(name = "active_version", insertable = false, updatable = false)
    private Integer activeVersion;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormTemplateStatus status;

    @Column(name = "affect_tags", columnDefinition = "json")
    @Convert(converter = AffectTagListConverter.class)
    private List<AffectTag> affectTags;

    @Column(name = "template_json", nullable = false, columnDefinition = "json")
    private String templateJson;

    @Column(name = "approval_rule_json", columnDefinition = "json")
    private String approvalRuleJson;

    public void updateTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }

    public void updateApprovalRuleJson(String approvalRuleJson) {
        this.approvalRuleJson = approvalRuleJson;
    }

    public void updateStatus(FormTemplateStatus status) {
        this.status = status;
    }

    public void updateVersion(int nextVersion) {this.version = nextVersion;}

    public boolean isActive() {
        return this.status == FormTemplateStatus.ACTIVE;
    }

    public  void deActive() {
        this.status = FormTemplateStatus.INACTIVE;
    }
}
