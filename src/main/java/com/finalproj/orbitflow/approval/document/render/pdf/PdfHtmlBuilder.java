package com.finalproj.orbitflow.approval.document.render.pdf;

import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.service.render.DocumentContentRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 결재 문서를 PDF로 변환하기 위한 HTML 문서를 구성하는 빌더 클래스.
 * <p>
 * 승인 완료된 결재 문서를 기준으로
 * - 결재선 영역
 * - 문서 본문 영역
 * - 하단 메타 정보(기안자, 상신일)
 * 를 하나의 HTML 문서로 조합한다.
 * <p>
 * 실제 필드 렌더링이나 컴포넌트별 HTML 생성은
 * DocumentContentRenderService에 위임하며,
 * 이 클래스는 PDF 변환에 필요한 전체 HTML 구조를 조립하는 역할만 담당한다.
 * <p>
 * 생성된 HTML은 OpenHTMLToPDF 기반 렌더링 파이프라인에서
 * 그대로 PDF로 변환되는 것을 전제로 한다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfHtmlBuilder
 * @since : 26. 1. 4. 일요일
 */


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
                    %s
                    <div class="document-content-panel">
                        %s
                    </div>
                    %s
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

            if (row == 0) {
                sb.append("<th class=\"approval-label\" rowspan=\"")
                        .append(rowCount)
                        .append("\">결<br/>재</th>");

            }

            int colCount = 0;

            while (index < total && colCount < 5) {

                var approver = approvalLine.getApprovers().get(index);

                sb.append("<td class=\"approval-cell\">");

                sb.append("<div class=\"stamp-position\">")
                        .append(approver.getPosition() != null ? approver.getPosition() : "")
                        .append("</div>");

                sb.append("<div class=\"stamp-signature\">");
                if (approver.getApproverLineId() != null) {
                    sb.append("<img src=\"pdf-image://signature/")
                            .append(documentId)
                            .append("/")
                            .append(approver.getApproverLineId())
                            .append("\" />");
                }
                sb.append("</div>");

                sb.append("<div class=\"stamp-name\">")
                        .append(approver.getName())
                        .append("</div>");

                sb.append("</td>");

                index++;
                colCount++;
            }

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
