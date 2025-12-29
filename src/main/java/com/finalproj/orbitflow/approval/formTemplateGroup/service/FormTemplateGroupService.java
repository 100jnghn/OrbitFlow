package com.finalproj.orbitflow.approval.formTemplateGroup.service;

import com.finalproj.orbitflow.approval.formTemplateGroup.dto.*;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupRepository;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.approval.templateCategory.repository.TemplateCategoryRepository;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupService
 * @since : 25. 12. 16. 화요일
 **/


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormTemplateGroupService {
    private final FormTemplateGroupRepository formTemplateGroupRepository;
    private final CompanyRepository companyRepository;
    private final TemplateCategoryRepository templateCategoryRepository;

    public List<FormTemplateGroupListResDto> getFormTemplateGroups(
            Long companyId,
            String keyword
    ) {
        String searchKeyword = (keyword == null) ? "" : keyword;

        List<FormTemplateGroup> list = formTemplateGroupRepository
                    .findByCompanyAndKeyword(companyId, searchKeyword);


        return list.stream().map(FormTemplateGroupListResDto::from).toList();
    }


    @Transactional
    public FormTemplateGroupCreateResDto saveFormTemplateGroup(
            FormTemplateGroupCreateReqDto dto,
            Long companyId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회사를 찾을 수 없습니다. companyId = " + companyId
                ));

        // ✅ 기본 값 검증
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "문서 양식 그룹 이름은 필수입니다."
            );
        }

        TemplateCategory category = templateCategoryRepository
                .findByCode(dto.getCategoryCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "유효하지 않은 카테고리입니다."
                ));

        validateCategoryAndBaseRole(
                category.getCode(),
                dto.getBaseRole()
        );

        // ✅ 중복 체크
        if (formTemplateGroupRepository.existsByCompanyIdAndName(
                companyId, dto.getName()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 존재하는 문서 양식 그룹 이름입니다."
            );
        }

        FormTemplateGroup entity = dto.toEntity(company, category);
        formTemplateGroupRepository.save(entity);

        return new FormTemplateGroupCreateResDto(entity.getId());
    }


    public FormTemplateGroupDetailResDto getDetailTemplateGroup(Long id) {
        FormTemplateGroup target = formTemplateGroupRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 그룹을 찾을 수 없습니다. formTemplateGroupId = " + id
                )
        );

        return FormTemplateGroupDetailResDto.from(target);
    }

    @Transactional
    public void updateFormTemplateGroup(FormTemplateGroupUpdateReqDto dto, Long id) {
        FormTemplateGroup target = formTemplateGroupRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 그룹을 찾을 수 없습니다. formTemplateGroupId = " + id
                )
        );

        if (dto.getName() != null) {
            target.changeName(dto.getName());
        }
        if (dto.getDescription() != null) {
            target.changeDescription(dto.getDescription());
        }
        if (dto.getActive() != null) {
            if (dto.getActive()) target.activate();
            else target.deactivate();
        }
    }


    private void validateCategoryAndBaseRole(
            TemplateCategoryCode categoryCode,
            BaseRole baseRole
    ) {
        if (categoryCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "카테고리는 필수입니다."
            );
        }

        switch (categoryCode) {
            case GENERAL -> {
                if (baseRole != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "GENERAL 카테고리는 일정 유형을 가질 수 없습니다."
                    );
                }
            }
            case SCHEDULE -> {
                if (baseRole != BaseRole.COMPANY_EVENT) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "SCHEDULE 카테고리는 회사 일정만 가능합니다."
                    );
                }
            }
            case ATTENDANCE -> {
                if (baseRole == null || baseRole == BaseRole.COMPANY_EVENT) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "ATTENDANCE 카테고리는 휴가/출장/외근 중 하나여야 합니다."
                    );
                }
            }
        }
    }

}
