package com.finalproj.orbitflow.approval.logFormTemplateAI.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.aiClient.FormDesignAiClient;
import com.finalproj.orbitflow.approval.documentAISummary.enums.AiStatus;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormTemplateAiReqDto;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormTemplateAiResDto;
import com.finalproj.orbitflow.approval.logFormTemplateAI.entity.LogFormTemplateAi;
import com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline.AiFormResParser;
import com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline.FormDesignPipeline;
import com.finalproj.orbitflow.approval.logFormTemplateAI.prompt.FormPromptBuilder;
import com.finalproj.orbitflow.approval.logFormTemplateAI.repository.LogFormTemplateAiRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAiService
 * @since : 26. 1. 7. 수요일
 **/


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
        // 1️⃣ 작성자 / 템플릿 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("No employee with id: " + employeeId));

        FormTemplate formTemplate = formTemplateRepository.findById(reqDto.formTemplateId())
                .orElseThrow(() -> new NotFoundException("No formTemplate with id: " + reqDto.formTemplateId()));

        BaseRole baseRole = formTemplate.getTemplateGroup() != null
                ? formTemplate.getTemplateGroup().getBaseRole()
                : null;

        boolean allowScheduleEvent = (baseRole != null);

        // 2️⃣ 요청 컨텍스트
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

        // 3️⃣ AI 호출
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

        // 4️⃣ 파싱
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

        // 5️⃣ 파이프라인 처리
        FormDesignPipeline.PipelineResult processed = pipeline.run(parsed, reqCtx);

        // 🔑 프론트 반환용 (DTO와 동일한 방식)
        Object templateJson;
        try {
            templateJson = objectMapper.readValue(
                    objectMapper.writeValueAsString(processed.processed()),
                    Object.class
            );
        } catch (Exception e) {
            throw new RuntimeException("AI template JSON 변환 실패", e);
        }

        // 🔑 로그 저장용
        String responseContextJson = toJsonSafe(processed.responseContext());

        // 6️⃣ 로그 저장
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

        // 7️⃣ 미리보기 반환
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
