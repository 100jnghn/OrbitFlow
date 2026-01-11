package com.finalproj.orbitflow.board.boardPermission.controller;

import com.finalproj.orbitflow.board.boardPermission.dto.BoardPermissionReqDto;
import com.finalproj.orbitflow.board.boardPermission.dto.BoardPermissionResDto;
import com.finalproj.orbitflow.board.boardPermission.service.AdminBoardPermissionService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 게시판 권한 관리 Controller
 */
@RestController
@RequestMapping("/api/admin/board-permissions")
@RequiredArgsConstructor
public class AdminBoardPermissionController {

    private final AdminBoardPermissionService adminBoardPermissionService;

    /**
     * [관리자용] 특정 게시판에 권한이 부여된 직원 목록 조회
     */
    @GetMapping("/board-categories/{boardCategoryId}")
    public ResponseEntity<ResponseDto<List<BoardPermissionResDto.Permission>>> getBoardPermissions(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long boardCategoryId
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        List<BoardPermissionResDto.Permission> permissions = 
                adminBoardPermissionService.getBoardPermissions(user.getCompanyId(), boardCategoryId);

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "게시판 권한 목록 조회 성공", permissions)
        );
    }

    /**
     * [관리자용] 게시판 권한 부여
     */
    @PostMapping
    public ResponseEntity<ResponseDto<List<BoardPermissionResDto.Permission>>> grantBoardPermissions(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid BoardPermissionReqDto.Permission dto
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        List<BoardPermissionResDto.Permission> permissions = 
                adminBoardPermissionService.grantBoardPermissions(
                        user.getCompanyId(), 
                        dto.getBoardCategoryId(), 
                        dto.getEmployeeIds()
                );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "게시판 권한 부여 성공", permissions)
        );
    }

    /**
     * [관리자용] 게시판 권한 제거
     */
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<ResponseDto<Void>> removeBoardPermission(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long permissionId
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        adminBoardPermissionService.removeBoardPermission(user.getCompanyId(), permissionId);

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "게시판 권한 제거 성공", null)
        );
    }
}

