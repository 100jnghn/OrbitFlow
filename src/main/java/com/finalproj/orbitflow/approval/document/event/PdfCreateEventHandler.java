package com.finalproj.orbitflow.approval.document.event;

import com.finalproj.orbitflow.approval.document.service.application.DocumentPdfApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결재 완료 이후 문서 PDF 생성을 처리하는 이벤트 핸들러.
 * <p>
 * 결재 트랜잭션이 정상적으로 커밋된 이후에만 실행되며,
 * 승인된 문서를 기준으로 PDF 생성 및 저장 작업을 비동기 방식으로 수행한다.
 * <p>
 * 실제 PDF 생성 로직은 DocumentPdfApplicationService에 위임하고,
 * 이 클래스는 트랜잭션 이벤트를 받아 실행을 연결하는 역할만 맡는다.
 * <p>
 * PDF 생성은 별도의 전용 스레드 풀(pdfTaskExecutor)에서 처리되어
 * 결재 흐름에 영향을 주지 않도록 구성되어 있다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfCreateEventHandler
 * @since : 26. 1. 4. 일요일
 */


@Component
@RequiredArgsConstructor
public class PdfCreateEventHandler {

    private final DocumentPdfApplicationService documentPdfApplicationService;

    @Async("pdfTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {
        documentPdfApplicationService.generateAndStorePdf(
                documentId
        );
    }
}