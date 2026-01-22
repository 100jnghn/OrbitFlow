package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.attendance.record.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendance.record.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.support.access.DocumentAccessValidator;
import com.finalproj.orbitflow.approval.document.support.approver.ApproverResolver;
import com.finalproj.orbitflow.approval.document.support.text.DocumentTextFormatter;
import com.finalproj.orbitflow.approval.document.signature.service.DocumentSignatureService;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결재 문서의 승인 및 반려 처리를 담당하는 Application Service.
 * <p>
 * 진행 중(IN_PROGRESS) 상태의 결재 문서에 대해
 * 현재 결재자의 승인 또는 반려 요청을 처리한다.
 * <p>
 * 주요 역할
 * - 결재 순서 검증 및 현재 결재자 확인
 * - 결재선 상태 변경 (승인 / 반려 / 취소)
 * - 최종 승인 시 문서 상태 변경 및 후속 이벤트 발행
 * - 결재 요청 / 결과 알림 전송
 * - 반려 시 근태 도메인 연동 롤백 처리
 * <p>
 * 이 서비스는 결재 흐름을 조율하는 역할만 담당하며,
 * 결재 규칙 판단이나 텍스트 가공과 같은 세부 로직은
 * 별도의 Support / Resolver 계층에 위임한다. *
 * @author : Choi MinHyeok
 * @filename : DocumentApprovalApplicationService
 * @since : 26. 1. 21. 수요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentApprovalApplicationService {


    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApprovalLineRepository approvalLineRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    private final NotificationCommandService notificationCommandService;
    private final DocumentAccessValidator documentAccessValidator;
    private final DocumentSignatureService documentSignatureService;
    private final ApproverResolver approverResolver;


    @Transactional
    public void approve(Long employeeId, Long documentId, String comment) {

        // 1. 문서 조회 + 상태 검증
        Document document = documentAccessValidator.getForApprover(documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 승인할 수 있습니다.");
        }

        // 2. 결재선 조회
        List<ApprovalLine> lines = getApprovalLines(documentId);

        // 3. 내 결재선 찾기
        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );

        // 4. 내 결재선 승인
        myLine.markApproved(comment);

        documentSignatureService.snapShotDocumentSignature(document, myLine);

        // 5. 다음 결재선 찾기
        ApprovalLine nextLine = lines.stream()
                .filter(line ->
                        line.getOrderNo() > myLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .findFirst()
                .orElse(null);

        if (nextLine != null) {

            // 결재자 상태 검증 로직

            Employee nextApprover = nextLine.getApprover();

            // 6-1. 다음 결재자 유효성 검증
            if (!approverResolver.isValid(nextApprover)
                    || !approverResolver.matchesRole(nextLine, nextApprover)) {

                // 조직/직책 기준으로 대체 결재자 탐색
                Employee replacement = approverResolver.findReplacement(nextLine);

                if (replacement == null) {
                    // 대체 결재자 없음 → 시스템 반려
                    rejectBySystem(
                            document,
                            lines,
                            myLine,
                            "결재자 없음"
                    );
                    return;
                }

                // 대체 결재자 지정
                nextLine.setApprover(replacement);
            }

            // 6-2. 다음 결재 진행
            nextLine.markInProgress();

            String shortTitle = DocumentTextFormatter.shortenTitle(document.getTitle(), 20);

            String content =
                    "[결재 요청 - " + DocumentTextFormatter.now() + "]\n" +
                            "문서 제목 : " + shortTitle + "\n" +
                            "기안자 : " + document.getWriter().getName() +
                            " | " + document.getWriter().getOrganization().getName() +
                            " | " + document.getWriter().getPositionCategory().getName();

            notificationCommandService.createNotification(
                    nextLine.getCompany().getId(),
                    nextLine.getApprover().getId(),
                    NotificationType.APPROVAL,
                    content,
                    "/view/document/" + documentId
            );

        } else {
            // 6-3. 다음 결재자가 없다 → 최종 승인 (기존 로직 그대로)
            document.approve();

            String shortTitle = DocumentTextFormatter.shortenTitle(document.getTitle(), 20);

            String content =
                    "[결재 완료 - " + DocumentTextFormatter.now() + "]\n" +
                            "문서 제목 : " + shortTitle + "\n" +
                            "최종 승인자 : " + myLine.getApprover().getName() +
                            " | " + myLine.getApprover().getOrganization().getName() +
                            " | " + myLine.getApprover().getPositionCategory().getName();

            notificationCommandService.createNotification(
                    document.getCompany().getId(),
                    document.getWriter().getId(),
                    NotificationType.APPROVAL,
                    content,
                    "/view/document/" + documentId
            );

            applicationEventPublisher.publishEvent(document.getId());
        }
    }

    @Transactional
    public void reject(Long employeeId, Long documentId, String comment) {

        Document document = documentAccessValidator.getForApprover(documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 반려할 수 있습니다.");
        }

        List<ApprovalLine> lines = getApprovalLines(documentId);

        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );

        rejectInternal(document, lines, myLine, comment, false);
    }

    private void rejectInternal(
            Document document,
            List<ApprovalLine> lines,
            ApprovalLine rejectLine,
            String comment,
            boolean systemReject
    ) {
        // 4. 내 결재선 반려
        rejectLine.reject(comment); // → REJECTED

        String shortTitle = DocumentTextFormatter.shortenTitle(document.getTitle(), 20);

        String header = systemReject ? "자동 반려" : "결재 문서 반려";

        String content =
                "[" + header + " - " + DocumentTextFormatter.now() + "]\n" +
                        "문서 제목 : " + shortTitle + "\n" +
                        (systemReject
                                ? "사유 : " + comment
                                : "반려자 : " + rejectLine.getApprover().getName()
                                + " | " + rejectLine.getApprover().getOrganization().getName()
                                + " | " + rejectLine.getApprover().getPositionCategory().getName()
                        );


        notificationCommandService.createNotification(
                document.getCompany().getId(),
                document.getWriter().getId(),
                NotificationType.APPROVAL,
                content,
                "/view/document/" + document.getId()
        );

        // 5. 내 이후 결재자 CANCELLED 처리
        lines.stream()
                .filter(line ->
                        line.getOrderNo() > rejectLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .forEach(ApprovalLine::markCancelled); // → CANCELLED

        // 6. 문서 상태 반려
        document.reject();

        // 근태 반영 롤백
        attendanceRecordRepository.findBySourceDocument_Id(document.getId())
                .ifPresent(AttendanceRecord::rejectedDocument);
    }

    private void rejectBySystem(
            Document document,
            List<ApprovalLine> lines,
            ApprovalLine actorLine,
            String reason
    ) {
        // actorLine: 현재 IN_PROGRESS 또는 방금 승인한 라인
        // (시스템 사용자 엔티티가 없다면, 가장 자연스럽게 로그/히스토리를 남길 수 있는 라인을 사용)
        rejectInternal(document, lines, actorLine, reason, true);
    }


    private List<ApprovalLine> getApprovalLines(Long documentId) {
        List<ApprovalLine> lines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (lines.isEmpty()) {
            throw new NotFoundException("문서의 결재선을 찾을 수 없습니다.");
        }
        return lines;
    }
}
