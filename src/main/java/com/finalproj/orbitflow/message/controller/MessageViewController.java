package com.finalproj.orbitflow.message.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 메시지 View Controller
 */
@Controller
@RequestMapping("/view")
public class MessageViewController {

    /**
     * 받은 메시지함 페이지
     */
    @GetMapping("/message/inbox")
    public String inboxPage(Model model) {
        model.addAttribute("pageTitle", "받은 메시지함");
        model.addAttribute("currentGNB", "message");
        model.addAttribute("folderType", "INBOX");
        return "message/message-inbox";
    }

    /**
     * 보낸 메시지함 페이지
     */
    @GetMapping("/message/sent")
    public String sentPage(Model model) {
        model.addAttribute("pageTitle", "보낸 메시지함");
        model.addAttribute("currentGNB", "message");
        model.addAttribute("folderType", "SENT");
        return "message/message-sent";
    }

    /**
     * 보관함 페이지
     */
    @GetMapping("/message/archive")
    public String archivePage(Model model) {
        model.addAttribute("pageTitle", "보관함");
        model.addAttribute("currentGNB", "message");
        model.addAttribute("folderType", "ARCHIVE");
        return "message/message-archive";
    }

    /**
     * 메시지 상세 조회 페이지
     */
    @GetMapping("/message/detail")
    public String messageDetailPage(
            @RequestParam Long messageId,
            @RequestParam(required = false) String folder,
            Model model
    ) {
        model.addAttribute("pageTitle", "메시지 상세");
        model.addAttribute("currentGNB", "message");
        model.addAttribute("messageId", messageId);
        model.addAttribute("folderType", folder != null ? folder : "INBOX");
        return "message/message-detail";
    }
}

