package com.finalproj.orbitflow.attendance.history.scheduler;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * 매일 새벽 00시 01분에 실행되어 전날의 결근자를 처리함
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void processAbsence() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1. 주말(토, 일) 제외 로직
        DayOfWeek day = yesterday.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            log.info("{}는 주말이므로 결근 처리를 진행하지 않습니다.", yesterday);
            return;
        }

        log.info("{} 결근 처리 스케줄러 가동", yesterday);

        // 2. 현재 재직 중인 모든 사원 조회
        List<Employee> allEmployees = employeeRepository.findAllByStatus("ACTIVE");

        for (Employee emp : allEmployees) {
            // 3. 해당 사원이 어제 출근 기록이 있는지 확인
            boolean exists = attendanceRepository.existsByEmployeeIdAndWorkDate(emp.getId(), yesterday);

            if (!exists) {
                // 4. 기록이 없으면 ABSENT(결근) 레코드 생성
                Attendance absenceRecord = Attendance.builder()
                        .employeeId(emp.getId())
                        .workDate(yesterday)
                        .status(AttendanceStatus.ABSENT)
                        .build();

                attendanceRepository.save(absenceRecord);
            }
        }
        log.info("{} 결근 처리 완료", yesterday);
    }
}