package com.finalproj.orbitflow.hr.rank.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.rank.dto.RankCreateReqDto;
import com.finalproj.orbitflow.hr.rank.dto.RankOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.rank.dto.RankResDto;
import com.finalproj.orbitflow.hr.rank.dto.RankUpdateReqDto;
import com.finalproj.orbitflow.hr.rank.service.RankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankController
 * @since : 2025-12-20 토요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ranks")
public class RankController {

    private final RankService rankService;

    /**
     * 직급 목록 조회
     * - keyword: 직급명 검색
     * - includeInactive: 비활성 포함 여부
     */
    @GetMapping
    public ResponseEntity<ResponseDto<List<RankResDto>>> getRanks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "직급 목록 조회 성공",
                        rankService.getRanks(SecurityUtils.getCompanyId(), keyword, includeInactive)
                )
        );
    }

    /**
     * 직급 생성
     */
    @PostMapping
    public ResponseEntity<ResponseDto<Long>> createRank(
            @RequestBody @Valid RankCreateReqDto request
    ) {
        Long id = rankService.createRank(SecurityUtils.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "직급 생성 성공", id));
    }

    /**
     * 직급 수정 (이름/상위직급/활성여부)
     */
    @PutMapping("/{rankId}")
    public ResponseEntity<ResponseDto<Void>> updateRank(
            @PathVariable Long rankId,
            @RequestBody @Valid RankUpdateReqDto request
    ) {
        rankService.updateRank(SecurityUtils.getCompanyId(), rankId, request);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "직급 수정 성공", null));
    }


    /**
     * 직급 순서 변경 저장 (드래그앤드롭)
     */
    @PutMapping("/order")
    public ResponseEntity<ResponseDto<Void>> updateOrder(
            @RequestBody @Valid RankOrderUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        rankService.updateOrder(companyId, request.getOrders());

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "직급 순서 변경 저장 성공", null)
        );
    }
}
