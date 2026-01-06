package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.event.DocumentContentParser;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalEventTxService
 * @since : 26. 1. 6. 화요일
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalEventTxService {

    private final DocumentRepository documentRepository;
    private final OrgRepository orgRepository;
    private final ScheduleService scheduleService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveService leaveService;
    private final LeaveCalculationService leaveCalculationService;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;
    private final AttendanceEventRepository attendanceEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processVacationApproval(Long documentId) {

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.error(
                            "[ApprovalEventTxService] Document not found - documentId={}",
                            documentId
                    );
                    return new IllegalStateException("Document not found");
                });

        if (document.getTemplateGroup().getBaseRole() != BaseRole.VACATION) {

            return;
        }

        Employee writer = document.getWriter();

        // 2️⃣ 휴가 계산
        LeaveCalculationResult result =
                leaveCalculationService.calculate(document);


        // 3️⃣ 최상위 조직 조회
        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() ->
                        new NotFoundException("작성자의 최상위 조직 조회 실패"));

        // 4️⃣ 스케줄 등록 (같은 트랜잭션)
        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .isPersonal(true)
                .orgCategoryId(org.getCategoryId())
                .orgId(org.getId())
                .title(result.leaveType().getTypeName())
                .description("휴가 사유는 공개되지 않습니다")
                .startAt(result.payload().startDate().atStartOfDay())
                .endAt(result.payload().endDate().atTime(23, 59, 59))
                .status("RELEASE")
                .build();

        scheduleService.insertSchedule(
                writer.getCompany().getId(),
                writer.getId(),
                scheduleReqDto
        );

        // 5️⃣ 근태 기록
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(writer)
                .company(writer.getCompany())
                .startDate(result.payload().startDate())
                .endDate(result.payload().endDate())
                .days(result.days())
                .leaveType(result.leaveType())
                .reason(result.payload().reason())
                .sourceDocument(document)
                .status(DocumentStatus.APPROVED)
                .approvedAt(
                        LocalDateTime.ofInstant(
                                document.getUpdatedAt(),
                                ZoneId.systemDefault()
                        )
                )
                .build();

        attendanceRecordRepository.save(record);

        // 6️⃣ 연차 차감
        leaveService.deduction(
                writer,
                result.days(),
                document,
                result.leaveType()
        );
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAttendanceApproval(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow();

        BaseRole baseRole = document.getTemplateGroup().getBaseRole();

        if (baseRole != BaseRole.BUSINESS_TRIP &&
                baseRole != BaseRole.OUTWORK) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow();

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();

        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null
                )
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        String title = switch (baseRole) {
            case BUSINESS_TRIP -> "출장";
            case OUTWORK -> "외근";
            default -> payload.title();
        };

        // 1. 일정 생성
        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .isPersonal(true)
                .orgCategoryId(org.getCategoryId())
                .orgId(org.getId())
                .title(title)
                .description(payload.description())
                .startAt(payload.startDate().atStartOfDay())
                .endAt(payload.endDate().atTime(23, 59, 59))
                .status("RELEASE")
                .build();

        scheduleService.insertSchedule(
                writer.getCompany().getId(),
                writer.getId(),
                scheduleReqDto
        );

        // 2. 근태 이벤트 저장
        attendanceEventRepository.save(
                AttendanceEvent.builder()
                        .employee(writer)
                        .company(writer.getCompany())
                        .baseRole(baseRole)
                        .startDate(payload.startDate())
                        .endDate(payload.endDate())
                        .sourceDocument(document)
                        .build()
        );
    }
}
