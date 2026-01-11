package com.finalproj.orbitflow.email.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PasswordResetViewController
 * @since : 2026-01-02 금요일
 */
@Controller
@RequestMapping("/reset-password")
public class PasswordResetViewController {

    @GetMapping
    public String resetPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "email/reset-password";
    }
}
