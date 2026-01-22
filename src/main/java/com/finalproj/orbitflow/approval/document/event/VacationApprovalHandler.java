package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.application.ApprovalEventTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결재 완료 이후 휴가 관련 후처리를 담당하는 이벤트 핸들러.
 * <p>
 * 결재 트랜잭션이 정상적으로 커밋된 이후에만 실행되며,
 * 승인된 문서를 기준으로 휴가 반영 로직을 비동기 방식으로 처리한다.
 * <p>
 * 실제 휴가 처리 로직은 ApprovalEventTxService에 위임하고,
 * 이 클래스는 트랜잭션 이벤트를 받아 실행을 연결하는 역할만 수행한다.
 *
 * @author : Choi MinHyeok
 * @filename : VacationApprovalHandler
 * @since : 25. 12. 31. 수요일
 */


@Component
@RequiredArgsConstructor
public class VacationApprovalHandler {

    private final ApprovalEventTxService approvalEventTxService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {
        approvalEventTxService.processVacationApproval(documentId);
    }
}
