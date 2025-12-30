package com.finalproj.orbitflow.approval.documentContent.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentContent
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.dto.DocumentFormFieldDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "document_content")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "content_json", nullable = false, columnDefinition = "json")
    private String contentJson;

    public void updateContentJson(String updateJson) {
        this.contentJson = updateJson;
    }

    public static DocumentContent fromSchema(
            Document document,
            FormTemplateSchema schema,
            ObjectMapper objectMapper
    ) {
        List<DocumentFormFieldDto> fields = schema.getFields().stream()
                .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                .map(DocumentFormFieldDto::from)
                .toList();

        Map<String, Object> content = Map.of("fields", fields);

        try {
            return DocumentContent.builder()
                    .document(document)
                    .contentJson(objectMapper.writeValueAsString(content))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 JSON 생성 실패", e);
        }
    }
}
