package com.finalproj.orbitflow.approval.line.enums;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RuleTargetType
 * @since : 25. 12. 25. 목요일
 **/


public enum RuleTargetType {
    ORG_CATEGORY_CHAIN,     // 1단계: 조직 카테고리 기반 상향 결재선
    ORG_AND_POSITION,       // 조직 + 직책
    FIXED_EMPLOYEE          // 사원 지정
}
