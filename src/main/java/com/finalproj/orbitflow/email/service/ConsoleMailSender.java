package com.finalproj.orbitflow.email.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : ConsoleMailSender
 * @since : 2026-01-01 목요일
 */
@Profile("dev")
@Component
public class ConsoleMailSender implements MailSender {
    public void send(String to, String subject, String content) {
        System.out.println("MAIL TO " + to);
        System.out.println(subject);
        System.out.println(content);
    }
}
