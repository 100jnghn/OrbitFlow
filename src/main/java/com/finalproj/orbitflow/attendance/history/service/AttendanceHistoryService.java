package com.finalproj.orbitflow.attendance.history.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.history.dto.DailyAttRecordResDto;
import com.finalproj.orbitflow.attendance.history.dto.MonthlyAttHistoryResDto;
import com.finalproj.orbitflow.attendance.history.dto.MonthlyHistoryResDto;
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

    /**
     * 리팩토링 핵심: 컨트롤러를 위해 요약과 목록을 한 번에 준비하는 통합 메서드
     */
    public MonthlyHistoryResDto getMonthlyHistoryData(Long empId, int year, int month, String status, Pageable pageable) {
        return MonthlyHistoryResDto.builder()
                .summary(getMonthlySummary(empId, year, month))
                .pagedData(getMonthlyHistoryPaged(empId, year, month, status, pageable))
                .build();
    }

    // [내부 로직 1] 페이징 처리된 목록 조회
    private Page<DailyAttRecordResDto> getMonthlyHistoryPaged(Long employeeId, int year, int month, String status, Pageable pageable) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        AttendanceStatus attendanceStatus = (status == null || "ALL".equals(status)) ? null : AttendanceStatus.valueOf(status);

        return attendanceRepository.findHistoryWithPaging(employeeId, startDate, endDate, attendanceStatus, pageable)
                .map(this::convertToDto);
    }

    // [내부 로직 2] 상단 요약 정보 계산
    private MonthlyAttHistoryResDto getMonthlySummary(Long employeeId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Attendance> allRecords = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, startDate, endDate);

        long totalMinutes = 0, lateCount = 0, absentCount = 0;

        for (Attendance a : allRecords) {
            if (a.getStatus() == AttendanceStatus.LATE) lateCount++;
            if (a.getStatus() == AttendanceStatus.ABSENT) absentCount++;
            if (a.getCommuteAt() != null && a.getLeaveAt() != null) {
                totalMinutes += Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes();
            }
        }

        return MonthlyAttHistoryResDto.builder()
                .totalWorkHours(totalMinutes / 60)
                .lateCount(lateCount)
                .leaveAbsentCount(absentCount)
                .build();
    }

    private DailyAttRecordResDto convertToDto(Attendance a) {
        long diffMin = 0;
        if (a.getCommuteAt() != null && a.getLeaveAt() != null) {
            diffMin = Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes();
        }
        return DailyAttRecordResDto.builder()
                .date(a.getWorkDate().format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)))
                .commuteAt(formatTime(a.getCommuteAt())).leaveAt(formatTime(a.getLeaveAt()))
                .workingTime(formatDuration(diffMin)).statusName(a.getStatus().getDescription())
                .statusCode(a.getStatus().name()).build();
    }

    private String formatTime(LocalDateTime dt) { return (dt != null) ? dt.format(DateTimeFormatter.ofPattern("HH:mm")) : "-"; }
    private String formatDuration(long min) { return String.format("%dh %02dm", min / 60, min % 60); }
}