package com.finalproj.orbitflow.approval.form.template.ai.service.client;

import com.finalproj.orbitflow.approval.ai.gateway.OpenAiChatGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormDesignAiClient
 * @since : 26. 1. 7. 수요일
 **/


@Component
@RequiredArgsConstructor
public class FormDesignAiClient {

    private final OpenAiChatGateway gateway;

    private static final String FORM_SYSTEM_PROMPT =
            "You are a strict JSON form schema designer. "
                    + "You must follow all rules and output valid JSON only.";

    public String completeFormDesign(String prompt) {
        return gateway.complete(
                FORM_SYSTEM_PROMPT,
                prompt,
                0.1
        );
    }
}
