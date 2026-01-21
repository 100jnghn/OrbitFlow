package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 문서 변경 사항(diff) 중 첨부파일 변경 정보를 프롬프트에 추가하는 컴포넌트.
 * <p>
 * 이전 문서와 현재 문서의 첨부파일 목록을 비교하여,
 * 파일명 기준으로 변경 여부를 AI가 판단할 수 있도록
 * 프롬프트에 첨부파일 구성 정보를 텍스트 형태로 출력한다.
 * <p>
 * 첨부파일의 실제 내용은 제공되지 않으며,
 * 오직 파일명 목록만을 기반으로 분석하도록 명시하는 역할을 한다.
 * <p>
 * DiffPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 첨부파일 변경 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffAttachmentComponent
 * @since : 26. 1. 6. 화요일
 */


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
