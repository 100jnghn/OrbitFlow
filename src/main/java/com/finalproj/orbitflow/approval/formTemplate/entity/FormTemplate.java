package com.finalproj.orbitflow.approval.formTemplate.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplate
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


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

@Entity
@Table(
        name = "form_template",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_template_group_company_version",
                        columnNames = {"company_id", "template_group_id", "version"}
                )
        }
)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_category_id", nullable = false)
    private TemplateCategory templateCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormTemplateStatus status;

    @Column(name = "affect_tags", columnDefinition = "json")
    private String affectTags;

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

    public void updateAffectTags(String affectTags) {
        this.affectTags = affectTags;
    }

    public void updateApprovalRuleJson(String approvalRuleJson) {
        this.approvalRuleJson = approvalRuleJson;
    }

    public void updateStatus(FormTemplateStatus formTemplateStatus) {
        this.status = formTemplateStatus;
    }
}
