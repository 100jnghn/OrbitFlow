package com.finalproj.orbitflow.email.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping
    public String activate(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "email/activate"; // 비밀번호 생성 페이지
    }
}
