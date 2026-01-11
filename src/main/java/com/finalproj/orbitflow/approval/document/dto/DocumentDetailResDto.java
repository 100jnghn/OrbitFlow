package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalLineViewResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentDetailResDto
 * @since : 25. 12. 28. 일요일
 **/


@Getter
@AllArgsConstructor
@Builder
public class DocumentDetailResDto {

    private Long documentId;
    private String title;
    private DocumentStatus status;

    private String submittedBy;
    private Instant submittedAt;

    private FormTemplateSchema contentSchema;
    private List<ApprovalLineViewResDto> approvalLines;

    private boolean myApprovalOrder;

    private Long pdfFileId;

    public static DocumentDetailResDto from(
            Document document,
            FormTemplateSchema schema,
            List<ApprovalLineViewResDto> approvalLines,
            boolean myApprovalOrder,
            Long pdfFileId
    ) {
        return DocumentDetailResDto.builder()
                .documentId(document.getId())
                .title(document.getTitle())
                .status(document.getStatus())
                .submittedBy(document.getWriter().getName())
                .submittedAt(document.getSubmittedAt())
                .contentSchema(schema)
                .approvalLines(approvalLines)
                .myApprovalOrder(myApprovalOrder)
                .pdfFileId(pdfFileId)
                .build();
    }
}
