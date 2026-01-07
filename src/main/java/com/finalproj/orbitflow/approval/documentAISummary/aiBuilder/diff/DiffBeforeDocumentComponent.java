package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.diff;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.documentAISummary.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffBeforeDocumentComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(30)
public class DiffBeforeDocumentComponent
        implements PromptComponent<DiffPromptContext> {

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        appendDocument("이전 문서", ctx.diff().getBefore(), sb);
    }

    private void appendDocument(
            String title,
            AiSummaryReqDto dto,
            StringBuilder sb
    ) {
        sb.append("\n");
        sb.append("[").append(title).append("]\n");
        sb.append("문서 제목: ").append(dto.getDocumentTitle()).append("\n");

        SummaryFieldRenderer.appendFields(
                sb, "[핵심 정보]", dto.getCoreFields()
        );
        SummaryFieldRenderer.appendFields(
                sb, "[추가 정보]", dto.getOptionalFields()
        );
    }
}
