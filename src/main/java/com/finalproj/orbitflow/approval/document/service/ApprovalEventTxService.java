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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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
    private final WorkingDayService workingDayService;

    public static List<DateRange> splitToRanges(List<LocalDate> dates) {

        List<DateRange> ranges = new ArrayList<>();
        if (dates.isEmpty()) return ranges;

        LocalDate start = dates.get(0);
        LocalDate prev = start;

        for (int i = 1; i < dates.size(); i++) {
            LocalDate curr = dates.get(i);

            if (!curr.equals(prev.plusDays(1))) {
                ranges.add(new DateRange(start, prev));
                start = curr;
            }
            prev = curr;
        }

        ranges.add(new DateRange(start, prev));
        return ranges;
    }

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

        // 휴가 문서가 아니면 종료
        if (document.getTemplateGroup().getBaseRole() != BaseRole.VACATION) {
            return;
        }

        Employee writer = document.getWriter();

        // 2️⃣ 휴가 계산 (주말 + 공휴일 제외는 여기서 이미 완료됨)
        LeaveCalculationResult result =
                leaveCalculationService.calculate(document);

        // 실제 휴가 날짜가 없으면 (전부 주말/공휴일)
        if (result.effectiveDates().isEmpty()) {
            log.warn(
                    "[ApprovalEventTxService] No effective vacation days - documentId={}",
                    documentId
            );
            return;
        }

        // 3️⃣ 최상위 조직 조회
        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() ->
                        new NotFoundException("작성자의 최상위 조직 조회 실패"));

        // 4️⃣ 스케줄 등록 (effectiveDates → 연속 구간 분해)
        List<DateRange> ranges =
                splitToRanges(result.effectiveDates());

        String writerInfo = writer.getOrganization().getName() + " | " + writer.getRank().getName() + " | " + writer.getName();

        for (DateRange range : ranges) {

            ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                    .isCompany(true)
                    .isPersonal(true)
                    .orgCategoryId(org.getCategoryId())
                    .orgId(org.getId())
                    .title(writer.getName() + " | " + result.leaveType().getTypeName())
                    .description("휴가 사유는 공개되지 않습니다. " + writerInfo)
                    .startAt(range.start().atStartOfDay())
                    .endAt(range.end().atTime(23, 59, 59))
                    .status("RELEASE")
                    .build();

            scheduleService.insertSchedule(
                    writer.getCompany().getId(),
                    writer.getId(),
                    scheduleReqDto
            );
        }

        // 5️⃣ 근태 기록 (실제 휴가 범위 기준)
        LocalDate actualStart = result.effectiveDates().get(0);
        LocalDate actualEnd =
                result.effectiveDates().get(result.effectiveDates().size() - 1);

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(writer)
                .company(writer.getCompany())
                .startDate(actualStart)
                .endDate(actualEnd)
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

        // 6️⃣ 연차 차감 (이미 주말/공휴일 제외된 days 사용)
        leaveService.deduction(
                writer,
                result.days(),
                document,
                result.leaveType()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAttendanceApproval(Long documentId) {

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow();

        BaseRole baseRole = document.getTemplateGroup().getBaseRole();

        // 출장 / 외근만 처리
        if (baseRole != BaseRole.BUSINESS_TRIP &&
                baseRole != BaseRole.OUTWORK) {
            return;
        }

        // 2️⃣ 문서 내용 파싱
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow();

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();

        // 3️⃣ 최상위 조직 조회
        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null
                )
                .orElseThrow(() ->
                        new NotFoundException("작성자의 최상위 조직 조회 실패"));

        String title = switch (baseRole) {
            case BUSINESS_TRIP -> "출장";
            case OUTWORK -> "외근";
            default -> payload.title();
        };

        // 4️⃣ 🔥 실제 근무일 계산 (주말 + 공휴일 제외)
        List<LocalDate> workingDates =
                workingDayService.getWorkingDates(
                        payload.startDate(),
                        payload.endDate()
                );

        // 전부 휴일인 경우
        if (workingDates.isEmpty()) {
            log.warn(
                    "[AttendanceApproval] No working days - documentId={}",
                    documentId
            );
            return;
        }

        // 5️⃣ 일별 일정 + 근태 이벤트 등록
        for (LocalDate date : workingDates) {

            // 📅 일정 생성 (일 단위)
            ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                    .isCompany(true)
                    .isPersonal(true)
                    .orgCategoryId(org.getCategoryId())
                    .orgId(org.getId())
                    .title(title)
                    .description(payload.description())
                    .startAt(date.atStartOfDay())
                    .endAt(date.atTime(23, 59, 59))
                    .status("RELEASE")
                    .build();

            scheduleService.insertSchedule(
                    writer.getCompany().getId(),
                    writer.getId(),
                    scheduleReqDto
            );

            // 🕒 근태 이벤트 저장 (일별)
            attendanceEventRepository.save(
                    AttendanceEvent.builder()
                            .employee(writer)
                            .company(writer.getCompany())
                            .baseRole(baseRole)
                            .startDate(date)
                            .endDate(date)
                            .sourceDocument(document)
                            .build()
            );
        }
    }


    public record DateRange(LocalDate start, LocalDate end) {
    }

}
