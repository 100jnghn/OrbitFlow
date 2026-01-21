package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 기간 기반 이벤트 필드(event-date-range)를 PDF용 HTML로 렌더링하는 렌더러.
 * <p>
 * 시작일과 종료일을 하나의 기간으로 묶어 표현하며,
 * 메타 정보(baseRole)에 따라 휴가(VACATION) 유형과
 * 일반 일정 유형을 구분해 렌더링한다.
 * <p>
 * 휴가 유형의 경우:
 * - 기간(start ~ end)
 * - 휴가 유형(연차, 반차 등)
 * - 사유
 * <p>
 * 일반 일정 유형의 경우:
 * - 기간(start ~ end)
 * - 일정 제목
 * - 설명
 * <p>
 * 값이 없거나 형식이 올바르지 않은 경우에는
 * 기본 대시(-) 형태로 출력한다.
 *
 * @author : Choi MinHyeok
 * @filename : EventDateRangeFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class EventDateRangeFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "event-date-range".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (!(value instanceof Map<?, ?> rawMap)) {
            return renderEmpty(field);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) rawMap;

        String baseRole = getMetaValue(field, "baseRole");

        String period =
                textOrDash(map.get("start")) + " ~ " + textOrDash(map.get("end"));

        String type;
        String reason;

        if ("VACATION".equals(baseRole)) {
            type = resolveVacationType(context, map.get("vacationTypeId"));
            reason = textOrDash(map.get("reason"));
        } else {
            type = textOrDash(map.get("title"));
            reason = textOrDash(map.get("description"));
        }

        return """
                    <div class="doc-field field-event-date-range">
                        <table class="event-table">
                            <tbody>
                                <tr>
                                    <th class="event-main-label" rowspan="3">%s</th>
                                    <th class="event-sub-label">기간</th>
                                    <td class="event-value">%s</td>
                                </tr>
                                <tr>
                                    <th class="event-sub-label">유형</th>
                                    <td class="event-value">%s</td>
                                </tr>
                                <tr>
                                    <th class="event-sub-label">사유</th>
                                    <td class="event-value">%s</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                """.formatted(
                escape(field.getLabel()),
                escape(period),
                escape(type),
                escape(reason)
        );
    }

    private String renderEmpty(PdfField field) {
        return """
                    <div class="doc-field field-event-date-range">
                        <table class="event-table">
                            <tbody>
                                <tr>
                                    <th class="event-main-label">%s</th>
                                    <td class="event-value" colspan="2">-</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                """.formatted(escape(field.getLabel()));
    }

    private String resolveVacationType(RenderContext context, Object vacationTypeId) {
        if (vacationTypeId == null) return "-";
        return context.resolveVacationTypeName(String.valueOf(vacationTypeId));
    }

    private String getMetaValue(PdfField field, String key) {
        if (field.getMeta() == null) return null;
        Object value = field.getMeta().get(key);
        return value == null ? null : String.valueOf(value);
    }
}
