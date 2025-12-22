package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentCreateResDto
 * @since : 25. 12. 23. 화요일
 **/

@Data
@Builder
public class DocumentCreateResDto {
    private Long documentId;
    private String title;
    private DocumentStatus status;
    private List<DocumentFormFieldDto> fields;

    public static DocumentCreateResDto from(
            Long documentId, String title, DocumentStatus status, List<DocumentFormFieldDto> fields
    ) {
        return DocumentCreateResDto.builder()
                .documentId(documentId)
                .title(title)
                .status(status)
                .fields(fields)
                .build();
    }
}
