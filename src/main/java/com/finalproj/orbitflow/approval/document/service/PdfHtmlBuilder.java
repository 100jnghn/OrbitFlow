package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.document.documentContentRender.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
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

    String build(
            Long documentId,
            PdfApprovalLineDto approvalLine,
            PdfContentSchema schema,
            String name,
            Instant submittedAt
    ) {
        String approvalLineHtml = renderApprovalLine(approvalLine);
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


    private String renderApprovalLine(PdfApprovalLineDto approvalLine) {

        if (approvalLine == null || approvalLine.getApprovers().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"approval-line-wrapper\">");
        sb.append("<div class=\"approval-line\">");

        for (var approver : approvalLine.getApprovers()) {
            sb.append("<div class=\"approver-box\">");

            // 서명 영역 (img 전용)
            sb.append("<div class=\"approver-signature\">");
            if (approver.getSignatureImageUrl() != null) {
                sb.append("<img src=\"")
                        .append(approver.getSignatureImageUrl())
                        .append("\" />");
            }
            sb.append("</div>");

            // 이름
            sb.append("<div class=\"approver-name\">")
                    .append(approver.getName())
                    .append("</div>");

            // 직책 (optional)
            if (approver.getPosition() != null) {
                sb.append("<div class=\"approver-position\">")
                        .append(approver.getPosition())
                        .append("</div>");
            }

            sb.append("</div>");
        }

        sb.append("</div>");
        sb.append("</div>");

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
