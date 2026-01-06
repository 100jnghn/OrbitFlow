package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PromptComponent
 * @since : 26. 1. 6. 화요일
 **/


public interface PromptComponent<T> {
    void append(StringBuilder sb, T context);
}
