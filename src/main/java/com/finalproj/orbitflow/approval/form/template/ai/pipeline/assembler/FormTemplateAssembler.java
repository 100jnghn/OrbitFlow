package com.finalproj.orbitflow.approval.form.template.ai.pipeline.assembler;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateJson;
import com.finalproj.orbitflow.approval.form.template.ai.service.FormTemplateBuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI가 설계한 양식 결과를 실제 결재 양식 JSON 구조로 변환하기 위한
 * 조립(Assembler) 역할의 클래스이다.
 * <p>
 * AI 응답 결과(AiFormDesignResult)는 설계 단계의 데이터 구조이기 때문에,
 * 그대로 저장하거나 화면에 사용할 수 없으며
 * 시스템에서 사용하는 FormTemplateJson 형태로 한 번 더 가공하는 과정이 필요하다.
 * <p>
 * 이 클래스는 해당 변환 과정을 담당하며,
 * 실제 조립 로직은 FormTemplateBuildService에 위임한다.
 * <p>
 * 즉, Assembler는
 * - AI 결과를 받아서
 * - 양식 이름(forName)과 함께
 * - 최종 저장 및 사용 가능한 양식 구조로 변환하는
 * 흐름상의 연결 지점 역할만 수행한다.
 * <p>
 * AI 파이프라인 단계에서
 * "설계 결과 → 실제 양식 구조"를 명확히 분리하기 위한 목적의 클래스이다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateAssembler
 * @since 2026. 1. 8.
 */


@Component
@RequiredArgsConstructor
public class FormTemplateAssembler {

    private final FormTemplateBuildService buildService;

    public FormTemplateJson assemble(AiFormDesignResult result, String forName) {
        return buildService.build(result, forName);
    }
}