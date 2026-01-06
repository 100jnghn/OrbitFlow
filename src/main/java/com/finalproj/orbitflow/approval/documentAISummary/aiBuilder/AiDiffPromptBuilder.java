package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

import com.finalproj.orbitflow.approval.documentAISummary.dto.AiDiffReqDto;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiDiffPromptBuilder
 * @since : 26. 1. 5. 월요일
 **/


@Component
public class AiDiffPromptBuilder {

    public String build(AiDiffReqDto request) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                다음은 동일한 양식으로 작성된 두 개의 문서이다.
                첫 번째는 이전 문서이고, 두 번째는 수정된 현재 문서이다.
                
                두 문서를 비교하여 변경된 내용을 중심으로 요약하라.
                
                규칙:
                - 변경된 항목만 서술할 것
                - 변경 전 → 변경 후 형태로 설명할 것
                - 변경이 없는 항목은 언급하지 말 것
                - 문서 제목의 변경도 변경 사항에 포함한다
                - 추측하지 말고 제공된 정보만 사용할 것
                - 결과는 한국어로 작성할 것
                """);

        appendDocument("이전 문서", request.getBefore(), sb);
        appendDocument("현재 문서", request.getCurrent(), sb);

        return sb.toString();
    }

    private void appendDocument(
            String title,
            AiSummaryReqDto dto,
            StringBuilder sb
    ) {
        sb.append("\n");
        sb.append("[").append(title).append("]\n");
        sb.append("문서 제목: ").append(dto.getDocumentTitle()).append("\n");

        appendFields("핵심 항목", dto.getCoreFields(), sb);
        appendFields("기타 항목", dto.getOptionalFields(), sb);
    }

    private void appendFields(
            String sectionTitle,
            List<AiSummaryField> fields,
            StringBuilder sb
    ) {
        if (fields.isEmpty()) return;

        sb.append(sectionTitle).append(":\n");

        for (AiSummaryField field : fields) {
            sb.append("- ")
                    .append(field.getLabel())
                    .append(": ")
                    .append(formatValue(field.getValue()))
                    .append("\n");
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "";

        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        if (value instanceof Collection<?> c) {
            return c.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        return String.valueOf(value);
    }
}
