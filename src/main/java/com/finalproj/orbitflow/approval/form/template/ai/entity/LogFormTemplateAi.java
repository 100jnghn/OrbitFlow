package com.finalproj.orbitflow.approval.form.template.ai.entity;

import com.finalproj.orbitflow.approval.document.ai.enums.AiStatus;
import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * AI를 통해 결재 양식을 생성하는 과정에서 발생하는 모든 요청과 결과를
 * 그대로 기록하기 위한 로그 엔티티이다.
 * <p>
 * 실제 양식(FormTemplate)을 생성하기 전에,
 * AI에게 어떤 프롬프트를 전달했고 어떤 응답을 받았는지를
 * 재현 가능하도록 남기는 것이 주 목적이다.
 * <p>
 * 이 엔티티에 저장된 데이터는
 * - AI 결과 검증
 * - 생성 실패 원인 추적
 * - 프롬프트 개선 및 품질 분석
 * - 운영 중 문제 발생 시 히스토리 확인
 * 을 위해 사용된다.
 * <p>
 * generatedTemplateJson은 AI가 반환한 결과를
 * 서버에서 가공하지 않은 원본 상태 그대로 저장하며,
 * 이후 실제 FormTemplate으로 변환되는 과정은 별도의 로직에서 처리된다.
 * <p>
 * AI 호출이 성공했더라도 양식 생성에 실패할 수 있으므로,
 * 생성 결과(FormTemplate)와 로그는 분리하여 관리한다.
 *
 * @author Choi MinHyeok
 * @filename LogFormTemplateAi
 * @since 2025. 12. 15.
 */

@Entity
@Table(name = "log_form_template_ai")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogFormTemplateAi extends BaseEntity {

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
    @Column(nullable = false, length = 20)
    private AiStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
}
