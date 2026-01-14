package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.event.DocumentContentParser;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
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
    private final LeaveTypeRepository leaveTypeRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;

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

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.error(
                            "[ApprovalEventTxService] Document not found - documentId={}",
                            documentId);
                    return new IllegalStateException("Document not found");
                });

        // 휴가 문서가 아니면 종료
        if (document.getTemplateGroup().getBaseRole() != BaseRole.VACATION) {
            return;
        }

        Employee writer = document.getWriter();

        // 2️⃣ 휴가 계산 (주말 + 공휴일 제외는 여기서 이미 완료됨)
        LeaveCalculationResult result = leaveCalculationService.calculate(document);

        // 실제 휴가 날짜가 없으면 (전부 주말/공휴일)
        if (result.effectiveDates().isEmpty()) {
            log.warn(
                    "[ApprovalEventTxService] No effective vacation days - documentId={}",
                    documentId);
            return;
        }

        // 3️⃣ 최상위 조직 조회
        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        // 4️⃣ 스케줄 등록 (effectiveDates → 연속 구간 분해)
        List<DateRange> ranges = splitToRanges(result.effectiveDates());

        String writerInfo = writer.getOrganization().getName() + " | " + writer.getRank().getName() + " | "
                + writer.getName();

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
                    scheduleReqDto);
        }

        // 5️⃣ 근태 기록 갱신
        attendanceRecordRepository.findBySourceDocument_Id(document.getId())
                .ifPresent(AttendanceRecord::approvedDocument);

        // 6️⃣ 연차 차감 (이미 주말/공휴일 제외된 days 사용)
        leaveService.deduction(
                writer,
                result.days(),
                document,
                result.leaveType());

        // 7️⃣ 🔥 근무 상태 즉시 변경 로직 추가
        LocalDate today = LocalDate.now();
        // effectiveDates에 오늘이 포함되어 있는지 확인
        if (result.effectiveDates().contains(today)) {
            // WorkStatus.VACATION 등의 열거형을 사용해 상태 업데이트
            leaveService.updateWorkStatus(writer.getId(), WorkStatus.VACATION);
            log.info("[StatusUpdate] 휴가 승인으로 인한 상태 변경: 사원={}, 상태=휴가중", writer.getName());
        }
    }

    @Transactional
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

        CommonPayload payload = documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();

        // 3️⃣ 최상위 조직 조회
        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        String title = switch (baseRole) {
            case BUSINESS_TRIP -> "출장";
            case OUTWORK -> "외근";
            default -> payload.title();
        };

        // 4️⃣ 🔥 실제 근무일 계산 (주말 + 공휴일 제외)
        List<LocalDate> workingDates = workingDayService.getWorkingDates(
                payload.startDate(),
                payload.endDate());

        // 전부 휴일인 경우
        if (workingDates.isEmpty()) {
            log.warn(
                    "[AttendanceApproval] No working days - documentId={}",
                    documentId);
            return;
        }


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
                    scheduleReqDto);


            // 6️⃣ 🔥 추가된 로직: 승인된 날짜 중 오늘이 포함되어 있으면 즉시 근무 상태 변경
            if (date.equals(LocalDate.now())) {
                // BaseRole에 따라 WorkStatus 매핑 (출장 중 또는 외근 중)
                WorkStatus targetStatus;
                if (baseRole == BaseRole.BUSINESS_TRIP) {
                    targetStatus = WorkStatus.BUSINESS_TRIP;
                } else {
                    targetStatus = WorkStatus.OUTWORK;
                }

                // LeaveService의 상태 변경 메서드 호출 (사전에 LeaveService에 해당 메서드 구현 필요)
                leaveService.updateWorkStatus(writer.getId(), targetStatus);

                log.info("[StatusUpdate] {} 승인으로 인한 상태 변경: 사원={}, 날짜={}",
                        title, writer.getName(), date);
            }
        }

        // 🕒근태 이벤트 저장(전체 저장하고 스케쥴러에서 오늘이 근무일인지 체크)
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

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new NotFoundException("문서를 찾을 수 없습니다.")
                );

        // 2️⃣ COMPANY_EVENT가 아니면 무시
        BaseRole baseRole = document.getTemplateGroup().getBaseRole();
        if (baseRole != BaseRole.COMPANY_EVENT) {
            return;
        }

        // 3️⃣ 문서 내용 조회 + 공통 Payload 파싱
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() ->
                        new NotFoundException("문서 내용을 찾을 수 없습니다.")
                );

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        // 4️⃣ 작성자 / 회사 정보
        Employee writer = document.getWriter();
        Long companyId = writer.getCompany().getId();

        // 5️⃣ 회사 최상위 조직 조회
        Organization rootOrg = orgRepository
                .findFirstByCompanyIdAndParentOrgId(companyId, null)
                .orElseThrow(() ->
                        new NotFoundException("회사 최상위 조직 조회 실패")
                );

        // 6️⃣ 회사 기본 근무 시간 조회
        AttendanceRule rule = attendanceRuleRepository
                .findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() ->
                        new NotFoundException("회사 기본 근무 시간 조회 실패")
                );

        LocalTime startTime = rule.getDefaultStartTime();
        LocalTime endTime = rule.getDefaultEndTime();

        // 7️⃣ 회사 일정 생성
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

        // 8️⃣ 일정 저장 (새 트랜잭션 분리 필요 시 내부에서 처리)
        scheduleService.insertSchedule(
                companyId,
                writer.getId(),
                scheduleReqDto
        );
    }


    public record DateRange(LocalDate start, LocalDate end) {
    }

}
