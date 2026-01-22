package com.finalproj.orbitflow.approval.document.service.assembler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.document.dto.DocumentFormFieldDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.form.template.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 결재 문서의 본문(DocumentContent)을 생성하기 위한 조립 책임을 가지는 Assembler.
 * <p>
 * 이 클래스는 결재 양식(FormTemplateSchema)을 기반으로,
 * 실제 문서에 저장될 본문 JSON 구조를 생성하는 역할을 담당한다.
 * <p>
 * 양식 스키마에 정의된 필드 목록을 문서용 DTO로 변환하고,
 * 필드 순서를 보장한 뒤 JSON 형태로 직렬화하여
 * DocumentContent 엔티티로 감싸 반환한다.
 * <p>
 * 이 과정에서 비즈니스 판단이나 검증 로직은 포함하지 않으며,
 * "양식 스키마 → 문서 본문 JSON" 변환이라는 단일 책임에만 집중한다.
 * <p>
 * 문서 초안 생성이나 재기안과 같은 상위 유즈케이스에서는
 * 이 Assembler를 통해 일관된 구조의 문서 본문을 생성하도록 한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentAssembler
 * @since : 26. 1. 21. 수요일
 */


@Component
@RequiredArgsConstructor
public class DocumentContentAssembler {

    private final ObjectMapper objectMapper;

    public DocumentContent fromSchema(
            Document document,
            FormTemplateSchema schema
    ) {
        try {
            List<DocumentFormFieldDto> fields = schema.getFields().stream()
                    .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                    .map(DocumentFormFieldDto::from)
                    .toList();

            Map<String, Object> content = Map.of("fields", fields);

            String json = objectMapper.writeValueAsString(content);

            return DocumentContent.create(document, json);

        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 JSON 생성 실패", e);
        }
    }
}
