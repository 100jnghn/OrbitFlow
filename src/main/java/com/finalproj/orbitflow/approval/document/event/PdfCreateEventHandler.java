package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.DocumentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfCreateEventHandler
 * @since : 26. 1. 4. 일요일
 **/


@Component
@RequiredArgsConstructor
public class PdfCreateEventHandler {

    private final DocumentApplicationService documentApplicationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {
        documentApplicationService.generateAndStorePdf(
                documentId
        );
    }
}