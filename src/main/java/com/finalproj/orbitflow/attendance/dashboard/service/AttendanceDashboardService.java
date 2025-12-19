package com.finalproj.orbitflow.attendance.dashboard.service;

import com.finalproj.orbitflow.attendance.dashboard.dto.*;
import com.finalproj.orbitflow.attendance.dashboard.repository.AttendanceDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

//    private final AttendanceDashboardRepository dashboardRepository;
//
//    /**
//     * 대시보드 메인 데이터 조회 (요약 + 초기 목록)
//     */
//    public AttendanceDashboardResDto getDashboardMainData(Long companyId) {
//        // 1. 금일 요약 통계 계산
//        DashBoardSummaryDto summary = getSummary(companyId);
//
//        // 2. 초기 목록 조회 (기본 검색 조건 없음, 첫 페이지)
//        SearchConditionDto defaultCondition = new SearchConditionDto();
//        defaultCondition.setStartDate(LocalDate.now().toString());
//        defaultCondition.setEndDate(LocalDate.now().toString());
//
//        Page<DashBoardListDto> initialList = getAttendanceList(companyId, defaultCondition, Pageable.ofSize(10));
//
//        return AttendanceDashboardResDto.builder()
//                .summary(summary)
//                .attendanceList(initialList)
//                .build();
//    }
//
//    /**
//     * 상단 요약 통계 집계 (이미지 2번 영역)
//     */
//    public DashBoardSummaryDto getSummary(Long companyId) {
//        LocalDate today = LocalDate.now();
//
//        return DashBoardSummaryDto.builder()
//                .totalEmployeeCount(dashboardRepository.countTotalEmployees(companyId))
//                .checkedInCount(dashboardRepository.countCheckedIn(companyId, today))
//                .lateCount(dashboardRepository.countLateEmployees(companyId, today))
//                .workingCount(dashboardRepository.countWorkingNow(companyId, today))
//                .correctionRequestCount(dashboardRepository.countPendingCorrections(companyId))
//                .build();
//    }
//
//    /**
//     * 사원별 근태 목록 조회 (이미지 4번 영역, 필터링 포함)
//     */
//    public Page<DashBoardListDto> getAttendanceList(Long companyId, SearchConditionDto condition, Pageable pageable) {
//        // QueryDSL을 사용하여 동적 쿼리 실행
//        return dashboardRepository.searchAttendanceList(companyId, condition, pageable);
//    }
}