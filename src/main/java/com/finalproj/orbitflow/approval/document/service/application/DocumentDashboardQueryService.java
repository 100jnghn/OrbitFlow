package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.DocumentMainInfoResDto;
import com.finalproj.orbitflow.approval.document.dto.TimeRange;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결재 대시보드 화면에 표시될 요약 정보를 조회하는 전용 조회 서비스.
 * <p>
 * 특정 사원 기준으로,
 * - 현재 본인 결재 차례인 문서 수
 * - 본인 차례 이전에 대기 중인 문서 수
 * - 진행 중인 기안 문서 수
 * - 반려 후 재상신되지 않은 문서 수
 * - 이번 달 / 지난 달 승인 완료 문서 수
 * 를 집계하여 하나의 응답 DTO로 반환한다.
 * <p>
 * 이 서비스는 화면 표시용 통계 조회에만 사용되며,
 * 문서 상태 변경이나 결재 처리와 같은 쓰기 로직은 포함하지 않는다.
 * 복잡한 집계 로직은 Repository 레벨에서 처리하고,
 * 이 클래스에서는 조회 결과를 조합하는 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentDashboardQueryService
 * @since : 26. 1. 21. 수요일
 */


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentDashboardQueryService {

    private final ApprovalLineRepository approvalLineRepository;
    private final DocumentRepository documentRepository;

    public DocumentMainInfoResDto getMainInfo(Long employeeId) {

        int myTurnWaitingCount =
                approvalLineRepository.countMyTurnWaiting(
                        employeeId,
                        DocumentStatus.IN_PROGRESS
                );

        int waitingBeforeMyTurnCount =
                approvalLineRepository.countWaitingBeforeMyTurn(
                        employeeId,
                        ApprovalStatus.WAITING,
                        DocumentStatus.IN_PROGRESS
                );

        int inProgressCount =
                documentRepository.countByWriterAndStatus(
                        employeeId,
                        DocumentStatus.IN_PROGRESS
                );

        int rejectedCount =
                documentRepository.countRejectedNotResubmitted(
                        employeeId,
                        DocumentStatus.REJECTED
                );

        TimeRange thisMonth = TimeRange.thisMonth();
        TimeRange lastMonth = TimeRange.lastMonth();

        int approvedThisMonthCount =
                documentRepository.countByWriterAndStatusFromDate(
                        employeeId,
                        DocumentStatus.APPROVED,
                        thisMonth.start()
                );

        int approvedLastMonthCount =
                documentRepository.countByWriterAndStatusBetween(
                        employeeId,
                        DocumentStatus.APPROVED,
                        lastMonth.start(),
                        lastMonth.end()
                );

        return new DocumentMainInfoResDto(
                myTurnWaitingCount,
                waitingBeforeMyTurnCount,
                inProgressCount,
                rejectedCount,
                approvedThisMonthCount,
                approvedLastMonthCount
        );
    }
}
