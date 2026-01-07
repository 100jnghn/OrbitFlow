package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.email.dto.PasswordActivateReqDto;
import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.finalproj.orbitflow.email.enums.EmailTokenType.ACTIVATE_ACCOUNT;

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
    public ResponseEntity<?> activate(
            @RequestParam String token,
            @RequestBody @Valid PasswordActivateReqDto dto
    ) {
        EmailVerificationToken vt =
                emailService.verify(token, ACTIVATE_ACCOUNT);

        Employee employee = vt.getEmployee();

        if (employee.getStatus() != EmployeeStatus.TEMP) {
            throw new IllegalStateException("이미 활성화된 계정입니다.");
        }

        // 비밀번호 설정
        employeeService.resetPassword(employee, dto.getPassword());

        // 계정 활성화
        employeeService.activate(employee);

        // 토큰 사용 처리
        emailService.markTokenUsed(vt);

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
