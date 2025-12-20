package com.finalproj.orbitflow.board.boardCategory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 게시판 관리 View Controller
 */
@Controller
@RequestMapping("/view/admin")
public class AdminBoardViewController {

    /**
     * 게시판 관리 페이지
     */
    @GetMapping("/board")
    public String boardAdminPage(Model model) {
        model.addAttribute("pageTitle", "게시판 관리자");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "board");
        return "admin/admin-board";
    }
}
