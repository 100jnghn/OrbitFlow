package com.finalproj.orbitflow.approval.logFormTemplateAI.prompt;

import static com.finalproj.orbitflow.approval.logFormTemplateAI.policy.FormInputConstraints.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormGlobalRulesPrompt
 * @since : 26. 1. 7. 수요일
 **/


public final class FormGlobalRulesPrompt implements PromptFragment {

    @Override
    public int order() {
        return 10;
    }


    @Override
    public String render() {
        return String.format("""
                        [Global Form Design Rules]
                        
                        1. AI의 역할은 문서 폼을 구성하는 컴포넌트를 조합하는 것이다.
                           실제 필드 ID 생성, 순서 보정, 기본 meta 값 설정은 서버가 담당한다.
                        
                        2. 결과는 제공된 컴포넌트 카탈로그에 정의된 컴포넌트만 사용한다.
                           새로운 컴포넌트 타입을 만들어서는 안 된다.
                        
                        3. 각 컴포넌트는 문서 작성자가 입력해야 할 '의미 단위'를 기준으로 선택한다.
                           동일한 의미를 중복해서 입력받는 컴포넌트를 생성하지 않는다.
                        
                        4. 특수(복합) 컴포넌트는 내부에 여러 입력 의미를 포함할 수 있다.
                           해당 의미가 이미 포함된 경우, 별도의 컴포넌트를 추가하지 않는다.
                        
                        5. event-date-range 컴포넌트는 문서 작성 시작 시 baseRole이 지정된 경우에만 사용할 수 있다.
                           baseRole이 없는 문서에서는 event-date-range 컴포넌트를 생성하지 않는다.
                        
                        6. 하나의 문서에는 event-date-range 컴포넌트를 최대 1개만 생성한다.
                           하나의 문서는 하나의 일정(도메인 이벤트)만을 생성한다.
                        
                        7. event-date-range 컴포넌트가 사용된 경우,
                           동일한 의미의 기간 또는 날짜를 입력받는
                           date, date-range, time, time-range 컴포넌트를 추가로 생성하지 않는다.
                        
                        
                        [Global Label Naming Rules]
                        
                        1. 라벨은 문서 작성자가 입력해야 할 내용을 나타내는 명사형 표현으로 작성한다.
                        2. 라벨에는 '입력', '작성', '선택'과 같은 UI 동작 단어를 포함하지 않는다.
                        3. 라벨만 보고 사용자가 무엇을 입력해야 하는지 바로 이해할 수 있어야 한다.
                        4. 모든 컴포넌트는 동일한 라벨 네이밍 규칙을 따른다.
                        
                        
                        [Column & Option Naming Rules]
                        
                        1. table의 각 column은 '컬럼명'이며, 컴포넌트 라벨과는 다른 개념이다.
                        2. radio, checkbox의 각 항목은 '옵션명'이다.
                        3. 컬럼명과 옵션명은 각각의 데이터 의미를 나타내는 명사형으로 작성한다.
                        4. '컬럼', '옵션', '선택'과 같은 단어를 포함하지 않는다.
                        
                        
                        [Global Input Constraints]
                        
                        1. 단일 텍스트 입력(text)은 최대 %d자까지 허용된다.
                        2. 여러 줄 텍스트 입력(textarea 및 복합 컴포넌트 내부 설명/사유)은 최대 %d자까지 허용된다.
                        3. notice 컴포넌트의 안내 문구(message)는 최대 %d자까지 허용된다.
                        4. table 컴포넌트의 text 컬럼은 최대 %d자까지 허용된다.
                        5. 하나의 입력 목적이 최대 길이를 초과할 것으로 예상되면 입력 항목을 분리한다.
                        
                        6. image 컴포넌트는 하나의 컴포넌트 인스턴스당 최대 %d개의 이미지만 포함할 수 있다.
                        7. 문서 본문에 포함되어야 할 이미지가 최대 개수를 초과하는 경우,
                           image 컴포넌트를 여러 개로 분리하여 구성한다.
                        8. 분리된 image 컴포넌트는 각각의 용도가 구분되도록 서로 다른 라벨을 사용한다.
                        
                        
                        [Global Required Rules]
                        
                        1. required=true로 설정된 컴포넌트는 해당 컴포넌트가 의미하는 입력 값이
                           최소한 하나 이상 입력되어야 문서 작성을 완료할 수 있다.
                        
                        2. 다음 컴포넌트들은 required 설정을 가지지 않는다.
                           - divider
                           - notice
                        
                        3. radio 컴포넌트에 required=true가 설정된 경우,
                           옵션 중 하나는 반드시 선택되어야 한다.
                        
                        4. radio 컴포넌트가 required=true이지만
                           사용자에게 해당되는 선택지가 없을 수 있는 경우,
                           '없음' 또는 '해당 없음'과 같은 옵션을 명시적으로 제공한다.
                        
                        5. table 컴포넌트에 required=true가 설정된 경우,
                           테이블 전체에 대해 최소 한 행 이상이 작성되어야 한다.
                        
                        6. table의 column에 required=true가 설정된 경우,
                           해당 컬럼은 행이 존재할 때 모든 행에서 값이 반드시 입력되어야 한다.
                        
                        7. 일정과 같은 복합 컴포넌트(event-date-range)는
                           내부에 포함된 모든 입력 요소가 기본적으로 required로 간주된다.
                        
                        8. checkbox 컴포넌트에 최소 선택 개수(minSelected)가 1 이상으로 설정된 경우,
                           일부 사용자에게 모든 옵션이 해당되지 않을 수 있다면
                           '없음' 또는 이에 준하는 옵션을 명시적으로 제공한다.
                        
                        """,
                TEXT_MAX_LENGTH,
                TEXTAREA_MAX_LENGTH,
                NOTICE_MESSAGE_MAX,
                TABLE_CELL_TEXT_MAX_LENGTH,
                IMAGE_COMPONENT_MAX
        );
    }
}