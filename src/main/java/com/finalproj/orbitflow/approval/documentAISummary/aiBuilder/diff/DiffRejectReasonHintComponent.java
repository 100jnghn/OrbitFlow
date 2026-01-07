package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.diff;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffRejectReasonHintComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(15)
public class DiffRejectReasonHintComponent
        implements PromptComponent<DiffPromptContext> {

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        String reason = ctx.rejectComment();
        if (reason == null || reason.isBlank()) return;

        sb.append("""
                
                [변경 배경 정보]
                이 문서는 이전 결재 과정에서 다음과 같은 사유로 반려되었다.
                """);

        sb.append("- ").append(reason).append("\n");

        sb.append("""
                
                위 내용은 변경 이유를 이해하기 위한 참고 정보이다.
                변경된 항목 자체를 추측하거나
                제공되지 않은 변경 사항을 생성하지 말 것.
                """);
    }
}
