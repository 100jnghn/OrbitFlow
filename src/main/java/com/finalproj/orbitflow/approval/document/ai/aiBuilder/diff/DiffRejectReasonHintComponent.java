package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 문서 변경 사항(diff) 분석 시 반려 사유를 참고 정보로 제공하는 컴포넌트.
 * <p>
 * 이전 결재 과정에서 문서가 반려된 경우,
 * 반려 사유를 프롬프트에 포함시켜
 * AI가 변경 배경을 이해할 수 있도록 돕는다.
 * <p>
 * 반려 사유는 변경 이유를 추측하기 위한 힌트로만 사용되며,
 * 실제 변경된 항목이나 제공되지 않은 정보를
 * 새로 생성하거나 추론하지 않도록 명확히 제한한다.
 * <p>
 * DiffPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 변경 배경 정보 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffRejectReasonHintComponent
 * @since : 26. 1. 6. 화요일
 */


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
