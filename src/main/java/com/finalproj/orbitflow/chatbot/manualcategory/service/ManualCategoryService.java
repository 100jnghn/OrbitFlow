package com.finalproj.orbitflow.chatbot.manualcategory.service;

import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualcategory.dto.ManualCategoryReqDto;
import com.finalproj.orbitflow.chatbot.manualcategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualcategory.repository.ManualCategoryRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author : rlagkdus
 * @filename : ManualCategoryService
 * @since : 2025. 12. 30. 화요일
 */

@Service
@RequiredArgsConstructor
public class ManualCategoryService {

    private final ManualCategoryRepository categoryRepository;
    private final ManualRepository manualRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public void createCategory(Long companyId, ManualCategoryReqDto dto) {
        ManualCategory category = ManualCategory.builder()
                .company(companyRepository.getReferenceById(companyId))
                .categoryName(dto.getCategoryName())
                .description(dto.getDescription())
                .sortOrder(dto.getSortOrder())
                .isActive(true)
                .build();
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long companyId, Long categoryId, ManualCategoryReqDto dto) {
        ManualCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new InvalidRequestException("권한이 없습니다.");
        }

         category.update(dto.getCategoryName(), dto.getDescription(), dto.getSortOrder());
    }

    @Transactional
    public void deleteCategory(Long companyId, Long categoryId) {
        ManualCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));

        if (manualRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(companyId, categoryId)) {
            throw new InvalidRequestException("해당 카테고리에 사용 중인 매뉴얼이 있어 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
    }
}