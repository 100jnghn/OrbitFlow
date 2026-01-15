package com.finalproj.orbitflow.approval.logformtemplateai.pipeline.processor;

import com.finalproj.orbitflow.approval.logformtemplateai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignResContext;

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