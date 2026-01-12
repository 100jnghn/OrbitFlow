package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.ApprovalEventTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceApprovalHandler
 * @since : 25. 12. 31. 수요일
 **/

@Component
@RequiredArgsConstructor
public class AttendanceApprovalHandler {

    private final ApprovalEventTxService approvalEventTxService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {
        approvalEventTxService.processAttendanceApproval(documentId);
    }
}
