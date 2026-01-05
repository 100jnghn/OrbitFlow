package com.finalproj.orbitflow.email.service;

import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.email.repository.EmailVerificationTokenRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendActivateMail(Employee employee) {
        EmailVerificationToken token =
                EmailVerificationToken.create(employee, EmailTokenType.ACTIVATE_ACCOUNT, 30);

        tokenRepository.save(token);

        String link = baseUrl + "/activate?token=" + token.getToken();

        mailSender.send(
                employee.getEmail(),
                "[OrbitFlow] 계정 활성화",
                link
        );
    }

    public void requestPasswordReset(Employee employee) {
        EmailVerificationToken token =
                EmailVerificationToken.create(employee, EmailTokenType.RESET_PASSWORD, 15);

        tokenRepository.save(token);

        String link = baseUrl + "/reset-password?token=" + token.getToken();

        mailSender.send(
                employee.getEmail(),
                "[OrbitFlow] 비밀번호 재설정",
                link
        );
    }

    public EmailVerificationToken verify(String tokenValue, EmailTokenType type) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰"));

        if (token.isUsed()) throw new IllegalStateException("이미 사용된 토큰입니다.");
        if (token.isExpired()) throw new IllegalStateException("만료된 토큰입니다.");
        if (token.getType() != type) throw new IllegalStateException("타입이 올바르지 않습니다.");

        // markUsed() 여기서 하지 않음
        return token;
    }

    public void markTokenUsed(EmailVerificationToken token) {
        token.markUsed();
    }


    public Employee verifyAndGetEmployee(String tokenValue, EmailTokenType type) {
        EmailVerificationToken token = verify(tokenValue, type);
        return token.getEmployee();
    }

}
