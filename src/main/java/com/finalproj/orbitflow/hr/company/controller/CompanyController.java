package com.finalproj.orbitflow.hr.company.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.hr.company.dto.CompanySignupReqDto;
import com.finalproj.orbitflow.hr.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CompanyController
 * @since : 2025-12-16 화요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping /** 회사 가입 **/
    public ResponseEntity<ResponseDto> signup(@RequestBody CompanySignupReqDto request) {
        Long companyId = companyService.signup(request);
        return ResponseEntity.ok(new ResponseDto(
                HttpStatus.CREATED, "회사 가입 완료", Map.of("companyId", companyId)));
    }

    @GetMapping("/check-business-number") /** 사업자번호 검증 **/
    public ResponseEntity<ResponseDto> checkBusinessNumber(@RequestParam String businessNumber) {
        companyService.checkBusinessNumberAvailable(businessNumber);
        return ResponseEntity.ok(new ResponseDto(
                HttpStatus.OK, "사용 가능한 사업자번호입니다.", Map.of("available", true)));
    }

    @GetMapping("/check-email") /** 이메일 검증 **/
    public ResponseEntity<ResponseDto> checkEmail(@RequestParam String email) {
        companyService.isEmailAvailable(email);
        return ResponseEntity.ok(new ResponseDto(
                        HttpStatus.OK, "이메일 중복 확인 완료", Map.of("available", true)));
    }
}
