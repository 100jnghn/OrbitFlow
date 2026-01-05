package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RadioFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class RadioFieldRenderer extends HtmlRenderUtil
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
