package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.diff;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffBasePromptComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(10)
public class DiffBasePromptComponent
        implements PromptComponent<DiffPromptContext> {

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        sb.append("""
                너는 사내 결재 문서를 검토·비교하는 AI다.
                아래 정보는 동일한 양식으로 작성된 두 문서를 비교하기 위해
                서버에서 정규화한 결과이다.
                
                이전 문서와 현재 문서를 비교하여
                변경된 사항의 핵심이 잘 드러나도록 요약하라.
                
                규칙:
                - 변경된 항목만 서술할 것
                - 변경 전 → 변경 후 형태로 설명할 것
                - 변경이 없는 항목은 언급하지 말 것
                - 단순한 표현 변경이나 형식 차이는 중요도가 낮다
                - 문서 제목이 변경된 경우에만 변경 사항에 포함한다
                - 추측하지 말고 제공된 정보만 사용할 것
                - 결과는 한국어로 작성할 것
                - 3~6문장 이내의 보고용 문장으로 작성할 것
                
                """);
    }
}
