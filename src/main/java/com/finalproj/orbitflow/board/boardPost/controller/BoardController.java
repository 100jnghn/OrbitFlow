package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardPost.dto.BoardResDto;
import com.finalproj.orbitflow.board.boardPost.service.BoardService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    /**
     * [사용자용] 게시글 목록 조회
     * - 공용 게시판
     * - 조직 게시판
     *
     * categoryId : 게시판 카테고리 ID
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseDto> getBoardList(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<BoardResDto.ListInfo> result =
                boardService.getBoardList(
                        user.getCompanyId(),
                        user.getOrganizationId(),
                        categoryId,
                        pageable
                );

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "게시글 목록 조회 성공",
                        result
                )
        );
    }
}
