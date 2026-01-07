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
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "log_form_template_ai")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogFormTemplateAI extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    private FormTemplateGroup templateGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private FormTemplate createdTemplate;

    // 🔹 AI에게 실제로 보낸 프롬프트
    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    // 🔹 AI 판단 전제 (formName, baseRole 등)
    @Column(name = "request_context", nullable = false, columnDefinition = "json")
    private String requestContext;

    // 🔹 AI가 그대로 반환한 JSON (절대 수정 금지)
    @Column(name = "generated_template_json", columnDefinition = "json")
    private String generatedTemplateJson;

    // 🔹 서버가 AI 응답을 어떻게 처리했는지
    @Column(name = "response_context", columnDefinition = "json")
    private String responseContext;

    @Column(length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AiStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
}
