package com.finalproj.orbitflow.approval.document.render.pdf;

import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.service.render.DocumentContentRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfHtmlBuilder
 * @since : 26. 1. 4. 일요일
 **/


@Component
@RequiredArgsConstructor
public class PdfHtmlBuilder {

    private final DocumentContentRenderService renderService;

    public String build(
            Long documentId,
            PdfApprovalLineDto approvalLine,
            PdfContentSchema schema,
            String name,
            Instant submittedAt
    ) {
        String approvalLineHtml = renderApprovalLine(documentId, approvalLine);
        String bodyHtml = renderService.render(documentId, schema);
        String footerHtml = renderFooter(name, submittedAt);

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8"/>
                    <link rel="stylesheet" href="/css/user-document/pdf-view.css"/>
                </head>
                <body class="pdf-document">
                
                    %s   <!-- 결재선 -->
                
                    <div class="document-content-panel">
                        %s   <!-- 본문 -->
                    </div>
                
                    %s   <!-- 하단 메타 -->
                
                </body>
                </html>
                """.formatted(
                approvalLineHtml,
                bodyHtml,
                footerHtml
        );
    }


    private String renderApprovalLine(
            Long documentId,
            PdfApprovalLineDto approvalLine
    ) {

        if (approvalLine == null || approvalLine.getApprovers().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<table class=\"approval-table\">");

        int index = 0;
        int total = approvalLine.getApprovers().size();
        int rowCount = (int) Math.ceil(total / 5.0);

        for (int row = 0; row < rowCount; row++) {

            sb.append("<tr>");

        /* =========================
           좌측 세로 '결재' 라벨
           (첫 줄에만 rowspan)
        ========================= */
            if (row == 0) {
                sb.append("<th class=\"approval-label\" rowspan=\"")
                        .append(rowCount)
                        .append("\">결<br/>재</th>");

            }

            int colCount = 0;

        /* =========================
           실제 결재자 (최대 5명)
        ========================= */
            while (index < total && colCount < 5) {

                var approver = approvalLine.getApprovers().get(index);

                sb.append("<td class=\"approval-cell\">");

                // 직급
                sb.append("<div class=\"stamp-position\">")
                        .append(approver.getPosition() != null ? approver.getPosition() : "")
                        .append("</div>");

                // 서명
                sb.append("<div class=\"stamp-signature\">");
                if (approver.getApproverLineId() != null) {
                    sb.append("<img src=\"pdf-image://signature/")
                            .append(documentId)
                            .append("/")
                            .append(approver.getApproverLineId())
                            .append("\" />");
                }
                sb.append("</div>");

                // 이름
                sb.append("<div class=\"stamp-name\">")
                        .append(approver.getName())
                        .append("</div>");

                sb.append("</td>");

                index++;
                colCount++;
            }

        /* =========================
           빈 셀 채우기 (5칸 고정)
        ========================= */
            while (colCount < 5) {
                sb.append("<td class=\"approval-cell empty\"></td>");
                colCount++;
            }

            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }


    private String renderFooter(String name, Instant submittedAt) {

        if (name == null && submittedAt == null) {
            return "";
        }

        String dateText =
                submittedAt != null
                        ? submittedAt.toString().substring(0, 10) // yyyy-MM-dd
                        : "";

        return """
                <div class="document-footer">
                    <div class="footer-date">%s</div>
                    <div class="footer-writer">기안자 : %s</div>
                </div>
                """.formatted(
                dateText,
                name != null ? name : "-"
        );
    }

}
