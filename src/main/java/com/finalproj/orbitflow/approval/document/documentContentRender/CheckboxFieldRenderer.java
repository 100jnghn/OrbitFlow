package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CheckboxFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class CheckboxFieldRenderer extends HtmlRenderUtil
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
