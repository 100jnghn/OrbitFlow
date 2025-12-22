package com.finalproj.orbitflow.board.boardPost.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * 게시글 작성 페이지
     */
    @GetMapping("/board/write")
    public String boardWritePage(
            @RequestParam(required = false) Long categoryId,
            Model model
    ) {
        model.addAttribute("pageTitle", "글쓰기");
        model.addAttribute("currentGNB", "board");
        model.addAttribute("categoryId", categoryId);
        return "board/board-write";
    }

    /**
     * 게시글 상세 조회 페이지
     */
    @GetMapping("/board/detail")
    public String boardDetailPage(
            @RequestParam Long boardId,
            Model model
    ) {
        model.addAttribute("pageTitle", "게시글 상세");
        model.addAttribute("currentGNB", "board");
        model.addAttribute("boardId", boardId);
        return "board/board-detail";
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/board/edit")
    public String boardEditPage(
            @RequestParam Long boardId,
            Model model
    ) {
        model.addAttribute("pageTitle", "글수정");
        model.addAttribute("currentGNB", "board");
        model.addAttribute("boardId", boardId);
        return "board/board-write";
    }
}


