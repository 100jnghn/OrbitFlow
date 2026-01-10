package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardPost.service.BoardService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BoardViewController {

    private final BoardService boardService;

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
            Model model) {
        // [NPE 해결] 서버 사이드에서는 @AuthenticationPrincipal을 사용하지 않습니다.
        // 현재 프로젝트는 JWT를 sessionStorage에 저장하므로, 브라우저 직접 접속 시 서버가 인증 정보를 알 수 없습니다.
        // 대신 프론트엔드(board-write.js 및 common.js)에서 API 호출을 통해 권한을 검증합니다.

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
            Model model) {
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
            Model model) {
        // [NPE 해결] 서버 사이드에서는 @AuthenticationPrincipal을 사용하지 않습니다.
        // 게시글 정보 및 수정 권한 체크는 프론트엔드가 API(/api/boards/{id})를 호출할 때 수행됩니다.

        model.addAttribute("pageTitle", "글수정");
        model.addAttribute("currentGNB", "board");
        model.addAttribute("boardId", boardId);
        return "board/board-write";
    }
}
