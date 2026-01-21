package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.DocumentMainInfoResDto;
import com.finalproj.orbitflow.approval.document.dto.TimeRange;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentDashboardQueryService
 * @since : 26. 1. 21. 수요일
 **/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentDashboardQueryService {

    private final ApprovalLineRepository approvalLineRepository;
    private final DocumentRepository documentRepository;

    /**
     * 결재 메인 화면에 표시할 문서 관련 요약 정보를 조회한다.
     * <p>
     * - 내 결재 차례 대기 건수
     * - 내 차례 이전 대기 건수
     * - 내가 작성한 진행중 문서 수
     * - 반려 후 재상신되지 않은 문서 수
     * - 이번 달 / 지난 달 승인 완료 문서 수
     */
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
