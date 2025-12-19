package com.finalproj.orbitflow.approval.formTemplate.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplate
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_category_id", nullable = false)
    private TemplateCategory templateCategory;

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

    public void changeCategory(TemplateCategory category) {
        this.templateCategory = category;
    }

    public void updateTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }

    public void updateAffectTags(List<AffectTag> affectTags) {
        this.affectTags = affectTags;
    }

    public void updateApprovalRuleJson(String approvalRuleJson) {
        this.approvalRuleJson = approvalRuleJson;
    }

    public void updateStatus(FormTemplateStatus status) {
        this.status = status;
    }

    public void updateVersion(int nextVersion) {this.activeVersion = nextVersion;}
}
