package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.application.ApprovalEventTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결재 완료 이후 근태 관련 후처리를 수행하는 이벤트 핸들러.
 * <p>
 * 결재 트랜잭션이 정상적으로 커밋된 이후에만 동작하며,
 * 결재된 문서를 기준으로 근태 반영 로직을 비동기 방식으로 처리한다.
 * <p>
 * 실제 근태 처리 로직은 ApprovalEventTxService에 위임하고,
 * 이 클래스는 트랜잭션 이벤트를 받아 연결해주는 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceApprovalHandler
 * @since : 25. 12. 31. 수요일
 */


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
