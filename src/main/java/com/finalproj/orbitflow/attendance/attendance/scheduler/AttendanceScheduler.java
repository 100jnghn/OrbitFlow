package com.finalproj.orbitflow.attendance.attendance.scheduler;

import com.finalproj.orbitflow.attendance.attendance.entity.Attendance;
import com.finalproj.orbitflow.attendance.attendance.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.attendance.repository.AttendanceRepository;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // 매일 00:01분에 어제 출근 안 한 사람들을 '결근' 처리
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void processAbsentRecords() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        employeeRepository.findAll().forEach(emp -> {
            boolean hasRecord = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(
                    emp.getCompany().getId(), emp.getId(), yesterday).isPresent();

            if (!hasRecord) {
                Attendance absent = new Attendance();
                absent.setCompanyId(emp.getCompany().getId());
                absent.setEmployeeId(emp.getId());
                absent.setWorkDate(yesterday);
                absent.setStatus(AttendanceStatus.ABSENT); // 결근 처리
                absent.setIsCorrected(false);
                attendanceRepository.save(absent);

                // 실시간 상태도 퇴근으로 초기화
                emp.updateWorkStatus(WorkStatus.OFF_WORK);
            }
        });
    }
}