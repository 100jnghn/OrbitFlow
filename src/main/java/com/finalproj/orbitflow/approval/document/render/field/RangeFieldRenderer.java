package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 기간(date-range) 및 시간 범위(time-range) 필드를
 * PDF 출력용 문자열로 변환하는 렌더러.
 * <p>
 * 시작 값(start)과 종료 값(end)을 하나의 범위로 묶어
 * "start ~ end" 형식으로 출력한다.
 * <p>
 * 값이 없거나 형식이 맞지 않는 경우에는
 * 해당 필드를 "-"로 표시하여 빈 값임을 명확히 한다.
 * <p>
 * 날짜 범위와 시간 범위는 출력 방식이 동일하기 때문에
 * 하나의 렌더러에서 함께 처리하도록 구성되어 있다.
 * <p>
 * 이 클래스는 값의 유효성이나 의미 해석은 수행하지 않으며,
 * 전달받은 데이터를 PDF에 표현하는 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : RangeFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class RangeFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "date-range".equals(fieldType)
                || "time-range".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (!(value instanceof Map<?, ?> map)) {
            return wrapField(field, "-");
        }

        String start = textOrDash(map.get("start"));
        String end = textOrDash(map.get("end"));

        return wrapField(
                field,
                "<span>%s ~ %s</span>".formatted(start, end)
        );
    }
}