package com.finalproj.orbitflow.resource.car.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.resource.car.dto.CarReqDto;
import com.finalproj.orbitflow.resource.car.dto.CarResDto;
import com.finalproj.orbitflow.resource.car.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarController
 * @since : 2025-12-16 오후 6:52 화요일
 */
@RequestMapping("/api")
@Controller
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    // 관리자 - 차량 리스트 조회
    @GetMapping("/admin/cars")
    public ResponseEntity<ResponseDto> getCars(@AuthenticationPrincipal SecurityUser user) {

        Long companyId = user.getCompanyId();
        List<CarResDto> cars = carService.getCars(companyId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "차량 목록 조회 성공", cars)
        );
    }

    // 관리자 - 차량 상세 조회
    @GetMapping("/admin/cars/{carId}")
    public ResponseEntity<ResponseDto> getCar(@PathVariable Long carId) {
        CarResDto car = carService.getCar(carId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "차량 상세 조회 성송", car)
        );
    }

    // 관리자 - 차량 등록
    @PostMapping("/admin/cars")
    public ResponseEntity<ResponseDto> insertCar(
            @ModelAttribute CarReqDto dto,
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long companyId = user.getCompanyId();
        carService.insertCar(companyId, dto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "차량 등록 성공", null)
        );
    }

    // 관리자 - 차량 수정
    @PutMapping("/admin/cars/{carId}")
    public ResponseEntity<ResponseDto> updateCar(
            @PathVariable Long carId,
            @ModelAttribute CarReqDto dto
    ) {
        carService.updateCar(carId, dto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "차량 수정 성공", null)
        );
    }

    // 관리자 - 차량 삭제
    @PatchMapping("/admin/cars/{carId}/delete")
    public ResponseEntity<ResponseDto> deleteCar(
            @PathVariable Long carId
    ) {
        carService.deleteCar(carId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "차량 삭제 성공", null)
        );
    }
}
