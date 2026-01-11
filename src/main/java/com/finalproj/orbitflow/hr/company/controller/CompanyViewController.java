package com.finalproj.orbitflow.hr.company.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CompanyViewController
 * @since : 2025-12-17 수요일
 */
@Controller
@RequestMapping("/companies")
public class CompanyViewController {

    @GetMapping("/signup")
    public String signupPage() {
        return "company/signup"; // templates/company/signup.html
    }
}