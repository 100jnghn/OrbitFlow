package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.service.CalendarWorkingDayService;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.attendance.leave.dto.LeaveValidationReqDto;
import com.finalproj.orbitflow.attendance.leave.dto.LeaveValidationResDto;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveBalance;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceValidService
 * @since : 26. 1. 9. 금요일
 **/

@Service
@RequiredArgsConstructor
public class AttendanceValidService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final AttendanceEventRepository attendanceEventRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CalendarWorkingDayService workingDayService;

    public LeaveValidationResDto validateLeave(
            Long employeeId,
            LeaveValidationReqDto reqDto
    ) {
        /* =========================
           1️⃣ 근무일 기준 날짜 계산
        ========================= */
        List<LocalDate> workingDates =
                workingDayService.getWorkingDates(
                        reqDto.getStartDate(),
                        reqDto.getEndDate()
                );

        // ❌ 캘린더 데이터 자체 없음
        if (workingDates == null) {
            return LeaveValidationResDto.builder()
                    .valid(false)
                    .message("해당 연도의 근무일 정보가 아직 등록되지 않았습니다.")
                    .build();
        }

        boolean isVacation = reqDto.getLeaveTypeId() != null;

        // ❌ 근무일은 있으나 전부 휴일
        if (workingDates.isEmpty()) {
            return LeaveValidationResDto.builder()
                    .valid(false)
                    .message("선택한 기간에는 근무일이 없습니다.")
                    .build();
        }

        /* =========================
           2️⃣ 일정 충돌 검증
        ========================= */
        for (LocalDate date : workingDates) {

            // 2-1. 출장 / 외근 충돌
            boolean hasEvent =
                    attendanceEventRepository
                            .existsByEmployee_IdAndBaseRoleInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                    employeeId,
                                    List.of(BaseRole.BUSINESS_TRIP, BaseRole.OUTWORK),
                                    date,
                                    date
                            );

            if (hasEvent) {
                return LeaveValidationResDto.builder()
                        .valid(false)
                        .message(
                                String.format(
                                        "%s에 이미 출장 또는 외근 일정이 존재합니다.",
                                        date
                                )
                        )
                        .build();
            }

            // 2-2. 기존 휴가 충돌
            boolean hasLeave =
                    attendanceRecordRepository
                            .existsByEmployee_IdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                    employeeId,
                                    DocumentStatus.APPROVED,
                                    date,
                                    date
                            );

            if (hasLeave) {
                return LeaveValidationResDto.builder()
                        .valid(false)
                        .message(
                                String.format(
                                        "%s에 이미 등록된 휴가가 있습니다.",
                                        date
                                )
                        )
                        .build();
            }
        }

        /* =========================
           3️⃣ 휴가 유형이 없으면 종료
           (출장 / 외근 공통 검증)
        ========================= */
        if (reqDto.getLeaveTypeId() == null) {
            return LeaveValidationResDto.builder()
                    .valid(true)
                    .message("신청 가능한 일정입니다.")
                    .build();
        }

        /* =========================
           4️⃣ 연차 검증
        ========================= */
        LeaveBalance leaveBalance =
                leaveBalanceRepository
                        .findTopByEmployeeIdOrderByYearDesc(employeeId)
                        .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        LeaveType leaveType =
                leaveTypeRepository.findById(reqDto.getLeaveTypeId())
                        .orElseThrow(() -> new NotFoundException("휴가 유형 조회 실패"));

        BigDecimal remainingDays = leaveBalance.getRemainingDays();

        // 연차 차감 대상 아님
        if (!leaveType.getIsCountable()) {
            return LeaveValidationResDto.builder()
                    .valid(true)
                    .requiredDays(BigDecimal.ZERO)
                    .remainingDays(remainingDays)
                    .message("연차 차감 대상이 아닌 휴가입니다.")
                    .build();
        }

        BigDecimal requiredDays =
                leaveType.getUnitDays()
                        .multiply(BigDecimal.valueOf(workingDates.size()));

        boolean valid =
                remainingDays.compareTo(requiredDays) >= 0;

        return LeaveValidationResDto.builder()
                .valid(valid)
                .requiredDays(requiredDays)
                .remainingDays(remainingDays)
                .message(
                        valid
                                ? "신청 가능한 휴가입니다."
                                : "잔여 연차가 부족합니다."
                )
                .build();
    }
}
