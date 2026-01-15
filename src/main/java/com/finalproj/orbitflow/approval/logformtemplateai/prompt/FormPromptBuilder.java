package com.finalproj.orbitflow.approval.logformtemplateai.prompt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormPromptBuilder
 * @since : 26. 1. 7. 수요일
 **/


public class FormPromptBuilder {

    private final List<PromptFragment> fragments = new ArrayList<>();

    public FormPromptBuilder base() {
        fragments.add(new SystemRolePrompt());
        fragments.add(new FormGlobalRulesPrompt());
        return this;
    }

    public FormPromptBuilder components() {
        fragments.add(new ComponentCatalogPrompt());
        return this;
    }

    public FormPromptBuilder responseFormat() {
        fragments.add(new FormAiResponseFormatPrompt());
        return this;
    }

    public FormPromptBuilder userRequest(String formName, String purpose, Boolean allowScheduleEvent) {
        fragments.add(new UserRequestPrompt(formName, purpose, allowScheduleEvent));
        return this;
    }


    public String build() {
        return fragments.stream()
                .sorted(Comparator.comparingInt(PromptFragment::order))
                .map(PromptFragment::render)
                .collect(Collectors.joining("\n\n"));
    }
}