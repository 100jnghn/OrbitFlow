package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffAttachmentComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(50)
public class DiffAttachmentComponent
        implements PromptComponent<DiffPromptContext> {

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        List<String> before = ctx.diff().getBefore().getAttachmentNames();
        List<String> current = ctx.diff().getCurrent().getAttachmentNames();

        if ((before == null || before.isEmpty())
                && (current == null || current.isEmpty())) {
            return;
        }

        sb.append("""
                
                [첨부파일 변경 정보]
                다음은 이전 문서와 현재 문서의 첨부파일 구성이다.
                """);

        appendAttachmentList(sb, "이전 문서", before);
        appendAttachmentList(sb, "현재 문서", current);

        sb.append("""
                
                ※ 첨부파일의 실제 내용은 제공되지 않았으며,
                파일명 기준으로만 변경 여부를 판단하라.
                """);
    }

    private void appendAttachmentList(
            StringBuilder sb,
            String title,
            List<String> names
    ) {
        sb.append("- ").append(title).append(":\n");

        if (names == null || names.isEmpty()) {
            sb.append("  - (첨부파일 없음)\n");
            return;
        }

        names.forEach(name ->
                sb.append("  - ").append(name).append("\n")
        );
    }
}
