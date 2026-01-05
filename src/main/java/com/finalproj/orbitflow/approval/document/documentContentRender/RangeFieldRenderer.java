package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RangeFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class RangeFieldRenderer extends HtmlRenderUtil
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