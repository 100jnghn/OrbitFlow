package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.schema.PdfField;

/**
 * PDF 렌더링 과정에서 각 필드의 공통 HTML 구조를 생성하기 위한 추상 클래스.
 * <p>
 * 개별 필드 렌더러는 이 클래스를 상속하여
 * 값(value)에 대한 렌더링 로직만 구현하고,
 * 라벨, 기본 레이아웃, 공통 스타일 구조는 이 클래스에서 일관되게 처리한다.
 * <p>
 * 공통적으로 다음 책임을 가진다.
 * - 필드 라벨과 값을 감싸는 기본 HTML 구조 제공
 * - 값이 비어 있는 경우 "-"로 치환
 * - 라벨 및 텍스트 값에 대한 최소한의 HTML 이스케이프 처리
 * <p>
 * 이 클래스를 통해 PDF 필드 렌더링 결과의 형태를 통일하고,
 * 개별 필드 렌더러의 구현 복잡도를 낮춘다.
 *
 * @author : Choi MinHyeok
 * @filename : AbstractFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


public abstract class AbstractFieldRenderer {

    protected String wrapField(PdfField field, String valueHtml) {

        String label = field.getLabel() != null
                ? field.getLabel()
                : "";

        String safeValue =
                (valueHtml == null || valueHtml.isBlank())
                        ? "-"
                        : valueHtml;

        return """
                    <div class="doc-field field-%s">
                        <div class="doc-field-label">%s</div>
                        <div class="doc-field-value">
                            %s
                        </div>
                    </div>
                """.formatted(
                field.getFieldType(),
                escape(label),
                safeValue
        );
    }

    protected String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    protected String textOrDash(Object value) {
        if (value == null) return "-";

        String s = value.toString().trim();
        return s.isEmpty() ? "-" : escape(s);
    }
}
