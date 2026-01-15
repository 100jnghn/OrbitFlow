// ===========================================================
// [전자결재 문서 양식 생성/수정 스크립트]
//   - 주요 상수/스키마, 컴포넌트 렌더링, 유틸 함수, 이벤트 바인딩 등
//   - 주석 그룹 순서: 상수/스키마 > 전역변수 > 유틸 > 검증 > 컴포넌트 관리 > 렌더링 > 패널 > 데이터 변환 > 그룹정보 > 바인딩 > 초기화
// ===========================================================

// ===========================
// 1. 상수/타입/스키마 정의
// ===========================

// -- 주요 컴포넌트 타입 상수 (확장 가능)
const FORM_COMPONENT_TYPES = [
    "text",
    "textarea",
    "number",
    "divider",
    "time",
    "time-range",
    "date",
    "date-range",
    "radio",
    "checkbox",
    "event-date-range",
    "notice",
    "table",
    "image",
    "currency",
    "address",
    "employee-search",
    "department-search"
];

const REQUIRED_TOGGLE_HIDDEN_TYPES = [
    'divider',
    'checkbox',
    'notice',
    'event-date-range'
];

// 이미지 컴포넌트 정책
const IMAGE_COMPONENT_SYSTEM_MAX = 5;
const IMAGE_COMPONENT_DEFAULT_MAX = 1;

// -- 타입별 기본 스키마: 공통필드+meta
const FORM_COMPONENT_SCHEMAS = {
    text: {
        type: "text",
        label: "텍스트 입력",
        required: false,
        meta: {
            placeholder: "",
            maxLength: undefined
        }
    },
    textarea: {
        type: "textarea",
        label: "여러 줄 입력",
        required: false,
        meta: {
            placeholder: "",
            maxLength: undefined
        }
    },
    number: {
        type: "number",
        label: "숫자 입력",
        required: false,
        meta: {
            min: undefined,
            max: undefined
        }
    },
    divider: {
        type: "divider",
        label: "구분선",
        required: false,
        meta: {}
    },
    time: {
        type: "time",
        label: "시간",
        required: false,
        meta: {
            format: ""  // 예시: "HH:mm"
        }
    },
    "time-range": {
        type: "time-range",
        label: "시간 범위",
        required: false,
        meta: {
            startLabel: "",
            endLabel: ""
        }
    },
    date: {
        type: "date",
        label: "날짜",
        required: false,
        meta: {
            format: ""  // 예시: "YYYY-MM-DD"
        }
    },
    "date-range": {
        type: "date-range",
        label: "날짜 범위",
        required: false,
        meta: {
            startLabel: "",
            endLabel: ""
        }
    },
    "event-date-range": {
        type: "event-date-range",
        label: "일정",
        required: true,
        meta: {
            startLabel: "시작일",
            endLabel: "종료일",
            // 관리자 고정 설정
            baseRole: "COMPANY_EVENT",
            // VACATION | BUSINESS_TRIP | OUTWORK | COMPANY_EVENT

            affect: {
                attendance: false,
                schedule: true
            },
            // UI 표시 제어용
            ui: {
                requireReason: false,      // 휴가일 때 true
                requireDescription: true,   // 회사 일정 등
                requireTitle: true
            }
        }
    },
    radio: {
        type: "radio",
        label: "라디오 버튼",
        required: false,
        meta: {
            options: [
                {id: 'opt1', label: '옵션 1'}
            ],
            minSelected: undefined,
            maxSelected: undefined
        }
    },
    checkbox: {
        type: "checkbox",
        label: "체크 박스",
        required: false,
        meta: {
            options: [
                {id: 'opt1', label: '옵션 1'}
            ],
            minSelected: undefined,
            maxSelected: undefined
        }
    },
    notice: {
        type: "notice",
        label: "안내 문구",
        required: false,
        meta: {
            message: "",
            style: "info" // "info", "warning", "danger"
        }
    },
    // table column schema 예시
    // { id, label, type: "text"|"number"|"currency", required, meta }
    table: {
        type: "table",
        label: "테이블",
        required: false,
        meta: {
            columns: [],
            rowPolicy: {
                min: 1,
                max: undefined,
                addable: true,
                removable: true
            }
        }
    },
    image: {
        type: "image",
        label: "이미지",
        required: false,
        meta: {
            maxCount: IMAGE_COMPONENT_DEFAULT_MAX
        }
    },
    currency: {
        type: "currency",
        label: "통화 입력",
        required: false,
        meta: {
            unit: "KRW",
            locale: ""
        }
    },
    address: {
        type: "address",
        label: "주소 입력",
        required: false,
        meta: {
            usePostcodeApi: false
        }
    },
    "employee-search": {
        type: "employee-search",
        label: "사원 검색",
        required: false,
        meta: {
            multiple: false
        }
    },
    "department-search": {
        type: "department-search",
        label: "조직 검색",
        required: false,
        meta: {
            multiple: false
        }
    }
};

// -- 문서정보용 고정형 컴포넌트 예시
const FIXED_COMPONENTS = [
    {
        id: 'document-title',
        type: 'document-title',
        label: '문서 제목',
        required: true,
        fixed: true,
        meta: {
            placeholder: "문서 제목을 입력하세요.",
            value: ""
        }
    },
    {
        id: 'document-meta',
        type: 'document-meta',
        label: '작성자/작성일',
        required: false,
        fixed: true,
        meta: {
            // 실제 값은 서버/세션 정보와 연동
        }
    }
];

// =====================================
// 2. 전역 변수 (폼 상태 & 상수 값 등)
// =====================================



// -- 폼 컴포넌트(양식요소) 목록
let formComponents = [
    {...FIXED_COMPONENTS[0], id: 'document-title'},
    {...FIXED_COMPONENTS[1], id: 'document-meta'}
];

// -- 기타 전역 및 상수
let selectedComponentId = null;            // 선택중인 컴포넌트 id(UI 상태)
let dragSrcIdx = null;                    // 드래그 상태 index
// =======================
// 그룹 기준 정책 값 (전역)
// =======================
let templateGroupBaseRole = null;
let templateGroupCategoryCode = null;


// -- 입력 제한/정책 상수
const DOCUMENT_TITLE_MAX = 25;
const COMPONENT_LABEL_MAX = 10;
const NOTICE_MESSAGE_MAX = 200;
const POPUP_TEMPLATE_DESC_MAX = 200;
const TABLE_ROW_MIN = 1;
const TABLE_ROW_MAX = 10;
const TABLE_COLUMN_MAX = 10;
let dragBlockSize = 1;

let aiGenerating = false;


// =====================
// 3. 유틸리티 함수
// =====================


function bindAutoClearInvalidFocus() {
    const getScope = (target) =>
        target.closest('.table-column-item') ||
        target.closest('#option-list-pane > div') ||
        target.closest('.setting-row');

    const clearError = (target) => {
        if (!(target instanceof HTMLElement)) return;

        /* =========================
           1️⃣ 자기 자신 scope 정리
        ========================= */
        target.classList.remove('invalid-focus');
        target.closest('.invalid-focus')?.classList.remove('invalid-focus');

        const scope = getScope(target);
        scope?.querySelectorAll('.hint.error').forEach(hint => {
            hint.textContent = '';
            hint.className = 'hint';
        });

        /* =========================
           2️⃣ 🔥 min/max 입력 시
               옵션 목록 에러도 해제
        ========================= */
        if (
            target.id === 'option-min-selected' ||
            target.id === 'option-max-selected'
        ) {
            const optionPane = document.getElementById('option-list-pane');
            if (optionPane) {
                optionPane
                    .querySelectorAll('.invalid-focus')
                    .forEach(el => el.classList.remove('invalid-focus'));

                optionPane
                    .querySelectorAll('.hint.error')
                    .forEach(hint => {
                        hint.textContent = '';
                        hint.className = 'hint';
                    });
            }
        }
    };


    document.addEventListener('input', e => clearError(e.target));
    document.addEventListener('change', e => clearError(e.target));

    document.addEventListener('focusout', e => {
        const scope = getScope(e.target);
        scope?.querySelectorAll('.hint.success').forEach(hint => {
            hint.textContent = '';
            hint.className = 'hint';
        });
    });
}

function focusDocumentSettingError({field, message, autoFocus = true}) {
    if (!(field instanceof HTMLElement)) return;

    // 기존 invalid-focus 초기화
    document
        .querySelectorAll('.invalid-focus')
        .forEach(el => el.classList.remove('invalid-focus'));

    // 하이라이트
    field.classList.add('invalid-focus');

    // 포커스
    if (autoFocus && typeof field.focus === "function") {
        field.focus({preventScroll: false});
    }

    // 같은 setting-row 안의 hint를 찾아서 메시지 표시(선택)
    if (message) {
        const hint = field.closest('.setting-row')?.querySelector('.hint');
        if (hint) showMsg(hint, message, 'error');
    }
}


function focusComponentError({
                                 componentId,
                                 panelField,
                                 message,
                                 autoFocus = true
                             }) {
    // 실시간 입력 중이면 중단
    if (
        document.activeElement &&
        (
            document.activeElement.closest('#table-column-list') ||
            document.activeElement.closest('#option-list-pane')
        )
    ) {
        return;
    }

    if (componentId) {
        highlightComponent(componentId);

        if (selectedComponentId !== componentId) {
            selectedComponentId = componentId;
            showComponentSettingPanel(componentId);
        }
    }


    // ⚠️ DOM 재생성 이후에 실행
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {

            // invalid-focus 초기화
            document
                .querySelectorAll('.invalid-focus')
                .forEach(el => el.classList.remove('invalid-focus'));

            let field = null;
            let hint = null;

            /* =========================
               panelField가 함수인 경우
               (테이블 컬럼 index 기반)
            ========================= */
            if (typeof panelField === "function") {
                field = panelField();

                if (field) {
                    field.classList.add('invalid-focus');

                    // ✅ 테이블 컬럼 hint 재탐색
                    const columnItem = field.closest('.table-column-item');
                    if (columnItem) {
                        const next = columnItem.nextElementSibling;
                        if (next && next.classList.contains('column-hint')) {
                            hint = next;
                        }
                    }
                }
            }

            /* =========================
               selector 문자열인 경우
            ========================= */
            if (!field && typeof panelField === "string") {
                field = document.querySelector(panelField);

                if (field) {
                    field.classList.add('invalid-focus');
                    hint =
                        field.closest('.setting-row')
                            ?.querySelector('.hint');
                }
            }

            if (!field) return;

            if (autoFocus && typeof field.focus === 'function') {
                field.focus({preventScroll: true});
            }

            if (hint && message) {
                showMsg(hint, message, 'error');
            }
        });
    });
}


function deepCopy(obj) {
    if (obj === null || typeof obj !== "object") return obj;
    if (Array.isArray(obj)) return obj.map(deepCopy);
    let res = {};
    for (let k in obj) {
        if (obj.hasOwnProperty(k)) {
            res[k] = deepCopy(obj[k]);
        }
    }
    return res;
}

function generateId() {
    return 'comp_' + Math.random().toString(36).substr(2, 6) + '_' + Date.now();
}

function generateOptionId() {
    return 'opt_' + Math.random().toString(36).substr(2, 4) + '_' + Date.now();
}

function enforceMaxLength(inputEl, max) {
    if (inputEl.value.length > max) {
        inputEl.value = inputEl.value.slice(0, max);
    }
}

function showMsg(el, message, type) {
    if (!el) {
        console.warn('[showMsg] hint element not found');
        return;
    }
    el.textContent = message;
    el.className = `hint active ${type}`;
}


function clearSuccessHint(hint) {
    if (!hint) return;
    hint.classList.remove('success');
    hint.classList.remove('active');
    hint.textContent = '';
}


function highlightComponent(componentId) {
    // 기존 invalid 제거
    document
        .querySelectorAll('.form-comp-row.invalid')
        .forEach(el => el.classList.remove('invalid'));
    const target = document.querySelector(
        `.form-comp-row[data-comp-id="${componentId}"]`
    );
    if (target) {
        target.classList.add('invalid');
        // 화면 중앙으로 스크롤
        target.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });
    }
}

function removeComponent(componentId) {
    const idx = formComponents.findIndex(c => c.id === componentId);
    if (idx === -1) return;

    const comp = formComponents[idx];
    if (comp.fixed) return;

    formComponents.splice(idx, 1);

    // 선택 상태 정리
    if (selectedComponentId === componentId) {
        selectedComponentId = null;

        const body = document.getElementById('component-setting-body');
        if (body) {
            body.innerHTML = `
                <span class="hint-msg" id="component-setting-hint">
                    컴포넌트를 선택하세요.
                </span>
            `;
        }
    }

    renderFormComponents();
}


// =====================
// 4. 검증/Validation 함수
// =====================

// -- hint 관련 함수
function showHint(id, message) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = message;
    el.classList.add('active');
}

function clearHint(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = '';
    el.classList.remove('active');
}

// -- 문서 설정 패널 검증
function validateDocumentSettingPanel() {
    const titleInput = document.getElementById('form-title-input');
    const titleHint = document.getElementById('title-hint');
    const categorySelect = document.getElementById('form-category-input');
    const categoryHint = document.getElementById('category-hint');

    const title = titleInput?.value.trim() ?? '';
    const category = categorySelect?.value ?? '';

    // 제목 필수
    if (!title) {
        focusDocumentSettingError({
            field: titleInput,
            message: '문서 제목은 필수입니다.'
        });
        return false;
    }


    if (title.length > DOCUMENT_TITLE_MAX) {
        focusDocumentSettingError({
            field: titleInput,
            message: `문서 제목은 ${DOCUMENT_TITLE_MAX}자 이하로 입력하세요.`
        });
        return false;
    }


    titleHint.textContent = '';
    titleHint.className = 'hint';
    return true;
}

function validateDocumentTitle() {
    const titleInput = document.getElementById('form-title-input');
    const titleHint = document.getElementById('title-hint');

    if (titleInput.classList.contains('invalid-focus')) {
        return false;
    }

    const v = titleInput.value.trim();
    if (!v) {
        titleHint.textContent = '';
        return false;
    }
    if (v.length > DOCUMENT_TITLE_MAX) {
        showMsg(
            titleHint,
            `문서 제목은 ${DOCUMENT_TITLE_MAX}자 이내여야 합니다. (${v.length}/${DOCUMENT_TITLE_MAX})`,
            'error'
        );
        return false;
    }
    showMsg(
        titleHint,
        `입력됨 (${v.length}/${DOCUMENT_TITLE_MAX})`,
        'success'
    );
    return true;
}

function validateComponentLabel(inputEl, hintEl) {
    const v = inputEl.value.trim();
    if (!v) {
        hintEl.textContent = '';
        return false;
    }
    if (v.length > COMPONENT_LABEL_MAX) {
        showMsg(
            hintEl,
            `이름은 ${COMPONENT_LABEL_MAX}자 이내여야 합니다. (${v.length}/${COMPONENT_LABEL_MAX})`,
            'error'
        );
        return false;
    }
    showMsg(
        hintEl,
        `입력됨 (${v.length}/${COMPONENT_LABEL_MAX})`,
        'success'
    );
    return true;
}

function validateAllComponentLabels() {
    for (const comp of formComponents) {
        if (comp.fixed) continue;
        const label = comp.label?.trim() ?? '';
        if (!label) {
            focusComponentError({
                componentId: comp.id,
                panelField: '#input-comp-label',
                message: '컴포넌트 이름은 필수입니다.'
            });
            return false;
        }
        if (label.length > COMPONENT_LABEL_MAX) {
            focusComponentError({
                componentId: comp.id,
                panelField: '#input-comp-label',
                message: `이름은 ${COMPONENT_LABEL_MAX}자 이내여야 합니다.`
            });
            return false;
        }
    }
    return true;
}

function validateOptionComponent(comp) {
    const options = comp.meta?.options ?? [];
    const min = comp.meta?.minSelected;
    const max = comp.meta?.maxSelected;

    /* =========================
       옵션 개수 최소 1
    ========================= */
    if (options.length === 0) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#option-list-pane',
            message: '옵션은 최소 1개 이상 필요합니다.'
        });
        return false;
    }

    /* =========================
       🔹 최소 ≤ 최대
    ========================= */
    if (
        min !== undefined &&
        max !== undefined &&
        min > max
    ) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#option-min-selected',
            message: '최소 선택 수는 최대 선택 수보다 클 수 없습니다.'
        });
        return false;
    }

    /* =========================
       🔹 옵션 개수 ≥ 최소
       (기존 로직)
    ========================= */
    if (
        min !== undefined &&
        options.length < min
    ) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#option-list-pane',
            message: `옵션은 최소 ${min}개 이상이어야 합니다.`
        });
        return false;
    }

    /* =========================
       🔹 옵션 개수 ≥ 최대
       (이번에 추가)
    ========================= */
    if (
        max !== undefined &&
        options.length < max
    ) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#option-list-pane',
            message: `옵션 수가 최대 선택 수(${max})보다 적습니다.`
        });
        return false;
    }

    /* =========================
       옵션 라벨 검증
    ========================= */
    for (let i = 0; i < options.length; i++) {
        const label = options[i].label?.trim() ?? '';

        if (!label) {
            focusComponentError({
                componentId: comp.id,
                panelField: () =>
                    document.querySelectorAll('#option-list-pane input')[i],
                message: `옵션 ${i + 1}의 이름을 입력해주세요.`
            });
            return false;
        }

        if (label.length > COMPONENT_LABEL_MAX) {
            focusComponentError({
                componentId: comp.id,
                panelField: () =>
                    document.querySelectorAll('#option-list-pane input')[i],
                message: `옵션 이름은 ${COMPONENT_LABEL_MAX}자 이내여야 합니다.`
            });
            return false;
        }
    }

    return true;
}

function validateTableComponent(comp) {
    const columns = comp.meta?.columns ?? [];
    const list = document.getElementById('table-column-list');

    if (columns.length === 0) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#table-column-list',
            message: '테이블에는 최소 1개 이상의 컬럼이 필요합니다.'
        });
        return false;
    }

    for (let i = 0; i < columns.length; i++) {
        const col = columns[i];
        const label = col.label?.trim() ?? '';

        if (!label) {
            focusComponentError({
                componentId: comp.id,
                panelField: () => {
                    if (!list) return null;
                    const items = list.querySelectorAll('.table-column-item');
                    return items[i]?.querySelector('.table-col-label-input') ?? null;
                },
                message: `컬럼 ${i + 1}의 이름을 입력해주세요.`
            });
            return false;
        }
    }

    const {min, max} = comp.meta?.rowPolicy ?? {};
    if (max !== undefined && min > max) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#table-row-max',
            message: '최대 행 수는 최소 행 수보다 작을 수 없습니다.'
        });
        return false;
    }

    return true;
}


function validateAllComponents() {
    if (!validateAllComponentLabels()) return false;
    for (const comp of formComponents) {
        if (comp.fixed) continue;
        switch (comp.type) {
            case 'radio':
            case 'checkbox':
                if (!validateOptionComponent(comp)) return false;
                break;
            case 'table':
                if (!validateTableComponent(comp)) return false;
                break;
            case 'notice':
                const msg = comp.meta?.message?.trim() ?? '';
                if (!msg) {
                    focusComponentError({
                        componentId: comp.id,
                        panelField: '#input-notice-message',
                        message: '안내 문구를 입력해주세요.'
                    });
                    return false;
                }
                if (msg.length > NOTICE_MESSAGE_MAX) {
                    focusComponentError({
                        componentId: comp.id,
                        panelField: '#input-notice-message',
                        message: `안내 문구는 ${NOTICE_MESSAGE_MAX}자 이내여야 합니다.`
                    });
                    return false;
                }
                break;
        }
    }
    return true;
}

function validateOptionLabel(inputEl, hintEl) {
    const v = inputEl.value.trim();
    if (!v) {
        hintEl.textContent = '';
        return false;
    }
    if (v.length > COMPONENT_LABEL_MAX) {
        showMsg(
            hintEl,
            `옵션 이름은 ${COMPONENT_LABEL_MAX}자 이내 (${v.length}/${COMPONENT_LABEL_MAX})`,
            'error'
        );
        return false;
    }
    showMsg(
        hintEl,
        `입력됨 (${v.length}/${COMPONENT_LABEL_MAX})`,
        'success'
    );
    return true;
}

function validateNoticeMessage(inputEl, hintEl) {
    const v = inputEl.value;
    if (!v.trim()) {
        hintEl.textContent = '';
        return false;
    }
    if (v.length > NOTICE_MESSAGE_MAX) {
        showMsg(
            hintEl,
            `안내 문구는 ${NOTICE_MESSAGE_MAX}자 이내여야 합니다. (${v.length}/${NOTICE_MESSAGE_MAX})`,
            'error'
        );
        return false;
    }
    showMsg(
        hintEl,
        `입력됨 (${v.length}/${NOTICE_MESSAGE_MAX})`,
        'success'
    );
    return true;
}

// ========================
// 5. 컴포넌트 관리 관련 함수
// ========================

function applyGroupCategoryToDocumentSetting() {
    const categorySelect = document.getElementById('form-category-input');
    if (!categorySelect || !templateGroupCategoryCode) return;

    // 값 반영
    categorySelect.value = templateGroupCategoryCode;

    // 수정 불가
    categorySelect.disabled = true;

    // 스타일로 읽기 전용 느낌 강화 (선택)
    categorySelect.style.backgroundColor = '#f2f4f8';
    categorySelect.style.color = '#555';
    categorySelect.style.cursor = 'not-allowed';
}


function applyEventComponentPolicy(comp, baseRole) {
    if (comp.type !== "event-date-range") return;

    // 1️⃣ baseRole 고정
    comp.meta.baseRole = baseRole ?? "COMPANY_EVENT";

    // 2️⃣ UI 정책 (명확한 역할 분리)
    comp.meta.ui ??= {};

    if (comp.meta.baseRole === "VACATION") {
        // 휴가
        comp.meta.ui.requireTitle = false;        // ❌ 일정명 입력 불필요
        comp.meta.ui.requireReason = true;        // ✅ 휴가 사유 필수
        comp.meta.ui.requireDescription = false; // textarea는 사유로 대체
    } else {
        // 출장 / 외근 / 회사 일정
        comp.meta.ui.requireTitle = true;         // ⭐ 일정명 필수
        comp.meta.ui.requireReason = false;
        comp.meta.ui.requireDescription = true;  // 설명(선택 or 필수는 UI에서)
    }

    // 3️⃣ affect 정책
    switch (comp.meta.baseRole) {
        case "VACATION":
            comp.meta.affect = {attendance: true, schedule: true};
            break;

        case "BUSINESS_TRIP":
        case "OUTWORK":
            comp.meta.affect = {attendance: false, schedule: true};
            break;

        case "COMPANY_EVENT":
        default:
            comp.meta.affect = {attendance: false, schedule: true};
            break;
    }

    // 4️⃣ 필수 보장 (event-date-range 자체는 항상 필수)
    comp.required = true;
}


async function addComponentByType(type) {
    if (!FORM_COMPONENT_SCHEMAS[type]) return;

    // 🔒 일정 컴포넌트는 1개만 허용
    if (type === "event-date-range") {
        const exists = formComponents.some(c => c.type === "event-date-range");
        if (exists) {
            await sweetWarning("일정 컴포넌트는 문서에 하나만 추가할 수 있습니다.");
            return;
        }
    }

    const schema = FORM_COMPONENT_SCHEMAS[type];
    const metaCopy = deepCopy(schema.meta);

    /* ===============================
       선택형 옵션 ID 재생성
    =============================== */
    if (type === "radio" || type === "checkbox") {
        metaCopy.options = metaCopy.options?.length
            ? metaCopy.options.map(opt => ({
                ...opt,
                id: generateOptionId()
            }))
            : [{id: generateOptionId(), label: "옵션 1"}];
    }

    /* ===============================
       🖼 image 컴포넌트 전용 초기화
    =============================== */
    if (type === "image") {
        // 기본 maxCount
        let maxCount = metaCopy.maxCount ?? IMAGE_COMPONENT_DEFAULT_MAX;

        // 시스템 상한 보정
        if (maxCount > IMAGE_COMPONENT_SYSTEM_MAX) {
            maxCount = IMAGE_COMPONENT_SYSTEM_MAX;
        }

        metaCopy.maxCount = maxCount;

        // value는 작성 시점에 채워짐 (fileId 배열)
        metaCopy.value = [];
    }

    const component = {
        id: generateId(),
        type: schema.type,
        label: schema.label,
        required: schema.required,
        fixed: false,
        meta: metaCopy
    };

    // ===============================
    // 🔑 일정 컴포넌트: 그룹 baseRole 절대 고정
    // ===============================
    if (type === "event-date-range") {
        applyEventComponentPolicy(
            component,
            templateGroupBaseRole || "COMPANY_EVENT"
        );
    }

    // document-meta 바로 앞에 삽입
    formComponents.splice(formComponents.length - 1, 0, component);

    selectedComponentId = component.id;
    renderFormComponents();
    showComponentSettingPanel(component.id);
}


function initComponentListPanel() {

    const listContainer = document.getElementById("component-btn-list");
    if (!listContainer) return;
    listContainer.innerHTML = "";

    FORM_COMPONENT_TYPES.forEach(function (type) {
        if (!FORM_COMPONENT_SCHEMAS[type]) return;

        const btn = document.createElement("button");
        btn.className = "component-btn";
        btn.type = "button";
        btn.setAttribute("data-type", type);
        btn.innerText = FORM_COMPONENT_SCHEMAS[type].label || type;


        btn.addEventListener("click", function () {
            // 일반 컴포넌트 추가
            addComponentByType(type);
        });


        listContainer.appendChild(btn);
    });
}

// ============================
// 6. 렌더링 함수 (폼 미리보기 등)
// ============================

function renderFormComponents() {
    enforceSystemRequiredRules();

    const container = document.getElementById("form-edit-area");
    if (!container) return;
    container.innerHTML = "";

    const fixedFirstIdx = 0;
    const fixedLastIdx = formComponents.length - 1;

    formComponents.forEach((comp, idx) => {
        const row = document.createElement("div");
        row.className = "form-comp-row";
        row.dataset.compId = comp.id;

        if (comp.fixed) row.classList.add("fixed");
        if (comp.id === selectedComponentId) row.classList.add("selected");

        /* ======================
           Drag & Drop
        ====================== */
        if (!comp.fixed) {
            row.draggable = true;

            row.addEventListener("dragstart", e => {
                dragSrcIdx = idx;
                dragBlockSize = 1;
                row.classList.add("dragging");
                e.dataTransfer.effectAllowed = "move";
                try {
                    e.dataTransfer.setData("text/plain", comp.id);
                } catch {
                }
            });

            row.addEventListener("dragend", () => {
                row.classList.remove("dragging");
                dragSrcIdx = null;
                dragBlockSize = 1;
            });
        }

        row.addEventListener("dragover", e => {
            if (
                dragSrcIdx === null ||
                comp.fixed ||
                idx === fixedFirstIdx ||
                idx === fixedLastIdx ||
                dragSrcIdx === idx
            ) return;
            e.preventDefault();
            row.classList.add("drag-hover");
        });

        row.addEventListener("dragleave", () => {
            row.classList.remove("drag-hover");
        });

        row.addEventListener("drop", e => {
            if (
                dragSrcIdx === null ||
                comp.fixed ||
                idx === fixedFirstIdx ||
                idx === fixedLastIdx
            ) return;

            e.preventDefault();
            row.classList.remove("drag-hover");

            const draggedBlock = formComponents.splice(dragSrcIdx, dragBlockSize);
            let insertIdx = idx;

            if (dragSrcIdx < idx) {
                insertIdx = idx - dragBlockSize + 1;
            }

            formComponents.splice(insertIdx, 0, ...draggedBlock);

            const firstFixed = formComponents.find(c => c.type === "document-title");
            const lastFixed = formComponents.find(c => c.type === "document-meta");

            formComponents = [
                firstFixed,
                ...formComponents.filter(c => !c.fixed),
                lastFixed
            ];

            renderFormComponents();
            selectedComponentId = draggedBlock[0].id;
            showComponentSettingPanel(draggedBlock[0].id);
        });

        if (!comp.fixed) {
            row.addEventListener("click", () => {
                if (row.classList.contains("dragging")) return;
                selectedComponentId = comp.id;
                renderFormComponents();
                showComponentSettingPanel(comp.id);
            });
        }

        /* ======================
           미리보기 렌더링
        ====================== */
        renderPreviewIntoRow(row, comp);

        /* ======================
           삭제 버튼
        ====================== */
        if (!comp.fixed) {
            const deleteBtn = document.createElement("button");
            deleteBtn.className = "delete-btn";
            deleteBtn.innerHTML = "🗑️";

            deleteBtn.onclick = async e => {
                e.stopPropagation();

                const result = await sweetConfirm(
                    '삭제 확인',
                    '이 컴포넌트를 삭제하시겠습니까?'
                );

                if (!result.isConfirmed) return;

                removeComponent(comp.id);
            };

            row.appendChild(deleteBtn);
        }


        container.appendChild(row);
    });
}


function renderPreviewIntoRow(row, comp) {
    if (comp.fixed) {
        renderFixedComponent(row, comp);
        return;
    }

    if (comp.type === "divider") {
        row.innerHTML = `
           <div class="preview-row divider-preview">
               <span class="divider-line"></span>
               <em>${comp.label || "구분선"}</em>
               <span class="divider-line"></span>
           </div>
       `;
        return;
    }

    const labelHtml = `
        <span class="comp-label ${comp.required ? "is-required" : ""}">
            ${comp.label || ""}
        </span>
    `;

    const inputHtml = renderPreviewInput(comp);

    row.innerHTML = `
        <div class="preview-row">
            ${labelHtml}
            ${inputHtml}
        </div>
    `;
}


function renderFixedComponent(row, comp) {
    if (comp.type === "document-title") {
        row.innerHTML = `
            <div class="preview-row">
                <span class="comp-label is-required">${comp.label}</span>
                <input disabled
                       class="preview-input input-long"
                       value="${comp.meta?.value ?? ""}"
                       placeholder="${comp.meta?.placeholder ?? ""}" />
            </div>
        `;
    } else if (comp.type === "document-meta") {
        row.innerHTML = `
            <span class="doc-meta-label">${comp.label}</span>
            <span class="doc-meta-auto">자동입력</span>
        `;
    }
}


function renderPreviewInput(comp) {
    switch (comp.type) {

        /* ===============================
           일정 (최대 폭, 독립 레이아웃)
        =============================== */
        case "event-date-range":
            return renderEventDateRangePreview(comp);

        /* ===============================
           텍스트 계열 (LONG)
        =============================== */
        case "text":
            return `
                <input disabled
                       class="preview-input input-long"
                       type="text"
                       placeholder="${comp.meta?.placeholder ?? ""}" />
            `;

        case "textarea":
            return `
                <textarea disabled
                          class="preview-textarea input-long"></textarea>
            `;

        /* ===============================
           숫자 / 통화 (COMPACT)
        =============================== */
        case "number":
            return `
                <input disabled
                       class="preview-input input-compact"
                       type="number"
                       placeholder="${comp.meta?.placeholder ?? ""}" />
            `;

        case "currency":
            return `
                <div class="currency-preview">
                    <input disabled
                           class="preview-input input-compact"
                           type="text"
                           placeholder="${comp.meta?.placeholder ?? ""}" />
                    <span class="unit">${comp.meta?.unit ?? "KRW"}</span>
                </div>
            `;

        /* ===============================
           날짜 / 시간 (MEDIUM)
        =============================== */
        case "date":
            return `
                <input type="date"
                       disabled
                       class="preview-input input-medium" />
            `;

        case "time":
            return `
                <input type="time"
                       disabled
                       class="preview-input input-medium" />
            `;

        /* ===============================
           날짜 / 시간 범위 (COMPACT)
        =============================== */
        case "date-range":
            return `
                <div class="range-preview input-compact">
                    <input type="date" disabled />
                    <span class="range-sep">~</span>
                    <input type="date" disabled />
                </div>
            `;

        case "time-range":
            return `
                <div class="range-preview input-compact">
                    <input type="time" disabled />
                    <span class="range-sep">~</span>
                    <input type="time" disabled />
                </div>
            `;

        /* ===============================
           선택형 (라디오 / 체크)
        =============================== */
        case "radio":
        case "checkbox":
            return `
                <div class="option-preview">
                    ${(comp.meta?.options ?? []).map(opt => `
                        <label>
                            <input type="${comp.type}" disabled />
                            ${opt.label}
                        </label>
                    `).join("")}
                </div>
            `;

        /* ===============================
           안내 문구
        =============================== */
        case "notice":
            return `
                <div class="notice-message-box ${comp.meta?.style ?? "info"}">${(comp.meta?.message ?? "안내 문구").trim().replace(/\n/g, "<br>")}</div>
            `;

        /* ===============================
           테이블
        =============================== */
        case "table":
            return `
        <div class="table-preview-wrapper">
            ${renderTablePreview(comp)}
        </div>
    `;

        /* ===============================
           이미지
        =============================== */
        case "image":
            return renderImagePreview(comp);

        /* ===============================
           주소 (LONG)
        =============================== */
        case "address":
            return `
                <div class="address-preview">
                    <input disabled class="preview-input input-long" placeholder="우편번호" />
                    <input disabled class="preview-input input-long" placeholder="기본 주소" />
                    <input disabled class="preview-input input-long" placeholder="상세 주소" />
                </div>
            `;

        /* ===============================
           검색 (MEDIUM)
        =============================== */
        case "employee-search":
            return `
                <div class="search-preview">
                    <input disabled
                           class="preview-input input-medium"
                           placeholder="사원 선택" />
                    <button disabled>🔍</button>
                </div>
            `;

        case "department-search":
            return `
                <div class="search-preview">
                    <input disabled
                           class="preview-input input-medium"
                           placeholder="조직 선택" />
                    <button disabled>🔍</button>
                </div>
            `;

        /* ===============================
           fallback
        =============================== */
        default:
            return `<div style="color:#aaa;">미리보기 미지원</div>`;
    }
}


function renderEventDateRangePreview(comp) {
    const isVacation = comp.meta?.baseRole === "VACATION";

    return `
        <div class="event-body preview-mode ${isVacation ? "vacation-preview" : ""}">
            <div class="event-row event-range range-preview input-medium">
                <input type="date" disabled />
                <span class="range-sep">~</span>
                <input type="date" disabled />
            </div>

            ${!isVacation ? `
                <div class="event-row event-title">
                    <input
                        type="text"
                        disabled
                        class="preview-input input-long"
                        placeholder="일정 제목을 입력하세요"
                    />
                </div>
            ` : ""}

            ${isVacation ? `
                <div class="event-row event-vacation-type">
                    <select disabled>
                        <option>휴가 유형 선택</option>
                    </select>
                </div>
            ` : ""}

            <div class="event-row event-description">
                <textarea
                    disabled
                    class="preview-textarea input-long"
                    placeholder="${isVacation
        ? "휴가 상세 사유를 입력하세요."
        : "일정 설명을 입력하세요."
    }"></textarea>
            </div>
        </div>
    `;
}


function renderTablePreview(comp) {
    const cols = comp.meta?.columns ?? [];
    const minRows = Math.max(1, comp.meta?.rowPolicy?.min ?? 1);

    return `
        <table class="table-preview">
            <thead>
                <tr>
                    ${cols.map(col => `
                        <th class="${col.required ? 'is-required' : ''}">
                            ${col.label ?? ''}
                        </th>
                    `).join("")}
                </tr>
            </thead>
            <tbody>
                ${Array.from({length: minRows}).map(() => `
                    <tr>
                        ${cols.map(() => `<td><input disabled /></td>`).join("")}
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}


function renderImagePreview(comp) {
    const max = comp.meta?.maxCount ?? 1;

    return `
        <div class="image-preview placeholder">
            이미지 (${max}장까지)
        </div>
    `;
}


function enforceSystemRequiredRules() {
    formComponents.forEach(comp => {
        if (comp.type === "event-date-range") {
            comp.required = true;
        }
    });
}

// ================================
// 7. 컴포넌트 설정 패널(사이드)
// ================================

function showComponentSettingPanel(componentId) {
    const comp = formComponents.find(c => c.id === componentId);
    const body = document.getElementById('component-setting-body');
    if (!body) return;

    if (!comp) {
        body.innerHTML = `
            <span class="hint-msg" id="component-setting-hint">
                컴포넌트를 선택하세요.
            </span>
        `;
        return;
    }

    const isFixed = !!comp.fixed;
    comp.meta ??= {};

    /* ======================
       1. 공통 정보 영역
    ====================== */
    let html = `
        <div style="margin-bottom:11px;">
            <div style="color:#888;font-size:0.9em;">
                타입: ${comp.type}
            </div>
        </div>

        <div class="setting-row">
            <label>이름</label>
            <input
                type="text"
                id="input-comp-label"
                value="${(comp.label || '').replace(/"/g, '&quot;')}"
                ${isFixed ? 'disabled' : ''}
            >
            <div class="hint" id="component-label-hint"></div>
        </div>
    `;

    const isForceRequired = comp.type === "event-date-range";
    const hideRequiredToggle = REQUIRED_TOGGLE_HIDDEN_TYPES.includes(comp.type);

    if (!isFixed && !isForceRequired && !hideRequiredToggle) {
        html += `
            <div class="setting-row">
                <label>
                    <input type="checkbox" id="toggle-required"
                        ${comp.required ? "checked" : ""}>
                    필수 입력
                </label>
            </div>
        `;
    }

    if (isForceRequired) {
        html += `
            <div class="setting-row" style="color:#888;font-size:0.85em;">
                ⚠ 이 항목은 정책상 필수 입력입니다.
            </div>
        `;
    }

    /* ======================
       divider
    ====================== */
    if (comp.type === "divider") {
        html += `
            <div class="setting-row" style="margin-top:12px;color:#888;font-size:0.9em;">
                구분선은 문서의 시각적 영역을 나누는 용도입니다.
            </div>
        `;
    }

    /* ======================
       notice
    ====================== */
    if (comp.type === "notice") {
        html += `
            <div class="setting-row" style="margin-top:14px;">
                <label>안내 문구</label>
                <textarea id="input-notice-message"
                          style="width:100%;min-height:60px;">${comp.meta.message ?? ""}</textarea>
                <div class="hint" id="notice-message-hint"></div>
            </div>
        `;
    }

    /* ======================
       image
    ====================== */
    if (comp.type === "image") {
        const currentMax = comp.meta.maxCount ?? IMAGE_COMPONENT_DEFAULT_MAX;

        html += `
            <div class="setting-row" style="margin-top:14px;">
                <label style="font-weight:600;">최대 이미지 개수</label>
                <input
                    type="number"
                    id="image-max-count"
                    min="1"
                    max="${IMAGE_COMPONENT_SYSTEM_MAX}"
                    value="${currentMax}"
                />
                <div class="hint" id="image-max-hint"></div>
                <div style="font-size:0.85em;color:#888;margin-top:4px;">
                    ※ 문서 본문에 출력되는 이미지 수입니다 (최대 ${IMAGE_COMPONENT_SYSTEM_MAX}장)
                </div>
            </div>
        `;
    }

    /* ======================
       radio / checkbox
    ====================== */
    if (comp.type === "radio" || comp.type === "checkbox") {
        comp.meta.options ??= [{id: generateOptionId(), label: "옵션 1"}];

        html += `
            <div class="setting-row" style="margin-top:14px;">
                <div style="font-weight:600;">옵션 목록</div>
                <div id="option-list-pane"></div>
                <button type="button" id="add-option-btn">옵션 추가</button>
            </div>

            <div class="setting-row option-limit-row">
                <label>최소 선택 수 : </label>
                <input type="number" id="option-min-selected" min="0">
            </div>
            
            <div class="setting-row option-limit-row">
                <label>최대 선택 수 : </label>
                <input type="number" id="option-max-selected" min="0">
            </div>
        `;
    }

    /* ======================
       table
    ====================== */
    if (comp.type === "table") {
        comp.meta.columns ??= [];
        comp.meta.rowPolicy ??= {min: 1, max: undefined, addable: true, removable: true};

        html += `
            <div class="setting-row" style="margin-top:14px;">
                <div style="font-weight:600;">컬럼 목록</div>
                <div id="table-column-list"></div>
                <button type="button" id="add-table-column-btn">+ 컬럼 추가</button>
            </div>

            <div class="setting-row table-row-setting" style="margin-top:14px;">
                <div style="font-weight:600;">행 설정</div>
            
                <label>
                    최소 행 : 
                    <input type="number" id="table-row-min">
                </label>
            
                <label>
                    최대 행 : 
                    <input type="number" id="table-row-max">
                </label>
            
                <label>
                    <input type="checkbox" id="table-row-addable">
                    행 추가 가능
                </label>
            
                <label>
                    <input type="checkbox" id="table-row-removable">
                    행 삭제 가능
                </label>
            </div>

        `;
    }

    /* ======================
       일정 유형 (읽기 전용)
    ====================== */
    if (comp.type === "event-date-range") {
        html += `
            <div class="setting-row" style="margin-top:14px;">
                <div style="font-weight:600;">일정 유형</div>

                <label><input type="radio" disabled ${comp.meta.baseRole === "VACATION" ? "checked" : ""}> 휴가</label>
                <label><input type="radio" disabled ${comp.meta.baseRole === "BUSINESS_TRIP" ? "checked" : ""}> 출장</label>
                <label><input type="radio" disabled ${comp.meta.baseRole === "OUTWORK" ? "checked" : ""}> 외근</label>
                <label><input type="radio" disabled ${comp.meta.baseRole === "COMPANY_EVENT" ? "checked" : ""}> 회사 일정</label>

                <div style="color:#888;font-size:0.85em;margin-top:6px;">
                    ※ 일정 유형은 문서 양식에서 고정됩니다.
                </div>
            </div>
        `;
    }

    /* ======================
       2. DOM 반영 (단 1회)
    ====================== */
    body.innerHTML = html;

    /* ======================
       3. 공통 이벤트
    ====================== */
    if (!isFixed) {
        const labelInput = document.getElementById("input-comp-label");
        const labelHint = document.getElementById("component-label-hint");

        labelInput?.addEventListener("input", () => {
            enforceMaxLength(labelInput, COMPONENT_LABEL_MAX);
            comp.label = labelInput.value;
            validateComponentLabel(labelInput, labelHint);
            renderFormComponents();
        });

        document.getElementById("toggle-required")
            ?.addEventListener("change", e => {
                comp.required = e.target.checked;
                renderFormComponents();
            });
    }

    /* ======================
       4. notice 이벤트
    ====================== */
    if (comp.type === "notice") {
        const textarea = document.getElementById("input-notice-message");
        const hint = document.getElementById("notice-message-hint");

        textarea?.addEventListener("input", () => {
            enforceMaxLength(textarea, NOTICE_MESSAGE_MAX);
            comp.meta.message = textarea.value;
            validateNoticeMessage(textarea, hint);
            renderFormComponents();
        });
    }

    /* ======================
       image 이벤트
    ====================== */
    if (comp.type === "image") {
        const input = document.getElementById("image-max-count");
        const hint = document.getElementById("image-max-hint");

        input?.addEventListener("input", () => {
            let v = Number(input.value);
            if (!v || v < 1) v = 1;
            if (v > IMAGE_COMPONENT_SYSTEM_MAX) v = IMAGE_COMPONENT_SYSTEM_MAX;

            input.value = v;
            comp.meta.maxCount = v;

            showMsg(hint, `최대 ${v}장까지 업로드 가능`, 'success');
        });
    }

    /* ======================
       5. radio / checkbox 이벤트
    ====================== */
    if (comp.type === "radio" || comp.type === "checkbox") {
        const pane = document.getElementById("option-list-pane");
        const addBtn = document.getElementById("add-option-btn");

        const renderOptions = () => {
            if (!pane) return;
            pane.innerHTML = "";

            comp.meta.options.forEach((opt, idx) => {
                const row = document.createElement("div");
                row.innerHTML = `
                    <div class="option-row">
                        <input class="option-label-input"
                               value="${(opt.label ?? '').replace(/"/g, '&quot;')}">
                        <button type="button"
                                class="option-delete-btn icon-delete-btn"
                                aria-label="옵션 삭제"></button>
                    </div>
                    <div class="hint option-hint"></div>
                `;

                const input = row.querySelector("input");
                const hint = row.querySelector(".option-hint");
                const del = row.querySelector("button");

                input.addEventListener("input", () => {
                    enforceMaxLength(input, COMPONENT_LABEL_MAX);
                    opt.label = input.value;
                    validateOptionLabel(input, hint);
                    renderFormComponents();
                });

                del.addEventListener("click", () => {
                    if (comp.meta.options.length <= 1) return;
                    comp.meta.options.splice(idx, 1);
                    renderOptions();
                    renderFormComponents();
                });

                pane.appendChild(row);
            });
        };

        renderOptions();

        addBtn?.addEventListener("click", () => {
            comp.meta.options.push({id: generateOptionId(), label: "옵션"});
            renderOptions();
            renderFormComponents();
        });

        const clampOptionLimit = (inputEl, setter) => {
            let v = inputEl.value;

            // 🔹 숫자 외 문자 즉시 제거 (IME 포함 보정)
            v = v.replace(/[^\d]/g, '');
            inputEl.value = v;

            // 🔹 빈 값 = 제한 없음
            if (v === "") {
                setter(undefined);
                return;
            }

            v = Number(v);

            if (isNaN(v)) v = 0;
            if (v < 0) v = 0;
            if (v > 10) v = 10;

            inputEl.value = v;
            setter(v);

            // ⭐ 옵션 관련 에러 해제
            clearOptionErrorState();
        };


        const minEl = document.getElementById("option-min-selected");
        const maxEl = document.getElementById("option-max-selected");

        minEl?.addEventListener("input", () => {
            clampOptionLimit(minEl, v => {
                comp.meta.minSelected = v;
            });
        });

        maxEl?.addEventListener("input", () => {
            clampOptionLimit(maxEl, v => {
                comp.meta.maxSelected = v;
            });
        });

    }

    /* ======================
       6. table 이벤트
    ====================== */
    if (comp.type === "table") {
        const list = document.getElementById("table-column-list");
        const addBtn = document.getElementById("add-table-column-btn");

        const renderColumns = () => {
            if (!list) return;
            list.innerHTML = "";

            comp.meta.columns.forEach((col, idx) => {
                const wrapper = document.createElement("div");

                wrapper.innerHTML = `
            <div class="table-column-item">
                <!-- 1️⃣ 컬럼명 -->
                <input
                    class="table-col-label-input"
                    placeholder="컬럼명"
                    value="${(col.label ?? '').replace(/"/g, '&quot;')}"
                >

                <!-- 2️⃣ 옵션 영역 -->
                <div class="table-column-controls">
                    <select class="table-col-type-select">
                        ${["text", "number", "currency"].map(t =>
                    `<option value="${t}" ${t === col.type ? "selected" : ""}>${t}</option>`
                ).join("")}
                    </select>

                    <label class="table-col-required">
                        <input type="checkbox"
                               class="table-col-required-checkbox"
                               ${col.required ? "checked" : ""}>
                        필수
                    </label>

                    <button
                        type="button"
                        class="table-col-delete-btn"
                        aria-label="컬럼 삭제">
                        🗑️
                    </button>
                </div>
            </div>

            <div class="hint column-hint"></div>
        `;

                const labelInput = wrapper.querySelector(".table-col-label-input");
                const typeSelect = wrapper.querySelector(".table-col-type-select");
                const reqCheck = wrapper.querySelector(".table-col-required-checkbox");
                const delBtn = wrapper.querySelector(".table-col-delete-btn");
                const hint = wrapper.querySelector(".column-hint");

                /* 🔒 checkbox 포커스 제거 */
                reqCheck.tabIndex = -1;

                /* 컬럼명 */
                labelInput.addEventListener("input", () => {
                    enforceMaxLength(labelInput, COMPONENT_LABEL_MAX);
                    col.label = labelInput.value;
                    validateComponentLabel(labelInput, hint);
                });

                labelInput.addEventListener("change", () => {
                    renderFormComponents();
                });

                /* 타입 */
                typeSelect.addEventListener("change", e => {
                    col.type = e.target.value;
                    renderFormComponents();
                });

                /* 필수 */
                reqCheck.addEventListener("change", e => {
                    col.required = e.target.checked;
                    renderFormComponents();
                });

                /* 삭제 */
                delBtn.addEventListener("click", () => {
                    comp.meta.columns.splice(idx, 1);
                    renderColumns();
                    renderFormComponents();
                });

                list.appendChild(wrapper);
            });
        };

        renderColumns();

        addBtn?.addEventListener("click", async () => {
            if (comp.meta.columns.length >= TABLE_COLUMN_MAX) {
                await sweetWarning(`컬럼은 최대 ${TABLE_COLUMN_MAX}개까지 추가할 수 있습니다.`);
                return;
            }
            comp.meta.columns.push({
                id: generateId(),
                label: "컬럼",
                type: "text",
                required: false,
                meta: {}
            });
            renderColumns();
            renderFormComponents();
        });

        const minEl = document.getElementById("table-row-min");
        const maxEl = document.getElementById("table-row-max");
        const addableEl = document.getElementById("table-row-addable");
        const removableEl = document.getElementById("table-row-removable");

        minEl?.addEventListener("input", e => {
            let v = Number(e.target.value);
            if (!v || v < TABLE_ROW_MIN) v = TABLE_ROW_MIN;
            if (v > TABLE_ROW_MAX) v = TABLE_ROW_MAX;
            e.target.value = v;
            comp.meta.rowPolicy.min = v;

            if (comp.meta.rowPolicy.max !== undefined && comp.meta.rowPolicy.max < v) {
                comp.meta.rowPolicy.max = v;
                if (maxEl) maxEl.value = String(v);
            }
            renderFormComponents();
        });

        maxEl?.addEventListener("input", e => {
            if (e.target.value === "") {
                comp.meta.rowPolicy.max = undefined;
                renderFormComponents();
                return;
            }

            let max = Number(e.target.value);
            if (!max || max < TABLE_ROW_MIN) max = TABLE_ROW_MIN;
            if (max > TABLE_ROW_MAX) max = TABLE_ROW_MAX;
            if (max < comp.meta.rowPolicy.min) max = comp.meta.rowPolicy.min;

            comp.meta.rowPolicy.max = max;
            e.target.value = String(max);
            renderFormComponents();
        });

        addableEl?.addEventListener("change", e => {
            comp.meta.rowPolicy.addable = e.target.checked;
            renderFormComponents();
        });

        removableEl?.addEventListener("change", e => {
            comp.meta.rowPolicy.removable = e.target.checked;
            renderFormComponents();
        });
    }
}

// ============================
// 8. 데이터 변환(저장/로드) 함수
// ============================

function buildAffectTagsFromComponents(components) {
    const result = new Set();

    components
        .filter(c => c.type === "event-date-range")
        .forEach(c => {
            const baseRole = c.meta?.baseRole;
            const affect = c.meta?.affect ?? {};

            // 1️⃣ baseRole 태그 (문서 성격 식별용)
            if (baseRole) {
                result.add(baseRole);
            }

            // 2️⃣ 기본 정책 계산
            let attendance = false;
            let schedule = false;

            switch (baseRole) {
                case "VACATION":
                    attendance = true;
                    schedule = true;
                    break;
                case "BUSINESS_TRIP":
                case "OUTWORK":
                    schedule = true;
                    break;
                case "COMPANY_EVENT":
                default:
                    // 기본 false
                    break;
            }

            // 3️⃣ 관리자 override 적용
            if (affect.attendance === true) attendance = true;
            if (affect.attendance === false) attendance = false;

            if (affect.schedule === true) schedule = true;
            if (affect.schedule === false) schedule = false;

            // 4️⃣ 최종 태그 반영
            if (attendance) result.add("ATTENDANCE");
            if (schedule) result.add("SCHEDULE");
        });

    return Array.from(result);
}


function convertComponentsToFields(components) {
    return components.map((comp, index) => ({
        fieldId: comp.id,
        fieldType: comp.type,
        label: comp.label,
        required: !!comp.required,
        order: index + 1,
        meta: comp.meta ?? {}
    }));
}

function loadTemplateJsonToFormComponents(templateJson, templateBaseRole) {
    if (!templateJson || !Array.isArray(templateJson.fields)) return;

    const fields = [...templateJson.fields]
        .sort((a, b) => a.order - b.order);

    let components = fields.map(field => ({
        id: field.fieldId,
        type: field.fieldType,
        label: field.label,
        required: field.required,
        fixed: field.fieldType === "document-title",
        meta: field.meta ?? {}
    }));

    // document-title 보장
    if (!components.some(c => c.type === "document-title")) {
        components.unshift({
            ...FIXED_COMPONENTS.find(c => c.type === "document-title"),
            id: "document-title"
        });
    }

    // 🔑 event-date-range만 보정
    components.forEach(comp => {
        if (comp.type !== "event-date-range") return;

        applyEventComponentPolicy(
            comp,
            templateGroupBaseRole ?? templateBaseRole
        );
    });


    // document-meta 항상 마지막
    components = components.filter(c => c.type !== "document-meta");
    components.push({
        ...FIXED_COMPONENTS.find(c => c.type === "document-meta"),
        id: "document-meta"
    });

    formComponents = components;
}

// ================================
// 9. 저장/로드 및 그룹정보 함수
// ================================
function bindDocumentTitleSync() {
    const titleInput = document.getElementById('form-title-input');
    if (!titleInput) return;

    titleInput.addEventListener('input', () => {
        enforceMaxLength(titleInput, DOCUMENT_TITLE_MAX);

        syncDocumentTitle(titleInput.value);
        renderFormComponents();

        validateDocumentTitle();
    });
}

function syncDocumentTitle(value) {
    const titleComp = formComponents.find(c => c.type === "document-title");
    if (!titleComp) return;

    titleComp.meta ??= {};
    titleComp.meta.value = value;
}


async function saveFormTemplateStructure(templateId) {
    if (!templateId) {
        await sweetError('저장할 문서 양식 ID(templateId)가 존재하지 않습니다.');
        return false;
    }

    const titleInput = document.getElementById('form-title-input');
    syncDocumentTitle(titleInput?.value ?? '');

    // 검증
    if (!validateDocumentSettingPanel()) return false;
    if (!validateAllComponents()) return false;


    const filteredComponents = formComponents
        .filter(fc => fc.type !== 'document-meta')
        .map(fc => {
            const comp = {...fc};
            delete comp.fixed;
            delete comp.selected;
            return comp;
        });

    const categoryCode =
        templateGroupCategoryCode ??
        document.getElementById('form-category-input')?.value;
    if (!categoryCode) {
        await sweetWarning('카테고리를 선택해주세요.');
        return false;
    }
    const affectTags = buildAffectTagsFromComponents(formComponents);
    const payload = {
        categoryCode,
        affectTags,
        templateJson: {
            fields: convertComponentsToFields(filteredComponents)
        }
    };
    try {
        const res = await apiFetch(
            `/api/admin/form-templates/${templateId}/structure`,
            {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            }
        );
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err?.message || '문서 양식 저장에 실패했습니다.');
        }
        await sweetSuccess('문서 양식이 저장되었습니다.');
        return true;
    } catch (e) {
        console.error('[FormTemplate SAVE ERROR]', e);
        await sweetError(e.message || '문서 양식 저장 중 오류가 발생했습니다.');
        return false;
    }
}

async function loadFormTemplateGroupInfo(groupId) {
    if (!groupId) return;
    try {
        const res = await apiFetch(`/api/form-template-groups/${groupId}`);
        if (!res.ok) return;
        const result = await res.json();
        const group = result?.data;
        if (!group) return;

        templateGroupBaseRole = group.baseRole;
        templateGroupCategoryCode = group.categoryCode;

        renderFormTemplateGroupInfo(group);
    } catch (e) {
        console.warn('[FormTemplateGroup] 그룹 정보 조회 실패', e);
    }
}


function renderFormTemplateGroupInfo(group) {
    const panel = document.getElementById('form-setting-panel');
    if (!panel) return;
    const blockId = 'form-template-group-info';
    if (document.getElementById(blockId)) return;
    const html = `
        <div id="${blockId}" style="margin-bottom:16px;padding:10px 12px;border:1px solid #dde1f0;border-radius:6px;background:#f7f8fc;">
            <div style="font-weight:600;margin-bottom:6px;">문서 양식 그룹 정보</div>
            <div style="display:flex;align-items:center;margin-bottom:6px;">
                <label style="min-width:70px;color:#666;">그룹명</label>
                <input type="text" value="${group.name ?? ''}" disabled
                    style="flex:1;padding:4px 7px;border:1px solid #ccd;background:#f2f4f8;color:#555;">
            </div>
            <div style="display:flex;align-items:flex-start;">
                <label style="min-width:70px;color:#666;margin-top:4px;">설명</label>
                <textarea disabled
                    style="flex:1;padding:4px 7px;border:1px solid #ccd;background:#f2f4f8;color:#555;resize:none;"
                    rows="2">${group.description ?? ''}</textarea>
            </div>
        </div>
    `;
    panel.insertAdjacentHTML('afterbegin', html);
}

// ============================
// AI 양식 생성 (팝업 + API)
// ============================

function lockUiForAi() {
    const overlay = document.getElementById('ai-block-overlay');
    if (overlay) overlay.style.display = 'flex';

    // 스크롤도 막기
    document.body.style.overflow = 'hidden';
}

function unlockUiForAi() {
    const overlay = document.getElementById('ai-block-overlay');
    if (overlay) overlay.style.display = 'none';

    document.body.style.overflow = '';
}


function bindAiGeneratePopup() {
    const aiBtn = document.querySelector('.ai-btn');
    const popup = document.getElementById('createPopup');

    const nameInput = document.getElementById('popupTemplateName');
    const descInput = document.getElementById('popupTemplateDesc');

    const cancelBtn = document.getElementById('popupCancelBtn');
    const createBtn = document.getElementById('popupCreateBtn');

    if (!aiBtn || !popup) return;

    const hidePopup = () => {
        popup.style.display = 'none';
    };

    const resetPopup = () => {
        popup.style.display = 'none';
        if (nameInput) nameInput.value = '';
        if (descInput) descInput.value = '';
    };

    aiBtn.addEventListener('click', () => {
        popup.style.display = 'flex';
        nameInput?.focus();
    });

    cancelBtn?.addEventListener('click', resetPopup);

    popup.addEventListener('click', e => {
        if (e.target === popup) resetPopup();
    });

    createBtn?.addEventListener('click', async () => {
        if (aiGenerating) return;

        const formName = nameInput.value.trim();
        const purpose = descInput.value.trim();

        if (!formName) {
            await sweetWarning('문서 양식 이름을 입력하세요.');
            nameInput.focus();
            return;
        }

        if (!purpose) {
            await sweetWarning('문서 양식 목적을 입력하세요.');
            descInput.focus();
            return;
        }

        aiGenerating = true;
        createBtn.disabled = true;
        createBtn.textContent = '생성 중...';

        hidePopup();
        lockUiForAi();

        try {
            const res = await apiFetch(
                '/api/admin/form-template/ai/generate',
                {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        formTemplateId: templateId,
                        formName,
                        purpose
                    })
                }
            );

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err?.message || 'AI 양식 생성에 실패했습니다.');
            }

            const result = await res.json();
            const templateJson = result?.data?.templateJson;

            if (!templateJson) {
                throw new Error('AI 응답에 templateJson이 없습니다.');
            }

            loadTemplateJsonToFormComponents(
                templateJson,
                templateGroupBaseRole
            );

            const titleComp = formComponents.find(c => c.type === 'document-title');
            const titleInput = document.getElementById('form-title-input');

            if (titleComp && titleInput) {
                titleInput.value = titleComp.meta?.value ?? formName;
            }

            renderFormComponents();
            resetPopup();

        } catch (e) {
            console.error('[AI GENERATE ERROR]', e);
            await sweetError(e.message || 'AI 양식 생성 중 오류가 발생했습니다.');
            popup.style.display = 'flex';
        } finally {
            aiGenerating = false;
            createBtn.disabled = false;
            createBtn.textContent = 'AI 생성';
            unlockUiForAi();
        }
    });
}


function bindPopupLiveHints() {
    const nameInput = document.getElementById('popupTemplateName');
    const nameHint = document.getElementById('popup-name-hint');

    const descInput = document.getElementById('popupTemplateDesc');
    const descHint = document.getElementById('popup-desc-hint');

    if (nameInput && nameHint) {
        nameInput.addEventListener('input', () => {
            enforceMaxLength(nameInput, DOCUMENT_TITLE_MAX);

            const v = nameInput.value.trim();
            if (!v) {
                nameHint.textContent = '';
                nameHint.className = 'hint';
                return;
            }

            if (v.length > DOCUMENT_TITLE_MAX) {
                showMsg(
                    nameHint,
                    `이름은 ${DOCUMENT_TITLE_MAX}자 이내여야 합니다.`,
                    'error'
                );
            } else {
                showMsg(
                    nameHint,
                    `입력됨 (${v.length}/${DOCUMENT_TITLE_MAX})`,
                    'success'
                );
            }
        });
    }

    if (descInput && descHint) {
        descInput.addEventListener('input', () => {
            enforceMaxLength(descInput, POPUP_TEMPLATE_DESC_MAX);

            const v = descInput.value.trim();
            if (!v) {
                descHint.textContent = '';
                descHint.className = 'hint';
                return;
            }

            if (v.length > POPUP_TEMPLATE_DESC_MAX) {
                showMsg(
                    descHint,
                    `목적은 ${POPUP_TEMPLATE_DESC_MAX}자 이내로 입력하세요.`,
                    'error'
                );
            } else {
                showMsg(
                    descHint,
                    `입력됨 (${v.length}/${POPUP_TEMPLATE_DESC_MAX})`,
                    'success'
                );
            }
        });
    }
}

// ============================
// 10. 이벤트 바인딩 관련 함수
// ============================


function bindSaveFormButton() {
    const saveBtn = document.getElementById('save-form-btn');
    if (!saveBtn) return;
    saveBtn.addEventListener('click', async function () {
        if (!validateDocumentSettingPanel()) return;
        if (!validateAllComponents()) return;
        saveBtn.disabled = true;
        try {
            await saveFormTemplateStructure(
                typeof templateId !== "undefined" ? templateId : undefined
            );
        } finally {
            saveBtn.disabled = false;
        }
    });
}

function observeComponentButtonHeight() {
    const list = document.getElementById('component-btn-list');
    if (!list) return;
    const equalizeHeights = () => {
        const btns = list.querySelectorAll('.component-btn');
        let maxH = 0;
        btns.forEach(b => {
            b.style.height = '';
            maxH = Math.max(maxH, b.offsetHeight);
        });
        btns.forEach(b => b.style.height = maxH + 'px');
    };
    new MutationObserver(equalizeHeights)
        .observe(list, {childList: true});
    equalizeHeights();
}

function bindStepNavigationButtons() {
    const prevBtn = document.getElementById('before-form-btn');
    const nextBtn = document.getElementById('next-form-btn');
    // 이전
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            window.history.back();
        });
    }
    // 다음
    if (nextBtn) {
        nextBtn.addEventListener('click', async () => {
            if (!templateId) {
                await sweetWarning("템플릿 ID가 없어 다음 단계로 이동할 수 없습니다.");
                return;
            }
            nextBtn.disabled = true;
            // 저장+검증
            const saved = await saveFormTemplateStructure(templateId);
            nextBtn.disabled = false;
            if (!saved) return;
            window.location.href =
                `/view/admin/approval-rule?templateId=${templateId}`;
        });
    }
}

function bindPreviewButton() {
    const previewBtn = document.querySelector('.preview-btn');
    if (!previewBtn) return;
    previewBtn.addEventListener('click', async () => {
        if (typeof templateId === "undefined" || !templateId) {
            await sweetWarning("템플릿 ID가 없어 미리보기를 열 수 없습니다.");
            return;
        }
        window.location.href = `/view/admin/preview-template/${templateId}`;
    });
}

// =======================
// 11. 초기화(로드 진입점)
// =======================

document.addEventListener('DOMContentLoaded', async function () {
    let resolvedGroupId = null;

    bindAutoClearInvalidFocus();

    // =====================================
    // 1. 템플릿 로드 (구조만 사용)
    // =====================================
    if (typeof templateId !== "undefined" && templateId) {
        try {
            const res = await apiFetch(`/api/form-templates/${templateId}`);
            if (res.ok) {
                const result = await res.json();
                const data = result?.data;

                // 🔹 필드 구조만 로드
                if (data?.templateJson) {
                    loadTemplateJsonToFormComponents(
                        data.templateJson,
                        data.baseRole // ⚠️ baseRole은 그룹 로드 후 덮어씀
                    );
                }

                // 🔹 groupId 확보
                resolvedGroupId = data?.templateGroupId ?? null;

                // 🔹 문서 제목 동기화
                const titleComp = formComponents.find(c => c.type === "document-title");
                const titleInput = document.getElementById("form-title-input");
                if (titleComp && titleInput) {
                    titleInput.value = titleComp.meta?.value ?? "";
                }
            }
        } catch (e) {
            console.warn('[FormTemplate] 상세 로드 실패', e);
        }
    }

    // =====================================
    // 2. 그룹 정보 로드 (정책 기준)
    // =====================================
    if (resolvedGroupId && typeof loadFormTemplateGroupInfo === 'function') {
        await loadFormTemplateGroupInfo(resolvedGroupId);

        // 🔑 카테고리 그룹 기준 고정 반영
        applyGroupCategoryToDocumentSetting();
        formComponents.forEach(comp => {
            if (comp.type === "event-date-range") {
                applyEventComponentPolicy(comp, templateGroupBaseRole);
            }
        });
    }

    // =====================================
    // 3. UI 초기화
    // =====================================
    initComponentListPanel();
    renderFormComponents();
    bindSaveFormButton();
    bindStepNavigationButtons();
    bindPreviewButton();
    observeComponentButtonHeight();
    bindDocumentTitleSync();
    bindAiGeneratePopup();
    bindPopupLiveHints();
});

