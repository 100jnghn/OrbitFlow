package com.finalproj.orbitflow.global.time.calendar.controller;

import com.finalproj.orbitflow.global.time.calendar.dto.CalendarDayResDto;
import com.finalproj.orbitflow.global.time.calendar.service.CalendarDayService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CalendarDayController
 * @since : 2026-01-06 오후 10:38 화요일
 */
@RequestMapping("/api/calendar")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CalendarDayController {

    private final CalendarDayService calendarDayService;

    // 공휴일 + 주말 반환
    @GetMapping("/holidays")
    public ResponseEntity<?> getHolidays() {

        log.info("휴일 조회 시작");

        List<CalendarDayResDto> list = calendarDayService.getHolidays();

        return ResponseEntity.ok().body(
                new ResponseDto<>(HttpStatus.OK, "휴일 조회 성공", list)
        );
    }
}
