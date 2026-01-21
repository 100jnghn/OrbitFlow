package com.finalproj.orbitflow.approval.form.template.ai.prompt;

/**
 * 실제 사용자 요청 내용을 AI 프롬프트에 전달하기 위한
 * 프롬프트 프래그먼트이다.
 * <p>
 * 이 프래그먼트는 앞서 정의된 시스템 역할과 전역 규칙을 바탕으로,
 * AI가 어떤 문서 폼을, 어떤 의도로 설계해야 하는지를
 * 구체적으로 알려주는 역할을 한다.
 * <p>
 * 문서 폼 이름과 목적 정보는
 * AI가 전체적인 폼 구성 방향을 잡는 참고 정보로 사용되며,
 * 시스템에서 자동으로 처리되는 항목(작성자, 작성일 등)은
 * 명시적으로 제외하도록 안내한다.
 * <p>
 * 또한 해당 문서가 일정 기반 이벤트를 생성할 수 있는지 여부를 함께 전달하여,
 * event-date-range 컴포넌트 사용 가능 여부를
 * AI가 사전에 인지하도록 한다.
 * <p>
 * 이 프래그먼트는 프롬프트의 마지막 단계에 위치하며,
 * AI가 최종적으로 “어떤 입력 항목을 구성해야 하는지”를
 * 판단하는 기준점 역할을 한다.
 *
 * @author Choi MinHyeok
 * @filename UserRequestPrompt
 * @since 2026. 1. 7.
 */


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
