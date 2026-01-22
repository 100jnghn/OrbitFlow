package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.attendance.event.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.attendance.event.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.attendance.record.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendance.record.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.schema.DocumentContentParser;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.domain.LeaveCalculationService;
import com.finalproj.orbitflow.approval.document.service.domain.WorkingDayService;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 결재 문서가 최종 승인된 이후 실행되어야 하는 후처리 로직을 담당하는 서비스.
 * <p>
 * 결재 흐름 자체에는 관여하지 않고,
 * 승인 완료라는 결과를 기준으로 다른 도메인에 영향을 주는 작업들을 처리한다.
 * (근태 반영, 휴가 차감, 일정 생성 등)
 * <p>
 * 문서에 설정된 TemplateGroup의 BaseRole을 기준으로
 * 어떤 후처리를 수행할지 판단하며,
 * 처리 대상이 아닌 경우에는 별도 동작 없이 종료된다.
 * <p>
 * 이 서비스는 이벤트 핸들러에서 호출되는 것을 전제로 하며,
 * 결재 도메인과 근태/일정/휴가 도메인 간의 책임을 분리하기 위한
 * 중간 처리 계층 역할을 한다.
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalEventTxService
 * @since : 26. 1. 6. 화요일
 */


@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalEventTxService {

    private final DocumentRepository documentRepository;
    private final OrgRepository orgRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final DocumentContentRepository documentContentRepository;
    private final AttendanceEventRepository attendanceEventRepository;
    private final WorkingDayService workingDayService;
    private final AttendanceRuleRepository attendanceRuleRepository;

    private final LeaveService leaveService;
    private final LeaveCalculationService leaveCalculationService;
    private final DocumentContentParser documentContentParser;
    private final ScheduleService scheduleService;

    public static List<DateRange> splitToRanges(List<LocalDate> dates) {

        List<DateRange> ranges = new ArrayList<>();
        if (dates.isEmpty())
            return ranges;

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

    @Transactional
    public void processVacationApproval(Long documentId) {

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

        LeaveCalculationResult result = leaveCalculationService.calculate(document);

        List<LocalDate> dates = result.effectiveDates();
        if (dates == null || dates.isEmpty()) {
            log.warn(
                    "[ApprovalEventTxService] Invalid vacation dates - documentId={}, start={}, end={}",
                    documentId,
                    result.payload().startDate(),
                    result.payload().endDate()
            );
            return;
        }

        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        List<DateRange> ranges = splitToRanges(dates);

        String writerInfo =
                writer.getOrganization().getName() + " | " +
                        writer.getRank().getName() + " | " +
                        writer.getName();

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

        attendanceRecordRepository.findBySourceDocument_Id(document.getId())
                .ifPresent(AttendanceRecord::approvedDocument);

        leaveService.deduction(
                writer,
                result.days(),
                document,
                result.leaveType()
        );

        LocalDate today = LocalDate.now();
        if (dates.contains(today)) {
            leaveService.updateWorkStatus(writer.getId(), WorkStatus.VACATION);
            log.info(
                    "[StatusUpdate] Vacation approved - employeeId={}, name={}",
                    writer.getId(),
                    writer.getName()
            );
        }
    }


    @Transactional
    public void processAttendanceApproval(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow();

        BaseRole baseRole = document.getTemplateGroup().getBaseRole();

        // 출장 / 외근만 처리
        if (baseRole != BaseRole.BUSINESS_TRIP &&
                baseRole != BaseRole.OUTWORK) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow();

        CommonPayload payload = documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();

        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        String title = switch (baseRole) {
            case BUSINESS_TRIP -> "출장";
            case OUTWORK -> "외근";
            default -> payload.title();
        };

        List<LocalDate> workingDates = workingDayService.getWorkingDates(
                payload.startDate(),
                payload.endDate());

        if (workingDates.isEmpty()) {
            log.warn(
                    "[AttendanceApproval] No working days - documentId={}",
                    documentId);
            return;
        }


        for (LocalDate date : workingDates) {

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
                    scheduleReqDto);


            if (date.equals(LocalDate.now())) {
                WorkStatus targetStatus;
                if (baseRole == BaseRole.BUSINESS_TRIP) {
                    targetStatus = WorkStatus.BUSINESS_TRIP;
                } else {
                    targetStatus = WorkStatus.OUTWORK;
                }

                leaveService.updateWorkStatus(writer.getId(), targetStatus);

                log.info("[StatusUpdate] {} 승인으로 인한 상태 변경: 사원={}, 날짜={}",
                        title, writer.getName(), date);
            }
        }

        attendanceEventRepository.save(
                AttendanceEvent.builder()
                        .employee(writer)
                        .company(writer.getCompany())
                        .baseRole(baseRole)
                        .startDate(workingDates.get(0))
                        .endDate(workingDates.get(workingDates.size() - 1))
                        .sourceDocument(document)
                        .build());
    }

    @Transactional
    public void processCompanyEvent(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new NotFoundException("문서를 찾을 수 없습니다.")
                );

        BaseRole baseRole = document.getTemplateGroup().getBaseRole();
        if (baseRole != BaseRole.COMPANY_EVENT) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() ->
                        new NotFoundException("문서 내용을 찾을 수 없습니다.")
                );

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();
        Long companyId = writer.getCompany().getId();

        Organization rootOrg = orgRepository
                .findFirstByCompanyIdAndParentOrgId(companyId, null)
                .orElseThrow(() ->
                        new NotFoundException("회사 최상위 조직 조회 실패")
                );

        AttendanceRule rule = attendanceRuleRepository
                .findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() ->
                        new NotFoundException("회사 기본 근무 시간 조회 실패")
                );

        LocalTime startTime = rule.getDefaultStartTime();
        LocalTime endTime = rule.getDefaultEndTime();

        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .isPersonal(false)
                .orgCategoryId(rootOrg.getCategoryId())
                .title(payload.title())
                .description(payload.description())
                .startAt(payload.startDate().atTime(startTime))
                .endAt(payload.endDate().atTime(endTime))
                .status("RELEASE")
                .build();

        scheduleService.insertSchedule(
                companyId,
                writer.getId(),
                scheduleReqDto
        );
    }


    public record DateRange(LocalDate start, LocalDate end) {
    }

}
