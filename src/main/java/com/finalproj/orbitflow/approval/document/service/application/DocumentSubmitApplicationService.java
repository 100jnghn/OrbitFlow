package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.service.domain.LeaveCalculationService;
import com.finalproj.orbitflow.approval.document.support.access.DocumentAccessValidator;
import com.finalproj.orbitflow.approval.document.support.approver.ApproverResolver;
import com.finalproj.orbitflow.approval.document.support.text.DocumentTextFormatter;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileCleanupService;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 결재 문서를 상신(SUBMIT)하는 Application Service.
 * -
 * 임시 저장(DRAFT) 상태의 문서를 결재 프로세스에 진입시킨다.
 * -
 * 주요 역할
 * - 작성자 및 문서 상태 검증
 * - 결재선 검증 및 최초 결재자 지정
 * - 문서 상태 변경 (DRAFT → IN_PROGRESS)
 * - 첨부파일 상태 확정
 * - 휴가 문서의 경우 근태 도메인 연동
 * - 최초 결재자에게 결재 요청 알림 전송
 *
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSubmitApplicationService
 * @since : 26. 1. 21. 수요일
 **/


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSubmitApplicationService {

    private final ApprovalLineRepository approvalLineRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentFileRepository documentFileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    private final DocumentAccessValidator documentAccessValidator;
    private final NotificationCommandService notificationCommandService;
    private final LeaveCalculationService leaveCalculationService;
    private final DocumentFileCleanupService documentFileCleanupService;
    private final ApproverResolver approverResolver;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 임시 저장된 결재 문서를 상신한다.
     */
    @Transactional
    public void submit(Long employeeId, Long documentId) {

        // 요청한 사원 및 문서 조회 (작성자 검증 포함)
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("요청한 사원 정보를 찾을 수 없습니다."));

        Document document = documentAccessValidator.getForWriter(employee, documentId);

        // 임시 저장 상태의 문서만 상신 가능
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException(
                    "이미 상신되었거나 처리 중인 문서는 다시 상신할 수 없습니다."
            );
        }

        // 문서를 결재 진행 상태로 전환
        document.submit();

        // 결재선 조회
        List<ApprovalLine> approvalLines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (approvalLines.isEmpty()) {
            throw new InvalidRequestException(
                    "결재선이 지정되지 않은 문서는 상신할 수 없습니다."
            );
        }

        // 최초 결재자 검증 및 필요 시 대체 결재자 지정
        ApprovalLine firstLine = approvalLines.get(0);
        Employee firstApprover = firstLine.getApprover();

        if (!approverResolver.isValid(firstApprover)
                || !approverResolver.matchesRole(firstLine, firstApprover)) {

            Employee replacement = approverResolver.findReplacement(firstLine);

            if (replacement == null) {
                throw new InvalidRequestException(
                        "현재 결재 규칙에 해당하는 결재자가 없어 문서를 상신할 수 없습니다."
                );
            }

            firstLine.setApprover(replacement);
        }

        // 결재선을 대기 상태로 초기화하고 최초 결재자를 진행 상태로 설정
        approvalLines.forEach(ApprovalLine::markWaiting);
        firstLine.markInProgress();

        // 첨부파일 상태 확정 (TEMP → FINAL) 및 삭제 대상 분리
        List<DocumentFile> documentFiles =
                documentFileRepository.findByDocument_Id(documentId);

        documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.TEMP)
                .forEach(df -> df.updateStatus(DocumentFileStatus.FINAL));

        List<DocumentFile> deletedFiles = documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.DELETED)
                .toList();

        documentFileRepository.deleteAll(deletedFiles);
        entityManager.flush();

        // 문서와의 연결이 끊긴 실제 파일 리소스 정리
        documentFileCleanupService.cleanupDetachedFiles(deletedFiles);

        // 휴가 문서의 경우 근태 도메인에 반영
        if (BaseRole.VACATION.equals(document.getTemplateGroup().getBaseRole())) {

            LeaveCalculationResult result =
                    leaveCalculationService.calculate(document);

            List<LocalDate> dates = result.effectiveDates();
            if (dates == null || dates.isEmpty()) {
                throw new InvalidRequestException("유효한 휴가 일자가 없습니다.");
            }

            LocalDate startDate = dates.get(0);
            LocalDate endDate = dates.get(dates.size() - 1);

            if (endDate.isBefore(startDate)) {
                endDate = startDate;
            }

            AttendanceRecord record = AttendanceRecord.builder()
                    .employee(document.getWriter())
                    .company(document.getCompany())
                    .startDate(startDate)
                    .endDate(endDate)
                    .days(result.days())
                    .leaveType(result.leaveType())
                    .reason(result.payload().reason())
                    .sourceDocument(document)
                    .status(DocumentStatus.IN_PROGRESS)
                    .approvedAt(null)
                    .build();

            attendanceRecordRepository.save(record);
        }

        // 최초 결재자에게 결재 요청 알림 전송
        String shortTitle =
                DocumentTextFormatter.shortenTitle(document.getTitle(), 20);

        String content =
                "[결재 요청 - " + DocumentTextFormatter.now() + "]\n" +
                        "문서 제목 : " + shortTitle + "\n" +
                        "기안자 : " + document.getWriter().getName() +
                        " | " + document.getWriter().getOrganization().getName() +
                        " | " + document.getWriter().getPositionCategory().getName();

        notificationCommandService.createNotification(
                firstLine.getCompany().getId(),
                firstLine.getApprover().getId(),
                NotificationType.APPROVAL,
                content,
                "/view/document/" + documentId
        );
    }
}