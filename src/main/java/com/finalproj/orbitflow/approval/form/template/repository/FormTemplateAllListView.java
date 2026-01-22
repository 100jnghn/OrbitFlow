package com.finalproj.orbitflow.approval.form.template.repository;

import com.finalproj.orbitflow.approval.form.template.enums.AffectTag;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;

import java.time.Instant;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAllListView
 * @since : 25. 12. 18. 목요일
 **/


public interface FormTemplateAllListView {
    Long getFormTemplateId();
    String getFormTemplateGroupName();
    int getFormTemplateVersion();
    int getUseDocument();
    Instant getUpdatedAt();
    FormTemplateStatus getFormTemplateStatus();
    List<AffectTag> getAffectTags();
    TemplateCategoryCode getTemplateCategoryCode();
}
