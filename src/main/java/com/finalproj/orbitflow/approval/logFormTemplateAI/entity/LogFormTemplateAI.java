package com.finalproj.orbitflow.approval.logFormTemplateAI.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : LogFormTemplateAI
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.logFormTemplateAI.enums.AiStatus;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_form_template_ai")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogFormTemplateAI extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_group_id")
    private FormTemplateGroup templateGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_template_id")
    private FormTemplate createdTemplate;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(name = "generated_template_json", columnDefinition = "json")
    private String generatedTemplateJson;

    @Column(name = "generated_rule_json", columnDefinition = "json")
    private String generatedRuleJson;

    @Column(length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AiStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
}
