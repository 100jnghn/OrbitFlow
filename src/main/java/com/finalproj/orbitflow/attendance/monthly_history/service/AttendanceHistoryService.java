package com.finalproj.orbitflow.attendance.monthly_history.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;
import com.finalproj.orbitflow.attendance.monthly_history.dto.*;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*; // Duration, LocalDate 등을 위해 반드시 필요
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryService {

    private final CommuteRepository commuteRepository;
    private final EmployeeRepository employeeRepository;


    // 월별 근태 조회시 필요한 모든 데이터 조합
    public MonthlyHistoryResDto getMonthlyHistoryData(Long empId, Integer year, Integer month,
                                                      LocalDate startDate, LocalDate endDate,
                                                      String status, Pageable pageable) {

        // 1. 기간 유효성 검사
        validateSearchDates(startDate, endDate);

        // 2. 조회 기간 설정 (자유 기간 > 특정 연/월 > 현재 월)
        LocalDate[] period = resolvePeriod(year, month, startDate, endDate);
        LocalDate finalStart = period[0];
        LocalDate finalEnd = period[1];

        return MonthlyHistoryResDto.builder()
                .searchPeriod(finalStart + " ~ " + finalEnd)
                .summary(getMonthlySummary(empId, finalStart, finalEnd))
                .pagedData(getMonthlyHistoryPaged(empId, finalStart, finalEnd, status, pageable))
                .build();
    }


    // 지정  기간 내의 통계 데이터 계산
    private MonthlyAttHistoryResDto getMonthlySummary(Long empId, LocalDate start, LocalDate end) {
        List<Attendance> records = commuteRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(empId, start, end);
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


    // 페이징 처리된 근태목록
    private Page<DailyAttRecordResDto> getMonthlyHistoryPaged(Long empId, LocalDate start, LocalDate end, String status, Pageable pageable) {
        AttendanceStatus attStatus = null;
        try {
            if (status != null && !"ALL".equals(status)) {
                attStatus = AttendanceStatus.valueOf(status);
            }
        } catch (IllegalArgumentException e) {
            attStatus = null;
        }

        return commuteRepository.findHistoryWithPaging(empId, start, end, attStatus, pageable)
                .map(this::convertToDto);
    }


    private DailyAttRecordResDto convertToDto(Attendance a) {
        long diff = (a.getCommuteAt() != null && a.getLeaveAt() != null)
                ? Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes() : 0;

        return DailyAttRecordResDto.builder()
                .date(a.getWorkDate().format(DateTimeFormatter.ofPattern("MM월 dd일(E)", Locale.KOREAN)))
                .commuteAt(a.getCommuteAt() != null ? a.getCommuteAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-")
                .leaveAt(a.getLeaveAt() != null ? a.getLeaveAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-")
                .workingTime(String.format("%dh %02dm", diff / 60, diff % 60))
                .statusName(a.getStatus().getDescription())
                .statusCode(a.getStatus().name())
                .build();
    }

    /**
     * 기간 유효성 검사 분리
     */
    private void validateSearchDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new InvalidRequestException("시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    /**
     * 기간 결정 로직 분리
     */
    private LocalDate[] resolvePeriod(Integer year, Integer month, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return new LocalDate[]{start, end};
        }

        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        LocalDate firstDay = LocalDate.of(y, m, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        return new LocalDate[]{firstDay, lastDay};
    }
}