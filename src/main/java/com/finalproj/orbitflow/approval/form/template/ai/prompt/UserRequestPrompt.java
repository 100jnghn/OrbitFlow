package com.finalproj.orbitflow.approval.form.template.ai.prompt;

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
    private final boolean allowScheduleEvent;

    public UserRequestPrompt(
            String formName,
            String purpose,
            boolean allowScheduleEvent
    ) {
        this.formName = formName;
        this.purpose = purpose;
        this.allowScheduleEvent = allowScheduleEvent;
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

        sb.append("문서 폼의 목적:\n");
        if (purpose != null && !purpose.isBlank()) {
            sb.append("- ").append(purpose).append("\n\n");
        } else {
            sb.append("- (제공되지 않음)\n\n");
        }

        sb.append("문서 작성 환경 정보:\n");
        sb.append("- 문서 작성 시 작성자와 작성일은 시스템에 의해 자동으로 기입된다.\n");
        sb.append("- 작성자 및 작성일에 대한 별도의 입력 컴포넌트는 생성하지 않는다.\n\n");

        sb.append("문서 유형 제약:\n");
        if (allowScheduleEvent) {
            sb.append("- 이 문서는 일정 기반 이벤트를 생성할 수 있다.\n\n");
        } else {
            sb.append("- 이 문서는 일정 기반 이벤트를 생성하지 않는다.\n\n");
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
