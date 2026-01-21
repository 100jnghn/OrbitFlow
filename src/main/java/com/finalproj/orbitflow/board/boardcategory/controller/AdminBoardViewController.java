package com.finalproj.orbitflow.board.boardcategory.controller;

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
     * 공용 게시판 관리 페이지
     */
    @GetMapping("/board/common")
    public String commonBoardAdminPage(Model model) {
        model.addAttribute("pageTitle", "공용 게시판 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "common-board");
        return "admin/admin-board/admin-board-common";
    }

    /**
     * 부서 게시판 관리 페이지
     */
    @GetMapping("/board/dept-activation")
    public String deptBoardActivationPage(Model model) {
        model.addAttribute("pageTitle", "부서 게시판 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "dept-board");
        return "admin/admin-board/admin-board-dept";
    }
}
