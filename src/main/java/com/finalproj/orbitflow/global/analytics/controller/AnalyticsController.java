package com.finalproj.orbitflow.global.analytics.controller;

import com.finalproj.orbitflow.global.analytics.dto.AnalyticsOverviewResDto;
import com.finalproj.orbitflow.global.analytics.service.AnalyticsService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseDto<AnalyticsOverviewResDto> getOverview(
            @RequestParam(defaultValue = "month") String granularity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "none") String compare,
            @RequestParam(required = false) Long companyId) {
        AnalyticsOverviewResDto data = analyticsService.getOverview(granularity, from, to, compare, companyId);
        return new ResponseDto<>(HttpStatus.OK, "성공", data);
    }

    @GetMapping("/companies")
    public ResponseDto<List<Map<String, Object>>> getCompanyList() {
        return new ResponseDto<>(HttpStatus.OK, "성공", analyticsService.getCompanyList());
    }

    @PostMapping("/sync")
    public ResponseDto<Void> syncData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        analyticsService.syncNow(date);
        return new ResponseDto<>(HttpStatus.OK, "동기화 성공", null);
    }
}
