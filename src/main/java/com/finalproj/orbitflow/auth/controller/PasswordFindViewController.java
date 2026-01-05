package com.finalproj.orbitflow.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PasswordFindViewController
 * @since : 2026-01-05 월요일
 */
@Controller
public class PasswordFindViewController {

    @GetMapping("/find-password")
    public String findPasswordPage() {
        return "auth/find-password";
    }
}
