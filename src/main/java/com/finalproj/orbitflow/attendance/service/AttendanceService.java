package com.finalproj.orbitflow.attendance.service;

import com.finalproj.orbitflow.attendance.dto.AttendanceDto;
import com.finalproj.orbitflow.attendance.entity.Attendance;
import com.finalproj.orbitflow.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // TODO: 실제 구현 시 Spring Security 등을 통해 현재 로그인한 사원 ID와 회사 ID를 가져와야 함
    private final Long CURRENT_EMPLOYEE_ID = 1L;
    private final Long CURRENT_COMPANY_ID = 1L;

    /**
     * 오늘의 출퇴근 기록 조회
     */
    public AttendanceDto.TodayAttendanceResponse getTodayAttendance(Long employeeId) {
        Optional<Attendance> attendance = attendanceRepository.findTodayByEmployeeId(employeeId);
        
        if (attendance.isPresent()) {
            return new AttendanceDto.TodayAttendanceResponse(attendance.get());
        } else {
            // 기록이 없으면 빈 응답 반환
            return new AttendanceDto.TodayAttendanceResponse();
        }
    }

    /**
     * 출근 처리
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkIn(Long employeeId, Long companyId) {
        LocalDate today = LocalDate.now();
        
        // 오늘 이미 출근 기록이 있는지 확인
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        
        if (existing.isPresent()) {
            Attendance attendance = existing.get();
            if (attendance.getCommuteAt() != null) {
                throw new IllegalStateException("이미 출근 처리되었습니다.");
            }
            // 기록은 있지만 출근 시간이 없는 경우 (이론적으로는 발생하지 않아야 함)
            attendance.setCommuteAt(LocalDateTime.now());
            attendance.setStatus("근무 중");
            attendance = attendanceRepository.save(attendance);
            return new AttendanceDto.TodayAttendanceResponse(attendance);
        } else {
            // 새로운 출근 기록 생성
            Attendance attendance = new Attendance();
            attendance.setCompanyId(companyId);
            attendance.setEmployeeId(employeeId);
            attendance.setWorkDate(today);
            attendance.setCommuteAt(LocalDateTime.now());
            attendance.setStatus("근무 중");
            attendance.setIsCorrected(false);
            
            attendance = attendanceRepository.save(attendance);
            return new AttendanceDto.TodayAttendanceResponse(attendance);
        }
    }

    /**
     * 퇴근 처리
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        
        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        
        if (attendanceOpt.isEmpty()) {
            throw new IllegalStateException("출근 기록이 없습니다. 먼저 출근해주세요.");
        }
        
        Attendance attendance = attendanceOpt.get();
        
        if (attendance.getCommuteAt() == null) {
            throw new IllegalStateException("출근 기록이 없습니다.");
        }
        
        if (attendance.getLeaveAt() != null) {
            throw new IllegalStateException("이미 퇴근 처리되었습니다.");
        }
        
        attendance.setLeaveAt(LocalDateTime.now());
        attendance.setStatus("퇴근 완료");
        
        attendance = attendanceRepository.save(attendance);
        return new AttendanceDto.TodayAttendanceResponse(attendance);
    }
}

