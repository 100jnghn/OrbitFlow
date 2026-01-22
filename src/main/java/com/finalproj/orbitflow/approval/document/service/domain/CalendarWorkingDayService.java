package com.finalproj.orbitflow.approval.document.service.domain;

import com.finalproj.orbitflow.global.time.calendar.entity.CalendarDay;
import com.finalproj.orbitflow.global.time.calendar.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 회사 캘린더 정보를 기준으로 실제 근무일 목록을 계산하는 서비스.
 *
 * 시작일과 종료일 사이의 날짜 중,
 * 휴가 차감 대상이 되는 근무일만 선별하여 반환한다.
 *
 * 주말·공휴일 여부 및 근무 인정 기준은
 * CalendarDay에 정의된 정책을 따른다.
 *
 * 휴가, 출장, 외근 승인 처리 시
 * 실제 근무일 계산에 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : CalendarWorkingDayService
 * @since : 26. 1. 6. 화요일
 **/


@Service
@RequiredArgsConstructor
public class CalendarWorkingDayService implements WorkingDayService {

    private final CalendarDayRepository calendarDayRepository;

    @Override
    public List<LocalDate> getWorkingDates(
            LocalDate start,
            LocalDate end
    ) {
        List<LocalDate> result = new ArrayList<>();

        if (start == null || end == null || end.isBefore(start)) {
            return result;
        }

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            CalendarDay day = calendarDayRepository.findById(date).orElse(null);

            if (day == null) {
                continue;
            }

            if (day.isChargeableForLeave()) {
                result.add(date);
            }
        }
        return result;
    }
}
