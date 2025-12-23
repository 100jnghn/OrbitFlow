package com.finalproj.orbitflow.attendance.monthly_history.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.monthly_history.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryService {

    private final AttendanceRepository attendanceRepository;

    public MonthlyHistoryResDto getMonthlyHistoryData(Long empId, Integer year, Integer month,
                                                      LocalDate startDate, LocalDate endDate,
                                                      String status, Pageable pageable) {

        // 1. 조회 기간 확정 로직 (파라미터 우선순위 적용)
        LocalDate finalStart;
        LocalDate finalEnd;

        if (startDate != null && endDate != null) {
            finalStart = startDate;
            finalEnd = endDate;
        } else {
            // 연/월이 없으면 현재 날짜 기준
            int y = (year != null) ? year : LocalDate.now().getYear();
            int m = (month != null) ? month : LocalDate.now().getMonthValue();
            finalStart = LocalDate.of(y, m, 1);
            finalEnd = finalStart.withDayOfMonth(finalStart.lengthOfMonth());
        }

        // 2. 데이터 조회 및 DTO 조립
        return MonthlyHistoryResDto.builder()
                .searchPeriod(finalStart + " ~ " + finalEnd)
                .summary(getMonthlySummary(empId, finalStart, finalEnd))
                .pagedData(getMonthlyHistoryPaged(empId, finalStart, finalEnd, status, pageable))
                .build();
    }

    private Page<DailyAttRecordResDto> getMonthlyHistoryPaged(Long empId, LocalDate start, LocalDate end, String status, Pageable pageable) {
        AttendanceStatus attStatus = (status == null || "ALL".equals(status)) ? null : AttendanceStatus.valueOf(status);
        // Repository 메서드명이 findHistoryWithPaging 인지 확인 필요
        return attendanceRepository.findHistoryWithPaging(empId, start, end, attStatus, pageable)
                .map(this::convertToDto);
    }

    private MonthlyAttHistoryResDto getMonthlySummary(Long empId, LocalDate start, LocalDate end) {
        List<Attendance> records = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(empId, start, end);
        long totalMin = 0, lateCount = 0, absentCount = 0;

        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.LATE) lateCount++;
            if (a.getStatus() == AttendanceStatus.ABSENT) absentCount++;
            // 근무 시간 계산: 입근/퇴근 시간이 모두 있어야 함
            if (a.getCommuteAt() != null && a.getLeaveAt() != null) {
                totalMin += Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes();
            }
        }

        return MonthlyAttHistoryResDto.builder()
                .totalWorkTimeDisplay(String.format("%dh %02dm", totalMin / 60, totalMin % 60))
                .lateCount(lateCount)
                .leaveAbsentCount(absentCount)
                .build();
    }

    private DailyAttRecordResDto convertToDto(Attendance a) {
        long diff = (a.getCommuteAt() != null && a.getLeaveAt() != null)
                ? Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes() : 0;

        return DailyAttRecordResDto.builder()
                .date(a.getWorkDate().format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)))
                .commuteAt(a.getCommuteAt() != null ? a.getCommuteAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-")
                .leaveAt(a.getLeaveAt() != null ? a.getLeaveAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-")
                .workingTime(String.format("%dh %02dm", diff / 60, diff % 60))
                .statusName(a.getStatus().getDescription())
                .statusCode(a.getStatus().name())
                .build();
    }
}