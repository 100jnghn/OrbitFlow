package com.finalproj.orbitflow.board.boardPost.controller;

import com.finalproj.orbitflow.board.boardPost.dto.BoardPostReqDto;
import com.finalproj.orbitflow.board.boardPost.dto.BoardPostResDto;
import com.finalproj.orbitflow.board.boardPost.service.BoardPostService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-posts")
public class BoardPostController {

        private final BoardPostService boardPostService;

        /** [사용자용] 게시글 목록 조회(공용 게시판, 조직 게시판) */
        @GetMapping("/categories/{categoryId}")
        public ResponseEntity<ResponseDto<Page<BoardPostResDto.ListInfo>>> getBoardList(
                        @AuthenticationPrincipal SecurityUser user,
                        @PathVariable Long categoryId,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) String searchType, // TITLE/CONTENT/AUTHOR/ALL
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
                Page<BoardPostResDto.ListInfo> result = boardPostService.getBoardList(
                                user.getCompanyId(),
                                user.getOrganizationId(),
                                user.getEmployeeId(),
                                categoryId,
                                user.getRole(),
                                startDate,
                                endDate,
                                searchType,
                                keyword,
                                pageable);

                return ResponseEntity.ok(
                                new ResponseDto<>(
                                                HttpStatus.OK,
                                                "게시글 목록 조회 성공",
                                                result));
        }

        /** [사용자용] 게시글 상세 조회 */
        @GetMapping("/{boardId}")
        public ResponseEntity<ResponseDto<BoardPostResDto.DetailInfo>> getBoardDetail(
                        @AuthenticationPrincipal SecurityUser user,
                        @PathVariable Long boardId) {
                BoardPostResDto.DetailInfo detail = boardPostService.getBoardDetail(
                                user.getCompanyId(),
                                user.getOrganizationId(), // 없으면 null
                                user.getEmployeeId(),
                                boardId,
                                user.getRole());

                return ResponseEntity.ok(
                                new ResponseDto<>(HttpStatus.OK, "게시글 상세 조회 성공", detail));
        }

        /**
         * [사용자용] 게시글 생성 (첨부파일 포함)
         *
         * @param user           로그인 사용자 정보
         * @param organizationId 조직 ID (조직 게시판이면 필수)
         * @param request        게시글 생성 요청 DTO
         * @param files          첨부파일 목록 (선택)
         */
        @PostMapping
        public ResponseEntity<ResponseDto<BoardPostResDto.DetailInfo>> createBoard(
                        @AuthenticationPrincipal SecurityUser user,
                        @RequestParam(required = false) Long organizationId,
                        @Valid @ModelAttribute BoardPostReqDto.Create request,
                        @RequestPart(required = false) List<MultipartFile> files) {
                BoardPostResDto.DetailInfo response = boardPostService.createBoard(
                                user.getCompanyId(),
                                user.getOrganizationId(),
                                user.getEmployeeId(),
                                request,
                                files,
                                user.getRole());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ResponseDto<>(
                                                HttpStatus.CREATED,
                                                "게시글이 생성되었습니다.",
                                                response));
        }

        /** [사용자용] 게시글 수정 */
        @PutMapping(value = "/{boardId}", consumes = "multipart/form-data")
        public ResponseEntity<ResponseDto<BoardPostResDto.DetailInfo>> updateBoard(
                        @AuthenticationPrincipal SecurityUser user,
                        @PathVariable Long boardId,
                        @Valid @ModelAttribute BoardPostReqDto.Update request,
                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
                BoardPostResDto.DetailInfo result = boardPostService.updateBoard(
                                user.getCompanyId(),
                                user.getOrganizationId(),
                                user.getEmployeeId(),
                                boardId,
                                request,
                                files);

                return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "게시글 수정 성공", result));
        }

        /** [사용자용] 게시글 삭제 */
        @DeleteMapping("/{boardId}")
        public ResponseEntity<ResponseDto<Void>> deleteBoard(
                        @AuthenticationPrincipal SecurityUser user,
                        @PathVariable Long boardId) {
                boardPostService.deleteBoard(
                                user.getCompanyId(),
                                user.getOrganizationId(),
                                user.getEmployeeId(),
                                boardId);

                return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "게시글 삭제 성공", null));
        }
}
