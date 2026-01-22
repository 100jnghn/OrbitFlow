package com.finalproj.orbitflow.approval.form.template.ai.prompt;

import com.finalproj.orbitflow.approval.form.template.ai.catalog.AiComponentCatalog;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiComponentCatalogItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI에게 전달되는 프롬프트 중,
 * 사용 가능한 입력 컴포넌트 목록을 설명하기 위한 프래그먼트이다.
 * <p>
 * AI가 문서 폼을 설계할 때 임의의 컴포넌트를 만들어내지 않도록,
 * 시스템에 미리 정의된 컴포넌트 카탈로그를
 * 사람이 읽을 수 있는 설명 형태로 변환해 프롬프트에 포함시킨다.
 * <p>
 * 이 프롬프트는 "어떤 컴포넌트가 존재하는지"뿐만 아니라
 * "언제 사용해야 하는지", "어떤 규칙을 따라야 하는지"까지 함께 전달하여,
 * AI가 의미적으로 올바른 선택을 하도록 유도하는 역할을 한다.
 * <p>
 * 실제 컴포넌트 정의는 AiComponentCatalog에서 관리되며,
 * 이 클래스는 해당 정의를 프롬프트 문자열로 렌더링하는 책임만 가진다.
 * <p>
 * order 값은 전체 프롬프트 구성 흐름에서
 * 컴포넌트 카탈로그가 어느 시점에 포함될지를 결정하기 위해 사용된다.
 *
 * @author Choi MinHyeok
 * @filename ComponentCatalogPrompt
 * @since 2026. 1. 7.
 */


public class ComponentCatalogPrompt implements PromptFragment {

    @Override
    public int order() {
        return 20;
    }

    @Override
    public String render() {
        return """
                [Component Catalog]
                
                아래는 문서 폼을 구성할 때 사용할 수 있는 컴포넌트 목록이다.
                AI는 반드시 아래에 정의된 컴포넌트 중에서만 선택해야 한다.
                
                """ + renderComponents(AiComponentCatalog.COMPONENTS);
    }

    private String renderComponents(List<AiComponentCatalogItem> components) {
        return components.stream()
                .map(this::renderComponent)
                .collect(Collectors.joining("\n\n"));
    }

    private String renderComponent(AiComponentCatalogItem c) {
        StringBuilder sb = new StringBuilder();

        sb.append("- Component Type: ").append(c.type()).append("\n");
        sb.append("  Name: ").append(c.name()).append("\n");
        sb.append("  Description: ").append(c.description()).append("\n");

        if (!c.internalInputs().isEmpty()) {
            sb.append("  Internal Inputs:\n");
            c.internalInputs().forEach(i ->
                    sb.append("    - ").append(i).append("\n")
            );
        }

        if (!c.useWhen().isEmpty()) {
            sb.append("  Use When:\n");
            c.useWhen().forEach(u ->
                    sb.append("    - ").append(u).append("\n")
            );
        }

        if (!c.rules().isEmpty()) {
            sb.append("  Rules:\n");
            c.rules().forEach(r ->
                    sb.append("    - ").append(r).append("\n")
            );
        }

        return sb.toString();
    }
}