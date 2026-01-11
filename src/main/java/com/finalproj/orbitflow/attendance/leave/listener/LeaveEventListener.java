package com.finalproj.orbitflow.attendance.leave.listener;

import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.event.EmployeeCreatedEvent;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : LeaveEventListener
 * @since : 2026-01-10 토요일
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveEventListener {

    private final EmployeeRepository employeeRepository;
    private final LeaveService leaveService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {

        Employee emp = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (emp == null) {
            log.warn("[LeaveEvent] 사원 없음 - id={}", event.getEmployeeId());
            return;
        }

        try {
            leaveService.grantInitialLeave(emp);
        } catch (Exception e) {
            log.error("[LeaveEvent] 초기 연차 부여 실패 - employeeId={}", emp.getId(), e);
        }
    }
}
