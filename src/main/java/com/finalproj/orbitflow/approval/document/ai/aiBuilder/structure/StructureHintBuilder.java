package com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure;

import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;

/**
 * 문서 양식 구조 정보를 기반으로 AI 프롬프트용 구조 힌트를 생성하는 인터페이스.
 * <p>
 * FormTemplateSchema를 분석하여,
 * 문서가 어떤 형태와 성격을 가지는지에 대한
 * 설명용 힌트 문자열을 생성하는 역할을 정의한다.
 * <p>
 * 이 인터페이스의 구현체는
 * 문서 내용을 요약하거나 변경 사항을 직접 생성하지 않으며,
 * AI가 문서를 올바르게 해석할 수 있도록
 * 구조적 맥락을 보조하는 정보만 제공한다.
 * <p>
 * 생성된 힌트는 요약(summary)이나 변경 분석(diff) 프롬프트에 포함되어
 * AI의 이해를 돕는 참고 정보로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : StructureHintBuilder
 * @since : 26. 1. 6. 화요일
 */


public interface StructureHintBuilder {
    String build(FormTemplateSchema templateSchema);
}
