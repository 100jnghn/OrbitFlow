package com.finalproj.orbitflow.resource.itemcategory.service;

import com.finalproj.orbitflow.global.exception.ConfirmRequiredException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.resource.item.repository.ItemRepository;
import com.finalproj.orbitflow.resource.itemcategory.dto.ItemCategoryDto;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import com.finalproj.orbitflow.resource.itemcategory.repository.ItemCategoryRepository;
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
 * @filename : ItemCategoryService
 * @since : 2025-12-17 오후 3:17 수요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemCategoryService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemRepository itemRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getItemCategories(Long companyId) {

        return itemCategoryRepository.findAllByCompany_Id(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemCategoryDto getItemCategory(Long itemCategoryId) {
        return convertToDto(itemCategoryRepository.findById(itemCategoryId).get());
    }

    @Transactional
    public void insertItemCategory(Long companyId, ItemCategoryDto itemCategoryDto) {
        Company company = companyRepository.findById(companyId).get();

        ItemCategory newItemCategory = ItemCategory.builder()
                .company(company)
                .name(itemCategoryDto.getName())
                .build();

        itemCategoryRepository.save(newItemCategory);
    }

    @Transactional
    public void updateItemCategory(Long itemCategoryId, ItemCategoryDto dto) {

        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("자원 카테고리를 찾을 수 없습니다"));

        itemCategory.update(
                dto.getName()
        );
    }

    @Transactional
    public void deleteItemCategory(Long itemCategoryId, boolean force) {

        // todo - 카테고리 하위 자원들이 삭제됩니다 confirm 예외 전송 후 확인이 오면 삭제 진행

        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        int size = itemRepository.countByItemCategory(itemCategory);
        log.info("해당 카테고리에 아이템 " + size + "개 있음");

        if (size > 0 && !force) {
            throw new ConfirmRequiredException("해당 카테고리의 " + size + "개의 자원이 삭제됩니다");
        }


        itemCategoryRepository.deleteById(itemCategoryId);
    }

    // 엔티티 -> dto
    private ItemCategoryDto convertToDto(ItemCategory itemCategory) {
        return ItemCategoryDto.builder()
                .id(itemCategory.getId())
                .companyId(itemCategory.getCompany().getId())
                .name(itemCategory.getName())
                .build();
    }


}
