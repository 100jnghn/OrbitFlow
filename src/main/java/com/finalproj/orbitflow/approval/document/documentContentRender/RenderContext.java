package com.finalproj.orbitflow.approval.document.documentContentRender;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RenderContext
 * @since : 26. 1. 4. 일요일
 **/


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PDF 렌더링 시 FieldRenderer에 공통으로 전달되는 컨텍스트
 * <p>
 * - 문서 ID
 * - 이미지 접근 전략
 * - 외부 리소스 URL 생성 책임
 */

/**
 * RenderContext
 * <p>
 * PDF 렌더링 시 FieldRenderer에 공통으로 전달되는 컨텍스트.
 * Renderer는 외부 자원(DB, 코드 테이블, 이미지 등)에 직접 접근하지 않고
 * 반드시 이 Context를 통해 필요한 정보를 resolve 한다.
 * <p>
 * 책임:
 * - 문서 ID 제공
 * - 이미지 URL 생성
 * - 코드/도메인 값 → 표시 이름 변환
 *
 * @author : Choi MinHyeok
 * @since : 2026. 1. 4.
 */
@Getter
@RequiredArgsConstructor
public class RenderContext {

    /**
     * 문서 ID
     * - 이미지 URL 생성 시 필요
     */
    private final Long documentId;

    /**
     * 이미지 URL 제공자
     */
    private final ImageUrlProvider imageUrlProvider;

    /**
     * 휴가 타입 이름 resolver
     * (ex: vacationTypeId -> "연차")
     */
    private final VacationTypeNameResolver vacationTypeNameResolver;

    /* =========================================================
       Image
    ========================================================= */

    /**
     * 이미지 URL 생성
     */
    public String resolveImageUrl(Long documentFileId) {
        return imageUrlProvider.generate(documentId, documentFileId);
    }

    /* =========================================================
       Domain resolve
    ========================================================= */

    /**
     * 휴가 타입 ID를 표시용 이름으로 변환
     *
     * @param vacationTypeId 휴가 타입 ID
     * @return 표시용 휴가 타입 이름, 없으면 "-"
     */
    public String resolveVacationTypeName(String vacationTypeId) {
        if (vacationTypeId == null || vacationTypeId.isBlank()) {
            return "-";
        }
        return vacationTypeNameResolver.resolve(vacationTypeId);
    }
}