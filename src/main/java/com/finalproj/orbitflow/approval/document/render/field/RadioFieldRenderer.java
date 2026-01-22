package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

/**
 * 라디오 버튼(radio) 타입 필드를 PDF 출력용 HTML로 변환하는 렌더러.
 * <p>
 * radio 필드는 단일 선택 값을 가지며,
 * 선택된 값만 문자열로 표시하는 단순한 구조를 가진다.
 * <p>
 * 렌더링 시:
 * - 값이 존재하면 선택된 항목의 라벨을 그대로 출력하고
 * - 값이 없을 경우에는 "-" 로 대체하여 표시한다
 * <p>
 * 체크 상태나 선택지 목록 자체를 표현하지 않고,
 * 최종 선택 결과만 보여주는 용도로 사용된다.
 * <p>
 * 이 렌더러는 값의 의미나 유효성에 대해 판단하지 않으며,
 * 전달받은 값을 PDF에 표현하는 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : RadioFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class RadioFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "radio".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (value == null) {
            return wrapField(field, "<span>-</span>");
        }

        String label = escape(String.valueOf(value));
        return wrapField(field, "<span>" + label + "</span>");
    }
}
