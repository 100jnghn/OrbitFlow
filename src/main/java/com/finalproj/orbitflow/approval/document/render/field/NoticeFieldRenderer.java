package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 문서 내 안내용(notice) 컴포넌트를 PDF 출력용 HTML로 변환하는 렌더러.
 * <p>
 * notice 필드는 실제 입력 값(value)을 가지지 않고,
 * 필드의 meta 정보에 포함된 message와 style을 기반으로
 * 안내 문구를 화면에 표시하는 용도로 사용된다.
 * <p>
 * 렌더링 시:
 * - label은 일반 필드와 동일하게 상단에 출력하고
 * - message는 강조 박스 형태로 표시한다
 * - style 정보(info, warning 등)는 CSS 클래스에 그대로 전달한다
 * <p>
 * meta 정보가 존재하지 않는 경우에는
 * 의미 있는 출력이 불가능하므로 빈 문자열을 반환한다.
 * <p>
 * 이 렌더러는 안내 표현에만 집중하며,
 * 문서 데이터나 상태에 대한 판단 로직은 포함하지 않는다.
 *
 * @author : Choi MinHyeok
 * @filename : NoticeFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class NoticeFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "notice".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Map<String, Object> meta = field.getMeta();
        if (meta == null) {
            return "";
        }

        String label = escape(field.getLabel());
        String message = textOrDash(meta.get("message"));
        String style = meta.getOrDefault("style", "info").toString();

        return """
                <div class="doc-field field-notice">
                    <div class="doc-field-label">%s</div>
                    <div class="doc-field-value notice-value">
                        <div class="notice-box %s">%s</div>
                    </div>
                </div>
                """.formatted(
                label,
                escape(style),
                message
        );
    }
}
