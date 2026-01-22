package com.finalproj.orbitflow.approval.document.schema;

import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 결재 문서의 JSON 기반 내용(DocumentContent)을
 * 도메인에서 사용 가능한 구조화된 Payload로 변환하는 파서 클래스.
 * <p>
 * 문서 내용은 공통적으로 JSON 형태로 저장되며,
 * 이 클래스는 그 중 특정 필드 타입(event-date-range 등)을 기준으로
 * 필요한 값만 추출하여 도메인 DTO로 매핑하는 역할을 담당한다.
 * <p>
 * 주 사용 목적은 다음과 같다.
 * - 결재 승인 후 근태, 휴가, 회사 일정 등 후처리 로직에서
 * 문서 내용을 도메인 객체 형태로 안전하게 해석
 * - 렌더링이나 비즈니스 서비스에서
 * JSON 구조를 직접 다루지 않도록 책임 분리
 * <p>
 * 이 클래스는 문서 스키마 전체를 해석하지 않고,
 * “필요한 필드만 선택적으로 추출”하는 경량 파서로 설계되었다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentParser
 * @since : 25. 12. 31. 수요일
 */


@Component
@RequiredArgsConstructor
public class DocumentContentParser {

    private final ObjectMapper objectMapper;


    public CommonPayload extractCommon(DocumentContent content) {

        DocumentField eventField = findFirstByType(content, "event-date-range")
                .orElseThrow(() ->
                        new IllegalStateException("일정 필드를 찾을 수 없습니다.")
                );

        Map<String, Object> value = eventField.value();

        return new CommonPayload(
                (String) value.get("title"),
                (String) value.get("description"),
                LocalDate.parse((String) value.get("start")),
                LocalDate.parse((String) value.get("end"))
        );
    }


    public VacationPayload extractVacation(DocumentContent content) {

        DocumentField eventField = findFirstByType(content, "event-date-range")
                .orElseThrow(() ->
                        new IllegalStateException("회사 일정 필드를 찾을 수 없습니다.")
                );

        Map<String, Object> value = eventField.value();

        return new VacationPayload(
                value.get("vacationTypeId") == null
                        ? null
                        : Long.valueOf(value.get("vacationTypeId").toString()),
                value.get("reason").toString(),
                LocalDate.parse(value.get("start").toString()),
                LocalDate.parse(value.get("end").toString())
        );
    }


    public Optional<DocumentField> findFirstByType(
            DocumentContent content,
            String fieldType
    ) {
        try {
            Map<String, Object> root =
                    objectMapper.readValue(
                            content.getContentJson(),
                            new TypeReference<>() {
                            }
                    );

            List<Map<String, Object>> fields =
                    (List<Map<String, Object>>) root.get("fields");

            if (fields == null) {
                return Optional.empty();
            }

            return fields.stream()
                    .filter(f -> fieldType.equals(f.get("fieldType")))
                    .findFirst()
                    .map(f -> new DocumentField(
                            (String) f.get("fieldId"),
                            (String) f.get("fieldType"),
                            (String) f.get("label"),
                            (Map<String, Object>) f.get("value")
                    ));

        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 파싱 실패", e);
        }
    }
}
