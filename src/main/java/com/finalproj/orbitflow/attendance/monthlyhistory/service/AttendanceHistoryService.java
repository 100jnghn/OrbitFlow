package com.finalproj.orbitflow.attendance.monthlyhistory.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;
import com.finalproj.orbitflow.attendance.monthlyhistory.dto.*;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceHistoryService
 * @since : 2025. 12. 22. 월요일
 */


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryService {

    private final CommuteRepository commuteRepository;

    public MonthlyHistoryResDto getMonthlyHistoryData(Long empId, Integer year, Integer month,
                                                      LocalDate startDate, LocalDate endDate,
                                                      String status, Pageable pageable) {

        validateSearchDates(startDate, endDate);

        LocalDate[] period = resolvePeriod(year, month, startDate, endDate);
        LocalDate finalStart = period[0];
        LocalDate finalEnd = period[1];

        return MonthlyHistoryResDto.builder()
                .searchPeriod(finalStart + " ~ " + finalEnd)
                .summary(getMonthlySummary(empId, finalStart, finalEnd))
                .pagedData(getMonthlyHistoryPaged(empId, finalStart, finalEnd, status, pageable))
                .build();
    }


    private MonthlyAttHistoryResDto getMonthlySummary(Long empId, LocalDate start, LocalDate end) {
        List<Attendance> records = commuteRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(empId, start, end);
        long totalMin = 0, lateCount = 0, absentCount = 0;

        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.LATE) lateCount++;
            if (a.getStatus() == AttendanceStatus.ABSENT) absentCount++;

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


    private void validateSearchDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new InvalidRequestException("시작일이 종료일보다 늦을 수 없습니다.");
        }
    }


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