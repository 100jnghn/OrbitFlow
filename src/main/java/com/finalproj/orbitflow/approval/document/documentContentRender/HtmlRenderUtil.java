package com.finalproj.orbitflow.approval.document.documentContentRender;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : HtmlRenderUtil
 * @since : 26. 1. 4. 일요일
 **/


public abstract class HtmlRenderUtil {

    protected String wrapField(PdfField field, String valueHtml) {

        String label = field.getLabel() != null
                ? field.getLabel()
                : "";

        String safeValue =
                (valueHtml == null || valueHtml.isBlank())
                        ? "-"
                        : valueHtml;

        return """
                    <div class="doc-field field-%s">
                        <div class="doc-field-label">%s</div>
                        <div class="doc-field-value">
                            %s
                        </div>
                    </div>
                """.formatted(
                field.getFieldType(),
                escape(label),
                safeValue
        );
    }

    protected String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    protected String textOrDash(Object value) {
        if (value == null) return "-";

        String s = value.toString().trim();
        return s.isEmpty() ? "-" : escape(s);
    }
}
