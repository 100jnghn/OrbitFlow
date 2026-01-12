package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.ApprovalEventTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : VacationApprovalHandler
 * @since : 25. 12. 31. 수요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class VacationApprovalHandler {

    private final ApprovalEventTxService approvalEventTxService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {

        log.info(
                "[VacationApprovalHandler] event received - documentId={}",
                documentId
        );

        approvalEventTxService.processVacationApproval(documentId);
    }
}
