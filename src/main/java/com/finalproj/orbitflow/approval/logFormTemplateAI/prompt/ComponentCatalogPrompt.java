package com.finalproj.orbitflow.approval.logFormTemplateAI.prompt;

import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiComponentCatalogItem;
import com.finalproj.orbitflow.approval.logFormTemplateAI.catalog.AiComponentCatalog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ComponentCatalogPrompt
 * @since : 26. 1. 7. 수요일
 **/


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