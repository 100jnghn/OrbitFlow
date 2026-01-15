package com.finalproj.orbitflow.approval.logformtemplateai.dto;

import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormDesignReqContext
 * @since : 26. 1. 8. 목요일
 **/


public record FormDesignReqContext(
        Long formTemplateId,
        String formName,
        String purpose,
        boolean allowScheduleEvent,
        BaseRole baseRole) {
}