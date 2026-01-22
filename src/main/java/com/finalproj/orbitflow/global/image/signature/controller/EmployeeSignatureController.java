package com.finalproj.orbitflow.global.image.signature.controller;

import com.finalproj.orbitflow.global.image.signature.service.EmployeeSignatureService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사원(Employee) 서명(Signature) 관리 REST API 컨트롤러.
 *
 * <p>
 * 인증된 사원이 자신의 서명 이미지를 등록하거나,
 * 현재 활성화된 서명 보유 여부를 조회할 수 있는 API를 제공한다.
 * </p>
 *
 * @author : Choi MinHyeok
 * @filename : EmployeeSignature
 * @since : 26. 1. 8. 목요일
 **/

@RestController
@RequestMapping("/api/employee-signature")
@RequiredArgsConstructor
public class EmployeeSignatureController {

    private final EmployeeSignatureService employeeSignatureService;

    @PostMapping
    public ResponseEntity<?> saveSignature(
            @RequestParam MultipartFile file
    ) {

        employeeSignatureService.saveSignature(SecurityUtils.getEmployeeId(), file);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "서명 갱신 성공", null));
    }

    @GetMapping
    public ResponseEntity<?> hasActiveSignature() {
        boolean hasActiveSignature = employeeSignatureService.hasActiveSignature(SecurityUtils.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "서명 조회 성공", hasActiveSignature));
    }

}
