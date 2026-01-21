package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 결재 문서 요약 프롬프트에 첨부파일 정보를 추가하는 컴포넌트.
 * <p>
 * SummaryPromptContext에 포함된 첨부파일 목록을 기반으로,
 * 문서에 포함된 첨부파일 정보를 텍스트 형태로 프롬프트에 출력한다.
 * <p>
 * 첨부파일의 실제 내용은 제공되지 않으며,
 * 파일명 기준으로만 문서의 맥락을 참고하도록 명시하여
 * AI가 불필요한 추론을 하지 않도록 제한한다.
 * <p>
 * 요약 프롬프트를 구성하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 첨부 정보 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryAttachmentComponent
 * @since : 26. 1. 6. 화요일
 */


@Component
@Order(60)
public class SummaryAttachmentComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        var dto = ctx.request();

        List<String> names = dto.getAttachmentNames();
        if (names == null || names.isEmpty()) return;

        sb.append("""
                
                [첨부 정보]
                이 문서에는 다음 첨부파일이 포함되어 있다.
                """);

        names.forEach(name ->
                sb.append("- ").append(name).append("\n")
        );

        sb.append("""
                
                ※ 첨부파일의 실제 내용은 제공되지 않았다.
                파일명 기준으로만 문서의 맥락을 참고하라.
                """);
    }
}
