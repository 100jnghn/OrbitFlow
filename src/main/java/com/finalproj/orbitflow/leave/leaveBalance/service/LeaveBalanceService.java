package com.finalproj.orbitflow.leave.leaveBalance.service;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveBalanceResDto;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
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
     *  나의 연차 사용 내역 조회 (차감 항목들) - 페이지네이션
     * 연차 잔액에서 실제로 차감된 내역(연차, 반차 등)만 조회
     */
    public Page<LeaveHistoryResDto> getAnnualLeaveHistory(Long companyId, Long employeeId, Pageable pageable) {
        return attendanceRecordRepository.findAnnualLeaveHistory(companyId, employeeId, pageable)
                .map(this::mapRecordToDto);
    }

    /**
     * [도안 왼쪽] 나의 연차 사용 내역 조회 (차감 항목들) - 필터링 지원
     * 연차 잔액에서 실제로 차감된 내역(연차, 반차 등)만 조회
     */
    public Page<LeaveHistoryResDto> getAnnualLeaveHistory(
            Long companyId, 
            Long employeeId, 
            String typeName,
            DocumentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        return attendanceRecordRepository.findAnnualLeaveHistoryWithFilters(
                companyId, employeeId, typeName, status, startDate, endDate, pageable)
                .map(this::mapRecordToDto);
    }


    /**
     * [도안 오른쪽] 나의 휴가 신청 현황 조회 (비차감 항목들)
     * 연차 잔액에 영향을 주지 않는 휴가들(병가, 경조사 등)만 필터링하여 반환
     */
    public List<LeaveHistoryResDto> getOtherLeaveHistory(Long companyId, Long employeeId) {
        return attendanceRecordRepository.findByCompanyIdAndEmployeeIdOrderByStartDateDesc(companyId, employeeId)
                .stream()
                .filter(r -> r.getLeaveType() != null && Boolean.FALSE.equals(r.getLeaveType().getIsCountable()))
                .map(this::mapRecordToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 모든 휴가 이력 조회 (차감/비차감 구분 없이 모든 휴가 종류 포함)
     */
    public Page<LeaveHistoryResDto> getAllLeaveHistory(
            Long companyId,
            Long employeeId,
            String typeName,
            DocumentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Page<AttendanceRecord> records;
        
        // 필터가 하나라도 있으면 필터링 메서드 사용
        if (typeName != null || status != null || startDate != null || endDate != null) {
            records = attendanceRecordRepository.findAllLeaveHistoryWithFilters(
                    companyId,
                    employeeId,
                    typeName,
                    status,
                    startDate,
                    endDate,
                    pageable
            );
        } else {
            // 필터가 없으면 기본 메서드 사용
            records = attendanceRecordRepository.findByCompanyIdAndEmployeeIdOrderByStartDateDesc(
                    companyId,
                    employeeId,
                    pageable
            );
        }
        
        return records.map(this::mapRecordToDto);
    }

    private LeaveHistoryResDto mapRecordToDto(AttendanceRecord r) {
        // LeaveType 정보 추출
        String typeName = r.getLeaveType() != null ? r.getLeaveType().getTypeName() : "미지정";
        String typeDescription = r.getLeaveType() != null ? r.getLeaveType().getDescription() : null;

        // 기간 포맷팅: 하루여도 시작일~종료일 형식으로 표시 (공백 없음)
        String displayPeriod;
        if (r.getEndDate() != null) {
            displayPeriod = r.getStartDate() + "~" + r.getEndDate();
        } else {
            displayPeriod = r.getStartDate() + "~" + r.getStartDate();
        }

        // 신청일 추출 (Instant -> LocalDate 변환)
        String actionDate = r.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate()
                .toString();

        // AttendanceRecord의 reason 필드 포함
        return LeaveHistoryResDto.builder()
                .title(typeName)                      // LeaveType.typeName
                .actionDate(actionDate)               // AttendanceRecord.createdAt
                .period(displayPeriod)                // AttendanceRecord.startDate ~ endDate
                .days(r.getDays())                    // AttendanceRecord.days
                .type("USED")
                .statusName(mapStatusText(r.getStatus().name()))  // AttendanceRecord.status
                .statusCode(r.getStatus().name())
                .reason(r.getReason())                // AttendanceRecord.reason
                .typeDescription(typeDescription)     // LeaveType.description
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