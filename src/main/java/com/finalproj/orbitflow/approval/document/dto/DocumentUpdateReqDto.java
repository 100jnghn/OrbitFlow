package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentUpdateReqDto
 * @since : 25. 12. 23. 화요일
 **/


@Data
@Builder
public class DocumentUpdateReqDto {
    private String title;
    private DocumentStatus status;
}
