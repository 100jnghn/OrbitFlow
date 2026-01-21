package com.finalproj.orbitflow.approval.form.template.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplate
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


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
