package com.finalproj.orbitflow.global.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 메인(홈) 화면 View를 반환하는 컨트롤러.
 * - "/" 요청 시 메인 페이지를 렌더링한다.
 * - 실제 인증 여부는 화면 로드 후 /api/auth/me API로 검증한다.
 *
 * @author : seunga03
 * @filename : MainViewController
 * @since : 2025-12-17 수요일
 */
@Controller
public class MainViewController {

    @GetMapping("/")
    public String main() {
        return "main/index"; // templates/main/index.html
    }
}
