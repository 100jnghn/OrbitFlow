package com.finalproj.orbitflow.approval.document.eventHandler;

import com.finalproj.orbitflow.approval.document.dto.CompanyEventPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : BusinessTripHandler
 * @since : 25. 12. 31. 수요일
 **/

@Component
@RequiredArgsConstructor
public class BusinessTripHandler {


    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {


        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        // COMPANY_EVENT가 아니면 무시
        if (document.getTemplateGroup().getBaseRole() != BaseRole.BUSINESS_TRIP) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(document.getId())
                .orElseThrow(() ->
                        new NotFoundException("문서 내용을 찾을 수 없습니다.")
                );

        CompanyEventPayload payload =
                documentContentParser.extractCompanyEvent(content);

        Employee writer = document.getWriter();
    }
}
