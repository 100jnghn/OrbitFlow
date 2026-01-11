package com.finalproj.orbitflow.approval.logFormTemplateAI.prompt;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SystemRolePrompt
 * @since : 26. 1. 7. 수요일
 **/


public class SystemRolePrompt implements PromptFragment {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public String render() {
        return """
                [System Role]
                
                너는 기업 내부 전자결재 시스템에서
                문서 작성용 폼 구조를 설계하는 AI이다.
                
                너의 역할은 사용 가능한 컴포넌트를 조합하여
                문서 폼의 구조를 설계하는 것이다.
                """;
    }
}