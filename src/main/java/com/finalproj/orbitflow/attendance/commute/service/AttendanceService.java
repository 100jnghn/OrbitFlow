package com.finalproj.orbitflow.attendance.commute.service;

import com.finalproj.orbitflow.attendance.commute.dto.AttendanceDto;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // [TODO] 실제 구현 시 Spring Security 등을 통해 현재 로그인한 사원 ID와 회사 ID를 가져와야 함
    // 임시 상수 대신, 실제로는 SecurityContext에서 가져와야 함.
    // private final Long CURRENT_EMPLOYEE_ID = 1L;
    // private final Long CURRENT_COMPANY_ID = 1L;

    // [TODO] 회사의 표준 출근 시간 (규칙 테이블에서 가져와야 하지만, 임시로 9시로 가정)
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0);


    /**
     * 오늘의 출퇴근 기록 조회 (회사 ID를 기준으로 격리)
     * @param companyId 현재 로그인한 사원이 속한 회사 ID (보안 필터)
     * @param employeeId 기록을 조회할 사원 ID
     */
    public AttendanceDto.TodayAttendanceResponse getTodayAttendance(Long companyId, Long employeeId) {
        // [수정] findTodayByEmployeeId -> findTodayByCompanyIdAndEmployeeId 로 변경 필요 (Repository 수정 필요)
        Optional<Attendance> attendance = attendanceRepository.findTodayByCompanyIdAndEmployeeId(companyId, employeeId);

        if (attendance.isPresent()) {
            return new AttendanceDto.TodayAttendanceResponse(attendance.get());
        } else {
            // 기록이 없으면 빈 응답 반환
            return new AttendanceDto.TodayAttendanceResponse();
        }
    }

    /**
     * 출근 처리 (지각 여부 판단 로직 및 회사 ID 기반 격리 추가)
     * @param companyId 현재 로그인한 사원이 속한 회사 ID (보안 필터)
     * @param employeeId 기록을 조회할 사원 ID
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkIn(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime actualCommuteAt = LocalDateTime.now();
        LocalTime actualStartTime = actualCommuteAt.toLocalTime(); // 출근 시간만 추출

        // 1. 오늘 이미 출근 기록이 있는지 회사 ID 기반으로 확인 (데이터 격리)
        // [수정] findByEmployeeIdAndWorkDate -> findByCompanyIdAndEmployeeIdAndWorkDate 로 변경 필요
        Optional<Attendance> existing = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today);

        if (existing.isPresent()) {
            Attendance attendance = existing.get();
            if (attendance.getCommuteAt() != null) {
                throw new IllegalStateException("이미 출근 처리되었습니다.");
            }
            // 이 로직은 출근 버튼을 여러 번 눌렀을 경우를 대비하지만,
            // 보통 출근 시점에만 레코드를 생성하므로, 실제로는 아래 else 로직으로 통합될 수 있음.
            attendance.setCommuteAt(actualCommuteAt);
            // [TODO] 상태 값 (지각/정상) 재판단 로직 필요
            attendance = attendanceRepository.save(attendance);
            return new AttendanceDto.TodayAttendanceResponse(attendance);
        } else {
            // 2. 새로운 출근 기록 생성
            Attendance attendance = new Attendance();
            attendance.setCompanyId(companyId);
            attendance.setEmployeeId(employeeId);
            attendance.setWorkDate(today);
            attendance.setCommuteAt(actualCommuteAt);
            attendance.setIsCorrected(false);

            // 3. 지각/정상출근 상태 결정 및 설정
            AttendanceStatus determinedStatus;
            if (actualStartTime.isAfter(STANDARD_START_TIME)) {
                determinedStatus = AttendanceStatus.LATE; // 지각 처리
            } else {
                determinedStatus = AttendanceStatus.ON_TIME; // 정상 출근
            }
            attendance.setStatus(determinedStatus); // 상태 값 할당

            attendance = attendanceRepository.save(attendance);
            return new AttendanceDto.TodayAttendanceResponse(attendance);
        }
    }

    /**
     * 퇴근 처리 (회사 ID를 기준으로 격리)
     * @param companyId 현재 로그인한 사원이 속한 회사 ID (보안 필터)
     * @param employeeId 기록을 조회할 사원 ID
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkOut(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();

        // [수정] findByEmployeeIdAndWorkDate -> findByCompanyIdAndEmployeeIdAndWorkDate 로 변경 필요
        Optional<Attendance> attendanceOpt = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today);

        if (attendanceOpt.isEmpty()) {
            // [TODO] 만약 결근(ABSENT) 상태로 레코드가 존재하지 않는다면, 이 시점에 '결근' 레코드를 생성 후 퇴근 시간만 기록하지 않도록 처리해야 함.
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

        // [TODO] 조퇴 (Early Leave) 판단 로직 추가 필요

        attendance = attendanceRepository.save(attendance);
        return new AttendanceDto.TodayAttendanceResponse(attendance);
    }
}