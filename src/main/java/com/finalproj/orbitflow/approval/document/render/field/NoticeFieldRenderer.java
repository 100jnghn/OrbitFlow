package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : NoticeFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class NoticeFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "notice".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Map<String, Object> meta = field.getMeta();
        if (meta == null) {
            return "";
        }

        String label = escape(field.getLabel());
        String message = textOrDash(meta.get("message"));
        String style = meta.getOrDefault("style", "info").toString();

        return """
                <div class="doc-field field-notice">
                    <div class="doc-field-label">%s</div>
                    <div class="doc-field-value notice-value">
                        <div class="notice-box %s">%s</div>
                    </div>
                </div>
                """.formatted(
                label,
                escape(style),
                message
        );
    }
}
