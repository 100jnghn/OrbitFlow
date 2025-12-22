package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.enums.SearchType;
import lombok.Data;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentListReqDto
 * @since : 25. 12. 22. 월요일
 **/

@Data
public class DocumentListReqDto {
    String keyword;
    SearchType searchType;
    DocumentStatus status;
    LocalDate startDate;
    LocalDate endDate;
}
