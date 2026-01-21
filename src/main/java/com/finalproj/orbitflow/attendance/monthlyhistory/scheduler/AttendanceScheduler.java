package com.finalproj.orbitflow.attendance.monthlyhistory.scheduler;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceScheduler
 * @since : 2025. 12. 22. 월요일
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final CommuteRepository commuteRepository;
    private final EmployeeRepository employeeRepository;


    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void processAbsence() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        DayOfWeek day = yesterday.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return;
        }

        List<Employee> allEmployees = employeeRepository.findAllByStatus(EmployeeStatus.ACTIVE);

        for (Employee emp : allEmployees) {
            boolean exists = commuteRepository.existsByEmployeeIdAndWorkDate(emp.getId(), yesterday);

            if (!exists) {
                Attendance absenceRecord = Attendance.builder()
                        .employeeId(emp.getId())
                        .workDate(yesterday)
                        .status(AttendanceStatus.ABSENT)
                        .build();

                commuteRepository.save(absenceRecord);
            }
        }
    }
}