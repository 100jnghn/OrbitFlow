package com.finalproj.orbitflow.global.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : MainViewController
 * @since : 2025-12-17 수요일
 */
@Controller
public class MainViewController {

    @GetMapping("/")
    public String main() {
        return "main/index";
    }
}
