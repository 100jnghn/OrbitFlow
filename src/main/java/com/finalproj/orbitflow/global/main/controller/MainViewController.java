package com.finalproj.orbitflow.global.main.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.finalproj.orbitflow.global.security.SecurityUser;

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
    public String main(Model model, @AuthenticationPrincipal SecurityUser user) {
        // 레이아웃에 필요한 기본 정보 설정
        model.addAttribute("pageTitle", "홈 대시보드");
        model.addAttribute("currentGNB", "home");

        return "main/index"; // templates/main/index.html
    }
}
