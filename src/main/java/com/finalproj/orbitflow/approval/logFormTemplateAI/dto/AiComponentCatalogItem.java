package com.finalproj.orbitflow.approval.logFormTemplateAI.dto;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiComponentCatalogItem
 * @since : 26. 1. 7. 수요일
 **/


public record AiComponentCatalogItem(
        String type,                    // FORM_COMPONENT_SCHEMAS key
        String name,                    // 사람이 읽는 이름
        String description,             // 한 줄 설명

        List<String> internalInputs,     // 내부에 포함된 입력 의미
        List<String> configurableInputs,// ui/meta로 제어 가능한 입력
        List<String> excludes,           // 중복 생성 금지 의미
        List<String> useWhen,            // 사용 조건 힌트
        List<String> rules               // 강제 규칙
) {
}
