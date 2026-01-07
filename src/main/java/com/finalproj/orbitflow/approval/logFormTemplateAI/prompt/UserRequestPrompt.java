package com.finalproj.orbitflow.approval.logFormTemplateAI.prompt;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : UserRequestPrompt
 * @since : 26. 1. 7. 수요일
 **/



public class UserRequestPrompt implements PromptFragment {

    private final String formName;
    private final String purpose; // nullable

    public UserRequestPrompt(String formName, String purpose) {
        this.formName = formName;
        this.purpose = purpose;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();

        sb.append("[User Request]\n\n");
        sb.append("문서 폼 이름:\n");
        sb.append("- ").append(formName).append("\n\n");

        if (purpose != null && !purpose.isBlank()) {
            sb.append("문서 폼의 목적:\n");
            sb.append("- ").append(purpose).append("\n\n");
        } else {
            sb.append("문서 폼의 목적:\n");
            sb.append("- (제공되지 않음)\n\n");
        }

        sb.append("""
                위 정보를 바탕으로 문서 작성자가 입력해야 할 항목을 분석하여
                적절한 컴포넌트 조합과 순서를 설계하라.
                
                문서 폼의 목적은 참고 정보이며,
                문서 폼 이름과 충돌하는 해석을 하지 않는다.
                """);

        return sb.toString();
    }
}
