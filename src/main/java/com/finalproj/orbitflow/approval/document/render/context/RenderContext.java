package com.finalproj.orbitflow.approval.document.render.context;



import com.finalproj.orbitflow.approval.document.render.support.VacationTypeNameResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PDF 렌더링 과정에서 FieldRenderer들이 공통으로 사용하는 컨텍스트 객체.
 * <p>
 * Renderer는 외부 리소스(DB, 코드 테이블, 이미지 등)에 직접 접근하지 않고,
 * 필요한 정보는 이 컨텍스트를 통해서만 조회하도록 구성되어 있다.
 * <p>
 * 문서 ID, 이미지 URL 생성 방식,
 * 그리고 도메인 값(예: 휴가 타입)을 표시용 이름으로 변환하는 역할을 제공한다.
 *
 * @author : Choi MinHyeok
 * @filename : RenderContext
 * @since : 26. 1. 4. 일요일
 */


@Getter
@RequiredArgsConstructor
public class RenderContext {

    private final Long documentId;

    private final ImageUrlProvider imageUrlProvider;

    /**
     * 휴가 타입 이름 resolver
     * (ex: vacationTypeId -> "연차")
     */
    private final VacationTypeNameResolver vacationTypeNameResolver;

    /**
     * 이미지 URL 생성
     */
    public String resolveImageUrl(Long documentFileId) {
        return imageUrlProvider.generate(documentId, documentFileId);
    }


    public String resolveVacationTypeName(String vacationTypeId) {
        if (vacationTypeId == null || vacationTypeId.isBlank()) {
            return "-";
        }
        return vacationTypeNameResolver.resolve(vacationTypeId);
    }
}