package com.finalproj.orbitflow.hr.organization.repository;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : OrgResView
 * @since : 25. 12. 23. 화요일
 **/


public interface OrgResView {
    Long getId();

    Long getCategoryId();

    Long getParentOrgId();

    String getName();

    Integer getOrderIndex();

    Integer getIsActive(); //
}
