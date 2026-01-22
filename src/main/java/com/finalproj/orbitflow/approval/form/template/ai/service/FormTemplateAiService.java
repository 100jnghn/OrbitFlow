package com.finalproj.orbitflow.approval.form.template.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.form.template.ai.service.client.FormDesignAiClient;
import com.finalproj.orbitflow.approval.document.ai.enums.AiStatus;
import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateAiReqDto;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateAiResDto;
import com.finalproj.orbitflow.approval.form.template.ai.entity.LogFormTemplateAi;
import com.finalproj.orbitflow.approval.form.template.ai.pipeline.AiFormResParser;
import com.finalproj.orbitflow.approval.form.template.ai.pipeline.FormDesignPipeline;
import com.finalproj.orbitflow.approval.form.template.ai.prompt.FormPromptBuilder;
import com.finalproj.orbitflow.approval.form.template.ai.repository.LogFormTemplateAiRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI를 이용한 결재 양식 생성 요청을 처리하는 서비스 클래스이다.
 * <p>
 * 이 서비스는 단순히 AI를 호출하는 역할에 그치지 않고,
 * 프롬프트 구성부터 AI 응답 처리, 정책 보정, 결과 조립,
 * 그리고 전체 과정에 대한 로그 저장까지의 흐름을 책임진다.
 * <p>
 * 처리 흐름은 다음과 같이 구성된다.
 * - 요청자(사원) 및 기준이 되는 템플릿 정보 조회
 * - 문서 유형에 따른 요청 컨텍스트 구성
 * - 프롬프트 빌더를 통해 AI 입력 프롬프트 생성
 * - AI 호출 및 원본 응답 수신
 * - AI 응답 JSON 파싱
 * - 정책 및 규칙 기반 파이프라인 처리
 * - 최종 양식 JSON 조립
 * - 요청/응답 전 과정 로그 저장
 * <p>
 * AI 응답이 실패하거나 파싱에 실패한 경우에도,
 * 당시의 프롬프트와 컨텍스트, 오류 정보는
 * LogFormTemplateAi 엔티티로 반드시 기록된다.
 * <p>
 * 이 서비스에서 반환하는 결과는
 * 실제 저장용 FormTemplate이 아니라,
 * 프론트에서 미리보기 및 추가 편집을 위해 사용하는
 * 양식 JSON 구조이다.
 * <p>
 * 즉, 이 클래스는
 * “AI 양식 생성 기능의 진입점이자 전체 조정자” 역할을 수행한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateAiService
 * @since 2026. 1. 7.
 */


@Service
@RequiredArgsConstructor
public class FormTemplateAiService {

    private final FormDesignAiClient formDesignAiClient;
    private final LogFormTemplateAiRepository logRepo;
    private final EmployeeRepository employeeRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final ObjectMapper objectMapper;
    private final AiFormResParser resultParser;
    private final FormDesignPipeline pipeline;

    @Value("${openai.model}")
    String aiModel;

    public String generateFormTemplatePrompt(
            String formName,
            String purpose,
            boolean allowScheduleEvent
    ) {
        return new FormPromptBuilder()
                .base()
                .components()
                .responseFormat()
                .userRequest(formName, purpose, allowScheduleEvent)
                .build();
    }

    @Transactional
    public FormTemplateAiResDto requestFormTemplate(
            Long employeeId,
            FormTemplateAiReqDto reqDto
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("No employee with id: " + employeeId));

        FormTemplate formTemplate = formTemplateRepository.findById(reqDto.formTemplateId())
                .orElseThrow(() -> new NotFoundException("No template with id: " + reqDto.formTemplateId()));

        BaseRole baseRole = formTemplate.getTemplateGroup() != null
                ? formTemplate.getTemplateGroup().getBaseRole()
                : null;

        boolean allowScheduleEvent = (baseRole != null);

        FormDesignReqContext reqCtx = new FormDesignReqContext(
                reqDto.formTemplateId(),
                reqDto.formName(),
                reqDto.purpose(),
                allowScheduleEvent,
                baseRole
        );

        String requestContextJson = toJsonSafe(reqCtx);

        String prompt = generateFormTemplatePrompt(
                reqDto.formName(),
                reqDto.purpose(),
                allowScheduleEvent
        );

        String rawJson;
        try {
            rawJson = formDesignAiClient.completeFormDesign(prompt);
        } catch (Exception e) {
            logRepo.save(LogFormTemplateAi.builder()
                    .company(employee.getCompany())
                    .templateGroup(formTemplate.getTemplateGroup())
                    .prompt(prompt)
                    .requestContext(requestContextJson)
                    .generatedTemplateJson(null)
                    .responseContext(null)
                    .model(aiModel)
                    .status(AiStatus.FAILED)
                    .errorMessage("AI call failed: " + e.getMessage())
                    .build());
            throw e;
        }

        AiFormDesignResult parsed;
        try {
            parsed = resultParser.parse(rawJson);
        } catch (Exception e) {
            logRepo.save(LogFormTemplateAi.builder()
                    .company(employee.getCompany())
                    .templateGroup(formTemplate.getTemplateGroup())
                    .prompt(prompt)
                    .requestContext(requestContextJson)
                    .generatedTemplateJson(rawJson)
                    .responseContext(null)
                    .model(aiModel)
                    .status(AiStatus.FAILED)
                    .errorMessage("AI result parse failed: " + e.getMessage())
                    .build());
            throw e;
        }

        FormDesignPipeline.PipelineResult processed = pipeline.run(parsed, reqCtx);

        Object templateJson;
        try {
            templateJson = objectMapper.readValue(
                    objectMapper.writeValueAsString(processed.processed()),
                    Object.class
            );
        } catch (Exception e) {
            throw new RuntimeException("AI template JSON 변환 실패", e);
        }

        String responseContextJson = toJsonSafe(processed.responseContext());

        logRepo.save(LogFormTemplateAi.builder()
                .company(employee.getCompany())
                .templateGroup(formTemplate.getTemplateGroup())
                .prompt(prompt)
                .requestContext(requestContextJson)
                .generatedTemplateJson(rawJson)
                .responseContext(responseContextJson)
                .model(aiModel)
                .status(AiStatus.COMPLETED)
                .errorMessage(null)
                .build());

        return new FormTemplateAiResDto(templateJson);
    }


    private String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"json_serialization_failed\"}";
        }
    }
}
