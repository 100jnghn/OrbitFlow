package com.finalproj.orbitflow.approval.formTemplate.service;

import com.finalproj.orbitflow.approval.formTemplate.dto.*;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupRepository;
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

    /* =====================================================
     * Dependencies
     * ===================================================== */

    private final SampleDataGenerator sampleDataGenerator;
    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateGroupRepository formTemplateGroupRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final ObjectMapper objectMapper;

    /* =====================================================
     * Business Logic
     * ===================================================== */

    @Transactional
    public Long saveFormTemplate(
            Long templateGroupId,
            Long companyId
    ) {
        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        if (!group.getActive()) {
            throw new InvalidRequestException(
                    "비활성화된 양식 그룹에서는 결재 양식을 생성할 수 없습니다."
            );
        }

        Optional<FormTemplate> existingDraft =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                                templateGroupId,
                                FormTemplateStatus.DRAFT
                        );

        if (existingDraft.isPresent()) {
            return existingDraft.get().getId();
        }

        int baseVersion = formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        templateGroupId,
                        FormTemplateStatus.ACTIVE
                )
                .map(FormTemplate::getVersion)
                .orElse(1);

        List<AffectTag> affectTags = buildAffectTags(group);

        return createDraftTemplate(
                group,
                baseVersion,
                buildInitialTemplateJson(),
                buildInitialApprovalRuleJson(),
                affectTags
        );
    }


    @Transactional
    public Long reviseFormTemplateByTemplateGroup(Long groupId, Long companyId) {
        FormTemplateGroup group = findGroupAndCheckCompany(groupId, companyId);

        if (!group.getActive()) {
            throw new InvalidRequestException(
                    "비활성화된 양식 그룹에서는 결재 양식을 수정할 수 없습니다."
            );
        }

        // 1. 기존 DRAFT 우선
        Optional<FormTemplate> draft =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                                groupId, FormTemplateStatus.DRAFT
                        );

        if (draft.isPresent()) {
            return draft.get().getId();
        }

        // 2. ACTIVE 우선
        Optional<FormTemplate> active =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                                groupId, FormTemplateStatus.ACTIVE
                        );

        FormTemplate base;

        // 3. ACTIVE 없으면 가장 최신 INACTIVE
        base = active.orElseGet(() -> formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        groupId, FormTemplateStatus.INACTIVE
                )
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "복제할 기준 양식이 없습니다."
                        )
                ));

        return createDraftTemplate(
                group,
                base.getVersion(),   // version은 publish 시 다시 재계산
                base.getTemplateJson(),
                base.getApprovalRuleJson(),
                base.getAffectTags()
        );
    }


    @Transactional
    public void updateStructure(
            Long formTemplateId,
            Long companyId,
            FormTemplateUpdateReqDto reqDto
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);
        checkStatusIsDraft(formTemplate);


        try {
            if (reqDto.getTemplateJson() != null) {
                formTemplate.updateTemplateJson(
                        objectMapper.writeValueAsString(reqDto.getTemplateJson())
                );
            }
        } catch (Exception e) {
            throw new InvalidRequestException("templateJson 형식이 올바르지 않습니다.");
        }
    }

    @Transactional
    public void updateApprovalRule(
            Long formTemplateId,
            Long companyId,
            FormTemplateUpdateReqDto reqDto
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);
        checkStatusIsDraft(formTemplate);


        if (reqDto.getApprovalRuleJson() != null) {
            formTemplate.updateApprovalRuleJson(
                    objectMapper.writeValueAsString(reqDto.getApprovalRuleJson())
            );
        }
    }


    @Transactional
    public void publishFormTemplate(Long formTemplateId, Long companyId) {
        FormTemplate draft = findFormTemplate(formTemplateId);
        checkCompany(companyId, draft);

        checkStatusIsDraft(draft);

        int nextVersion =
                calculateNextActiveVersion(draft.getTemplateGroup().getId());

        log.info("nextVersion = {}", nextVersion);

        formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        draft.getTemplateGroup().getId(),
                        FormTemplateStatus.ACTIVE
                )
                .ifPresent(active -> {
                    active.updateStatus(FormTemplateStatus.INACTIVE);
                    formTemplateRepository.flush();
                });

        draft.updateVersion(nextVersion);
        draft.updateStatus(FormTemplateStatus.ACTIVE);
    }

    private static void checkStatusIsDraft(FormTemplate draft) {
        if (draft.getStatus() != FormTemplateStatus.DRAFT) {
            throw new InvalidRequestException(
                    "DRAFT 상태의 결재 양식만 수정 또는 활성화할 수 있습니다."
            );
        }

        if (!draft.getTemplateGroup().getActive()) {
            throw new InvalidRequestException(
                    "활성 상태의 양식 그룹만 수정 또는 활성화할 수 있습니다."
            );
        }
    }


    public List<FormTemplateActiveListResDto> getActiveFormTemplates(
            Long companyId,
            String keyword
    ) {
        String searchKeyword = keyword == null ? "" : keyword;

        return formTemplateRepository
                .findWithActiveTemplateAndCompanyAndKeyword(companyId, searchKeyword)
                .stream()
                .map(v ->
                        new FormTemplateActiveListResDto(
                                v.getId(),
                                v.getVersion(),
                                v.getGroupId(),
                                v.getName()
                        )
                )
                .toList();
    }

    public FormTemplateDetailResDto getDetailFormTemplate(
            Long formTemplateId,
            Long companyId
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);

        return FormTemplateDetailResDto.from(formTemplate, objectMapper);
    }

    public Page<FormTemplateAllListResDto> allFormTemplate(
            Long companyId,
            int size,
            int offset,
            FormTemplateAllListReqDto reqDto
    ) {
        Pageable pageable =
                PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "updatedAt"));


        if (reqDto.getKeyword() == null) {
            reqDto.setKeyword("");
        }

        return formTemplateRepository
                .findAllWithDocumentCount(companyId, reqDto.getKeyword(), reqDto.getStatus(), reqDto.getTemplateCategoryCode(), pageable)
                .map(FormTemplateAllListResDto::from);
    }


    public FormTemplatePreviewResDto getPreviewFormTemplate(
            Long companyId,
            Long formTemplateId
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);

        FormTemplateSchema contentSchema =
                parseSchema(formTemplate.getTemplateJson());

        sampleDataGenerator.fillValues(contentSchema);

        FormTemplateMetaDto meta =
                FormTemplateMetaDto.from(formTemplate);

        return FormTemplatePreviewResDto.from(meta, contentSchema);
    }



    /* =====================================================
     * Private helpers
     * ===================================================== */

    private List<AffectTag> buildAffectTags(FormTemplateGroup group) {
        return switch (group.getTemplateCategory().getCode()) {
            case GENERAL -> List.of();
            case SCHEDULE -> List.of(AffectTag.SCHEDULE);
            case ATTENDANCE -> List.of(AffectTag.ATTENDANCE);
        };
    }


    private FormTemplate findFormTemplate(Long formTemplateId) {
        return formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() ->
                        new NotFoundException("결재 양식을 찾을 수 없습니다.")
                );
    }

    private FormTemplateGroup findGroupAndCheckCompany(
            Long templateGroupId,
            Long companyId
    ) {
        FormTemplateGroup group = formTemplateGroupRepository.findById(templateGroupId)
                .orElseThrow(() ->
                        new NotFoundException("양식 그룹을 찾을 수 없습니다.")
                );

        if (!group.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 양식 그룹이 아닙니다.");
        }
        return group;
    }

    private static void checkCompany(Long companyId, FormTemplate formTemplate) {
        if (!formTemplate.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException(
                    "사용자의 소속(회사)와 문서 양식의 회사가 일치하지 않습니다."
            );
        }
    }

    private int calculateNextActiveVersion(Long templateGroupId) {
        return formTemplateRepository
                .findMaxVersionByTemplateGroupId(templateGroupId)
                .orElse(0) + 1;
    }

    private Long createDraftTemplate(
            FormTemplateGroup group,
            int version,
            String templateJson,
            String approvalRuleJson,
            List<AffectTag> affectTags
    ) {
        FormTemplate template = FormTemplate.builder()
                .company(group.getCompany())
                .templateGroup(group)
                .version(version)
                .status(FormTemplateStatus.DRAFT)
                .templateJson(templateJson)
                .approvalRuleJson(approvalRuleJson)
                .affectTags(affectTags == null ? List.of() : affectTags)
                .build();

        formTemplateRepository.save(template);
        return template.getId();
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

    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }
}
