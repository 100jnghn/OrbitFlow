package com.finalproj.orbitflow.resource.item.service;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
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
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());
        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());

        // 이미지 저장
        File imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());

        Item item = Item.builder()
                .company(company)
                .itemCategory(itemCategory)
                .name(dto.getName())
                .description(dto.getDescription())
                .resourceStatus(resourceStatus)
                .file(imgFile)
                .build();

        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long companyId, Long employeeId, Long itemId, ItemReqDto dto) {

        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());
        Item item = findItemById(itemId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());

        File imgFile = null;

        // 이미지 변경 있는지 확인
        // dto에 이미지 파일이 존재한다면
        if (dto.getImgFile() != null && !dto.getImgFile().isEmpty()) {

            // 1. 기존 img 삭제
            // 1-1. 기존 파일 존재하는지 확인
            File carImageFile = item.getFile();
            if (carImageFile != null) {
                // TODO - 이미지 삭제
            }

            // 2. 새 img 등록
            imgFile = fileService.upload(companyId, FileDomain.RESOURCE, dto.getImgFile());

        }
        // 이미지 변경 없으면
        else {
            // 기존 이미지
            imgFile = item.getFile();
        }

        item.update(
                itemCategory,
                dto.getName(),
                dto.getDescription(),
                resourceStatus,
                imgFile
        );
    }

    @Transactional
    public void deleteItem(Long itemId) {

        Item item = itemRepository.findItemById(itemId);
        ResourceStatus deleteStatus = resourceStatusRepository.findByResourceStatusCode(ResourceStatusCode.DELETED);

        // 이미지 삭제
        if (item.getFile() != null) {
            File file = item.getFile();
            fileService.deleteObject(file.getObjectKey());
        }

        item.delete(deleteStatus);
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
