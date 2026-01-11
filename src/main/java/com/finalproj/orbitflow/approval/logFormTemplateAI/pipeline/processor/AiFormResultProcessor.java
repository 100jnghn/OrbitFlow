package com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline.processor;

import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormDesignResContext;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiFormResultProcessor
 * @since : 26. 1. 8. 목요일
 **/


public interface AiFormResultProcessor {
    AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    );
}