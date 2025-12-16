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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_form_template_ai")
@Getter
@NoArgsConstructor
public class LogFormTemplateAI extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_group_id")
    private FormTemplateGroup templateGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_template_id")
    private FormTemplate createdTemplate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(columnDefinition = "JSON")
    private String generatedTemplateJson;

    @Column(columnDefinition = "JSON")
    private String generatedRuleJson;

    private String model;

    @Enumerated(EnumType.STRING)
    private AiStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
