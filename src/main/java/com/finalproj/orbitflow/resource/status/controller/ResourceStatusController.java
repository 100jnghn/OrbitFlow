package com.finalproj.orbitflow.resource.status.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.resource.status.dto.ResourceStatusResDto;
import com.finalproj.orbitflow.resource.status.service.ResourceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatusController
 * @since : 2025-12-20 오후 2:51 토요일
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceStatusController {

    private final ResourceStatusService resourceStatusService;

    @GetMapping("/admin/resource-status")
    public ResponseEntity<ResponseDto> getResourceStatus() {
        List<ResourceStatusResDto> list = resourceStatusService.getResourceStatus();
        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 상태 리스트 조회 성공", list)
        );
    }

}
