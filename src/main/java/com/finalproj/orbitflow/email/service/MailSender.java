package com.finalproj.orbitflow.email.service;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : MailSender
 * @since : 2026-01-01 목요일
 */
public interface MailSender {
    void send(String to, String subject, String content);
}
