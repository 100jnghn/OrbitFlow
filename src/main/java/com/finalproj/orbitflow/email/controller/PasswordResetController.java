package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.auth.service.AuthService;
import com.finalproj.orbitflow.email.dto.PasswordResetReqDto;
import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
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
    private final AuthService authService;

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> request(@RequestParam String email) {

        Employee employee = employeeService.findByEmail(email);

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("요청을 처리할 수 없습니다."); // 활성화된 계정만 비밀번호 재설정이 가능합니다.
        }

        emailService.requestPasswordReset(employee);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(
            @RequestParam String token,
            @RequestBody @Valid PasswordResetReqDto dto
    ) {
        EmailVerificationToken verificationToken =
                emailService.verify(token, EmailTokenType.RESET_PASSWORD);

        Employee employee = verificationToken.getEmployee();

        employeeService.resetPassword(employee, dto.getPassword());

        // 모든 세션 무효화 --> 비밀번호 변경 즉시 모든 기기 로그아웃
        authService.invalidateAll(employee.getId());

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
