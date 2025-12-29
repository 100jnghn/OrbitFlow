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
    "leave-date-range",
    "schedule-date-range",
    "notice",
    "table",
    "image",
    "currency",
    "address",
    "employee-search",
    "department-search"
];

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
        label: "체크박스",
        required: false,
        meta: {
            options: [
                {id: 'opt1', label: '옵션 1'}
            ],
            minSelected: undefined,
            maxSelected: undefined
        }
    },
    "leave-date-range": {
        type: "leave-date-range",
        label: "휴가 일정",
        required: false,
        meta: {
            startLabel: "",
            endLabel: ""
        }
    },
    "leave-reason": {
        type: "leave-reason",
        label: "휴가 사유",
        required: true,
        meta: {
            vacationTypeCode: null,     // 선택된 대분류 코드
            vacationTypeName: null,     // 표시용
            deductAttendance: null,     // ⭐ 서버 기준값
            detailReason: "",           // 사용자 입력
            options: []                 // 드롭다운 목록 (API)
        }
    },
    "schedule-date-range": {
        type: "schedule-date-range",
        label: "일정",
        required: false,
        meta: {
            startLabel: "시작일",
            endLabel: "종료일",
            description: ""   // ⬅️ 텍스트 입력
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
            src: "",
            alt: ""
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
        label: "부서 검색",
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
let documentSettings = {                  // 문서 전역 설정
    autoReflectAttendance: false,
    autoReflectSchedule: false
};

// -- 입력 제한/정책 상수
const DOCUMENT_TITLE_MAX = 50;
const COMPONENT_LABEL_MAX = 20;
const NOTICE_MESSAGE_MAX = 200;
const TABLE_ROW_MIN = 1;
const TABLE_ROW_MAX = 10;
const TABLE_COLUMN_MAX = 10;
let dragBlockSize = 1;

// =====================
// 3. 유틸리티 함수
// =====================

function syncDocumentPolicyToggles() {
    const hasLeave = formComponents.some(c =>
        c.type === 'leave-date-range' || c.type === 'leave-reason'
    );
    const hasSchedule = formComponents.some(c =>
        c.type === 'schedule-date-range'
    );

    const attToggle = document.getElementById('toggle-auto-attendance');
    const schToggle = document.getElementById('toggle-auto-schedule');

    /* =====================
       근태 자동 반영
       - 휴가 있으면 필수
    ===================== */
    if (hasLeave) {
        documentSettings.autoReflectAttendance = true;
        if (attToggle) {
            attToggle.checked = true;
            attToggle.disabled = true;
        }
    } else if (attToggle) {
        attToggle.disabled = false;
        attToggle.checked = documentSettings.autoReflectAttendance;
    }

    /* =====================
       일정 자동 반영
       - 휴가 or 일정 있으면 필수
    ===================== */
    if (hasLeave || hasSchedule) {
        documentSettings.autoReflectSchedule = true;
        if (schToggle) {
            schToggle.checked = true;
            schToggle.disabled = true;
        }
    } else if (schToggle) {
        schToggle.disabled = false;
        schToggle.checked = documentSettings.autoReflectSchedule;
    }
}


function bindAutoClearInvalidFocus() {

    const clearInvalid = (target) => {
        if (!(target instanceof HTMLElement)) return;

        // 1. invalid-focus 제거
        document
            .querySelectorAll('.invalid-focus')
            .forEach(el => el.classList.remove('invalid-focus'));

        // 2. setting-row 내부 hint 제거 (컴포넌트 패널)
        const row = target.closest('.setting-row');
        if (row) {
            const hint = row.querySelector('.hint.active');
            if (hint) {
                hint.className = 'hint';
                hint.textContent = '';
            }
        }

        // 3. 🔑 문서 설정 패널 전용 hint 제거 (ID 기반)
        if (target.id === 'form-title-input') {
            clearHint('title-hint');
        }

        if (target.id === 'form-category-input') {
            clearHint('category-hint');
        }
    };

    document.addEventListener('input', e => clearInvalid(e.target));
    document.addEventListener('change', e => clearInvalid(e.target));
}


function normalizeLeaveComponentSet(components) {
    const hasLeaveDate = components.some(c => c.type === "leave-date-range");
    const hasLeaveReason = components.some(c => c.type === "leave-reason");

    // 휴가 사유만 있으면 제거
    if (hasLeaveReason && !hasLeaveDate) {
        components = components.filter(c => c.type !== "leave-reason");
    }

    // 휴가 일정만 있으면 사유 자동 생성
    if (hasLeaveDate && !hasLeaveReason) {
        const dateIdx = components.findIndex(c => c.type === "leave-date-range");
        const groupId = generateId();

        components[dateIdx]._groupId = groupId;

        components.splice(dateIdx + 1, 0, {
            id: generateId(),
            type: "leave-reason",
            label: "휴가 사유",
            required: true,
            fixed: false,
            meta: {
                vacationTypeCode: null,
                vacationTypeName: null,
                deductAttendance: null,
                detailReason: "",
                options: []
            },
            _groupId: groupId,
            _linkedTo: components[dateIdx].id
        });
    }

    return components;
}


function focusComponentError({
                                 componentId,
                                 panelField,      // selector or function
                                 message,
                                 autoFocus = true
                             }) {
    // 1. 중앙 패널 강조
    if (componentId) {
        highlightComponent(componentId);
        selectedComponentId = componentId;
        showComponentSettingPanel(componentId);
    }

    requestAnimationFrame(() => {
        let field = null;

        document.querySelectorAll('.invalid-focus')
            .forEach(el => el.classList.remove('invalid-focus'));

        // 2. 필드 결정 방식 (핵심)
        if (typeof panelField === "function") {
            field = panelField();
        } else if (typeof panelField === "string") {
            field = document.querySelector(panelField);
        }

        // 3. fallback (패널 자체)
        if (!field) {
            field = document.getElementById('component-setting-content');
        }

        // 4. 스타일 & 포커스
        field.classList.add('invalid-focus');

        if (autoFocus && typeof field.focus === 'function') {
            field.focus({preventScroll: true});
        }

        // 5. hint 처리
        const hint =
            field.closest('.setting-row')
                ?.querySelector('.hint');

        if (hint && message) {
            hint.textContent = message;
            hint.classList.add('active');
        }
    });
}


function focusDocumentSettingError({field, hintId, message}) {
    // 기존 invalid-focus 제거
    document
        .querySelectorAll('.invalid-focus')
        .forEach(el => el.classList.remove('invalid-focus'));

    // 포커스 + 스타일
    if (field) {
        field.classList.add('invalid-focus');
        field.focus({preventScroll: false});
    }

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
    if (!el) return;
    el.textContent = message;
    el.className = 'hint ' + type;
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

    // 🚫 휴가 사유는 단독 삭제 불가
    if (comp.type === "leave-reason") {
        alert("휴가 사유는 휴가 일정과 함께 삭제됩니다.");
        return;
    }

    // ✅ 휴가 일정 삭제 시 → 바로 뒤 휴가 사유도 같이 삭제
    if (comp.type === "leave-date-range") {
        const next = formComponents[idx + 1];
        if (next && next.type === "leave-reason") {
            formComponents.splice(idx, 2); // 두 개 같이 제거
        } else {
            // 이론상 없어야 하지만, 안전장치
            formComponents.splice(idx, 1);
        }
    } else {
        // 일반 컴포넌트
        formComponents.splice(idx, 1);
    }

    // 선택 상태 정리
    if (selectedComponentId === componentId) {
        selectedComponentId = null;
        const panel = document.getElementById('component-setting-content');
        if (panel) {
            panel.innerHTML = `<span style="color:#9ab;">컴포넌트를 선택하세요.</span>`;
        }
    }

    renderFormComponents();
    updateLeaveComponentButtonState();
    syncDocumentPolicyToggles();
}


function updateLeaveComponentButtonState() {
    const btn = document.querySelector(
        '.component-btn[data-type="leave-date-range"]'
    );
    if (!btn) return;

    const hasLeave = hasLeaveComponentSet();

    btn.classList.toggle('is-disabled', hasLeave);
    btn.title = hasLeave
        ? "이미 추가된 휴가 컴포넌트가 있습니다."
        : "";
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
        focusDocumentSettingError({field: titleInput});
        showMsg(titleHint, '문서 제목은 필수입니다.', 'error');
        return false;
    }

    if (title.length > DOCUMENT_TITLE_MAX) {
        focusDocumentSettingError({field: titleInput});
        showMsg(
            titleHint,
            `문서 제목은 ${DOCUMENT_TITLE_MAX}자 이하로 입력하세요.`,
            'error'
        );
        return false;
    }

    titleHint.textContent = '';
    titleHint.className = 'hint';

    // 카테고리
    if (!category) {
        focusDocumentSettingError({field: categorySelect});
        showMsg(categoryHint, '카테고리를 선택하세요.', 'error');
        return false;
    }

    categoryHint.textContent = '';
    categoryHint.className = 'hint';

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
    if (options.length === 0) {
        focusComponentError({
            componentId: comp.id,
            panelField: '#option-list-pane',
            message: '옵션은 최소 1개 이상 필요합니다.'
        });
        return false;
    }
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
                panelField: () =>
                    document.querySelectorAll('#table-column-list input')[i],
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

function bindDocumentTitleValidation() {
    const titleInput = document.getElementById('form-title-input');
    if (!titleInput) return;
    titleInput.addEventListener('input', () => {
        enforceMaxLength(titleInput, DOCUMENT_TITLE_MAX);
        validateDocumentTitle();
    });
}


function validateLeavePolicyContract() {

    const hasLeave = formComponents.some(c =>
        c.type === "leave-date-range" ||
        c.type === "leave-reason"
    );

    const hasSchedule = formComponents.some(c =>
        c.type === "schedule-date-range"
    );

    const attendanceOn = documentSettings.autoReflectAttendance === true;
    const scheduleOn = documentSettings.autoReflectSchedule === true;

    /* ==========================
       1️⃣ 근태 자동 반영 → 휴가 필수
       (일정만 있어도 ❌)
    ========================== */
    if (attendanceOn && !hasLeave) {
        alert('근태 자동 반영을 사용하려면 휴가 컴포넌트가 필요합니다.');

        focusComponentError({
            componentId: null,
            panelField: '#toggle-auto-attendance',
            message: '근태 자동 반영은 휴가 컴포넌트와 함께 사용해야 합니다.',
            autoFocus: false
        });

        return false;
    }

    /* ==========================
       2️⃣ 휴가 사용 시 → 일정 자동 반영 필수
    ========================== */
    if (hasLeave && !scheduleOn) {
        const leaveComp = formComponents.find(c =>
            c.type === 'leave-date-range'
        );

        focusComponentError({
            componentId: leaveComp?.id,
            panelField: '#toggle-auto-schedule',
            message: '휴가 컴포넌트 사용 시 일정 자동 반영은 필수입니다.',
            autoFocus: false
        });
        return false;
    }

    /* ==========================
       3️⃣ 일정 컴포넌트 사용 시 → 일정 자동 반영 필수
    ========================== */
    if (hasSchedule && !scheduleOn) {
        const scheduleComp = formComponents.find(c =>
            c.type === 'schedule-date-range'
        );

        focusComponentError({
            componentId: scheduleComp?.id,
            panelField: '#toggle-auto-schedule',
            message: '일정 컴포넌트를 사용하는 경우 일정 자동 반영은 필수입니다.',
            autoFocus: false
        });
        return false;
    }

    /* ==========================
       4️⃣ 일정 자동 반영 ON → 일정 or 휴가 필요
    ========================== */
    if (scheduleOn && !hasLeave && !hasSchedule) {
        alert('일정 자동 반영을 사용하려면 일정 또는 휴가 컴포넌트를 추가해야 합니다.');

        focusComponentError({
            componentId: null,
            panelField: '.component-btn[data-type="schedule-date-range"]',
            message: '일정 자동 반영을 사용하려면 일정 컴포넌트를 추가하세요.',
            autoFocus: false
        });
        return false;
    }

    return true;
}


// ========================
// 5. 컴포넌트 관리 관련 함수
// ========================

function addComponentByType(type) {
    if (!FORM_COMPONENT_SCHEMAS.hasOwnProperty(type)) {
        console.warn(`[FormTemplateBuilder] 컴포넌트 타입 '${type}'는 스키마에 정의되어 있지 않습니다.`);
        return;
    }

    // 휴가 컴포넌트 (기존)
    if (type === "leave-date-range") {
        addLeaveDateRangeWithReason();
        return;
    }


    const schema = FORM_COMPONENT_SCHEMAS[type];
    const metaCopy = deepCopy(schema.meta);

    if (type === "radio" || type === "checkbox") {
        metaCopy.options = metaCopy.options && metaCopy.options.length > 0
            ? deepCopy(metaCopy.options.map(opt =>
                typeof opt === "string"
                    ? {id: generateOptionId(), label: opt}
                    : {...opt, id: generateOptionId()}
            ))
            : [{id: generateOptionId(), label: "옵션 1"}];
    }

    const component = {
        id: generateId(),
        type: schema.type,
        label: schema.label,
        required: schema.required,
        fixed: false,
        meta: metaCopy
    };

    // document-meta 앞에 삽입
    formComponents.splice(formComponents.length - 1, 0, component);

    selectedComponentId = component.id;
    renderFormComponents();
    showComponentSettingPanel(component.id);
}

function hasLeaveComponentSet() {
    return formComponents.some(c =>
        c.type === "leave-date-range" ||
        c.type === "leave-reason"
    );
}

function addLeaveDateRangeWithReason() {
    if (hasLeaveComponentSet()) {
        alert("휴가 컴포넌트는 문서에 한 번만 추가할 수 있습니다.");
        return;
    }

    const groupId = generateId();

    const leaveDateComp = {
        id: generateId(),
        type: "leave-date-range",
        label: "휴가 일정",
        required: true,
        fixed: false,
        meta: {
            startLabel: "시작일",
            endLabel: "종료일"
        },
        _groupId: groupId
    };

    const leaveReasonComp = {
        id: generateId(),
        type: "leave-reason",
        label: "휴가 사유",
        required: true,
        fixed: false,
        meta: {
            vacationTypeCode: null,
            vacationTypeName: null,
            deductAttendance: null,
            detailReason: "",
            options: []
        },
        _groupId: groupId,
        _linkedTo: leaveDateComp.id
    };

    // document-meta 앞에 세트 삽입
    const insertIdx = formComponents.length - 1;
    formComponents.splice(insertIdx, 0, leaveDateComp, leaveReasonComp);

    selectedComponentId = leaveDateComp.id;

    // 🔒 계약 강제 (상태)
    documentSettings.autoReflectAttendance = true;
    documentSettings.autoReflectSchedule = true;

    renderFormComponents();
    showComponentSettingPanel(leaveDateComp.id);
    updateLeaveComponentButtonState();
    syncDocumentPolicyToggles();
}


function initComponentListPanel() {

    const listContainer = document.getElementById("component-btn-list");
    if (!listContainer) return;
    listContainer.innerHTML = "";

    const hasLeave = hasLeaveComponentSet(); // ⭐ 현재 상태 한번 계산

    FORM_COMPONENT_TYPES.forEach(function (type) {
        if (!FORM_COMPONENT_SCHEMAS[type]) return;

        const btn = document.createElement("button");
        btn.className = "component-btn";
        btn.type = "button";
        btn.setAttribute("data-type", type);
        btn.innerText = FORM_COMPONENT_SCHEMAS[type].label || type;


        btn.addEventListener("click", function () {

            // 🔒 휴가 컴포넌트는 1회만 허용
            if (type === "leave-date-range" && hasLeaveComponentSet()) {
                const leaveComp = formComponents.find(c =>
                    c.type === "leave-date-range"
                );

                alert("휴가 컴포넌트는 문서에 한 번만 추가할 수 있습니다.");

                // 👉 기존 휴가 컴포넌트로 이동 + 설정 패널 열기
                if (leaveComp) {
                    selectedComponentId = leaveComp.id;
                    renderFormComponents();
                    showComponentSettingPanel(leaveComp.id);
                }
                return;
            }

            // 일반 컴포넌트 추가
            addComponentByType(type);
            syncDocumentPolicyToggles();
        });


        listContainer.appendChild(btn);
    });
    updateLeaveComponentButtonState();
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

        // 상태 class
        if (comp.fixed) row.classList.add("fixed");
        if (comp.id === selectedComponentId) row.classList.add("selected");

        // Drag & Drop
        if (!comp.fixed) {

            // 🚫 휴가 사유는 단독 드래그 불가
            if (comp.type === "leave-reason") {
                row.draggable = false;
            } else {
                row.draggable = true;
            }

            row.addEventListener("dragstart", e => {
                dragSrcIdx = idx;
                dragBlockSize = 1;

                // ✅ 휴가 일정이면 + 휴가 사유까지 같이 이동
                if (comp.type === "leave-date-range") {
                    const next = formComponents[idx + 1];
                    if (next && next.type === "leave-reason") {
                        dragBlockSize = 2;
                    }
                }

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

            // 🚫 휴가 사유 앞에 drop 금지
            if (formComponents[idx]?.type === "leave-reason") {
                return;
            }

            // 🔹 이동할 블록 추출
            const draggedBlock = formComponents.splice(dragSrcIdx, dragBlockSize);

            // 🔹 삽입 위치 계산
            let insertIdx = idx;
            if (dragSrcIdx < idx) {
                insertIdx = idx - dragBlockSize + 1;
            }

            formComponents.splice(insertIdx, 0, ...draggedBlock);

            // 🔹 fixed 컴포넌트 재정렬
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

            dragSrcIdx = null;
            dragBlockSize = 1;
        });


        // 클릭 선택
        if (!comp.fixed) {
            row.addEventListener("click", () => {
                if (row.classList.contains("dragging")) return;
                selectedComponentId = comp.id;
                renderFormComponents();
                showComponentSettingPanel(comp.id);
            });

        }

        // 타입별 미리보기
        if (comp.fixed) {
            if (comp.type === "document-title") {
                row.innerHTML = `
                    <strong class="doc-title-label">${comp.label}</strong>
                    <input
                        type="text"
                        disabled
                        value="${comp.meta?.value ?? ""}"
                        placeholder="${comp.meta?.placeholder ?? ""}"
                        class="doc-title-input"
                    />
                `;
            } else if (comp.type === "document-meta") {
                row.innerHTML = `
                    <span class="doc-meta-label">${comp.label}</span>
                    <span class="doc-meta-auto">자동입력</span>
                `;
            }
        } else {
            let labelHtml = `
<span class="comp-label ${comp.required ? 'is-required' : ''}">
    ${comp.label || ""}
  </span>
`;

            let inputHtml = "";
            if (["text", "number", "currency"].includes(comp.type)) {
                inputHtml = `
                    <input
                        type="${comp.type === "number" ? "number" : "text"}"
                        disabled
                        class="preview-input"
                        placeholder="${comp.meta?.placeholder ?? ""}"
                    />
                    ${comp.type === "currency"
                    ? `<span class="unit">${comp.meta?.unit ?? ""}</span>`
                    : ""}
                `;
            } else if (comp.type === "textarea") {
                inputHtml = `
                    <textarea
                        disabled
                        class="preview-textarea"
                        placeholder="${comp.meta?.placeholder ?? ""}">
                    </textarea>
                `;
            } else if (comp.type === "radio" || comp.type === "checkbox") {
                inputHtml = `
                    <div class="option-preview">
                        ${(comp.meta?.options ?? []).map(opt => `
                            <label>
                                <input type="${comp.type}" disabled />
                                ${opt.label}
                            </label>
                        `).join("")}
                    </div>
                `;
            } else if (comp.type === "date") {
                inputHtml = `<input type="date" disabled class="preview-input" />`;
            } else if (comp.type === "time") {
                inputHtml = `<input type="time" disabled class="preview-input" />`;
            } else if (["date-range", "leave-date-range"].includes(comp.type)) {
                const left = comp.meta?.startLabel || "시작";
                const right = comp.meta?.endLabel || "종료";
                inputHtml = `
                    <div class="range-preview">
                        <input type="date" disabled placeholder="${left}" />
                        <span class="range-sep">~</span>
                        <input type="date" disabled placeholder="${right}" />
                    </div>
                `;
            } else if (comp.type === "time-range") {
                const left = comp.meta?.startLabel || "시작";
                const right = comp.meta?.endLabel || "종료";
                inputHtml = `
                    <div class="range-preview">
                        <input type="time" disabled placeholder="${left}" />
                        <span class="range-sep">~</span>
                        <input type="time" disabled placeholder="${right}" />
                    </div>
                `;
            } else if (comp.type === "divider") {
                row.innerHTML = `
                    <div class="divider-preview">
                        <span></span>
                        <em>구분선</em>
                        <span></span>
                    </div>
                `;
            } else if (comp.type === "notice") {
                const message =
                    (comp.meta?.message ?? "안내 문구 예시")
                        .replace(/\n/g, "<br>");

                inputHtml = `
<div class="notice-message-box ${comp.meta?.style ?? "info"}">${message}</div>
`;//화면에서의 공백 문제때문에 일부러 들여쓰기 X
            } else if (comp.type === "leave-reason") {
                const options = comp.meta?.options ?? [];
                const selectedCode = comp.meta?.vacationTypeCode;

                inputHtml = `
                        <div class="leave-reason-body">
                            <select disabled>
                                <option value="">휴가 유형 선택</option>
                                ${options.map(opt => `
                                    <option value="${opt.code}"
                                        ${opt.code === selectedCode ? "selected" : ""}>
                                        ${opt.name}
                                    </option>
                                `).join("")}
                            </select>
                
                            <textarea
                                disabled
                                class="leave-reason-textarea"
                                placeholder="상세 휴가 사유를 입력하세요.">
                            </textarea>
                        </div>
                    `;
            } else if (comp.type === "table") {
                const cols = comp.meta?.columns ?? [];
                const minRows = Math.max(1, comp.meta?.rowPolicy?.min ?? 1);
                inputHtml = `
                    <table class="table-preview">
                        <thead>
                            <tr>
                                ${cols.map(col => `
                                    <th class="${col.required ? 'is-required' : ''}">
                                      ${col.label}
                                    </th>
                                `).join("")}
                            </tr>
                        </thead>
                        <tbody>
                            ${Array.from({length: minRows}).map(() => `
                                <tr>
                                    ${cols.map(() => `
                                        <td><input disabled /></td>
                                    `).join("")}
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                `;
            } else if (comp.type === "image") {
                const src = comp.meta?.src;
                const alt = comp.meta?.alt || "이미지 미리보기";
                inputHtml = `
                    <div class="image-preview-box">
                        ${src
                    ? `<img src="${src}" alt="${alt}" />`
                    : `<span class="image-placeholder">이미지</span>`
                }
                    </div>
                `;
            } else if (comp.type === "file") {
                inputHtml = `
                    <button type="button" class="file-preview-btn" disabled>
                        파일 선택
                    </button>
                `;
            } else if (comp.type === "address") {
                inputHtml = `
                    <input
                        type="text"
                        disabled
                        class="preview-input wide"
                        placeholder="주소 입력 (형식 검증)"
                    />
                `;
            } else if (comp.type === "employee-search") {
                inputHtml = `
                    <input
                        type="text"
                        disabled
                        class="preview-input"
                        placeholder="사원 검색"
                    />
                `;
            } else if (comp.type === "department-search") {
                inputHtml = `
                    <input
                        type="text"
                        disabled
                        class="preview-input"
                        placeholder="부서 검색"
                    />
                `;
            } else if (comp.type === "schedule-date-range") {
                const left = comp.meta?.startLabel || "시작일";
                const right = comp.meta?.endLabel || "종료일";

                // ⬇️ inputHtml에는 우측 영역만!
                inputHtml = `
                        <div class="schedule-body">
                            <div class="schedule-range">
                                <input type="date" disabled placeholder="${left}" />
                                <span class="range-sep">~</span>
                                <input type="date" disabled placeholder="${right}" />
                            </div>
                
                            <textarea
                                disabled
                                class="schedule-desc"
                                placeholder="일정 내용을 입력하세요.">
                            </textarea>
                        </div>
                    `;
            }


            if (!["divider"].includes(comp.type)) {
                row.innerHTML = `
                    <div class="preview-row">
                        ${labelHtml}
                        ${inputHtml}
                    </div>
                `;
            }
        }

        // 삭제 버튼 (고정형 제외)
        if (!comp.fixed) {
            const deleteBtn = document.createElement("button");
            deleteBtn.className = "delete-btn";
            deleteBtn.type = "button";
            deleteBtn.innerHTML = "🗑️";
            deleteBtn.title = "컴포넌트 삭제";

            deleteBtn.addEventListener("click", e => {
                e.stopPropagation();
                if (confirm("이 컴포넌트를 삭제하시겠습니까?")) {
                    removeComponent(comp.id);
                }
            });

            row.appendChild(deleteBtn);
        }
        container.appendChild(row);
    });

    syncDocumentPolicyToggles();

}

function enforceSystemRequiredRules() {
    formComponents.forEach(comp => {
        if (
            comp.type === "leave-date-range" ||
            comp.type === "leave-reason"
        ) {
            comp.required = true;
        }
    });
}


// ================================
// 7. 컴포넌트 설정 패널(사이드)
// ================================

function showComponentSettingPanel(componentId) {
    const comp = formComponents.find(c => c.id === componentId);
    const panel = document.getElementById('component-setting-content');
    if (!comp) {
        panel.innerHTML = `<span style="color:#9ab;">컴포넌트를 선택하세요.</span>`;
        return;
    }

    const isFixed = !!comp.fixed;

    /* ======================
       공통 정보 영역
    ====================== */
    let html = `
    <div style="margin-bottom:11px;">
        <div style="font-weight:600;margin-bottom:4px;">${comp.label || ''}</div>
        <div style="color:#888;font-size:0.97em;">타입: ${comp.type}</div>
    </div>

    <div class="setting-row" style="display:flex;flex-direction:column;">
        <label style="margin-bottom:4px;">이름</label>
        <input
            type="text"
            id="input-comp-label"
            value="${(comp.label || '').replace(/"/g, '&quot;')}"
            ${isFixed ? 'disabled' : ''}
        >
        <div class="hint" id="component-label-hint"></div>
    </div>
    `;

    const isForceRequired =
        comp.type === "leave-date-range" ||
        comp.type === "leave-reason";

    if (!isFixed && !isForceRequired) {
        html += `
        <div class="setting-row" style="margin-top:8px;">
            <label style="flex:1;">필수 입력</label>
            <input type="checkbox" id="toggle-required"
                ${comp.required ? "checked" : ""}>
        </div>`;
    }

    if (isForceRequired) {
        html += `
        <div class="setting-row" style="margin-top:8px;color:#888;font-size:0.9em;">
            ⚠ 이 항목은 근태 반영을 위해 필수 입력 항목입니다.
        </div>`;
    }

    /* ======================
       notice
    ====================== */
    if (comp.type === "notice") {
        html += `
        <div class="setting-row" style="margin-top:14px;display:flex;flex-direction:column;">
            <label style="margin-bottom:4px;">안내 문구</label>
            <textarea
                id="input-notice-message"
                style="width:100%;min-height:60px;"
            >${comp.meta?.message ?? ""}</textarea>
            <div class="hint" id="notice-message-hint"></div>
        </div>`;
    }

    /* ======================
       schedule-date-range ✅ 추가
    ====================== */


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
        </div>`;
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

        <div class="setting-row" style="margin-top:14px;">
            <div style="font-weight:600;">행 설정</div>
            <label>최소 행 <input type="number" id="table-row-min" value="${comp.meta.rowPolicy.min}"></label><br>
            <label>최대 행 <input type="number" id="table-row-max" value="${comp.meta.rowPolicy.max ?? ""}"></label><br>
            <label><input type="checkbox" id="table-row-addable" ${comp.meta.rowPolicy.addable ? "checked" : ""}> 행 추가 가능</label><br>
            <label><input type="checkbox" id="table-row-removable" ${comp.meta.rowPolicy.removable ? "checked" : ""}> 행 삭제 가능</label>
        </div>`;
    }

    panel.innerHTML = html;

    /* ======================
       공통 이벤트
    ====================== */
    if (!isFixed) {
        const labelInput = document.getElementById("input-comp-label");
        const labelHint = document.getElementById("component-label-hint");

        labelInput.addEventListener("input", () => {
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
       notice 이벤트
    ====================== */
    if (comp.type === "notice") {
        const textarea = document.getElementById("input-notice-message");
        const hint = document.getElementById("notice-message-hint");

        textarea.addEventListener("input", () => {
            enforceMaxLength(textarea, NOTICE_MESSAGE_MAX);
            comp.meta.message = textarea.value;
            validateNoticeMessage(textarea, hint);
            renderFormComponents();
        });
    }


    /* ======================
       radio / checkbox 옵션
    ====================== */
    if (comp.type === "radio" || comp.type === "checkbox") {
        const pane = document.getElementById("option-list-pane");

        const renderOptions = () => {
            pane.innerHTML = "";
            comp.meta.options.forEach((opt, idx) => {
                const row = document.createElement("div");
                row.innerHTML = `
                <div style="display:flex;flex-direction:column;gap:4px;">
                    <div style="display:flex;gap:6px;">
                        <input value="${opt.label}" style="flex:1;">
                        <button type="button">🗑️</button>
                    </div>
                    <div class="hint option-hint"></div>
                </div>`;

                const input = row.querySelector("input");
                const hint = row.querySelector(".option-hint");
                const del = row.querySelector("button");

                input.addEventListener("input", () => {
                    enforceMaxLength(input, COMPONENT_LABEL_MAX);
                    opt.label = input.value;
                    validateOptionLabel(input, hint);
                    renderFormComponents();
                });

                del.onclick = () => {
                    if (comp.meta.options.length > 1) {
                        comp.meta.options.splice(idx, 1);
                        renderOptions();
                        renderFormComponents();
                    }
                };

                pane.appendChild(row);
            });
        };

        renderOptions();
        document.getElementById("add-option-btn").onclick = () => {
            comp.meta.options.push({id: generateOptionId(), label: "옵션"});
            renderOptions();
            renderFormComponents();
        };
    }

    /* ======================
       table 이벤트
    ====================== */
    if (comp.type === "table") {
        const list = document.getElementById("table-column-list");

        const renderColumns = () => {
            list.innerHTML = "";
            comp.meta.columns.forEach((col, idx) => {
                const row = document.createElement("div");
                row.innerHTML = `
                <div style="display:flex;flex-direction:column;gap:4px;margin-bottom:6px;">
                    <div style="display:flex;gap:6px;">
                        <input value="${col.label}" style="flex:1;">
                        <select>
                            ${["text", "number", "currency"].map(t =>
                    `<option value="${t}" ${t === col.type ? "selected" : ""}>${t}</option>`
                ).join("")}
                        </select>
                        <input type="checkbox" ${col.required ? "checked" : ""}>
                        <button type="button">🗑️</button>
                    </div>
                    <div class="hint column-hint"></div>
                </div>`;

                const label = row.querySelector("input");
                const hint = row.querySelector(".column-hint");
                const type = row.querySelector("select");
                const req = row.querySelector('input[type="checkbox"]');
                const del = row.querySelector("button");

                label.addEventListener("input", () => {
                    enforceMaxLength(label, COMPONENT_LABEL_MAX);
                    col.label = label.value;
                    validateComponentLabel(label, hint);
                    renderFormComponents();
                });

                type.onchange = e => {
                    col.type = e.target.value;
                    renderFormComponents();
                };

                req.onchange = e => {
                    col.required = e.target.checked;
                    renderFormComponents();
                };

                del.onclick = () => {
                    comp.meta.columns.splice(idx, 1);
                    renderColumns();
                    renderFormComponents();
                };

                list.appendChild(row);
            });
        };

        renderColumns();

        document.getElementById("add-table-column-btn").onclick = () => {
            if (comp.meta.columns.length >= TABLE_COLUMN_MAX) {
                alert(`컬럼은 최대 ${TABLE_COLUMN_MAX}개까지 추가할 수 있습니다.`);
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
        };

        document.getElementById("table-row-min").oninput = e => {
            let v = Number(e.target.value);
            if (!v || v < TABLE_ROW_MIN) v = TABLE_ROW_MIN;
            if (v > TABLE_ROW_MAX) v = TABLE_ROW_MAX;
            e.target.value = v;
            comp.meta.rowPolicy.min = v;
            renderFormComponents();
        };

        document.getElementById("table-row-max").oninput = e => {
            let max = Number(e.target.value);
            if (e.target.value === "") {
                comp.meta.rowPolicy.max = undefined;
                renderFormComponents();
                return;
            }
            max = Math.min(TABLE_ROW_MAX, Math.max(TABLE_ROW_MIN, max));
            if (max < comp.meta.rowPolicy.min) {
                max = comp.meta.rowPolicy.min;
            }
            comp.meta.rowPolicy.max = max;
            e.target.value = max;
            renderFormComponents();
        };

        document.getElementById("table-row-addable").onchange =
            e => comp.meta.rowPolicy.addable = e.target.checked;

        document.getElementById("table-row-removable").onchange =
            e => comp.meta.rowPolicy.removable = e.target.checked;
    }
}

// ============================
// 8. 데이터 변환(저장/로드) 함수
// ============================

function buildAffectTags(settings) {
    const tags = [];
    if (settings.autoReflectAttendance) tags.push("ATTENDANCE");
    if (settings.autoReflectSchedule) tags.push("SCHEDULE");
    return tags;
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

function loadTemplateJsonToFormComponents(templateJson) {
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

    // 휴가 컴포넌트 계약 보정
    components = normalizeLeaveComponentSet(components);

    // document-meta 항상 마지막
    components = components.filter(c => c.type !== "document-meta");
    components.push({
        ...FIXED_COMPONENTS.find(c => c.type === "document-meta"),
        id: "document-meta"
    });

    formComponents = components;

    // 휴가 정책 → 문서 설정 동기화
    if (components.some(c => c.type === "leave-date-range")) {
        documentSettings.autoReflectAttendance = true;
        documentSettings.autoReflectSchedule = true;
    }

    if (components.some(c => c.type === "schedule-date-range")) {
        documentSettings.autoReflectSchedule = true;
    }

    syncDocumentPolicyToggles();
}

// ================================
// 9. 저장/로드 및 그룹정보 함수
// ================================

async function saveFormTemplateStructure(templateId) {
    if (!templateId) {
        alert('저장할 문서 양식 ID(templateId)가 존재하지 않습니다.');
        return false;
    }
    // 검증
    if (!validateDocumentSettingPanel()) return false;
    if (!validateAllComponents()) return false;
    if (!validateLeavePolicyContract()) return false;

    const filteredComponents = formComponents
        .filter(fc => fc.type !== 'document-meta')
        .map(fc => {
            const comp = {...fc};
            delete comp.fixed;
            delete comp.selected;
            return comp;
        });

    const categoryCode = document.getElementById('form-category-input')?.value;
    if (!categoryCode) {
        alert('카테고리를 선택해주세요.');
        return false;
    }
    const affectTags = buildAffectTags(documentSettings);
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
        alert('문서 양식이 저장되었습니다.');
        return true;
    } catch (e) {
        console.error('[FormTemplate SAVE ERROR]', e);
        alert(e.message || '문서 양식 저장 중 오류가 발생했습니다.');
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
// 10. 이벤트 바인딩 관련 함수
// ============================

function bindDocumentSettingsPanel() {
    const panel = document.getElementById('form-setting-panel');
    if (!panel) return;
    const insertedId = "document-global-toggles";
    if (document.getElementById(insertedId)) return;

    // -- 자동반영 토글 UI
    const settingTogglesHTML = `
      <div class="setting-row" style="margin-bottom:5px;align-items:center;display:flex;">
        <label for="toggle-auto-attendance" style="flex:1;">
          최종 승인 시 근태 자동 반영
        </label>
        <input
          type="checkbox"
          id="toggle-auto-attendance"
          style="transform:scale(1.15);margin-left:7px;cursor:pointer;"
        >
      </div>
      <div class="setting-row" style="margin-bottom:5px;align-items:center;display:flex;">
        <label for="toggle-auto-schedule" style="flex:1;">
          최종 승인 시 일정 자동 반영
        </label>
        <input
          type="checkbox"
          id="toggle-auto-schedule"
          style="transform:scale(1.15);margin-left:7px;cursor:pointer;"
        >
      </div>
    `;

    const wrapper = document.createElement('div');
    wrapper.id = insertedId;
    wrapper.innerHTML = settingTogglesHTML;

    const actionButtons = panel.querySelector('.form-action-buttons');
    if (actionButtons) {
        panel.insertBefore(wrapper, actionButtons);
    } else {
        panel.appendChild(wrapper);
    }

    /* ======================
       컴포넌트 존재 여부 헬퍼
    ====================== */
    const hasLeaveComponent = () =>
        formComponents.some(c =>
            c.type === 'leave-date-range' || c.type === 'leave-reason'
        );


    const autoAttendanceToggle = document.getElementById('toggle-auto-attendance');
    const autoScheduleToggle = document.getElementById('toggle-auto-schedule');

    // 초기 상태 동기화
    autoAttendanceToggle.checked = documentSettings.autoReflectAttendance;
    autoScheduleToggle.checked = documentSettings.autoReflectSchedule;

    /* ======================
       근태 자동 반영 토글
       - 휴가가 있으면 OFF 불가
    ====================== */
    autoAttendanceToggle.addEventListener('change', e => {
        documentSettings.autoReflectAttendance = e.target.checked;
        syncDocumentPolicyToggles();
    });

    autoScheduleToggle.addEventListener('change', e => {
        documentSettings.autoReflectSchedule = e.target.checked;
        syncDocumentPolicyToggles();
    });

    /* ======================
       문서 제목 실시간 동기화
    ====================== */
    const titleInput = document.getElementById("form-title-input");
    if (titleInput) {
        const titleComp = formComponents.find(c => c.type === "document-title");
        if (titleComp?.meta?.value !== undefined) {
            titleInput.value = titleComp.meta.value;
        }
        titleInput.addEventListener("input", e => {
            const titleComp = formComponents.find(c => c.type === "document-title");
            if (!titleComp) return;
            titleComp.meta.value = e.target.value;
            renderFormComponents();
        });
    }

    bindDocumentTitleValidation();
}

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
                alert("템플릿 ID가 없어 다음 단계로 이동할 수 없습니다.");
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
    previewBtn.addEventListener('click', () => {
        if (typeof templateId === "undefined" || !templateId) {
            alert("템플릿 ID가 없어 미리보기를 열 수 없습니다.");
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
    // templateId가 있으면 수정/복제/최신버전 로드

    bindAutoClearInvalidFocus();


    if (typeof templateId !== "undefined" && templateId) {
        try {
            const res = await apiFetch(`/api/form-templates/${templateId}`);
            if (res.ok) {
                const result = await res.json();
                const data = result?.data;
                if (data?.templateJson) {
                    loadTemplateJsonToFormComponents(data.templateJson);
                }
                // groupId 확보
                resolvedGroupId = data?.templateGroupId ?? null;
                // 카테고리 반영
                const categorySelect = document.getElementById('form-category-input');
                if (categorySelect && data?.templateCategoryCode) {
                    categorySelect.value = data.templateCategoryCode;
                }
                // affectTags → documentSettings 변환
                documentSettings.autoReflectAttendance =
                    Array.isArray(data.affectTags) && data.affectTags.includes("ATTENDANCE");
                documentSettings.autoReflectSchedule =
                    Array.isArray(data.affectTags) && data.affectTags.includes("SCHEDULE");
                // 문서 제목 input 초기 동기화
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
    // groupId 있으면 그룹 info 로드
    if (resolvedGroupId && typeof loadFormTemplateGroupInfo === 'function') {
        await loadFormTemplateGroupInfo(resolvedGroupId);
    }
    // 컴포넌트 목록 UI
    initComponentListPanel();
    // 설정패널 전역 옵션 바인딩
    bindDocumentSettingsPanel();

    syncDocumentPolicyToggles();

    // 구성 요소 렌더링
    renderFormComponents();
    // 저장 버튼 바인딩
    bindSaveFormButton();
    // 이전/다음 버튼 바인딩
    bindStepNavigationButtons();
    // 미리보기 버튼 바인딩
    bindPreviewButton();
    // 컴포넌트 버튼 높이 정렬
    observeComponentButtonHeight();
});

