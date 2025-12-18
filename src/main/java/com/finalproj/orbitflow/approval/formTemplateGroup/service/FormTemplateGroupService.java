package com.finalproj.orbitflow.approval.formTemplateGroup.service;

import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupCreateReqDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupDetailResDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupListResDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupListView;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupRepository;
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

    public List<FormTemplateGroupListResDto> getFormTemplateGroups(
            Long companyId,
            String keyword
    ) {
        String searchKeyword = (keyword == null) ? "" : keyword;

        List<FormTemplateGroupListView> views = formTemplateGroupRepository
                    .findByCompanyAndKeyword(companyId, searchKeyword);

        return views.stream()
                .map(v -> new FormTemplateGroupListResDto(v.getId(), v.getName()))
                .toList();
    }


    @Transactional
    public Long saveFormTemplateGroup(FormTemplateGroupCreateReqDto dto, Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회사를 찾을 수 없습니다. companyId = " + companyId
                )
        );

        FormTemplateGroup entity = dto.toEntity(company);
        formTemplateGroupRepository.save(entity);

        return entity.getId();
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
}
