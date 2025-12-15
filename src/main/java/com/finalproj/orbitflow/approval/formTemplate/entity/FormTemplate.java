package com.finalproj.orbitflow.approval.formTemplate.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplate
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_template", uniqueConstraints = {@UniqueConstraint(columnNames = {"template_group_id", "version"})})
@Getter
@NoArgsConstructor
public class FormTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_group_id", nullable = false)
    private FormTemplateGroup templateGroup;

    @Column(nullable = false)
    private int version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_category_id", nullable = false)
    private TemplateCategory templateCategory;

    @Column(columnDefinition = "JSON")
    private String affectTags;

    @Column(columnDefinition = "JSON", nullable = false)
    private String templateJson;

    @Column(columnDefinition = "JSON", nullable = false)
    private String approvalRuleJson;

    @Column(nullable = false)
    private boolean isActive;

}
