package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardPost.dto.BoardReqDto;
import com.finalproj.orbitflow.board.boardPost.dto.BoardResDto;
import com.finalproj.orbitflow.board.boardPost.service.BoardService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    /** [사용자용] 게시글 목록 조회(공용 게시판, 조직 게시판) */
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

    /** [사용자용] 게시글 상세 조회 */
    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseDto> getBoardDetail(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long boardId
    ) {
        BoardResDto.DetailInfo detail =
                boardService.getBoardDetail(
                        user.getCompanyId(),
                        user.getOrganizationId(), // 없으면 null
                        boardId
                );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시글 상세 조회 성공", detail)
        );
    }

    /**
     * [사용자용] 게시글 생성 (첨부파일 포함)
     *
     * @param user 로그인 사용자 정보
     * @param organizationId 조직 ID (조직 게시판이면 필수)
     * @param request 게시글 생성 요청 DTO
     * @param files 첨부파일 목록 (선택)
     */
    @PostMapping
    public ResponseEntity<ResponseDto<BoardResDto.DetailInfo>> createBoard(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Long organizationId,
            @Valid @ModelAttribute BoardReqDto.Create request,
            @RequestPart(required = false) List<MultipartFile> files
    ) {
        BoardResDto.DetailInfo response =
                boardService.createBoard(
                        user.getCompanyId(),
                        user.getOrganizationId(),
                        user.getEmployeeId(),
                        request,
                        files
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        HttpStatus.CREATED,
                        "게시글이 생성되었습니다.",
                        response
                ));
    }
}

