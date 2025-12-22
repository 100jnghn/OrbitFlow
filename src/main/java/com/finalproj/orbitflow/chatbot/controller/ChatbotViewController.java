package com.finalproj.orbitflow.chatbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 챗봇 매뉴얼 관리 페이지 View Controller
 */
@Controller
@RequestMapping("/view")
public class ChatbotViewController {

    @GetMapping("/admin/chatbot")
    public String chatbotManualPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "chatbot");
        return "admin/admin-chatbot-manual";
    }
}

