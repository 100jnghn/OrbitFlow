package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 결재 문서 요약을 위한 AI 프롬프트를 생성하는 빌더 클래스.
 * <p>
 * 문서 요약 요청 정보와 양식 스키마 정보를 기반으로
 * SummaryPromptContext를 생성한 뒤,
 * 여러 PromptComponent 구현체를 순차적으로 조합하여
 * 하나의 완성된 요약 프롬프트 문자열을 만든다.
 * <p>
 * 각 PromptComponent는 요약 프롬프트의 특정 영역을 담당하며,
 * 이 클래스는 컴포넌트들을 정해진 순서대로 조립하는 역할만 수행한다.
 * <p>
 * 프롬프트 구성 로직을 컴포넌트 단위로 분리하여
 * 요약 방식이나 출력 형식이 변경되더라도
 * 빌더 자체의 수정 없이 확장이 가능하도록 설계되었다.
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryPromptBuilder
 * @since : 26. 1. 5. 월요일
 */


@Component
@RequiredArgsConstructor
public class AiSummaryPromptBuilder {

    private final List<PromptComponent<SummaryPromptContext>> components;

    public String build(
            AiSummaryReqDto req,
            FormTemplateSchema schema
    ) {
        SummaryPromptContext ctx =
                new SummaryPromptContext(req, schema);

        StringBuilder sb = new StringBuilder();
        for (PromptComponent<SummaryPromptContext> c : components) {
            c.append(sb, ctx);
        }
        return sb.toString();
    }
}
