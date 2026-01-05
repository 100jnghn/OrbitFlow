package com.finalproj.orbitflow.resource.car.service;

import com.finalproj.orbitflow.global.exception.DuplicateCarNumberException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.resource.car.dto.CarReqDto;
import com.finalproj.orbitflow.resource.car.dto.CarResDto;
import com.finalproj.orbitflow.resource.car.entity.Car;
import com.finalproj.orbitflow.resource.car.repository.CarRepository;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final EmployeeRepository employeeRepository;

    private final FileService fileService;
    private final FileRepository fileRepository;

    @Transactional(readOnly = true)
    public Page<CarResDto> getCars(Long companyId, Pageable pageable) {

        return carRepository.findAllByCompany_Id(companyId, pageable)
                .map(this::convertToResDto);
    }


    @Transactional(readOnly = true)
    public List<CarResDto> getAvailableCars(Long companyId) {

        return carRepository.findAllByCompanyIdAndStatus(companyId, ResourceStatusCode.AVAILABLE).stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarResDto getCar(Long carId) {

        Car car = findCarById(carId);
        return convertToResDto(car);
    }

    @Transactional
    public void insertCar(Long companyId, Long employeeId, CarReqDto dto) {

        Company company = companyRepository.getReferenceById(companyId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());

        // 차량 번호 unique하게 (공백 제거)
        String number = dto.getNumber().replace(" ", "");

        if (carRepository.existsByNumber(number)) {
            throw new DuplicateCarNumberException("이미 존재하는 차량 번호입니다");
        }

        // 이미지 저장 기능 추가
        File imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());

        Car car = Car.builder()
                .company(company)
                .number(number)
                .name(dto.getName())
                .driverAge(dto.getDriverAge())
                .description(dto.getDescription())
                .resourceStatus(resourceStatus)
                .file(imgFile)
                .build();

        carRepository.save(car);
    }

    @Transactional
    public void updateCar(Long companyId, Long employeeId, Long carId, CarReqDto dto) {

        Car car = findCarById(carId);
        ResourceStatus status = findResourceStatus(dto.getStatusId());

        File imgFile = null;

        // 기존 이미지
        File carImageFile = car.getFile();


        // ----- 이미지 수정 ----- //

        // 기존 이미지 X
        // 이미지 추가 O
        if (carImageFile == null && dto.getImgFile() != null) {
            imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());

        }

        // 기존 이미지 O
        // 이미지 변경
        else if (carImageFile != null && dto.getImgFile() != null) {
            boolean result = deleteCarFileInternal(car);
            imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());
        }

        // 기존 이미지 O
        // 이미지 유지
        else if (carImageFile != null && dto.getImgFile() == null) {
            imgFile = carImageFile;
        }


        // 차량 번호 unique하게 (공백 제거)
        String number = dto.getNumber().replace(" ", "");

        car.update(
                number,
                dto.getName(),
                dto.getDriverAge(),
                dto.getDescription(),
                status,
                imgFile
        );
    }

    // 차량 삭제
    @Transactional
    public void deleteCar(Long carId) {

        Car car = findCarById(carId);

        // 이미지 삭제
        if (car.getFile() != null) {

            boolean result = deleteCarFileInternal(car);
        }

        ResourceStatus deleteStatus = resourceStatusRepository.findByResourceStatusCode(ResourceStatusCode.DELETED);
        car.delete(deleteStatus);
    }


    // 차량 이미지 삭제
    @Transactional
    public boolean deleteCarFile(Long carId) {

        Car car = findCarById(carId);
        boolean result = deleteCarFileInternal(car);

        return result;
    }

    // 차량 이미지 삭제 함수 호출
    private boolean deleteCarFileInternal(Car car) {

        if (car.getFile() == null) {
            return false;
        }

        File file = car.getFile();

        // db file 삭제, 반영
        fileRepository.delete(file);
        fileRepository.flush();

        // s3 file 삭제
        fileService.deleteObjectAfterCommit(file.getObjectKey());

        return true;
    }


    // dto로 변환
    private CarResDto convertToResDto(Car car) {
        Long statusId = null;
        String code = "ETC";
        String name = "기타";

        if (car.getResourceStatus() != null) {
            statusId = car.getResourceStatus().getId();
            code = car.getResourceStatus().getResourceStatusCode().name();
            name = car.getResourceStatus().getResourceStatusCode().getDescription();
        }

        String number = car.getNumber();
        number = number.replaceAll("([가-힣])", "$1 ");

        Employee uploader = employeeRepository.getReferenceById(car.getCreatedBy());
        String uploaderName = uploader.getName();

        LocalDate createdAt = car.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        Long fileId = null;

        // 이미지 유효성 검사
        if (car.getFile() != null) {
            fileId = car.getFile().getId();
        }

        return CarResDto.builder()
                .carId(car.getId())
                .number(number)
                .name(car.getName())
                .driverAge(car.getDriverAge())
                .description(car.getDescription())
                .statusId(statusId)
                .statusCode(code)
                .statusName(name)
                .fileId(fileId)
                .uploaderName(uploaderName)
                .createdAt(createdAt)
                .build();
    }

    // 상태 코드 조회
    private ResourceStatus findResourceStatus(Long statusId) {
        log.info("Find resource status by id {}", statusId);
        return resourceStatusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 자원 상태입니다."));
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량을 찾을 수 없습니다"));
    }

}
