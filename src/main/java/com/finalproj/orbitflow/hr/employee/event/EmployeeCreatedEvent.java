package com.finalproj.orbitflow.hr.employee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeCreatedEvent
 * @since : 2026-01-10 토요일
 */
@Getter
@AllArgsConstructor
public class EmployeeCreatedEvent {
    private final Long employeeId;
}
