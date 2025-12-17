package com.finalproj.orbitflow.resource.car.service;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.resource.car.dto.CarReqDto;
import com.finalproj.orbitflow.resource.car.dto.CarResDto;
import com.finalproj.orbitflow.resource.car.entity.Car;
import com.finalproj.orbitflow.resource.car.repository.CarRepository;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarService
 * @since : 2025-12-16 오후 6:52 화요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CompanyRepository companyRepository;
    private final ResourceStatusRepository resourceStatusRepository;

    @Transactional(readOnly = true)
    public List<CarResDto> getCars(Long companyId) {

        return carRepository.findAllByCompany_Id(companyId).stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarResDto getCar(Long carId) {

        Car car = findCarById(carId);
        return convertToResDto(car);
    }

    @Transactional
    public void insertCar(Long companyId, CarReqDto dto) {

        Company company = companyRepository.getOne(companyId);
        ResourceStatus status = findResourceStatus(dto.getResourceStatusCode());

        // todo - 이미지 파일 저장 기능 추가
        
        Car car = Car.builder()
                .company(company)
                .number(dto.getNumber())
                .name(dto.getName())
                .driverAge(dto.getDriverAge())
                .description(dto.getDescription())
                .resourceStatus(status)
                // todo - 이미지 파일 추가
                .build();

        carRepository.save(car);
    }







    // dto로 변환
    private CarResDto convertToResDto(Car car) {
        String code = "ETC";
        String name = "기타";

        if (car.getResourceStatus() != null) {
            code = car.getResourceStatus().getResourceStatusCode().name();
            name = car.getResourceStatus().getResourceStatusCode().getDescription();
        }

        String fileName = null;
        String objectKey = null;

        if (car.getFile() != null) {
            fileName = car.getFile().getOriginFile();
            objectKey = car.getFile().getObjectKey();
        }

        return CarResDto.builder()
                .carId(car.getId())
                .number(car.getNumber())
                .name(car.getName())
                .driverAge(car.getDriverAge())
                .description(car.getDescription())
                .statusCode(code)
                .statusName(name)
                .objectKey(objectKey)
                .originFile(fileName)
                .build();
    }

    @Transactional
    public void updateCar(Long carId, CarReqDto dto) {

        Car car = findCarById(carId);
        ResourceStatus status = findResourceStatus(dto.getResourceStatusCode());

        // todo - 이미지 수정 로직 추가
        File imgFile = car.getFile();

        car.update(
                dto.getNumber(),
                dto.getName(),
                dto.getDriverAge(),
                dto.getDescription(),
                status,
                imgFile // todo - 이미지 수정 로직 추가
        );
    }

    @Transactional
    public void deleteCar(Long carId) {

        Car car = findCarById(carId);

        ResourceStatus deleteStatus = resourceStatusRepository.findById(ResourceStatusCode.DELETED)
                .orElseThrow(() -> new IllegalStateException("삭제 코드가 없음"));

        car.delete(deleteStatus);
    }

    // 상태 코드 조회
    private ResourceStatus findResourceStatus(String statusCodeStr) {
        ResourceStatusCode statusCode;

        try {
            statusCode = ResourceStatusCode.valueOf(statusCodeStr);

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다: " + statusCodeStr);
        }

        return resourceStatusRepository.findById(statusCode)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 자원 상태입니다."));
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량을 찾을 수 없습니다"));
    }

}
