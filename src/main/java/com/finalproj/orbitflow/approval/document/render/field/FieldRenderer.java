package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FieldRenderer
 * @since : 26. 1. 3. 토요일
 **/


public interface FieldRenderer {

    boolean supports(String fieldType);

    String render(RenderContext context, PdfField field);
}