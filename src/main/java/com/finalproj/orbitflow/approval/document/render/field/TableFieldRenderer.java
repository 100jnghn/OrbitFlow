package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 표(table) 형태의 필드를 PDF 출력용 HTML 테이블로 변환하는 렌더러.
 * <p>
 * 필드의 meta 정보에 정의된 컬럼 목록(columns)을 기준으로
 * 테이블의 헤더를 구성하고,
 * value에 포함된 각 행 데이터를 순서대로 출력한다.
 * <p>
 * 각 행에는 자동으로 번호(No) 컬럼이 추가되며,
 * 셀 값이 없거나 null인 경우에는 "-"로 표시한다.
 * <p>
 * 이 렌더러는 테이블 구조를 그대로 시각화하는 역할만 담당하며,
 * 데이터의 의미 해석이나 가공 로직은 포함하지 않는다.
 * <p>
 * meta 정보나 rows 데이터가 올바르지 않은 경우에는
 * 렌더링 결과를 생성하지 않고 빈 문자열을 반환한다.
 *
 * @author : Choi MinHyeok
 * @filename : TableFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


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

        table.append("<thead><tr>");
        table.append("<th class=\"col-no\">No</th>");

        for (Object col : columns) {
            Map<?, ?> c = (Map<?, ?>) col;
            table.append("<th>")
                    .append(escape(String.valueOf(c.get("label"))))
                    .append("</th>");
        }
        table.append("</tr></thead>");

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
