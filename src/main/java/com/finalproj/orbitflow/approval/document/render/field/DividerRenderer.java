package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

/**
 * 문서 내 구분선(divider) 컴포넌트를 PDF용 HTML로 렌더링하는 렌더러.
 * <p>
 * divider 필드는 값이나 라벨을 가지지 않으며,
 * 문서 영역을 시각적으로 구분하기 위한 용도로만 사용된다.
 * <p>
 * 렌더링 시에는 고정된 <hr> 태그를 반환하며,
 * 필드 값이나 RenderContext 정보는 사용하지 않는다.
 *
 * @author : Choi MinHyeok
 * @filename : DividerRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class DividerRenderer implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "divider".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {
        return "<hr class=\"doc-divider\"/>";
    }
}
