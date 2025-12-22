package com.finalproj.orbitflow.resource.item.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ResourceStatusRepository resourceStatusRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    // companyId
    @Transactional(readOnly = true)
    public List<ItemResDto> getItems(Long companyId) {
        return itemRepository.getAllByCompanyId(companyId, ResourceStatusCode.DELETED).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
    public List<ItemResDto> getItemsByCategory(Long companyId, Long categoryId) {
        return itemRepository.getAllByCompanyIdAndItemCategoryId(companyId, categoryId, ResourceStatusCode.DELETED).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
    public void insertItem(Long companyId, ItemReqDto dto) {
        log.info("insert item");
        log.info("dto " + dto.getStatusId());

        Company company = companyRepository.getReferenceById(companyId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());
        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());

        Item item = Item.builder()
                .company(company)
                .itemCategory(itemCategory)
                .name(dto.getName())
                .description(dto.getDescription())
                .resourceStatus(resourceStatus)
                // todo - 이미지 파일 처리
                .build();

        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, ItemReqDto dto) {

        ItemCategory itemCategory = itemCategoryRepository.getReferenceById(dto.getItemCategoryId());
        Item item = findItemById(itemId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());

        // todo - 이미지 파일 처리

        item.update(
                itemCategory,
                dto.getName(),
                dto.getDescription(),
                resourceStatus,
                null    // todo - 이미지 파일 처리
        );
    }

    @Transactional
    public void deleteItem(Long itemId) {

        Item item = itemRepository.findItemById(itemId);
        ResourceStatus deleteStatus = resourceStatusRepository.findByResourceStatusCode(ResourceStatusCode.DELETED);
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

        return ItemResDto.builder()
                .itemId(item.getId())
                .itemCategoryId(item.getItemCategory().getId())
                .itemCategoryName(item.getItemCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .statusId(statusId)
                .statusCode(code)
                .statusName(name)
                // todo - 이미지 파일 추가
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
