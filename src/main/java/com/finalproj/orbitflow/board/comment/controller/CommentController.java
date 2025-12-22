package com.finalproj.orbitflow.board.comment.controller;

import com.finalproj.orbitflow.board.comment.dto.CommentResDto;
import com.finalproj.orbitflow.board.comment.service.CommentService;
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
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    /** [사용자용] 댓글 목록 조회 */
    @GetMapping("/boards/{boardId}/comments")
    public ResponseEntity<ResponseDto> getCommentList(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long boardId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<CommentResDto.ListInfo> result = commentService.getCommentList(
                user.getCompanyId(),
                user.getOrganizationId(),
                boardId,
                pageable
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "댓글 목록 조회 성공", result)
        );
    }
}
