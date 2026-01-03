package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
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


    @PostMapping("/activate/resend")
    public ResponseEntity<?> resendActivateMail(@RequestParam Long employeeId) {

        Employee employee = employeeService.findById(employeeId);

        // 이미 ACTIVE면 재전송 불가
        if (employee.getStatus() != EmployeeStatus.TEMP) {
            throw new IllegalStateException("이미 활성화된 계정입니다.");
        }

        emailService.sendActivateMail(employee);

        return ResponseEntity.ok().build();
    }

}
