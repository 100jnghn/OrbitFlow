package com.finalproj.orbitflow.approval.document.render.html;

import org.springframework.stereotype.Component;

/**
 * PDF 및 HTML 렌더링 과정에서 사용할 문자열 이스케이프 유틸리티.
 * <p>
 * 렌더링 대상 값에 포함될 수 있는 특수 문자를
 * HTML 엔티티로 치환하여,
 * 마크업 깨짐이나 의도치 않은 HTML 해석을 방지한다.
 * <p>
 * null 값이 전달되는 경우에는 "-"를 반환하여
 * 렌더링 결과가 비어 보이지 않도록 처리한다.
 * <p>
 * 상태를 가지지 않는 순수 유틸리티 성격의 클래스이므로
 * 인스턴스 생성을 막고 정적 메서드만 제공한다.
 *
 * @author : Choi MinHyeok
 * @filename : HtmlEscaper
 * @since : 26. 1. 3. 토요일
 */


@Component
public final class HtmlEscaper {

    public static String escape(Object value) {
        if (value == null) return "-";

        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
