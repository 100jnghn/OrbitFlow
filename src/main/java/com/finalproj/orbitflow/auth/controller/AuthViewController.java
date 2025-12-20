package com.finalproj.orbitflow.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuthViewController
 * @since : 2025-12-17 수요일
 */
@Controller
@RequestMapping("/login")
public class AuthViewController {

    @GetMapping
    public String loginPage() {
        return "auth/login";
    }
}
