package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 주소(address) 타입 필드를 PDF용 HTML로 렌더링하는 필드 렌더러.
 * <p>
 * 주소 필드는 하나의 값이 아닌 복합 구조(Map)를 가지며,
 * 우편번호, 도로명 주소, 상세 주소를 각각 분리하여 테이블 형태로 출력한다.
 * <p>
 * 값이 정상적인 주소 구조가 아닌 경우에는
 * 주소 전체 영역을 "-"로 표시하여 빈 값임을 명확히 드러낸다.
 * <p>
 * 이 렌더러는 AbstractFieldRenderer를 상속하여
 * 공통적인 텍스트 처리 및 HTML 이스케이프 로직을 재사용하며,
 * FieldRenderer 인터페이스를 통해 필드 타입 매칭과 렌더링 책임을 분리한다.
 *
 * @author : Choi MinHyeok
 * @filename : AddressFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class AddressFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "address".equals(fieldType);
    }

    @Override
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (!(value instanceof Map<?, ?> rawMap)) {
            return renderEmpty(field);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) rawMap;

        String postcode = textOrDash(map.get("postcode"));
        String roadAddress = textOrDash(map.get("roadAddress"));
        String detailAddress = textOrDash(map.get("detailAddress"));

        return """
                <div class="doc-field field-address">
                    <table class="address-table">
                        <tbody>
                            <tr>
                                <th class="address-main-label" rowspan="3">%s</th>
                                <th class="address-sub-label">우편번호</th>
                                <td class="address-value">%s</td>
                            </tr>
                            <tr>
                                <th class="address-sub-label">도로명 주소</th>
                                <td class="address-value">%s</td>
                            </tr>
                            <tr>
                                <th class="address-sub-label">상세 주소</th>
                                <td class="address-value">%s</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                escape(field.getLabel()),
                escape(postcode),
                escape(roadAddress),
                escape(detailAddress)
        );
    }

    private String renderEmpty(PdfField field) {
        return """
                <div class="doc-field field-address">
                    <table class="address-table">
                        <tbody>
                            <tr>
                                <th class="address-main-label">%s</th>
                                <td class="address-value" colspan="2">-</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(escape(field.getLabel()));
    }
}
