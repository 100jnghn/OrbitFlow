package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.document.documentContentRender.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentRenderService
 * @since : 26. 1. 3. 토요일
 **/


@Service
@RequiredArgsConstructor
public class DocumentContentRenderService {

    private final List<FieldRenderer> renderers;
    private final RenderContextFactory renderContextFactory;


    /**
     * PDF / Detail 공용 문서 본문 HTML 렌더링
     */
    public String render(Long documentId, PdfContentSchema schema) {

        RenderContext context =
                renderContextFactory.create(documentId);

        return schema.getFields().stream()
                .sorted(Comparator.comparingInt(PdfField::getOrder))
                .map(field -> renderField(context, field))
                .collect(Collectors.joining("\n"));
    }

    private String renderField(RenderContext context, PdfField field) {

        return renderers.stream()
                .filter(r -> r.supports(field.getFieldType()))
                .findFirst()
                .map(r -> r.render(context, field))
                .orElseGet(() -> fallbackRender(field));
    }

    /**
     * 🔥 진짜 최후 fallback (어떤 Renderer도 없을 때)
     */
    private String fallbackRender(PdfField field) {

        String label = field.getLabel() != null ? field.getLabel() : "";
        Object value = field.getValue();

        String text = (value == null)
                ? "-"
                : value.toString();

        return """
                <div class="doc-field field-%s">
                    <div class="doc-field-label">%s</div>
                    <div class="doc-field-value">
                        <span>%s</span>
                    </div>
                </div>
                """.formatted(
                field.getFieldType(),
                escape(label),
                escape(text)
        );
    }

    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}