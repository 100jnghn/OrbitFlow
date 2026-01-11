package com.finalproj.orbitflow.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : SmtpMailSender
 * @since : 2026-01-03 토요일
 */
@Component
@RequiredArgsConstructor
public class SmtpMailSender implements MailSender {

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        msg.setFrom("OrbitFlow");
        mailSender.send(msg);
    }
}
