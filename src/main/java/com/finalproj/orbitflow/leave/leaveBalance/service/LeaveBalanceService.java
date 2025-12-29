package com.finalproj.orbitflow.leave.leaveBalance.service;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveBalanceResDto;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    /**
     * 내 연차 요약 정보 조회
     */
    public LeaveBalanceResDto getMySummary(Long companyId, Long employeeId, Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(companyId, employeeId, targetYear)
                .orElseGet(() -> LeaveBalance.builder()
                        .companyId(companyId).employeeId(employeeId).year(targetYear)
                        .totalGranted(BigDecimal.ZERO).remainingDays(BigDecimal.ZERO).build());

        BigDecimal total = balance.getTotalGranted();
        BigDecimal remaining = balance.getRemainingDays();

        return LeaveBalanceResDto.builder()
                .year(targetYear)
                .totalGranted(total)
                .usedDays(total.subtract(remaining))
                .remainingDays(remaining)
                .build();
    }

    /**
     * 내 연차 상세 내역 (부여 + 차감 사용 내역)
     */
    public List<LeaveHistoryResDto> getAnnualLeaveHistory(Long companyId, Long employeeId) {
        // 1. 부여 내역 (GRANT)
        List<LeaveHistoryResDto> history = leaveGrantRepository.findByCompanyIdAndEmployeeIdOrderByGrantDateDesc(companyId, employeeId)
                .stream().map(this::mapGrantToDto).collect(Collectors.toCollection(ArrayList::new));

        // 2. 사용 내역 (USED) 중 차감 항목 필터링
        List<LeaveHistoryResDto> usage = attendanceRecordRepository.findByCompanyIdAndEmployeeIdOrderByStartDateDesc(companyId, employeeId)
                .stream()
                .filter(r -> r.getLeaveType() != null && Boolean.TRUE.equals(r.getLeaveType().getIsCountable()))
                .map(this::mapRecordToDto)
                .collect(Collectors.toList());

        history.addAll(usage);
        history.sort((a, b) -> b.getActionDate().compareTo(a.getActionDate()));
        return history;
    }

    /**
     * 기타 휴가 신청 현황 (비차감 항목)
     */
    public List<LeaveHistoryResDto> getOtherLeaveHistory(Long companyId, Long employeeId) {
        return attendanceRecordRepository.findByCompanyIdAndEmployeeIdOrderByStartDateDesc(companyId, employeeId)
                .stream()
                .filter(r -> r.getLeaveType() != null && Boolean.FALSE.equals(r.getLeaveType().getIsCountable()))
                .map(this::mapRecordToDto)
                .collect(Collectors.toList());
    }

    private LeaveHistoryResDto mapGrantToDto(LeaveGrant grant) {
        return LeaveHistoryResDto.builder()
                .title("발생 (" + (grant.getGrantType().contains("REGULAR") ? "정기" : "신입") + " 부여)")
                .actionDate(grant.getGrantDate().toString())
                .period("-")
                .days(grant.getGrantedDays())
                .type("GRANT")
                .statusName(grant.getIsExpired() ? "소멸" : "완료")
                .statusCode(grant.getIsExpired() ? "EXPIRED" : "APPROVED")
                .build();
    }

    private LeaveHistoryResDto mapRecordToDto(AttendanceRecord r) {
        String typeName = r.getLeaveType().getTypeName();
        String displayPeriod = r.getStartDate().toString();

        if (typeName.contains("반차")) {
            displayPeriod += " (" + typeName.substring(0, 2) + ")";
        } else if (r.getEndDate() != null && !r.getStartDate().equals(r.getEndDate())) {
            displayPeriod = r.getStartDate() + " ~ " + r.getEndDate();
        }

        String actionDate = r.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate().toString();

        return LeaveHistoryResDto.builder()
                .title(typeName)
                .actionDate(actionDate)
                .period(displayPeriod)
                .days(r.getDays())
                .type("USED")
                .statusName(mapStatusText(r.getStatus().name()))
                .statusCode(r.getStatus().name())
                .build();
    }

    private String mapStatusText(String status) {
        return switch (status) {
            case "APPROVED" -> "승인";
            case "REJECTED" -> "반려";
            default -> "대기";
        };
    }
}