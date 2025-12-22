package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardPost.service.BoardCategoryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 게시판 카테고리 Controller
 */
@RestController
@RequestMapping("/api/board-categories")
@RequiredArgsConstructor
public class BoardCategoryController {

    private final BoardCategoryService boardCategoryService;

    /**
     * [사용자용] 권한이 부여된 활성 일반 게시판 목록 조회
     */
    @GetMapping("/accessible")
    public ResponseEntity<ResponseDto<List<BoardCategoryResDto.Category>>> getAccessibleBoards(
            @AuthenticationPrincipal SecurityUser user
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        List<BoardCategoryResDto.Category> boards = 
                boardCategoryService.getAccessibleBoards(user.getEmployeeId());

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "권한이 있는 게시판 목록 조회 성공", boards)
        );
    }

    /**
     * [사용자용] 본인 소속 조직 게시판 목록 조회
     */
    @GetMapping("/organization")
    public ResponseEntity<ResponseDto<List<BoardCategoryResDto.Category>>> getOrganizationBoards(
            @AuthenticationPrincipal SecurityUser user
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        if (user.getOrganizationId() == null) {
            return ResponseEntity.ok(
                    new ResponseDto<>(HttpStatus.OK, "조직 게시판 목록 조회 성공", List.of())
            );
        }

        List<BoardCategoryResDto.Category> boards = 
                boardCategoryService.getOrganizationBoards(user.getOrganizationId());

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "조직 게시판 목록 조회 성공", boards)
        );
    }
}

