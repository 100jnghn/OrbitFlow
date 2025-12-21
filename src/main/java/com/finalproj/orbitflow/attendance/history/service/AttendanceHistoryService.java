package com.finalproj.orbitflow.attendance.history.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.history.dto.MonthlyAttHistoryResDto;
import com.finalproj.orbitflow.attendance.history.dto.DailyAttRecordResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryService {

    private final AttendanceRepository attendanceRepository;

    public MonthlyAttHistoryResDto getMonthlyHistory(Long employeeId, int year, int month) {
        // 1. 조회 기간 설정
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 2. 데이터 조회
        List<Attendance> records = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                employeeId, startDate, endDate);

        // 3. 통계 및 리스트 변환
        long totalMinutes = 0;
        long lateCount = 0;

        List<DailyAttRecordResDto> dailyList = new ArrayList<>();

        for (Attendance a : records) {
            // 지각 횟수 카운트
            if (a.getStatus() == AttendanceStatus.LATE) lateCount++;

            // 근무 시간 계산 (초 단위까지 정밀 계산 후 포맷팅)
            long diffMin = 0;
            if (a.getCommuteAt() != null && a.getLeaveAt() != null) {
                diffMin = Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes();
                totalMinutes += diffMin;
            }

            dailyList.add(DailyAttRecordResDto.builder()
                    .date(a.getWorkDate().format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)))
                    .commuteAt(formatTime(a.getCommuteAt()))
                    .leaveAt(formatTime(a.getLeaveAt()))
                    .workingTime(formatDuration(diffMin)) // "8h 07m" 형식
                    .statusName(a.getStatus().getDescription())
                    .statusCode(a.getStatus().name())
                    .build());
        }

        return MonthlyAttHistoryResDto.builder()
                .totalWorkHours(totalMinutes / 60)
                .lateCount(lateCount)
                .leaveAbsentCount(0) // 추후 휴가 테이블 연동 시 구현
                .dailyRecords(dailyList)
                .build();
    }

    private String formatTime(java.time.LocalDateTime dt) {
        return (dt != null) ? dt.format(DateTimeFormatter.ofPattern("HH:mm")) : "-";
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) return "0h 00m";
        return String.format("%dh %02dm", minutes / 60, minutes % 60);
    }
}