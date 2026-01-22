package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;

/**
 * PDF 렌더링 과정에서 개별 필드 타입을 HTML로 변환하는 렌더러의 공통 인터페이스.
 * <p>
 * 각 구현체는 하나 이상의 fieldType을 지원하며,
 * 주어진 PdfField를 기반으로 PDF에 삽입될 HTML 문자열을 생성한다.
 * <p>
 * FieldRenderer는 실제 데이터 조회나 비즈니스 로직을 처리하지 않고,
 * RenderContext를 통해 전달받은 정보만을 사용해
 * 순수하게 "표현(Rendering)" 책임만을 가진다.
 * <p>
 * 새로운 필드 타입이 추가될 경우,
 * 이 인터페이스를 구현한 렌더러를 추가하는 방식으로
 * 기존 코드 수정 없이 확장이 가능하도록 설계되었다.
 *
 * @author : Choi MinHyeok
 * @filename : FieldRenderer
 * @since : 26. 1. 3. 토요일
 */


public interface FieldRenderer {

    boolean supports(String fieldType);

    String render(RenderContext context, PdfField field);
}