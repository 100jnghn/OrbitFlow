package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : TableFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class TableFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "table".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        Map<String, Object> meta = field.getMeta();

        if (!(value instanceof List<?> rows) || meta == null) {
            return "";
        }

        Object columnsObj = meta.get("columns");
        if (!(columnsObj instanceof List<?> columns)) {
            return "";
        }

        String label = escape(field.getLabel());

        StringBuilder table = new StringBuilder();
        table.append("<table class=\"doc-table\">");

        /* ===============================
           THEAD
        =============================== */
        table.append("<thead><tr>");
        table.append("<th class=\"col-no\">No</th>");

        for (Object col : columns) {
            Map<?, ?> c = (Map<?, ?>) col;
            table.append("<th>")
                    .append(escape(String.valueOf(c.get("label"))))
                    .append("</th>");
        }
        table.append("</tr></thead>");

        /* ===============================
           TBODY
        =============================== */
        table.append("<tbody>");

        int rowNo = 1;
        for (Object rowObj : rows) {
            if (!(rowObj instanceof Map<?, ?> row)) continue;

            table.append("<tr>");
            table.append("<td class=\"col-no\">")
                    .append(rowNo++)
                    .append("</td>");

            for (Object col : columns) {
                Map<?, ?> c = (Map<?, ?>) col;
                Object cellValue = row.get(c.get("id"));

                table.append("<td>")
                        .append(textOrDash(cellValue))
                        .append("</td>");
            }
            table.append("</tr>");
        }

        table.append("</tbody></table>");

        /* ===============================
           FINAL HTML (event-date-range 패턴)
        =============================== */
        return """
                <div class="doc-field field-table">
                    <div class="doc-field-label">%s</div>
                    %s
                </div>
                """.formatted(
                label,
                table
        );
    }
}
