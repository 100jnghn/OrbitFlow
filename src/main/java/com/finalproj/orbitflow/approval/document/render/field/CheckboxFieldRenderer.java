package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

/**
 * 체크박스(checkbox) 타입 필드를 PDF용 HTML로 렌더링하는 필드 렌더러.
 * <p>
 * 체크박스 필드는 선택 결과가 단일 값 또는 문자열 형태로 전달되며,
 * 렌더링 시에는 선택된 값을 그대로 텍스트로 출력한다.
 * <p>
 * 값이 존재하지 않는 경우에는 "-"를 표시하여
 * 미선택 상태임을 명확하게 표현한다.
 * <p>
 * 공통 필드 레이아웃은 AbstractFieldRenderer의 wrapField 메서드를 사용하여
 * 라벨과 값 출력 구조를 일관되게 유지한다.
 *
 * @author : Choi MinHyeok
 * @filename : CheckboxFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class CheckboxFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "checkbox".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (value == null) {
            return wrapField(field, "<span>-</span>");
        }

        return wrapField(
                field,
                "<span>" + escape(String.valueOf(value)) + "</span>"
        );
    }
}
