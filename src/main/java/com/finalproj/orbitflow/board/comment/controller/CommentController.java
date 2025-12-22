package com.finalproj.orbitflow.board.comment.controller;

import com.finalproj.orbitflow.board.comment.dto.CommentReqDto;
import com.finalproj.orbitflow.board.comment.dto.CommentResDto;
import com.finalproj.orbitflow.board.comment.service.CommentService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    /** 댓글 작성 */
    @PostMapping("/boards/{boardId}/comments")
    public ResponseEntity<ResponseDto<CommentResDto.DetailInfo>> createComment(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long boardId,
            @Valid @RequestBody CommentReqDto.Create request
    ) {
        CommentResDto.DetailInfo result = commentService.createComment(
                user.getCompanyId(),
                user.getOrganizationId(),
                user.getEmployeeId(),
                boardId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "댓글이 등록되었습니다.", result));
    }

    /** [사용자용] 댓글 수정 */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ResponseDto<CommentResDto.DetailInfo>> updateComment(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentReqDto.Update request
    ) {
        CommentResDto.DetailInfo result = commentService.updateComment(
                user.getCompanyId(),
                user.getEmployeeId(),
                commentId,
                request
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "댓글이 수정되었습니다.", result)
        );
    }
}
