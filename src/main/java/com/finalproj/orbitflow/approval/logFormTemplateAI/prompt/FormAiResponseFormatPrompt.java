package com.finalproj.orbitflow.approval.logFormTemplateAI.prompt;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormAiResponseFormatPrompt
 * @since : 26. 1. 7. 수요일
 **/


public final class FormAiResponseFormatPrompt implements PromptFragment {

    @Override
    public int order() {
        return 30;
    }


    @Override
    public String render() {
        return """
                [AI Response Format Rules]
                
                AI는 문서 폼 설계 결과를 반드시 JSON 형식으로 반환해야 한다.
                반환되는 JSON은 아래 구조를 정확히 따른다.
                
                
                [Top-Level Structure]
                
                {
                  "components": [
                    {
                      "type": "type",
                      "label": "label name",
                      "required": true,
                      "meta": {}
                    }
                  ]
                }
                
                
                [Common Component Rules]
                
                1. components 배열의 각 요소는 하나의 컴포넌트 설계 결과를 의미한다.
                2. components 배열의 순서는 문서 화면에 표시될 순서를 의미한다.
                3. 모든 컴포넌트는 다음 필드를 반드시 포함해야 한다.
                   - type: 컴포넌트 타입 (카탈로그에 정의된 type 값)
                   - label: 사용자에게 표시될 라벨
                   - required: 필수 입력 여부
                   - meta: 컴포넌트별 추가 설정 객체
                4. fieldId, order, value, 기본 meta 값은 AI가 생성하지 않는다.
                   해당 값들은 서버에서 자동으로 생성 및 보정된다.
                5. meta에 설정할 항목이 없는 경우에도 meta 객체는 반드시 포함한다.
                
                
                [Meta Rules]
                
                1. meta 객체는 컴포넌트 타입에 따라 필요한 정보만 포함한다.
                2. meta에 포함되지 않은 기본 설정 값은 서버에서 자동으로 보완된다.
                3. meta는 자유 형식 객체가 아니며,
                   카탈로그 및 글로벌 규칙에서 허용한 구조만 사용한다.
                
                
                [Radio & Checkbox Components]
                
                1. radio 및 checkbox 컴포넌트의 meta에는 options를 포함할 수 있다.
                2. options는 문자열 배열로 반환한다.
                
                   예시:
                   {
                     "meta": {
                       "options": ["옵션 A", "옵션 B", "해당 없음"]
                     }
                   }
                
                3. checkbox 컴포넌트는 필요 시 minSelected, maxSelected를 meta에 포함할 수 있다.
                
                
                [Table Component Rules]
                
                table 컴포넌트는 meta 구조가 고정되어 있으며,
                다음 형식을 반드시 따른다.
                
                {
                  "type": "table",
                  "label": "테이블 라벨",
                  "required": true,
                  "meta": {
                    "rowPolicy": {
                      "min": 1,
                      "max": 5
                    },
                    "columns": [
                      {
                        "name": "컬럼명",
                        "type": "text | number | currency",
                        "required": true
                      }
                    ]
                  }
                }
                
                1. columns 배열은 하나의 행이 가지는 컬럼 구조를 정의한다.
                2. 각 컬럼의 type은 text, number, currency 중 하나만 사용할 수 있다.
                3. 컬럼명(name)은 사용자에게 표시될 컬럼 이름이다.
                
                
                [Event Component Rules]
                
                event-date-range 컴포넌트는 별도의 meta 정보를 반환하지 않는다.
                해당 컴포넌트의 동작 및 연동 방식은
                서버에서 문서 컨텍스트에 따라 자동으로 처리된다.
                
                
                [Strict Prohibitions]
                
                1. AI는 value 데이터를 생성하지 않는다.
                2. AI는 fieldId, order, 기본 meta 설정을 생성하지 않는다.
                3. AI는 카탈로그에 정의되지 않은 필드를 추가하지 않는다.
                4. AI는 설명 텍스트나 주석을 JSON 외부에 포함하지 않는다.
                
                AI의 응답은 오직 위 JSON 구조만을 포함해야 한다.
                """;
    }

}