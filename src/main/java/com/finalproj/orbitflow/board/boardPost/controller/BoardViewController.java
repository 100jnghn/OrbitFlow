package com.finalproj.orbitflow.board.boardPost.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 사용자 게시판 View Controller
 */
@Controller
@RequestMapping("/view")
public class BoardViewController {

    /**
     * 사용자 게시판 페이지
     */
    @GetMapping("/board")
    public String boardPage(Model model) {
        model.addAttribute("pageTitle", "게시판");
        model.addAttribute("currentGNB", "board");
        return "board/board";
    }
}


