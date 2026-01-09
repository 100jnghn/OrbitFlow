package com.finalproj.orbitflow.approval.logFormTemplateAi.pipeline.processor;

import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.FormDesignResContext;

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