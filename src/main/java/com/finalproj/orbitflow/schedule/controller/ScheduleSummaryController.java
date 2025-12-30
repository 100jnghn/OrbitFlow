package com.finalproj.orbitflow.schedule.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.schedule.dto.ScheduleSummaryResDto;
import com.finalproj.orbitflow.schedule.service.ScheduleSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleSummaryController
 * @since : 2025-12-30 오후 5:21 화요일
 */

@RequestMapping("/api/schedule/summary")
@Controller
@RequiredArgsConstructor
@Slf4j
public class ScheduleSummaryController {

    private final ScheduleSummaryService scheduleSummaryService;

    /**
     * 일정 요약
     * employeeId / companyId / orgId -> 소속 조직 / today Date
     */
    @GetMapping
    public ResponseEntity<ResponseDto> getScheduleSummary(
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long companyId = user.getCompanyId();
        Long orgId = user.getOrganizationId();
        Long employeeId = user.getEmployeeId();

        ScheduleSummaryResDto result = scheduleSummaryService.getScheduleSummary(companyId, orgId, employeeId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 요약 성공", result)
        );
    }
}
