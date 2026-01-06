package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiClient
 * @since : 26. 1. 5. 월요일
 **/


public interface AiClient {
    String summarize(String prompt);
    String diff(String prompt);
}
