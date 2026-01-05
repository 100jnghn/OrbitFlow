package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AddressFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class AddressFieldRenderer extends HtmlRenderUtil
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
