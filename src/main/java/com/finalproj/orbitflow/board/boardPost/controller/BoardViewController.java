package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardPost.service.BoardService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        if (categoryId != null) {
            // 카테고리 권한 체크
            boardService.getVerifiedAccessibleCategory(
                    user.getCompanyId(),
                    user.getOrganizationId(),
                    user.getEmployeeId(),
                    categoryId,
                    user.getRole());
        }

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
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam Long boardId,
            Model model) {
        // 수정 시에도 게시판 권한 및 작성자 권한 체크가 필요할 수 있으나,
        // 여기서는 카테고리 접근 권한을 먼저 체크합니다. (상세 내역은 BoardService.getBoardDetail 등에서 수행됨)
        // 화면 진입 시점에도 최소한의 카테고리 활성화/삭제 여부 체크를 위해 getBoardDetail의 로직을 활용할 수 있습니다.
        boardService.getBoardDetail(
                user.getCompanyId(),
                user.getOrganizationId(),
                user.getEmployeeId(),
                boardId,
                user.getRole());

        model.addAttribute("pageTitle", "글수정");
        model.addAttribute("currentGNB", "board");
        model.addAttribute("boardId", boardId);
        return "board/board-write";
    }
}
