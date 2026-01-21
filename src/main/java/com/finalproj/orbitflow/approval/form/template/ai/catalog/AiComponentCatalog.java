package com.finalproj.orbitflow.approval.form.template.ai.catalog;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiComponentCatalogItem;

import java.util.List;

/**
 * AI가 결재 양식을 자동으로 생성할 때 참고하는
 * 입력 컴포넌트 정의 모음 클래스이다.
 * <p>
 * 양식 생성 과정에서 AI가 임의로 컴포넌트를 만들어내는 것을 방지하고,
 * 미리 정의된 컴포넌트 중에서만 의미에 맞는 항목을 선택하도록 하기 위해
 * 이 카탈로그를 분리하여 관리한다.
 * <p>
 * 각 컴포넌트는 type, 설명, 내부 입력 구조, 사용 권장 상황,
 * 그리고 중복 생성이나 의미 충돌을 막기 위한 규칙 정보를 함께 가진다.
 * <p>
 * 이 클래스는 실행 로직을 가지지 않으며,
 * 모든 컴포넌트는 static 상수로 정의되어
 * AI 프롬프트 생성 및 양식 구조 판단 시 참조 용도로만 사용된다.
 * <p>
 * 새로운 입력 컴포넌트를 추가하거나
 * 기존 컴포넌트의 사용 정책이 변경되는 경우에는
 * 반드시 이 파일에서 함께 관리하도록 한다.
 *
 * @author Choi MinHyeok
 * @filename AiComponentCatalog
 * @since 2026. 1. 7.
 */


public class AiComponentCatalog {


    public static final AiComponentCatalogItem TEXT =
            new AiComponentCatalogItem(
                    "text",
                    "텍스트 입력",
                    "짧은 자유 텍스트를 입력받는 단일 입력 컴포넌트",

                    List.of("텍스트"),

                    List.of("텍스트"),

                    List.of(),

                    List.of(
                            "단순한 문자열 입력이 필요할 때",
                            "다른 복합 컴포넌트가 해당 의미를 포함하지 않을 때"
                    ),

                    List.of(
                            "복합 컴포넌트가 동일한 의미를 내부에 포함하는 경우 이 컴포넌트를 추가하지 않는다"
                    )
            );


    public static final AiComponentCatalogItem TEXTAREA =
            new AiComponentCatalogItem(
                    "textarea",
                    "여러 줄 입력",
                    "여러 줄의 자유 텍스트를 입력받는 컴포넌트",

                    List.of("여러 줄 텍스트"),

                    List.of("여러 줄 텍스트"),

                    List.of(),

                    List.of(
                            "상세 설명, 사유, 비고 등 여러 줄 입력이 필요할 때",
                            "하나의 목적에 대한 설명을 입력받아야 할 때",
                            "다른 복합 컴포넌트가 해당 의미를 포함하지 않을 때"
                    ),

                    List.of(
                            "하나의 textarea는 하나의 목적에 대한 설명만 담당한다",
                            "입력 목적이 여러 개인 경우 textarea를 분리한다",
                            "복합 컴포넌트가 설명 또는 사유를 내부 입력으로 포함하는 경우 textarea를 추가하지 않는다"
                    )
            );
    public static final AiComponentCatalogItem NUMBER =
            new AiComponentCatalogItem(
                    "number",
                    "숫자 입력",
                    "하나의 숫자 값을 입력받는 컴포넌트",

                    List.of(
                            "숫자"
                    ),

                    List.of(
                            "숫자"
                    ),

                    List.of(),

                    List.of(
                            "수량, 횟수, 인원 수 등 숫자 값이 필요한 경우",
                            "하나의 의미를 가지는 단일 수치를 입력받아야 할 때"
                    ),

                    List.of(
                            "금액을 입력받는 경우에는 currency 컴포넌트를 사용한다",
                            "기간이나 날짜를 의미하는 경우에는 date 또는 date-range 컴포넌트를 사용한다",
                            "의미가 다른 여러 숫자를 하나의 number 컴포넌트로 묶지 않는다"
                    )
            );
    public static final AiComponentCatalogItem DIVIDER =
            new AiComponentCatalogItem(
                    "divider",
                    "구분선",
                    "문서 작성 및 조회 화면에서 영역을 시각적으로 구분하기 위한 레이아웃 전용 컴포넌트",

                    List.of(),          // 내부 입력 없음
                    List.of(),          // 설정 가능한 입력 없음
                    List.of(),          // 중복 개념 없음

                    List.of(
                            "서로 다른 성격의 입력 그룹을 시각적으로 구분하고 싶을 때",
                            "문서의 가독성을 높이기 위해 섹션을 나누고 싶을 때"
                    ),

                    List.of(
                            "divider는 입력을 받지 않는 컴포넌트이므로 value를 생성하지 않는다",
                            "divider는 라벨을 생성하지 않으며 화면에서는 구분선(hr)으로 표시된다",
                            "의미적 입력 구분이 아닌 단순한 레이아웃 및 가독성 향상 용도로만 사용한다",
                            "연속으로 여러 개의 divider를 생성하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem TIME =
            new AiComponentCatalogItem(
                    "time",
                    "시간",
                    "하나의 시각을 입력받는 시간 입력 컴포넌트",

                    List.of(
                            "시간"
                    ),

                    List.of(
                            "시간"
                    ),

                    List.of(),

                    List.of(
                            "특정 시각을 입력받아야 할 때",
                            "하나의 시간 값만 의미를 가지는 경우"
                    ),

                    List.of(
                            "시작 시간과 종료 시간이 함께 필요한 경우 time-range 컴포넌트를 사용한다",
                            "시간의 범위나 지속 시간을 표현하기 위해 time 컴포넌트를 여러 개 사용하지 않는다",
                            "날짜와 시간이 함께 필요한 경우 date 또는 date-range 컴포넌트와 조합하여 사용한다"
                    )
            );

    public static final AiComponentCatalogItem TIME_RANGE =
            new AiComponentCatalogItem(
                    "time-range",
                    "시간 범위",
                    "시작 시간과 종료 시간을 함께 입력받는 시간 범위 컴포넌트",

                    List.of(
                            "시작 시간",
                            "종료 시간"
                    ),

                    List.of(
                            "시작 시간",
                            "종료 시간"
                    ),

                    List.of(),

                    List.of(
                            "하루 안에서의 시작 시간과 종료 시간을 함께 입력받아야 할 때",
                            "단일 시각이 아닌 시간 범위를 의미해야 할 때"
                    ),

                    List.of(
                            "시간 범위를 표현하기 위해 time 컴포넌트를 여러 개 사용하지 않는다",
                            "날짜 또는 날짜 범위를 표현하는 용도로 사용하지 않는다",
                            "일정이나 근무 기간을 표현하는 경우 event-date-range 컴포넌트를 사용한다"
                    )
            );

    public static final AiComponentCatalogItem DATE =
            new AiComponentCatalogItem(
                    "date",
                    "날짜",
                    "하나의 날짜를 입력받는 단일 날짜 입력 컴포넌트",

                    List.of(
                            "날짜"
                    ),

                    List.of(
                            "날짜"
                    ),

                    List.of(),

                    List.of(
                            "특정 하루를 지정해야 할 때",
                            "단일 날짜만 의미를 가지는 경우"
                    ),

                    List.of(
                            "시작일과 종료일이 함께 필요한 경우 date-range 컴포넌트를 사용한다",
                            "일정, 휴가, 출장 등 기간을 표현하는 경우 event-date-range 컴포넌트를 사용한다",
                            "날짜 범위를 표현하기 위해 date 컴포넌트를 여러 개 사용하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem DATE_RANGE =
            new AiComponentCatalogItem(
                    "date-range",
                    "날짜 범위",
                    "시작 날짜와 종료 날짜를 함께 입력받는 날짜 범위 컴포넌트",

                    List.of(
                            "시작 날짜",
                            "종료 날짜"
                    ),

                    List.of(
                            "시작 날짜",
                            "종료 날짜"
                    ),

                    List.of(),

                    List.of(
                            "기간을 기준으로 데이터를 입력해야 할 때",
                            "단일 날짜가 아닌 날짜 범위를 의미해야 할 때"
                    ),

                    List.of(
                            "날짜 범위를 표현하기 위해 date 컴포넌트를 여러 개 사용하지 않는다",
                            "캘린더 또는 근태 이벤트를 생성하는 경우 event-date-range 컴포넌트를 사용한다"
                    )
            );


    public static final AiComponentCatalogItem RADIO =
            new AiComponentCatalogItem(
                    "radio",
                    "라디오 버튼",
                    "여러 선택지 중 하나를 선택하는 단일 선택 컴포넌트",

                    List.of(
                            "선택 값"
                    ),

                    List.of(
                            "선택 값"
                    ),

                    List.of(),

                    List.of(
                            "서로 배타적인 선택지 중 하나를 선택해야 할 때",
                            "정해진 옵션 중 하나만 의미를 가질 때"
                    ),

                    List.of(
                            "radio는 단일 선택 컴포넌트이므로 여러 값을 선택할 수 없다",
                            "선택지는 사전에 정의된 옵션으로만 제공한다",
                            "선택지가 명확하게 정의될 수 있는 경우에만 사용한다"
                    )
            );

    public static final AiComponentCatalogItem CHECKBOX =
            new AiComponentCatalogItem(
                    "checkbox",
                    "체크 박스",
                    "여러 선택지 중 하나 이상을 동시에 선택할 수 있는 복수 선택 컴포넌트",

                    List.of(
                            "선택 값 목록"
                    ),

                    List.of(
                            "선택 값 목록"
                    ),

                    List.of(),

                    List.of(
                            "여러 항목을 동시에 선택할 수 있어야 할 때",
                            "각 항목이 독립적인 의미를 가질 때"
                    ),

                    List.of(
                            "최소 또는 최대 선택 개수 제한이 필요한 경우 minSelected, maxSelected를 함께 설정한다",
                            "선택 개수 제한이 없는 경우 최소 선택 개수는 0, 최대 선택 개수는 옵션 개수로 간주한다"
                    )
            );

    public static final AiComponentCatalogItem NOTICE =
            new AiComponentCatalogItem(
                    "notice",
                    "안내 문구",
                    "문서 작성 시 작성자에게 필요한 안내 정보를 표시하기 위한 컴포넌트",

                    List.of(),

                    List.of(),

                    List.of(),

                    List.of(
                            "특정 입력 컴포넌트에 대한 작성 가이드를 제공해야 할 때",
                            "문서 작성 시 주의사항이나 보충 설명이 필요할 때"
                    ),

                    List.of(
                            "notice는 입력을 받지 않으며 value를 생성하지 않는다",
                            "notice는 문서 작성 화면에만 표시되며 작성 완료된 문서에는 포함되지 않는다",
                            "기본적으로 안내 대상이 되는 컴포넌트의 바로 위 또는 아래에 배치한다",
                            "문서 전체에 대한 안내인 경우 문서의 최상단 또는 최하단에 배치하며 라벨을 사용해 안내 성격을 명확히 한다"
                    )
            );


    public static final AiComponentCatalogItem IMAGE =
            new AiComponentCatalogItem(
                    "image",
                    "이미지",
                    "문서 본문에 포함되어 출력되는 이미지를 첨부하기 위한 컴포넌트",

                    List.of(
                            "이미지 파일 목록"
                    ),

                    List.of(
                            "이미지 파일 목록"
                    ),

                    List.of(),

                    List.of(
                            "문서 내용의 일부로 이미지가 함께 출력되어야 할 때",
                            "설명이나 증빙을 위해 문서 본문에 이미지를 포함해야 할 때"
                    ),

                    List.of(
                            "image 컴포넌트는 문서 출력 시 실제로 표시되는 이미지에만 사용한다",
                            "선택적으로 참고만 하는 자료는 image가 아닌 첨부파일로 처리한다",
                            "이미지 업로드 개수 제한이 필요한 경우 maxCount를 설정한다",
                            "이미지만으로 문서의 핵심 의미를 구성하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem CURRENCY =
            new AiComponentCatalogItem(
                    "currency",
                    "통화 입력",
                    "한국 원화(KRW) 금액을 입력받기 위한 통화 전용 컴포넌트",

                    List.of(
                            "금액"
                    ),

                    List.of(
                            "금액"
                    ),

                    List.of(),

                    List.of(
                            "비용, 금액 등 통화 단위의 값을 입력받아야 할 때",
                            "숫자 자체가 아닌 금전적 의미를 가지는 값을 입력해야 할 때"
                    ),

                    List.of(
                            "통화 단위는 한국 원화(KRW)로 고정되어 있다",
                            "금액 입력에는 number 컴포넌트를 사용하지 않는다",
                            "다국가 통화 또는 환율 계산이 필요한 경우에는 사용하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem ADDRESS =
            new AiComponentCatalogItem(
                    "address",
                    "주소 입력",
                    "외부 주소 검색 API를 통해 실제 주소를 검색하고 입력하는 복합 주소 입력 컴포넌트",

                    List.of(
                            "우편번호",
                            "도로명 주소",
                            "상세 주소"
                    ),

                    List.of(
                            "주소"
                    ),

                    List.of(),

                    List.of(
                            "실제 주소 정보를 정확하게 입력받아야 할 때",
                            "외부 주소 검색을 통해 표준화된 주소가 필요한 경우"
                    ),

                    List.of(
                            "address 컴포넌트는 외부 주소 검색 API를 사용하여 주소를 입력한다",
                            "우편번호와 도로명 주소는 검색 결과에 따라 자동으로 입력된다",
                            "문서 작성자는 상세 주소만 직접 입력한다",
                            "주소의 내부 구성 요소를 개별 컴포넌트로 분리하지 않는다",
                            "자유 텍스트 입력으로 주소를 대체하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem EMPLOYEE_SEARCH =
            new AiComponentCatalogItem(
                    "employee-search",
                    "사원 검색",
                    "현재 사용자의 소속 회사에 속한 사원을 검색하여 선택하는 컴포넌트",

                    List.of(
                            "사원"
                    ),

                    List.of(
                            "사원"
                    ),

                    List.of(),

                    List.of(
                            "특정 사원 한 명을 지정해야 할 때",
                            "문서와 연관된 담당자 또는 대상 사원을 선택해야 할 때"
                    ),

                    List.of(
                            "사원 검색은 현재 사용자의 소속 회사 기준으로만 수행된다",
                            "비활성화되었거나 존재하지 않는 사원은 선택할 수 없다",
                            "단일 사원만 선택할 수 있으며 다중 선택은 지원하지 않는다",
                            "자유 입력으로 사원을 지정하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem DEPARTMENT_SEARCH =
            new AiComponentCatalogItem(
                    "department-search",
                    "조직 검색",
                    "현재 사용자의 소속 회사에 속한 조직을 검색하여 선택하는 컴포넌트",

                    List.of(
                            "조직"
                    ),

                    List.of(
                            "조직"
                    ),

                    List.of(),

                    List.of(
                            "특정 조직 또는 부서를 지정해야 할 때",
                            "문서와 연관된 담당 조직을 선택해야 할 때"
                    ),

                    List.of(
                            "조직 검색은 현재 사용자의 소속 회사 기준으로만 수행된다",
                            "비활성화되었거나 존재하지 않는 조직은 선택할 수 없다",
                            "단일 조직만 선택할 수 있으며 다중 선택은 지원하지 않는다",
                            "자유 입력으로 조직을 지정하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem TABLE =
            new AiComponentCatalogItem(
                    "table",
                    "테이블",
                    "동일한 구조의 데이터 행을 여러 개 입력받기 위한 반복 입력 컴포넌트",

                    List.of(
                            "행 목록"
                    ),

                    List.of(
                            "행 목록"
                    ),

                    List.of(),

                    List.of(
                            "동일한 형식의 데이터를 여러 개 입력받아야 할 때",
                            "반복되는 항목을 구조적으로 정리하여 입력해야 할 때"
                    ),

                    List.of(
                            "table은 하나의 의미 단위를 가지는 반복 데이터에만 사용한다",
                            "서로 다른 의미의 데이터를 하나의 table에 섞지 않는다",
                            "각 컬럼은 text, number, currency 중 하나의 입력 형식을 가진다",
                            "currency 컬럼은 통화 입력 컴포넌트와 동일하게 한국 원화(KRW)만 입력 가능하다",
                            "각 컬럼은 해당 입력 컴포넌트의 정책을 그대로 따른다",
                            "반복 구조가 아닌 경우 table 컴포넌트를 사용하지 않는다",
                            "table을 자유 형식 입력(textarea)의 대체 수단으로 사용하지 않는다"
                    )
            );

    public static final AiComponentCatalogItem EVENT_DATE_RANGE =
            new AiComponentCatalogItem(
                    "event-date-range",
                    "일정",
                    "기간을 기반으로 캘린더 및 근태 도메인 이벤트를 생성하며, 일정 목적을 단일 입력으로 수집하는 복합 일정 컴포넌트",

                    // Internal Inputs
                    List.of(
                            "시작 날짜",
                            "종료 날짜",
                            "일정 유형",
                            "일정 목적"
                    ),

                    // Output Summary
                    List.of(
                            "일정 정보"
                    ),

                    // Restrictions (unused)
                    List.of(),

                    // Use When
                    List.of(
                            "회사 일정, 출장, 외근, 휴가 등 기간 기반 도메인 이벤트를 생성해야 할 때",
                            "문서 승인과 함께 캘린더 또는 근태 기록에 반영되어야 할 때"
                    ),

                    // Rules
                    List.of(
                            // 사용 범위
                            "event-date-range 컴포넌트는 회사 일정, 출장, 외근, 휴가 문서에서만 사용된다",
                            "일정 기반 이벤트 생성 여부는 문서 컨텍스트에 따라 이미 결정되어 있으며 AI가 변경하지 않는다",

                            // 필수 입력
                            "모든 경우에 시작 날짜와 종료 날짜는 필수 입력 항목이다",
                            "모든 경우에 일정 유형과 일정 목적은 필수 입력 항목이다",

                            // 🔒 일정 목적 단일 입력 원칙
                            "일정 목적은 event-date-range 컴포넌트 내부에서만 입력된다",
                            "일정 목적은 일정 생성, 캘린더 반영, 근태 기록, 문서 승인에 사용되는 유일한 목적 입력 값이다",

                            // 의미 범위 정의
                            "일정 목적은 해당 문서에서 작성자가 입력해야 할 사유, 목적, 업무 내용 설명을 모두 포함한다",

                            // 문서 유형별 의미 고정
                            "문서 유형에 따라 일정 목적은 다음 의미를 가진다: 회사 일정은 일정의 성격 및 진행 목적, 출장은 출장 사유 및 업무 목적, 외근은 외근 사유 및 수행 업무 내용, 휴가는 휴가 사유를 의미한다",

                            // 🔒 추가 입력 차단
                            "event-date-range가 사용된 문서에서는 일정 목적과 관련된 입력을 다른 컴포넌트로 분리하거나 중복 생성할 수 없다",
                            "AI는 문서 목적, 사유, 설명, 보완 입력을 이유로 일정 목적에 해당하는 추가 컴포넌트를 생성하지 않는다",

                            // 구조 분리 금지
                            "기간, 일정 유형, 일정 목적은 다른 컴포넌트로 분리할 수 없다",
                            "event-date-range와 동일하거나 유사한 의미를 가지는 추가 입력 컴포넌트는 생성 불가하다"
                    )
            );


    public static final List<AiComponentCatalogItem> COMPONENTS = List.of(
            TEXT,
            TEXTAREA,
            NUMBER,
            DIVIDER,
            TIME,
            TIME_RANGE,
            DATE,
            DATE_RANGE,
            RADIO,
            CHECKBOX,
            NOTICE,
            IMAGE,
            CURRENCY,
            ADDRESS,
            EMPLOYEE_SEARCH,
            DEPARTMENT_SEARCH,
            TABLE,
            EVENT_DATE_RANGE
    );


}
