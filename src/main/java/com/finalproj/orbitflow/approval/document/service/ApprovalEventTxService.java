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
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        private final LeaveTypeRepository leaveTypeRepository;

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

                // 5️⃣ 근태 기록 (실제 휴가 범위 기준)
                LocalDate actualStart = result.effectiveDates().get(0);
                LocalDate actualEnd = result.effectiveDates().get(result.effectiveDates().size() - 1);

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
                                                                ZoneId.systemDefault()))
                                .build();

                attendanceRecordRepository.save(record);

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

                // 🔥 4-1. AttendanceRecord 저장 (전체 기간 기준) - 스케줄러 동기화를 위해 필수
                LocalDate actualStart = workingDates.get(0);
                LocalDate actualEnd = workingDates.get(workingDates.size() - 1);

                // 출장/외근은 연차 차감이 없으므로 days는 0으로 설정
                // leaveType은 NOT NULL 제약이 있으므로, 비차감 항목(isCountable = false) 중 첫 번째를 찾아서 설정
                // 실제로는 출장/외근에 leaveType이 의미가 없지만, DB 제약을 위해 설정
                LeaveType defaultLeaveType = leaveTypeRepository.findAll().stream()
                                .filter(lt -> !Boolean.TRUE.equals(lt.getIsCountable()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("비차감 휴가 유형을 찾을 수 없습니다. 시스템 설정을 확인해주세요."));

                AttendanceRecord record = AttendanceRecord.builder()
                                .employee(writer)
                                .company(writer.getCompany())
                                .startDate(actualStart)
                                .endDate(actualEnd)
                                .days(BigDecimal.ZERO) // 출장/외근은 연차 차감 없음
                                .leaveType(defaultLeaveType) // DB 제약을 위해 비차감 휴가 유형 설정 (실제 차감은 하지 않음)
                                .reason(payload.description())
                                .sourceDocument(document)
                                .status(DocumentStatus.APPROVED)
                                .approvedAt(
                                                LocalDateTime.ofInstant(
                                                                document.getUpdatedAt(),
                                                                ZoneId.systemDefault()))
                                .build();

                attendanceRecordRepository.save(record);
                log.info("[AttendanceApproval] AttendanceRecord 저장 완료: documentId={}, 사원={}, 기간={}~{}",
                                documentId, writer.getName(), actualStart, actualEnd);

                // 5️⃣ 일별 일정 + 근태 이벤트 등록
                LocalDate today = LocalDate.now();

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

                        // 🕒 근태 이벤트 저장 (일별)
                        attendanceEventRepository.save(
                                        AttendanceEvent.builder()
                                                        .employee(writer)
                                                        .company(writer.getCompany())
                                                        .baseRole(baseRole)
                                                        .startDate(date)
                                                        .endDate(date)
                                                        .sourceDocument(document)
                                                        .build());

                        // 6️⃣ 🔥 추가된 로직: 승인된 날짜 중 오늘이 포함되어 있으면 즉시 근무 상태 변경
                        if (date.equals(today)) {
                                // BaseRole에 따라 WorkStatus 매핑 (출장 중 또는 외근 중)
                                try {
                                        WorkStatus targetStatus;
                                        if (baseRole == BaseRole.BUSINESS_TRIP) {
                                                targetStatus = WorkStatus.BUSINESS_TRIP;
                                        } else if (baseRole == BaseRole.OUTWORK) {
                                                targetStatus = WorkStatus.OUTWORK;
                                        } else {
                                                log.warn("[StatusUpdate] 알 수 없는 BaseRole: {}", baseRole);
                                                continue; // Skip
                                        }

                                        // LeaveService의 상태 변경 메서드 호출 (사전에 LeaveService에 해당 메서드 구현 필요)
                                        leaveService.updateWorkStatus(writer.getId(), targetStatus);

                                        log.info("[StatusUpdate] {} 승인으로 인한 상태 변경: 사원={}, 날짜={}",
                                                        title, writer.getName(), date);
                                } catch (Exception e) {
                                        log.error("[StatusUpdate] 상태 변경 실패 - 사원: {}, 에러: {}", writer.getName(),
                                                        e.getMessage());
                                        // 트랜잭션 롤백을 할지 말지 결정: 여기선 로깅만 하고 진행 (이미 승인된 문서이므로 상태 변경 실패로 전체 승인 취소는 부담됨...
                                        // 하지만 사용자는 '통합 트랜잭션'을 원했음. 따라서 예외 던지는 게 맞을 수도.
                                        // 사용자 요청: "데이터 무결성 및 트랜잭션". -> 실패하면 롤백이 맞음.
                                        throw e; // 롤백
                                }
                        }
                }
        }

        public record DateRange(LocalDate start, LocalDate end) {
        }

}
