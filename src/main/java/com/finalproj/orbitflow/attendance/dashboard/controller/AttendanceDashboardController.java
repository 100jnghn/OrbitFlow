//package com.finalproj.orbitflow.attendance.dashboard.controller;
//
//import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceDashboardResDto;
//import com.finalproj.orbitflow.attendance.dashboard.dto.DashBoardListDto;
//import com.finalproj.orbitflow.attendance.dashboard.dto.SearchConditionDto;
//import com.finalproj.orbitflow.attendance.dashboard.service.AttendanceDashboardService;
//import com.finalproj.orbitflow.global.security.SecurityUser;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/admin/attendance/dashboard")
//public class AttendanceDashboardController {
//
//    private final AttendanceDashboardService dashboardService;
//
//    /**
//     * 대시보드 초기 진입 시: 상단 요약 통계 + 첫 페이지 목록을 한 번에 반환
//     */
//    @GetMapping("/summary")
//    public ResponseEntity<AttendanceDashboardResDto> getDashboardSummary(
//            @AuthenticationPrincipal SecurityUser admin) {
//
//        // 서비스에서 Summary 정보와 초기 목록(기본 페이징)을 조합하여 반환
//        AttendanceDashboardResDto dashboardData = dashboardService.getDashboardMainData(admin.getCompanyId());
//        return ResponseEntity.ok(dashboardData);
//    }
//
//    /**
//     * 목록 필터링 및 페이징: 검색 조건(날짜, 상태 등)에 따른 사원별 근태 목록 반환
//     */
//    @GetMapping("/list")
//    public ResponseEntity<Page<DashBoardListDto>> getAttendanceList(
//            @AuthenticationPrincipal SecurityUser admin,
//            SearchConditionDto condition,
//            @PageableDefault(size = 10) Pageable pageable) {
//
//        // 관리자의 회사 ID와 검색 조건을 서비스에 전달
//        Page<DashBoardListDto> list = dashboardService.getAttendanceList(admin.getCompanyId(), condition, pageable);
//        return ResponseEntity.ok(list);
//    }
//}