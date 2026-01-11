package com.finalproj.orbitflow.board.boardCategory.controller;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryReqDto;
import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.service.AdminBoardCategoryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
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
@RequestMapping("/api/admin/board-categories")
@RequiredArgsConstructor
public class AdminBoardCategoryController {

    private final AdminBoardCategoryService adminBoardCategoryService;

    // =========================================================================
    // 1. 일반 게시판 관리
    // =========================================================================

    /** [관리자용] 게시판 카테고리 목록 조회 (토큰의 회사 ID 기준) */
    @GetMapping
    public ResponseEntity<ResponseDto> getBoardCategoryList(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "false") boolean organizationOnly,
            @PageableDefault(size = 8, sort = "createdAt") Pageable pageable
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        Page<BoardCategoryResDto.Category> page =
                adminBoardCategoryService.getBoardCategoryList(
                        user.getCompanyId(),
                        organizationOnly,
                        pageable
                );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시판 목록 조회 성공", page)
        );
    }


    /** [관리자용] 일반 카테고리 상세 조회(추가, 수정용) */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ResponseDto> getCategoryDetail(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        BoardCategoryResDto.Detail detail =
                adminBoardCategoryService.getCategoryDetail(
                        user.getCompanyId(),
                        categoryId
                );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시판 상세 조회 성공", detail)
        );
    }

    /** [관리자용] 게시판 카테고리 생성 */
    @PostMapping
    public ResponseEntity<ResponseDto> createCategory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid BoardCategoryReqDto.Create dto
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        Long id = adminBoardCategoryService.createCategory(
                user.getCompanyId(),
                dto
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시판 생성 성공", id)
        );
    }


    /** [관리자용] 게시판 카테고리 수정 */
    @PutMapping("/{categoryId}")
    public ResponseEntity<ResponseDto> updateCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId,
            @RequestBody @Valid BoardCategoryReqDto.Update dto
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        adminBoardCategoryService.updateCategory(
                user.getCompanyId(),
                categoryId,
                dto
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시판 수정 성공", categoryId)
        );
    }


    /** [관리자용] 게시판 카테고리 삭제 */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ResponseDto> deleteCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        adminBoardCategoryService.deleteCategory(
                user.getCompanyId(),
                categoryId
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "게시판 삭제 성공", null)
        );
    }



    /** [관리자용] 조직 게시판 활성/비활성 토글 */
    @PatchMapping("/organization/{categoryId}/activation")
    public ResponseEntity<ResponseDto> changeOrganizationBoardActivation(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId,
            @RequestBody @Valid BoardCategoryReqDto.Activation dto
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        adminBoardCategoryService.changeOrganizationBoardActivation(
                user.getCompanyId(),
                categoryId,
                dto.getIsActivated()
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "조직 게시판 활성화 상태 변경 성공", null)
        );
    }


    // =========================================================================
    // 1. 일반 사용자 게시판
    // =========================================================================





}
