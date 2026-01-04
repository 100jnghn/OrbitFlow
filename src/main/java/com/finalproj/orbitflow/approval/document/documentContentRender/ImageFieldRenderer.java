package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ImageFieldRenderer
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class ImageFieldRenderer extends HtmlRenderUtil
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