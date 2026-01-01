package com.finalproj.orbitflow.approval.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentRevisionInfoResDto
 * @since : 26. 1. 1. 목요일
 **/

@Getter
@Builder
public class DocumentRevisionInfoResDto {
    private Long beforeDocumentId;
    private Long nextDocumentId;

    private DocumentStatus nextDocumentStatus;

    @JsonProperty("isMine")
    private boolean mine;
}