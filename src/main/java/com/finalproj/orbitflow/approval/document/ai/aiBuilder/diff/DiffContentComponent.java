package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.AiDiffReqDto;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryReqDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffContentComponent
 * @since : 26. 1. 6. 화요일
 **/


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
