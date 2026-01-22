package com.finalproj.orbitflow.approval.document.schema;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * PDF 렌더링에 사용되는 문서 본문 구조를 표현하는 스키마 객체.
 * <p>
 * 결재 문서의 내용(JSON)을 PDF로 변환하는 과정에서,
 * 실제 렌더링 대상이 되는 필드 목록(PdfField)을 담는다.
 * <p>
 * 이 클래스는 문서 내용을 표현하기 위한 데이터 전달용 객체이며,
 * 비즈니스 로직이나 상태 변경 책임은 가지지 않는다.
 * <p>
 * 주로 DocumentContentRenderService 및 PdfHtmlBuilder에서
 * 필드 단위 렌더링을 수행하기 위한 입력 값으로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfContentSchema
 * @since : 26. 1. 3. 토요일
 */


@Getter
@Builder
public class PdfContentSchema {

    private final List<PdfField> fields;

    public PdfContentSchema(List<PdfField> fields) {
        this.fields = fields;
    }
}