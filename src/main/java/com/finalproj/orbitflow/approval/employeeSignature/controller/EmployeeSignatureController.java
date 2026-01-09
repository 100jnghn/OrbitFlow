package com.finalproj.orbitflow.approval.employeeSignature.controller;

import com.finalproj.orbitflow.approval.employeeSignature.service.EmployeeSignatureService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Please explain the class!!!
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
