package com.finalproj.orbitflow.hr.company.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.hr.company.dto.CompanySignupReqDto;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;

    @PostMapping
    public ResponseEntity<ResponseDto> signup(
            @RequestBody CompanySignupReqDto request
    ) {
        Long companyId = companyService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "회사 가입이 완료되었습니다.",
                        Map.of("companyId", companyId)
                ));
    }

    @GetMapping("/check-business-number")
    public ResponseEntity<ResponseDto> checkBusinessNumber(
            @RequestParam String businessNumber
    ) {
        boolean exists = companyRepository.existsByBusinessNumber(businessNumber);

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        exists ? "이미 등록된 사업자번호입니다." : "사용 가능한 사업자번호입니다.",
                        Map.of("available", !exists)
                )
        );
    }
}
