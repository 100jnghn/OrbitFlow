package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.AiDiffReqDto;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryReqDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 문서 변경 사항(diff) 분석을 위해 문서 본문 내용을 프롬프트에 추가하는 컴포넌트.
 * <p>
 * 이전 문서와 현재 문서의 요약 정보를 각각 출력하여,
 * AI가 두 문서의 내용을 직접 비교할 수 있도록 프롬프트를 구성한다.
 * <p>
 * 문서 제목과 함께 핵심 항목과 기타 항목을 구분하여 전달하며,
 * 각 필드의 라벨과 값을 그대로 제공함으로써
 * 변경 전·후 차이를 명확하게 파악할 수 있도록 돕는다.
 * <p>
 * AiDiffReqDto를 컨텍스트로 사용하는 PromptComponent로,
 * 프롬프트 내에서 실제 문서 내용 비교 영역을 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffContentComponent
 * @since : 26. 1. 6. 화요일
 */


@Component
public class DiffContentComponent
        implements PromptComponent<AiDiffReqDto> {

    @Override
    public void append(StringBuilder sb, AiDiffReqDto req) {
        appendDocument("이전 문서", req.getBefore(), sb);
        appendDocument("현재 문서", req.getCurrent(), sb);
    }

    private void appendDocument(
            String title,
            AiSummaryReqDto dto,
            StringBuilder sb
    ) {
        sb.append("\n[").append(title).append("]\n");
        sb.append("문서 제목: ").append(dto.getDocumentTitle()).append("\n");

        appendFields("핵심 항목", dto.getCoreFields(), sb);
        appendFields("기타 항목", dto.getOptionalFields(), sb);
    }

    private void appendFields(
            String section,
            List<AiSummaryField> fields,
            StringBuilder sb
    ) {
        if (fields == null || fields.isEmpty()) return;

        sb.append(section).append(":\n");
        for (AiSummaryField f : fields) {
            sb.append("- ")
                    .append(f.getLabel())
                    .append(": ")
                    .append(f.getValue())
                    .append("\n");
        }
    }
}
