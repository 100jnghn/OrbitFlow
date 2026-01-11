package com.finalproj.orbitflow.resource.item.service;

import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.item.dto.ItemReqDto;
import com.finalproj.orbitflow.resource.item.dto.ItemResDto;
import com.finalproj.orbitflow.resource.item.entity.Item;
import com.finalproj.orbitflow.resource.item.repository.ItemRepository;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import com.finalproj.orbitflow.resource.itemcategory.repository.ItemCategoryRepository;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemService
 * @since : 2025-12-18 오후 2:20 목요일
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final ResourceStatusRepository resourceStatusRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    private final FileRepository fileRepository;
    private final FileService fileService;

    // companyId
    @Transactional(readOnly = true)
    public Page<ItemResDto> getItems(
            Long companyId,
            Pageable pageable
    ) {
        return itemRepository
                .getAllByCompanyId(companyId, ResourceStatusCode.DELETED, pageable)
                .map(this::convertToDto);
    }

    // companyId
    // status != DELETED
    @Transactional(readOnly = true)
    public List<ItemResDto> getAvailableItems(Long companyId) {
        return itemRepository.getAllByCompanyIdAndStatus(companyId, ResourceStatusCode.DELETED).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // companyId
    // status != DELETED
    // categoryId
    @Transactional(readOnly = true)
    public Page<ItemResDto> getItemsByCategory(Long companyId, Long categoryId, Pageable pageable) {
        return itemRepository.getAllByCompanyIdAndItemCategoryId(companyId, categoryId, ResourceStatusCode.DELETED, pageable)
                .map(this::convertToDto);
    }

    // companyId
    // status = AVAILABLE
    // categoryId
    @Transactional(readOnly = true)
    public List<ItemResDto> getItemsByStatusAndCategory(Long companyId, Long categoryId) {
        return itemRepository.getAllByCompanyIdAndItemCategoryIdAndStatus(companyId, categoryId, ResourceStatusCode.AVAILABLE).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResDto getItem(Long itemId) {
        Item item = itemRepository.findItemById(itemId);
        return convertToDto(item);
    }

    @Transactional
    public void insertItem(Long companyId, Long employeeId, ItemReqDto dto) {

        Company company = companyRepository.getReferenceById(companyId);
        Employee employee = employeeRepository.getReferenceById(employeeId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());
        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());

        // 관리자만 업로드할 수 있도록 확인
        String role = employee.getRole().toString();
        if (!role.endsWith("ADMIN")) {
            throw new InvalidRequestException("관리자만 업로드할 수 있습니다.");
        }

        // 파일 검증
        File file = null;
        MultipartFile imgFile = dto.getImgFile();

        if (imgFile != null && !imgFile.isEmpty()) {

            String contentType = imgFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidRequestException("이미지 파일만 업로드할 수 있습니다.");
            }

            file = fileService.upload(companyId, FileDomain.RESOURCE, imgFile);
        }

        Item item = Item.builder()
                .company(company)
                .itemCategory(itemCategory)
                .name(dto.getName())
                .description(dto.getDescription())
                .resourceStatus(resourceStatus)
                .file(file)
                .build();

        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long companyId, Long employeeId, Long itemId, ItemReqDto dto) {

        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());
        Item item = findItemById(itemId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());

        File imgFile = null;

        // 기존 이미지
        File itemImageFile = item.getFile();

        // ----- 이미지 수정 ----- //

        // 기존 이미지 X
        // 이미지 추가 O
        if (itemImageFile == null && dto.getImgFile() != null) {
            imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());
        }

        // 기존 이미지 O
        // 이미지 변경
        else if (itemImageFile != null && dto.getImgFile() != null) {
            boolean result = deleteItemFileInternal(item);
            imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());
        }

        // 기존 이미지 O
        // 이미지 유지
        else if (itemImageFile != null && dto.getImgFile() == null) {
            imgFile = itemImageFile;
        }

        item.update(
                itemCategory,
                dto.getName(),
                dto.getDescription(),
                resourceStatus,
                imgFile
        );
    }

    // 자원 삭제
    @Transactional
    public void deleteItem(Long itemId) {

        Item item = itemRepository.findItemById(itemId);

        // 이미지 삭제
        if (item.getFile() != null) {
            boolean result = deleteItemFileInternal(item);
        }

        ResourceStatus deleteStatus = resourceStatusRepository.findByResourceStatusCode(ResourceStatusCode.DELETED);
        item.delete(deleteStatus);
    }

    // 자원 이미지 삭제
    @Transactional
    public boolean deleteItemFile(Long itemId) {

        Item item = itemRepository.findItemById(itemId);

        boolean result = deleteItemFileInternal(item);
        return result;
    }

    private boolean deleteItemFileInternal(Item item) {

        if (item.getFile() == null) {
            return false;
        }

        File file = item.getFile();
        item.deleteFile();

        // db file 삭제, 반영
        fileRepository.delete(file);
        fileRepository.flush();

        // s3 file 삭제
        fileService.deleteObjectAfterCommit(file.getObjectKey());

        return true;
    }


    private ItemResDto convertToDto(Item item) {

        Long statusId = null;
        String code = "ETC";
        String name = "기타";

        if (item.getResourceStatus() != null) {
            statusId = item.getResourceStatus().getId();
            code = item.getResourceStatus().getResourceStatusCode().name();
            name = item.getResourceStatus().getResourceStatusCode().getDescription();
        }

        Employee uploader = employeeRepository.getReferenceById(item.getCreatedBy());
        String uploaderName = uploader.getName();

        LocalDate createdAt = item.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        // 이미지 파일 있으면 추가
        Long fileId = null;

        if (item.getFile() != null) {
            fileId = item.getFile().getId();
        }

        return ItemResDto.builder()
                .itemId(item.getId())
                .itemCategoryId(item.getItemCategory().getId())
                .itemCategoryName(item.getItemCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .statusId(statusId)
                .statusCode(code)
                .statusName(name)
                .fileId(fileId)
                .uploaderName(uploaderName)
                .createdAt(createdAt)
                .build();
    }

    // 자원 조회
    private Item findItemById(Long itemId) {
        return itemRepository.findItemById(itemId);
    }

    // 상태 코드 조회
    private ResourceStatus findResourceStatus(Long statusId) {
        log.info("Find resource status by id {}", statusId);
        return resourceStatusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 자원 상태입니다."));
    }

}
