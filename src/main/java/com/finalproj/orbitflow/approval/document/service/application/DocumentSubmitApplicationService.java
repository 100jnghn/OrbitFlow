package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.attendance.record.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendance.record.repository.AttendanceRecordRepository;
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
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
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
 * 임시 저장된 결재 문서를 실제 결재 흐름에 올리는 상신(SUBMIT) 유즈케이스를 담당하는 Application Service.
 * <p>
 * 이 클래스는 DRAFT 상태의 문서를 결재 프로세스에 진입시키는 역할을 수행한다.
 * 단순히 문서 상태를 변경하는 것이 아니라, 결재선 검증과 초기화,
 * 첨부 파일 상태 확정, 도메인 연동 처리, 알림 전송까지
 * 상신 시점에 필요한 모든 작업을 하나의 트랜잭션 흐름으로 조율한다.
 * <p>
 * 상신 과정에서는 다음과 같은 책임을 가진다.
 * <p>
 * - 문서 작성자 및 문서 상태(DRAFT) 검증
 * - 결재선 존재 여부 확인 및 최초 결재자 검증
 * - 결재 규칙에 맞지 않는 경우 대체 결재자 지정
 * - 문서 상태를 IN_PROGRESS로 전환
 * - 결재선 상태 초기화 및 최초 결재 단계 활성화
 * - 첨부 파일 상태 확정(TEMP → FINAL) 및 삭제 파일 정리
 * - 휴가 문서의 경우 근태 도메인(Attendance)과의 연동 처리
 * - 최초 결재자에게 결재 요청 알림 전송
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSubmitApplicationService
 * @since : 26. 1. 21. 수요일
 */


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

    @Transactional
    public void submit(Long employeeId, Long documentId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("요청한 사원 정보를 찾을 수 없습니다."));

        Document document = documentAccessValidator.getForWriter(employee, documentId);

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException(
                    "이미 상신되었거나 처리 중인 문서는 다시 상신할 수 없습니다."
            );
        }

        document.submit();

        List<ApprovalLine> approvalLines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (approvalLines.isEmpty()) {
            throw new InvalidRequestException(
                    "결재선이 지정되지 않은 문서는 상신할 수 없습니다."
            );
        }

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

        approvalLines.forEach(ApprovalLine::markWaiting);
        firstLine.markInProgress();

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

        documentFileCleanupService.cleanupDetachedFiles(deletedFiles);

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