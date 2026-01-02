package com.finalproj.orbitflow.email.service;

import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.repository.EmailVerificationTokenRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmailVerificationService
 * @since : 2026-01-01 목요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final MailSender mailSender;

    public void sendActivateMail(Employee employee) {
        EmailVerificationToken token =
                EmailVerificationToken.create(employee, EmailTokenType.ACTIVATE_ACCOUNT, 30);

        tokenRepository.save(token);

        mailSender.send(
                employee.getEmail(),
                "[OrbitFlow] 계정 활성화",
                "https://orbitflow.local/activate?token=" + token.getToken()
        );
    }

    public EmailVerificationToken verify(String tokenValue, EmailTokenType type) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰"));

        if (token.isUsed()) throw new IllegalStateException("이미 사용됨");
        if (token.isExpired()) throw new IllegalStateException("만료됨");
        if (token.getType() != type) throw new IllegalStateException("타입 불일치");

        token.markUsed();
        return token;
    }

    public void requestPasswordReset(Employee employee) {
        EmailVerificationToken token =
                EmailVerificationToken.create(employee, EmailTokenType.RESET_PASSWORD, 15);

        tokenRepository.save(token);

        mailSender.send(
                employee.getEmail(),
                "[OrbitFlow] 비밀번호 재설정",
                "https://orbitflow.local/reset-password?token=" + token.getToken()
        );
    }

    public Employee verifyAndGetEmployee(String tokenValue, EmailTokenType type) {
        EmailVerificationToken token = verify(tokenValue, type);
        return token.getEmployee();
    }

}
