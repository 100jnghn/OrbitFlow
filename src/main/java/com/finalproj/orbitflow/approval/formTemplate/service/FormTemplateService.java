package com.finalproj.orbitflow.approval.formTemplate.service;

import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateActiveListResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateAllListResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateDetailResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateAllListView;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateListView;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupRepository;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.approval.templateCategory.repository.TemplateCategoryRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

/**
 * 결재 양식(FormTemplate)의 생명주기 전반을 관리하는 서비스 클래스.
 * -
 * 주요 책임:
 * - 결재 양식 초안 생성 (버전 관리 포함)
 * - 결재 양식 구조(JSON), 카테고리, 영향 태그 수정
 * - 결재선 규칙 저장
 * - 결재 양식 최종 활성화(publish) 및 기존 ACTIVE 양식 비활성화
 * -
 * 설계 원칙:
 * - DRAFT 상태에서만 수정 가능
 * - publish는 단순 상태 변경이 아닌 비즈니스 행위(Command)
 * - ACTIVE 양식은 템플릿 그룹당 하나만 유지
 * - 모든 수정/활성화 시 회사 소속 검증 수행
 * -
 * 이 클래스는 CRUD 중심이 아닌
 * "결재 양식 도메인의 유스케이스 단위 서비스"를 지향한다.
 * *
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateService
 * @since : 25. 12. 17. 수요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateGroupRepository formTemplateGroupRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final ObjectMapper objectMapper;


    @Transactional
    public Long saveFormTemplate(
            Long templateGroupId,
            Long companyId,
            TemplateCategoryCode categoryCode
    ) {
        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        Optional<FormTemplate> existingDraft =
                formTemplateRepository.findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                        templateGroupId,
                        FormTemplateStatus.DRAFT
                );

        if (existingDraft.isPresent()) {
            return existingDraft.get().getId();
        }

        TemplateCategory category = templateCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new NotFoundException("양식 카테고리를 찾을 수 없습니다."));

        int baseVersion = formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        templateGroupId,
                        FormTemplateStatus.ACTIVE
                )
                .map(FormTemplate::getVersion)
                .orElse(1);

        String initialTemplateJson = buildInitialTemplateJson();
        String initialApprovalRuleJson = buildInitialApprovalRuleJson();

        return createDraftTemplate(
                group,
                baseVersion,
                category,
                initialTemplateJson,
                initialApprovalRuleJson,
                null
        );
    }


    private String buildInitialTemplateJson() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode fields = root.putArray("fields");

        ObjectNode titleField = objectMapper.createObjectNode();
        titleField.put("fieldId", "document-title");
        titleField.put("fieldType", "document-title");
        titleField.put("label", "문서 제목");
        titleField.put("required", true);
        titleField.put("order", 1);

        ObjectNode meta = titleField.putObject("meta");
        meta.put("placeholder", "문서 제목을 입력하세요.");
        meta.put("maxLength", 100);

        fields.add(titleField);

        return root.toString();
    }


    private String buildInitialApprovalRuleJson() {
        ArrayNode rootArray = objectMapper.createArrayNode();

        ObjectNode firstStep = objectMapper.createObjectNode();
        firstStep.put("step", 1);
        firstStep.putNull("organizationId");
        firstStep.putNull("positionCategoryId");

        rootArray.add(firstStep);

        return rootArray.toString();
    }


    @Transactional
    public Long reviseFormTemplateByTemplateGroup(Long templateGroupId, Long companyId) {

        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        Optional<FormTemplate> existingDraft =
                formTemplateRepository.findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                        templateGroupId,
                        FormTemplateStatus.DRAFT
                );

        if (existingDraft.isPresent()) {
            return existingDraft.get().getId();
        }

        FormTemplate active = formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        templateGroupId,
                        FormTemplateStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(
                        "ACTIVE 상태의 양식을 찾을 수 없습니다."
                ));

        int baseVersion = active.getVersion();

        return createDraftTemplate(
                group,
                baseVersion,
                active.getTemplateCategory(),
                active.getTemplateJson(),
                active.getApprovalRuleJson(),
                active.getAffectTags()
        );
    }


    private FormTemplateGroup findGroupAndCheckCompany(Long templateGroupId, Long companyId) {
        FormTemplateGroup group = formTemplateGroupRepository.findById(templateGroupId)
                .orElseThrow(() -> new NotFoundException(
                        "양식 그룹을 찾을 수 없습니다."
                ));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException(
                    "해당 회사의 양식 그룹이 아닙니다."
            );
        }
        return group;
    }

    private int calculateNextActiveVersion(Long templateGroupId) {
        return formTemplateRepository
                .findMaxVersionByTemplateGroupId(templateGroupId)
                .orElse(0) + 1;
    }


    private Long createDraftTemplate(
            FormTemplateGroup group,
            int version,
            TemplateCategory category,
            String templateJson,
            String approvalRuleJson,
            List<AffectTag> affectTags
    ) {
        FormTemplate template = FormTemplate.builder()
                .company(group.getCompany())
                .templateGroup(group)
                .version(version)
                .templateCategory(category)
                .status(FormTemplateStatus.DRAFT)
                .templateJson(templateJson)
                .approvalRuleJson(approvalRuleJson)
                .affectTags(affectTags == null ? List.of() : affectTags)
                .build();

        formTemplateRepository.save(template);
        return template.getId();
    }


    @Transactional
    public void updateStructure(Long formTemplateId, Long companyId, FormTemplateUpdateReqDto reqDto) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, formTemplate);

        if (reqDto.getCategoryCode() != null) {
            TemplateCategory category = templateCategoryRepository
                    .findByCode(reqDto.getCategoryCode())
                    .orElseThrow(() -> new NotFoundException(
                            "양식 카테고리를 찾을 수 없습니다."
                    ));

            formTemplate.changeCategory(category);
        }

        if (reqDto.getTemplateJson() != null) {

            formTemplate.updateTemplateJson(objectMapper.writeValueAsString(reqDto.getTemplateJson()));
        }

        if (reqDto.getAffectTags() != null) {
            formTemplate.updateAffectTags(reqDto.getAffectTags());
        }
    }

    @Transactional
    public void updateApprovalRule(Long formTemplateId, Long companyId, FormTemplateUpdateReqDto reqDto) {

        FormTemplate formTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, formTemplate);

        if (reqDto.getApprovalRuleJson() != null) {
            formTemplate.updateApprovalRuleJson(objectMapper.writeValueAsString(reqDto.getApprovalRuleJson()));
        }
    }

    @Transactional
    public void publishFormTemplate(Long formTemplateId, Long companyId) {

        FormTemplate draft = findFormTemplate(formTemplateId);
        checkCompany(companyId, draft);

        if (draft.getStatus() != FormTemplateStatus.DRAFT) {
            throw new InvalidRequestException(
                    "DRAFT 상태의 결재 양식만 활성화할 수 있습니다."
            );
        }

        int nextVersion = calculateNextActiveVersion(
                draft.getTemplateGroup().getId()
        );


        formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        draft.getTemplateGroup().getId(),
                        FormTemplateStatus.ACTIVE
                )
                .ifPresent(active -> {
                    active.updateStatus(FormTemplateStatus.INACTIVE);
                    formTemplateRepository.flush(); // 🔥 핵심
                });

        draft.updateVersion(nextVersion);
        draft.updateStatus(FormTemplateStatus.ACTIVE);
    }


    private FormTemplate findFormTemplate(Long formTemplateId) {
        return formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() -> new NotFoundException(
                        "결재 양식을 찾을 수 없습니다."
                ));
    }


    private static void checkCompany(Long companyId, FormTemplate formTemplate) {
        if (!formTemplate.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("사용자의 소속(회사) 와 문서 양식의 주인(회사)가 일치하지 않습니다.");
        }
    }

    public List<FormTemplateActiveListResDto> getActiveFormTemplates(Long companyId, String keyword) {
        String searchKeyword = (keyword == null) ? "" : keyword;


        List<FormTemplateListView> views = formTemplateRepository.findWithActiveTemplateAndCompanyAndKeyword(companyId, searchKeyword);
        return views.stream()
                .map(v -> new FormTemplateActiveListResDto(v.getId(), v.getVersion(), v.getGroupId(), v.getName()))
                .toList();
    }

    public FormTemplateDetailResDto getDetailFormTemplate(Long formTemplateId, Long companyId) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, formTemplate);

        return FormTemplateDetailResDto.from(formTemplate, objectMapper);
    }

    public Page<FormTemplateAllListResDto> allFormTemplate(Long companyId, int size, int offset, String keyword, FormTemplateStatus status) {
        Pageable pageable = PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "updatedAt"));


        Page<FormTemplateAllListView> page = formTemplateRepository.findAllWithDocumentCount(companyId, keyword, status, pageable);


        return page.map(FormTemplateAllListResDto::from);
    }

}
