package com.finalproj.orbitflow.attendance.history.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.history.dto.DailyAttRecordResDto;
import com.finalproj.orbitflow.attendance.history.dto.MonthlyAttHistoryResDto;
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
        // 1. 해당 월의 시작일과 마지막일 설정
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 미래의 날짜까지 결근으로 나오지 않게 오늘까지만 데이터를 생성 (필요 시 lastDayOfMonth로 변경 가능)
        LocalDate today = LocalDate.now();
        LocalDate endLoopDate = (today.isBefore(lastDayOfMonth) && today.getMonthValue() == month) ? today : lastDayOfMonth;

        // 2. DB에서 해당 월의 모든 기록을 가져와 Map으로 변환 (날짜 찾기 최적화)
        List<Attendance> records = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                employeeId, startDate, lastDayOfMonth);
        Map<LocalDate, Attendance> attendanceMap = records.stream()
                .collect(Collectors.toMap(Attendance::getWorkDate, a -> a));

        long totalMinutes = 0;
        long lateCount = 0;
        long absentCount = 0;
        List<DailyAttRecordResDto> dailyList = new ArrayList<>();

        // 3. 1일부터 말일까지 루프를 돌며 데이터 생성
        for (LocalDate date = startDate; !date.isAfter(endLoopDate); date = date.plusDays(1)) {
            Attendance a = attendanceMap.get(date);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

            DailyAttRecordResDto.DailyAttRecordResDtoBuilder builder = DailyAttRecordResDto.builder()
                    .date(date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)));

            if (a != null) {
                // [Case 1] DB에 출퇴근 기록이 있는 경우
                if (a.getStatus() == AttendanceStatus.LATE) lateCount++;

                long diffMin = 0;
                if (a.getCommuteAt() != null && a.getLeaveAt() != null) {
                    diffMin = Duration.between(a.getCommuteAt(), a.getLeaveAt()).toMinutes();
                    totalMinutes += diffMin;
                }

                builder.commuteAt(formatTime(a.getCommuteAt()))
                        .leaveAt(formatTime(a.getLeaveAt()))
                        .workingTime(formatDuration(diffMin))
                        .statusName(a.getStatus().getDescription()) // Enum에서 "정상출근", "지각" 등을 가져옴
                        .statusCode(a.getStatus().name());
            } else {
                // [Case 2] DB에 기록이 없는 경우
                if (isWeekend) {
                    // 주말인 경우
                    builder.commuteAt("-").leaveAt("-").workingTime("-")
                            .statusName("주말").statusCode("WEEKEND");
                } else {
                    // 평일인데 기록이 없는 경우 -> 결근
                    absentCount++;
                    builder.commuteAt("-").leaveAt("-").workingTime("0h 00m")
                            .statusName("결근").statusCode("ABSENT");
                }
            }
            dailyList.add(builder.build());
        }

        return MonthlyAttHistoryResDto.builder()
                .totalWorkHours(totalMinutes / 60)
                .lateCount(lateCount)
                .leaveAbsentCount(absentCount)
                .dailyRecords(dailyList)
                .build();
    }

    private String formatTime(LocalDateTime dt) {
        return (dt != null) ? dt.format(DateTimeFormatter.ofPattern("HH:mm")) : "-";
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) return "0h 00m";
        return String.format("%dh %02dm", minutes / 60, minutes % 60);
    }
}