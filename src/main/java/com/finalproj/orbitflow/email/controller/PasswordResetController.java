package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.email.dto.PasswordResetReqDto;
import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PasswordResetController
 * @since : 2026-01-01 목요일
 */

@RestController
@AllArgsConstructor
@RequestMapping("/api/email")
public class PasswordResetController {

    private final EmployeeService employeeService;
    private final EmailVerificationService emailService;
    private final AuditLogService auditLogService;

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> request(@RequestParam String email) {

        Employee employee = employeeService.findByEmail(email);
        emailService.requestPasswordReset(employee);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(
            @RequestParam String token,
            @RequestBody @Valid PasswordResetReqDto dto
    ) {
        EmailVerificationToken verificationToken =
                emailService.verify(token, EmailTokenType.ACTIVATE_ACCOUNT);

        Employee employee = verificationToken.getEmployee();

        employeeService.resetPassword(employee, dto.getPassword());

        // 여기서 토큰 사용 처리
        emailService.markTokenUsed(verificationToken);

        auditLogService.log(
                employee.getCompany(),
                employee,
                AuditEntityType.EMPLOYEE,
                employee.getId(),
                AuditEventType.UPDATE,
                Map.of("password", "***"),
                Map.of("password", "***")
        );

        return ResponseEntity.ok().build();
    }

}
