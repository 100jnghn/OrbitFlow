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
     * 특정 사원의 연차 요약 정보 조회 (도안 왼쪽 상단 카드용)
     */
    public LeaveBalanceResDto getMySummary(Long companyId, Long employeeId, Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(companyId, employeeId, targetYear)
                .orElseGet(() -> LeaveBalance.builder()
                        .companyId(companyId).employeeId(employeeId).year(targetYear)
                        .totalGranted(BigDecimal.ZERO).remainingDays(BigDecimal.ZERO)
                        .build());

        BigDecimal total = balance.getTotalGranted() != null ? balance.getTotalGranted() : BigDecimal.ZERO;
        BigDecimal remaining = balance.getRemainingDays() != null ? balance.getRemainingDays() : BigDecimal.ZERO;

        return LeaveBalanceResDto.builder()
                .year(targetYear)
                .totalGranted(total)
                .usedDays(total.subtract(remaining))
                .remainingDays(remaining)
                .build();
    }

    /**
     * [도안 왼쪽] 내 연차 상세 내역 (isCountable == true 인 항목들)
     */
    public List<LeaveHistoryResDto> getAnnualLeaveHistory(Long companyId, Long employeeId) {
        // 1. 부여 내역 (GRANT)
        List<LeaveHistoryResDto> history = leaveGrantRepository.findByCompanyIdAndEmployeeIdOrderByGrantDateDesc(companyId, employeeId)
                .stream().map(this::mapGrantToDto).collect(Collectors.toCollection(ArrayList::new));

        // 2. 사용 내역 중 차감되는 항목만 필터링 (AttendanceRecordRepository에서 명확한 타입 반환 필요)
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
     * [도안 오른쪽] 나의 휴가 신청 현황 조회 (isCountable == false 인 항목들)
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
                .statusCode(grant.getIsExpired() ? "REJECTED" : "APPROVED")
                .build();
    }

    private LeaveHistoryResDto mapRecordToDto(AttendanceRecord r) {
        String typeName = r.getLeaveType().getTypeName();
        String displayPeriod = r.getStartDate().toString();

        // 도안 반영: 반차 명칭 및 기간 텍스트 처리
        if (typeName.contains("반차")) {
            displayPeriod += " (" + typeName.substring(0, 2) + ")";
        } else if (r.getEndDate() != null && !r.getStartDate().equals(r.getEndDate())) {
            displayPeriod = r.getStartDate() + " ~ " + r.getEndDate();
        }

        // [해결] Instant 타입의 createdAt을 한국 시간대 LocalDate로 변환하여 '신청일' 생성
        String actionDate = r.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate()
                .toString();

        // Enum 상태값을 문자열로 변환하여 처리
        String statusNameStr = (r.getStatus() != null) ? r.getStatus().name() : "SUBMITTED";

        return LeaveHistoryResDto.builder()
                .title(typeName)
                .actionDate(actionDate) // 변환된 신청일 매핑
                .period(displayPeriod)
                .days(r.getDays())
                .type("USED")
                .statusName(mapStatusText(statusNameStr))
                .statusCode(statusNameStr)
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