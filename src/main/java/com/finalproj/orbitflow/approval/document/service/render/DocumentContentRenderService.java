package com.finalproj.orbitflow.approval.document.service.render;

import com.finalproj.orbitflow.approval.document.render.context.RenderContext;
import com.finalproj.orbitflow.approval.document.render.context.RenderContextFactory;
import com.finalproj.orbitflow.approval.document.render.field.FieldRenderer;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.schema.PdfField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 본문을 HTML로 렌더링하는 서비스.
 * <p>
 * PdfContentSchema에 정의된 필드 목록을 순회하면서,
 * 각 필드 타입에 맞는 FieldRenderer를 찾아 HTML 조각으로 변환한다.
 * <p>
 * 렌더링 과정에서는 문서 ID 기반의 RenderContext를 생성해
 * 이미지 URL, 휴가 유형명 등 문맥 정보가 필요한 필드에서도
 * 동일한 방식으로 접근할 수 있도록 한다.
 * <p>
 * 특정 필드 타입에 대응하는 Renderer가 존재하지 않는 경우에는
 * 기본 fallback 렌더링을 통해 최소한의 출력은 보장한다.
 * <p>
 * PDF 생성과 문서 상세 조회 화면에서 공용으로 사용된다.
 *
 * @author Choi MinHyeok
 * @filename DocumentContentRenderService
 * @since 2026.01.03
 */


@Service
@RequiredArgsConstructor
public class DocumentContentRenderService {

    private final List<FieldRenderer> renderers;
    private final RenderContextFactory renderContextFactory;


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