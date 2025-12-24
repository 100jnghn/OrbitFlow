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

import java.time.*; // Duration, LocalDate 등을 위해 반드시 필요
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryService {

    private final AttendanceRepository attendanceRepository;

    /**
     * [사원/관리자 공통] 월별 근태 데이터 조회 메인 로직
     */
    public MonthlyHistoryResDto getMonthlyHistoryData(Long empId, Integer year, Integer month,
                                                      LocalDate startDate, LocalDate endDate,
                                                      String status, Pageable pageable) {

        LocalDate finalStart;
        LocalDate finalEnd;

        // 기간 우선순위 결정
        if (startDate != null && endDate != null) {
            finalStart = startDate;
            finalEnd = endDate;
        } else {
            int y = (year != null) ? year : LocalDate.now().getYear();
            int m = (month != null) ? month : LocalDate.now().getMonthValue();
            finalStart = LocalDate.of(y, m, 1);
            finalEnd = finalStart.withDayOfMonth(finalStart.lengthOfMonth());
        }

        return MonthlyHistoryResDto.builder()
                .searchPeriod(finalStart + " ~ " + finalEnd)
                .summary(getMonthlySummary(empId, finalStart, finalEnd))
                .pagedData(getMonthlyHistoryPaged(empId, finalStart, finalEnd, status, pageable))
                .build();
    }

    private Page<DailyAttRecordResDto> getMonthlyHistoryPaged(Long empId, LocalDate start, LocalDate end, String status, Pageable pageable) {
        AttendanceStatus attStatus = null;
        try {
            if (status != null && !"ALL".equals(status)) {
                attStatus = AttendanceStatus.valueOf(status);
            }
        } catch (IllegalArgumentException e) {
            attStatus = null;
        }

        // Repository에 추가한 findHistoryWithPaging 호출
        return attendanceRepository.findHistoryWithPaging(empId, start, end, attStatus, pageable)
                .map(this::convertToDto);
    }

    private MonthlyAttHistoryResDto getMonthlySummary(Long empId, LocalDate start, LocalDate end) {
        List<Attendance> records = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(empId, start, end);
        long totalMin = 0, lateCount = 0, absentCount = 0;

        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.LATE) lateCount++;
            if (a.getStatus() == AttendanceStatus.ABSENT) absentCount++;

            // 출퇴근 기록이 모두 있을 때만 근무 시간 합산
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