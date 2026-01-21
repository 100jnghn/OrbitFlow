package com.finalproj.orbitflow.approval.document.render.field;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 이미지 필드를 PDF용 HTML로 렌더링하는 FieldRenderer 구현체.
 * <p>
 * 문서에 첨부된 이미지 컴포넌트(image field)를 대상으로 하며,
 * 각 이미지 항목에 포함된 documentFileId를 기준으로
 * RenderContext를 통해 접근 가능한 이미지 URL을 생성한다.
 * <p>
 * 이 렌더러는 이미지의 실제 저장 위치나 접근 방식에 대해 알지 않으며,
 * 이미지 URL 생성 책임은 ImageUrlProvider에 위임한다.
 * 이를 통해 렌더링 로직과 파일 접근 정책을 분리하고,
 * PDF / 미리보기 등 다양한 출력 환경에서도 동일한 구조를 재사용할 수 있도록 설계되었다.
 * <p>
 * 값이 없거나 이미지 목록이 비어 있는 경우에는
 * 일반 필드와 동일하게 "-" 형태로 출력한다.
 *
 * @author : Choi MinHyeok
 * @filename : ImageFieldRenderer
 * @since : 26. 1. 4. 일요일
 */


@Component
public class ImageFieldRenderer extends AbstractFieldRenderer
        implements FieldRenderer {

    @Override
    public boolean supports(String fieldType) {
        return "image".equals(fieldType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String render(RenderContext context, PdfField field) {

        Object value = field.getValue();
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return wrapField(field, "-");
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"image-field readonly\">");

        for (Object o : list) {
            if (!(o instanceof Map<?, ?> map)) continue;

            // 🔥 핵심 수정: documentFileId 사용
            Object documentFileIdObj = map.get("documentFileId");
            if (documentFileIdObj == null) continue;

            Long documentFileId = Long.valueOf(documentFileIdObj.toString());

            String imageUrl =
                    context.resolveImageUrl(documentFileId);


            html.append("""
                    <div class="image-row readonly">
                        <img src="%s" alt="첨부 이미지"/>
                    </div>
                    """.formatted(escape(imageUrl)));
        }

        html.append("</div>");

        return wrapField(field, html.toString());
    }
}