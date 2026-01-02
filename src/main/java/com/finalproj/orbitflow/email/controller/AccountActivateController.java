package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AccountActivateController
 * @since : 2026-01-01 목요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class AccountActivateController {

    private final EmailVerificationService emailService;
    private final EmployeeService employeeService;

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {

        Employee employee =
                emailService.verifyAndGetEmployee(token, EmailTokenType.ACTIVATE_ACCOUNT);

        employeeService.activate(employee);

        return ResponseEntity.ok().build();
    }

}
