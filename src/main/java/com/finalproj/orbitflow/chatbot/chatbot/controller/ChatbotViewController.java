package com.finalproj.orbitflow.chatbot.chatbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatbotViewController
 * @since : 2025. 12. 30. 화요일
 */

@Controller
@RequestMapping("/view")
public class ChatbotViewController {

    @GetMapping("/admin/chatbot")
    public String chatbotManualPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "chatbot");
        return "admin-chatbot/admin-chatbot-manual";
    }
}

