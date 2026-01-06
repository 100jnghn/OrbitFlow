package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.summary;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryBasePromptComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(10)
public class SummaryBasePromptComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        sb.append("""
                너는 사내 결재 문서를 요약하는 AI다.
                아래 정보를 바탕으로 간결한 요약문을 작성하라.
                
                조건:
                - 불필요한 수식어 제거
                - 핵심 정보 중심
                - 한글로 작성
                - 3~5문장 이내
                - 첨부파일이 있는 경우, 문서 맥락 수준에서만 언급
                - 첨부파일의 구체적인 내용은 추측하지 말 것
                - 요약의 첫 문장은 문서 제목을 반영하여 공식적인 보고서 문장으로 작성할 것
                
                """);
    }
}
