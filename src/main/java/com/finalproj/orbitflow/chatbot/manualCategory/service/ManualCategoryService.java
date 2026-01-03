package com.finalproj.orbitflow.chatbot.manualCategory.service;

import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.dto.ManualCategoryReqDto;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. 해당 카테고리에 등록된 매뉴얼이 있는지 확인
        if (!manualRepository.findByCompanyIdAndCategoryIdOrderByIdDesc(companyId, categoryId).isEmpty()) {
            throw new InvalidRequestException("해당 카테고리에 등록된 매뉴얼이 있어 삭제할 수 없습니다. 매뉴얼을 먼저 삭제해주세요.");
        }

        categoryRepository.delete(category);
    }
}