package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DividerRenderer
 * @since : 26. 1. 4. 일요일
 **/


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
