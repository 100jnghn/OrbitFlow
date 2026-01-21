package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryAttachmentPart
 * @since : 26. 1. 6. 화요일
 **/


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
