package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 결재 문서 요약 프롬프트에 추가 정보 영역을 보조적으로 포함시키는 컴포넌트.
 * <p>
 * SummaryPromptContext에 포함된 문서의 선택적 필드 정보를 기반으로,
 * 핵심 정보에는 포함되지 않지만
 * 요약에 참고가 될 수 있는 부가 정보를 프롬프트에 추가한다.
 * <p>
 * 추가 정보가 없는 경우에는 아무 내용도 출력하지 않으며,
 * 요약 결과에서 핵심 정보와 중복되지 않도록
 * 보조적인 역할만 수행하도록 설계되었다.
 * <p>
 * SummaryPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 "[추가 정보]" 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryOptionalFieldsComponent
 * @since : 26. 1. 6. 화요일
 */


@Component
@Order(50)
public class SummaryOptionalFieldsComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        var dto = ctx.request();

        if (dto.getOptionalFields() == null ||
                dto.getOptionalFields().isEmpty()) {
            return;
        }

        sb.append("\n");

        SummaryFieldRenderer.appendFields(
                sb,
                "[추가 정보]",
                dto.getOptionalFields()
        );
    }
}
