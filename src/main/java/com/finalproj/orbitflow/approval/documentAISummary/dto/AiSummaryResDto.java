package com.finalproj.orbitflow.approval.documentAISummary.dto;

import com.finalproj.orbitflow.approval.documentAISummary.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryStatus;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryResDto
 * @since : 26. 1. 5. 월요일
 **/

@Getter
@Builder
public class AiSummaryResDto {
    private SummaryType summaryType;
    private SummaryStatus summaryStatus;
    private String context;
    private Long beforeDocumentId;

    public static AiSummaryResDto from(DocumentAISummary documentAISummary) {
        return AiSummaryResDto.builder()
                .summaryType(documentAISummary.getSummaryType())
                .summaryStatus(documentAISummary.getStatus())
                .context(documentAISummary.getContent())
                .beforeDocumentId(
                        documentAISummary.getBeforeDocument() != null
                                ? documentAISummary.getBeforeDocument().getId()
                                : null
                )
                .build();
    }
}
