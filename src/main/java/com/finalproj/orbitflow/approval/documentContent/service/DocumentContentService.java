package com.finalproj.orbitflow.approval.documentContent.service;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.documentContent.dto.DocumentContentPatchReqDto;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentService
 * @since : 25. 12. 26. 금요일
 **/

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentContentService {
    private final DocumentContentRepository documentContentRepository;
    private final ObjectMapper objectMapper;

    public FormTemplateSchema getDocumentContentByDocumentId(Long documentId) {

        DocumentContent byDocumentId = documentContentRepository.findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException("Document with id: " + documentId + " not found"));


        FormTemplateSchema schema = parseSchema(byDocumentId.getContentJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        return schema;
    }


    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }

    @Transactional
    public void patchContent(Long documentId, DocumentContentPatchReqDto reqDto) {
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException("문서 내용을 찾을 수 없습니다."));

        Document document = content.getDocument();

        if(document.getStatus() != DocumentStatus.DRAFT) {
            throw new ForbiddenException("결재 진행 중인 문서는 수정할 수 없습니다.");
        }

        // 기존 JSON 파싱
        ObjectNode root =
                (ObjectNode) objectMapper.readTree(content.getContentJson());

        ArrayNode fieldsNode = (ArrayNode) root.get("fields");

        if (fieldsNode == null) {
            throw new IllegalStateException("문서 필드 정보가 없습니다.");
        }

        // 요청 값 Map으로 변환
        Map<String, JsonNode> patchMap = reqDto.getFields().stream()
                .filter(f -> f.getFieldId() != null)
                .filter(f -> f.getValue() != null)
                .collect(Collectors.toMap(
                        DocumentContentPatchReqDto.FieldValueDto::getFieldId,
                        DocumentContentPatchReqDto.FieldValueDto::getValue
                ));

        String newTitle = null;

        // 기존 fields 순회하며 value만 덮어쓰기
        for (JsonNode fieldNode : fieldsNode) {
            String fieldId = fieldNode.get("fieldId").asText();

            if (!patchMap.containsKey(fieldId)) continue;

            JsonNode newValue = patchMap.get(fieldId);

            ((ObjectNode) fieldNode).set("value", newValue);

            if ("document-title".equals(fieldId)) {
                if (newValue.isString()) {
                    newTitle = newValue.asString().trim();
                }
            }
        }

        if (newTitle != null && !newTitle.isBlank()) {
            document.updateTitle(newTitle);
        }

        // 다시 JSON 저장
        try {
            content.updateContentJson(
                    objectMapper.writeValueAsString(root)
            );
        } catch (Exception e) {
            throw new IllegalStateException("문서 JSON 직렬화 실패", e);
        }
    }
}
