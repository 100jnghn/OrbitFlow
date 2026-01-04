package com.finalproj.orbitflow.email.controller;

import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.service.EmailVerificationService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : ActivateViewController
 * @since : 2026-01-02 금요일
 */
@Controller
@RequestMapping("/activate")
@RequiredArgsConstructor
public class ActivateViewController {

    private final EmailVerificationService emailService;
    private final EmployeeService employeeService;

    @GetMapping
    public String activateAndRedirect(@RequestParam String token) {

        Employee employee =
                emailService.verifyAndGetEmployee(token, EmailTokenType.ACTIVATE_ACCOUNT);

        employeeService.activate(employee);

        // 활성화 끝 --> 비밀번호 재설정으로 이동
        return "redirect:/reset-password?token=" + token;
    }
}
