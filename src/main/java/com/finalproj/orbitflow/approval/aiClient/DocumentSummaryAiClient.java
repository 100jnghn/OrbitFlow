package com.finalproj.orbitflow.approval.aiClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : OpenAiClient
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
public class DocumentSummaryAiClient {

    private final OpenAiChatGateway gateway;

    private static final String SUMMARY_SYSTEM_PROMPT =
            "너는 사내 결재 문서를 간결하고 명확하게 요약하는 AI다.";

    private static final String DIFF_SYSTEM_PROMPT =
            "너는 사내 결재 문서의 변경 사항을 비교하여 요약하는 AI다.";

    public String summarize(String prompt) {
        return gateway.complete(
                SUMMARY_SYSTEM_PROMPT,
                prompt,
                0.3
        );
    }

    public String diff(String prompt) {
        return gateway.complete(
                DIFF_SYSTEM_PROMPT,
                prompt,
                0.2
        );
    }
}
