package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EventDateRangeFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class EventDateRangeFieldRenderer extends HtmlRenderUtil
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
