package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 문서 변경 사항(diff) 분석을 위해 이전 문서 정보를 프롬프트에 추가하는 컴포넌트.
 * <p>
 * DiffPromptContext에 포함된 이전 문서의 요약 정보를 기반으로,
 * 문서 제목과 핵심 정보, 추가 정보를 정리된 형태로 프롬프트에 출력한다.
 * <p>
 * SummaryFieldRenderer를 사용하여 문서 필드 정보를 사람이 읽기 쉬운 구조로 변환하며,
 * 이후에 이어지는 현재 문서 정보 및 변경 비교 컴포넌트들이
 * 이 내용을 기준으로 변경 여부를 판단할 수 있도록 돕는다.
 * <p>
 * DiffPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 "이전 문서" 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffBeforeDocumentComponent
 * @since : 26. 1. 6. 화요일
 */


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
