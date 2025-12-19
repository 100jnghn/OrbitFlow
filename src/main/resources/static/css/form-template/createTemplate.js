// ===============================
// 전자결재 문서 양식: 필수 컴포넌트 타입 정의 + 기본 스키마/메타 설계
// ===============================

// 1. 컴포넌트 타입 상수: 주요 타입 나열 (확장 가능)
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
    "notice",
    "table",
    "image",
    "currency",
    "address",
    "employee-search",
    "department-search"
];

// 2. 각 타입별 기본 스키마: 확장성 있는 구조(공통 필드 + meta)
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
                { id: 'opt1', label: '옵션 1' }
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
                { id: 'opt1', label: '옵션 1' }
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
    notice: {
        type: "notice",
        label: "안내 문구",
        required: false,
        meta: {
            message: "",
            style: "info" // "info", "warning", "danger"
        }
    },
    table: {
        type: "table",
        label: "테이블",
        required: false,
        meta: {
            columns: [],
            rows: undefined
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

// 3. 고정(문서 정보) 컴포넌트 예시: 별도 타입으로 구성(향후 확장 고려)
const FIXED_COMPONENTS = [
    {
        id: 'document-title',
        type: 'document-title',
        label: '문서 제목',
        required: true,
        fixed: true,
        meta: {
            placeholder: "문서 제목을 입력하세요.",
            maxLength: 100
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

// 4. formComponents: 필드 구조만 정의 - 문서 구조의 일관성 보장
let formComponents = [
    { ...FIXED_COMPONENTS[0], id: 'document-title' },
    { ...FIXED_COMPONENTS[1], id: 'document-meta' }
];

// 선택된 컴포넌트 id (동작용, 데이터 설계와 무관)
let selectedComponentId = null;

// 드래그 상태 변수
let dragSrcIdx = null;

// ===============================
// 중앙 문서 영역 렌더링 함수 정의 (renderFormComponents)
// ===============================
function renderFormComponents() {
    // [요구사항1] 문서영역 스크롤 처리: CSS에만 위임. (이 주석 유지)
    const container = document.getElementById("form-edit-area");
    if (!container) return;

    container.innerHTML = "";

    const fixedFirstIdx = 0;
    const fixedLastIdx = formComponents.length - 1;

    formComponents.forEach(function(comp, idx) {
        const row = document.createElement("div");
        row.className = "form-comp-row";
        row.setAttribute("data-comp-id", comp.id);

        // --- Drag & Drop 속성 및 핸들링 ---
        if (!comp.fixed) {
            row.setAttribute("draggable", "true");
            row.addEventListener("dragstart", function(e) {
                dragSrcIdx = idx;
                row.classList.add("dragging");
                e.dataTransfer.effectAllowed = "move";
                try {
                    e.dataTransfer.setData("text/plain", comp.id);
                } catch {}
            });
            row.addEventListener("dragend", function() {
                row.classList.remove("dragging");
                dragSrcIdx = null;
            });
        } else {
            row.setAttribute("draggable", "false");
        }

        row.addEventListener("dragover", function(e) {
            if (dragSrcIdx === null) return;
            if (comp.fixed) return;
            if (idx === fixedFirstIdx || idx === fixedLastIdx) return;
            if (dragSrcIdx === idx) return;
            e.preventDefault();
            row.style.backgroundColor = "#eaf4ff";
        });
        row.addEventListener("dragleave", function() {
            row.style.backgroundColor = (comp.id === selectedComponentId) ? "#eef5ff" : (comp.fixed ? "#f5f7fa" : "#fff");
        });
        row.addEventListener("drop", function(e) {
            if (dragSrcIdx === null) return;
            if (comp.fixed) return;
            if (idx === fixedFirstIdx || idx === fixedLastIdx) return;
            if (dragSrcIdx === idx) return;
            e.preventDefault();
            row.style.backgroundColor = "";
            const dragged = formComponents[dragSrcIdx];
            if (dragged.fixed) return;
            if (dragSrcIdx < idx) {
                formComponents.splice(idx + 1, 0, dragged);
                formComponents.splice(dragSrcIdx, 1);
            } else {
                formComponents.splice(dragSrcIdx, 1);
                formComponents.splice(idx, 0, dragged);
            }
            if (formComponents[0].type !== "document-title") {
                const firstFixed = formComponents.find(fc => fc.type === "document-title");
                if (firstFixed) {
                    formComponents = [firstFixed, ...formComponents.filter(fc => fc !== firstFixed)];
                }
            }
            if (formComponents[formComponents.length-1].type !== "document-meta") {
                const lastFixed = formComponents.find(fc => fc.type === "document-meta");
                if (lastFixed) {
                    formComponents = [...formComponents.filter(fc => fc !== lastFixed), lastFixed];
                }
            }
            renderFormComponents();
            selectedComponentId = dragged.id;
            showComponentSettingPanel(selectedComponentId);
            dragSrcIdx = null;
        });

        if (comp.fixed) {
            row.style.background = "#f5f7fa";
            row.style.border = "1px solid #e0e0ec";
        } else {
            if (comp.id === selectedComponentId) {
                row.style.border = "2px solid #196de7";
                row.style.background = "#eef5ff";
            } else {
                row.style.border = "1px solid #dedede";
                row.style.background = "#fff";
            }
            row.style.cursor = "pointer";
            row.addEventListener("click", function() {
                selectedComponentId = comp.id;
                renderFormComponents();
                showComponentSettingPanel(comp.id);
            });
        }

        row.style.padding = "10px 14px";
        row.style.marginBottom = "6px";

        // 타입별(preview)
        if (comp.fixed) {
            if (comp.type === 'document-title') {
                row.innerHTML = `<strong style="font-size:1.15em;">${comp.label}</strong>
                    <input type="text" disabled placeholder="${comp.meta && comp.meta.placeholder ? comp.meta.placeholder : ''}" style="width:66%;margin-left:7px;background:#f7f8fa;" />`;
            } else if (comp.type === 'document-meta') {
                row.innerHTML = `<span style="color:#789;">${comp.label}</span>
                    <span style="margin-left:10px;color:#bbb;">자동입력</span>`;
            } else {
                row.innerHTML = `<span><strong>${comp.label}</strong></span>`;
            }
            if (comp.required) {
                row.innerHTML += `<span style="color:#e22719;font-size:0.95em;margin-left:7px;">*</span>`;
            }
        }
        else {
            let inputHtml = "";
            let labelHtml = `<span>${comp.label || ""}</span>`;
            if (comp.required)
                labelHtml += `<span style="color:#e22719;font-size:0.95em;margin-left:7px;">*</span>`;

            if (["date-range","time-range","leave-date-range"].includes(comp.type)) {
                let leftLabel = "", rightLabel = "";
                if (comp.meta) {
                    leftLabel = comp.meta.startLabel || ((comp.type==="date-range"||comp.type==="leave-date-range")?"시작":"시작");
                    rightLabel = comp.meta.endLabel || ((comp.type==="date-range"||comp.type==="leave-date-range")?"종료":"종료");
                }
                if (comp.type === "date-range" || comp.type === "leave-date-range") {
                    inputHtml = `
                        <div style="display:flex;align-items:center;gap:10px;margin-top:6px;">
                            <input type="date" disabled style="flex:1;min-width:120px;" placeholder="${leftLabel}">
                            <span style="margin:0 8px;">~</span>
                            <input type="date" disabled style="flex:1;min-width:120px;" placeholder="${rightLabel}">
                        </div>
                    `;
                } else if (comp.type === "time-range") {
                    inputHtml = `
                        <div style="display:flex;align-items:center;gap:10px;margin-top:6px;">
                            <input type="time" disabled style="flex:1;min-width:110px;" placeholder="${leftLabel}">
                            <span style="margin:0 8px;">~</span>
                            <input type="time" disabled style="flex:1;min-width:110px;" placeholder="${rightLabel}">
                        </div>
                    `;
                }
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (["text","number","currency"].includes(comp.type)) {
                let inputType = comp.type === "number" ? "number" : "text";
                let placeholder = (comp.meta && comp.meta.placeholder) ? comp.meta.placeholder : "";
                let unit = (comp.type==="currency" && comp.meta && comp.meta.unit) ? ` <span style="font-size:90%;color:#888;margin-left:3px;">${comp.meta.unit}</span>` : '';
                inputHtml = `<input type="${inputType}" disabled style="width:60%;margin-left:12px;" placeholder="${placeholder}" />${unit}`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "textarea") {
                let placeholder = (comp.meta && comp.meta.placeholder) ? comp.meta.placeholder : "";
                inputHtml = `<textarea disabled placeholder="${placeholder}" style="margin-left:9px;min-width:170px;min-height:32px;vertical-align:middle;"></textarea>`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "radio" || comp.type === "checkbox") {
                let options =
                    (comp.meta && Array.isArray(comp.meta.options) && comp.meta.options.length > 0)
                        ? comp.meta.options
                        : [{ id: "opt1", label: comp.type === "radio" ? "항목 1" : "항목 1" }];
                let inputType = comp.type;
                inputHtml =
                    `<div style="margin-left:12px;display:flex;flex-direction:column;gap:2px;">` +
                    options.map(function(opt, i) {
                        const label = (typeof opt === "string" ? opt : (opt.label ?? `옵션${i+1}`));
                        return `<label style="margin-bottom:2px;display:flex;align-items:center;">
                            <input type="${inputType}" disabled style="margin-right:7px;">${label}
                        </label>`;
                    }).join("") +
                    `</div>`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "time") {
                inputHtml = `<input type="time" disabled style="width:160px;margin-left:12px;">`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "date") {
                inputHtml = `<input type="date" disabled style="width:160px;margin-left:12px;">`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "divider") {
                row.innerHTML = `<hr style="margin:8px 0 6px 0;border:solid #e9e9e9 1.5px;">`;
            }
            else if (comp.type === "notice") {
                let message = comp.meta && typeof comp.meta.message === "string" && comp.meta.message.length > 0
                    ? comp.meta.message
                    : "안내 문구 예시";
                let styleType = comp.meta && comp.meta.style ? comp.meta.style : "info";
                let color = styleType === "danger" ? "#e74c3c" : (styleType === "warning" ? "#f39c12" : "#1976d2");
                let bgColor = styleType === "danger" ? "#feeeef" : (styleType === "warning" ? "#fff8e5" : "#eaf3fc");
                row.innerHTML = `<div style="padding:8px 13px;border-radius:6px;border:1.5px solid ${color};background:${bgColor};color:${color};font-size:1em;">
                    <span style="font-weight:500;"><i class="ico-info" style="margin-right:6px;"></i>${labelHtml}</span>
                    <div style="margin-top:4px;font-size:0.98em;color:#595b60;">${message.replace(/\n/g, "<br>")}</div>
                </div>`;
            }
            else if (comp.type === "table") {
                const cols = Array.isArray(comp.meta && comp.meta.columns) && comp.meta.columns.length > 0 ? comp.meta.columns : ["열1", "열2"];
                inputHtml = `<table style="width:85%;margin-left:13px;border-collapse:collapse;">` +
                  `<thead><tr>${cols.map(col=>`<th style="border:1px solid #e0e0e0;background:#f8fafb;padding:4px 7px;">${col}</th>`).join('')}</tr></thead>` +
                  `</table>`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "image") {
                const src = comp.meta && comp.meta.src;
                const alt = comp.meta && comp.meta.alt ? comp.meta.alt : "이미지 미리보기";
                inputHtml = `<div style="display:inline-block;margin-left:12px;">
                    <div style="width:120px;height:64px;border:1.5px dotted #c6cbe7;background:#f4f4fa;display:flex;align-items:center;justify-content:center;border-radius:8px;">
                        ${src ? `<img src="${src}" alt="${alt}" style="max-width:110px;max-height:60px;" />` : `<span style="color:#aaa;font-size:16px;">이미지</span>`}
                    </div>
                </div>`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else if (comp.type === "file") {
                inputHtml = `<button type="button" disabled style="margin-left:14px;border:1px solid #bbb;padding:4px 10px;border-radius:5px;background:#f4f6fb;color:#888;">파일 선택</button>`;
                row.innerHTML = labelHtml + inputHtml;
            }
            else {
                row.innerHTML = labelHtml;
            }
        }

        if (row.getAttribute("draggable") === "true") {
            row.addEventListener("dragenter", function() {
                if (dragSrcIdx !== null && !comp.fixed && idx !== fixedFirstIdx && idx !== fixedLastIdx && dragSrcIdx !== idx) {
                    row.classList.add("drag-hover");
                }
            });
            row.addEventListener("dragleave", function() {
                row.classList.remove("drag-hover");
            });
        }

        container.appendChild(row);
    });
}

// ===============================
// [ 컴포넌트 추가 로직 ]
// ===============================
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

function addComponentByType(type) {
    if (!FORM_COMPONENT_SCHEMAS.hasOwnProperty(type)) {
        console.warn(`[FormTemplateBuilder] 컴포넌트 타입 '${type}'는 스키마에 정의되어 있지 않습니다. 추가 중단.`);
        return;
    }
    const schema = FORM_COMPONENT_SCHEMAS[type];
    const metaCopy = deepCopy(schema.meta);

    if ((type === "radio" || type === "checkbox")) {
        metaCopy.options = metaCopy.options && metaCopy.options.length > 0 ?
            deepCopy(metaCopy.options.map(opt => typeof opt === "string" ? {id: generateOptionId(), label: opt} : {...opt, id: generateOptionId()})) :
            [{ id: generateOptionId(), label: "옵션 1" }];
    }

    const component = {
        id: generateId(),
        type: schema.type,
        label: schema.label,
        required: schema.required,
        fixed: false,
        meta: metaCopy
    };
    formComponents.splice(formComponents.length - 1, 0, component);
    selectedComponentId = component.id;
    renderFormComponents();
    showComponentSettingPanel(component.id);
}

// 컴포넌트 목록 동적 생성
function initComponentListPanel() {
    const listContainer = document.getElementById("component-btn-list");
    if (!listContainer) return;
    listContainer.innerHTML = "";

    FORM_COMPONENT_TYPES.forEach(function(type) {
        if (!FORM_COMPONENT_SCHEMAS[type]) return;
        const btn = document.createElement("button");
        btn.className = "component-btn";
        btn.type = "button";
        btn.setAttribute("data-type", type);
        btn.innerText = FORM_COMPONENT_SCHEMAS[type].label || type;

        btn.addEventListener("click", function() {
            addComponentByType(type);
        });

        listContainer.appendChild(btn);
    });
}

// ===============================
// [ 추가 ] 문서 설정 전역 옵션
// ===============================
let documentSettings = {
    autoReflectAttendance: false,
    autoReflectSchedule: false
};

// ===============================
// [ 추가 ] 컴포넌트 설정 패널에 필수 입력 토글 및 라벨 수정 추가 + 확장(UI)
// ===============================
function showComponentSettingPanel(componentId) {
    const comp = formComponents.find(c => c.id === componentId);
    const panel = document.getElementById('component-setting-content');
    if (!comp) {
        panel.innerHTML = `<span style="color:#9ab;">컴포넌트를 선택하세요.</span>`;
        return;
    }

    const isFixed = !!comp.fixed;
    let html = `
        <div style="margin-bottom:11px;">
            <div style="font-weight:600;margin-bottom:4px;">${(comp.label || '')}</div>
            <div style="color:#888;font-size:0.97em;">타입: ${comp.type}</div>
        </div>
    `;

    let labelFieldHtml = `
        <div class="setting-row" style="margin-bottom:10px;align-items: center;display:flex;">
            <label style="min-width:54px;flex:1;" for="input-comp-label">이름</label>
            <input type="text" id="input-comp-label" value="${comp.label ? comp.label.replace(/"/g, '&quot;') : ''}" 
                style="flex:3;margin-left:7px;padding:3px 7px;border:1px solid #ccd;${isFixed? 'background:#f3f5fa;color:#bbb;' : ''}" 
                ${isFixed?'disabled="disabled"':''} maxlength="70" autocomplete="off" spellcheck="false"/>
        </div>
    `;
    html += labelFieldHtml;

    if (!isFixed) {
        html += `
        <div class="setting-row" style="margin-bottom:12px;align-items: center;display:flex;">
            <label style="flex:1;min-width: 62px;" for="toggle-required">필수 입력</label>
            <input type="checkbox" id="toggle-required" ${comp.required ? "checked" : ""} style="transform:scale(1.16);cursor:pointer;margin-left:4px;" />
        </div>
        `;
    }

    if (comp.type === "notice") {
        const msgValue = (comp.meta && typeof comp.meta.message === "string") ? comp.meta.message.replace(/</g,"&lt;") : "";
        html += `
        <div class="setting-row" style="margin-bottom:13px;margin-top:3px;display:flex;align-items:flex-start;">
            <label for="notice-message" style="min-width:54px;flex:1;align-self:flex-start;">안내 문구</label>
            <textarea id="input-notice-message"
                placeholder="안내 문구를 입력하세요"
                style="flex:3;margin-left:7px;padding:6px 7px;border:1.2px solid #ccd;min-height:44px;resize:vertical;font-size:0.99em;"
                maxlength="350"
                autocomplete="off"
                spellcheck="false"
                >${msgValue}</textarea>
        </div>
        `;
    }

    if (comp.type === "radio" || comp.type === "checkbox") {
        if (!comp.meta.options || !Array.isArray(comp.meta.options) || comp.meta.options.length === 0) {
            comp.meta.options = [{ id: generateOptionId(), label: "옵션 1" }];
        }

        html += `
        <div class="setting-row" style="margin-bottom:12px;">
            <div style="font-weight:500;font-size:1em;margin-bottom:4px;">옵션 목록</div>
            <div id="option-list-pane"></div>
            <button type="button" id="add-option-btn" style="margin-top:7px;padding:4px 10px;border:1px solid #c8cbe9;outline:none;border-radius:5px;background:#f7f7fc;cursor:pointer;font-size:0.97em;color:#234;">옵션 추가</button>
        </div>
        `;
    }

    // ...패널에 추가 설정 확장

    panel.innerHTML = html;

    if (!isFixed) {
        const labelInput = document.getElementById('input-comp-label');
        if (labelInput) {
            labelInput.addEventListener('input', function(e){
                comp.label = e.target.value;
                const idx = formComponents.findIndex(fc => fc.id === componentId);
                if (idx !== -1) formComponents[idx].label = comp.label;
                renderFormComponents();
            });
        }
    }

    if (!isFixed) {
        const requiredToggle = document.getElementById('toggle-required');
        if (requiredToggle) {
            requiredToggle.addEventListener('change', function(e){
                comp.required = !!e.target.checked;
                const idx = formComponents.findIndex(fc => fc.id === componentId);
                if (idx !== -1) formComponents[idx].required = comp.required;
                renderFormComponents();
            });
        }
    }

    if (comp.type === "notice") {
        const msgTarea = document.getElementById("input-notice-message");
        if (msgTarea) {
            msgTarea.value = comp.meta && typeof comp.meta.message === "string" ? comp.meta.message : "";
            msgTarea.addEventListener('input', function(e) {
                comp.meta = comp.meta || {};
                comp.meta.message = e.target.value;
                const idx = formComponents.findIndex(fc => fc.id === componentId);
                if (idx !== -1) formComponents[idx].meta.message = comp.meta.message;
                renderFormComponents();
            });
        }
    }

    // 라디오/체크박스 옵션 관리 UI/로직 (최소 1개 옵션 유지)
    if (comp.type === 'radio' || comp.type === 'checkbox') {
        function renderOptionPanel() {
            const pane = document.getElementById('option-list-pane');
            if (!pane) return;
            let optListHTML = "";
            comp.meta.options.forEach(function(opt, idx) {
                const optId = opt.id || generateOptionId();
                if (!opt.id) opt.id = optId;
                optListHTML += `
                <div class="option-row" data-opt-idx="${idx}" style="display:flex;align-items:center;gap:4px;margin-bottom:4px;">
                    <input type="text"
                        class="input-option-label"
                        data-opt-idx="${idx}"
                        value="${opt.label ? String(opt.label).replace(/"/g, '&quot;') : ''}"
                        style="flex:3;padding:3px 7px;border:1px solid #ccd;font-size:0.97em;"
                        maxlength="70"
                        autocomplete="off"
                        spellcheck="false"
                    >
                    <button type="button" class="btn-remove-option" data-opt-idx="${idx}" style="flex:0 0 auto;margin-left:3px;padding:1.5px 6px;font-size:14px;background:#f7f7fa;color:#c33;border:1px solid #ddd;border-radius:4px;cursor:pointer;"
                        ${comp.meta.options.length <= 1 ? 'disabled' : ''} title="옵션 삭제"
                    >🗑️</button>
                </div>
                `;
            });
            pane.innerHTML = optListHTML;

            Array.from(pane.querySelectorAll('.input-option-label')).forEach(function(input) {
                input.addEventListener('input', function(e) {
                    const idx = Number(input.getAttribute('data-opt-idx'));
                    comp.meta.options[idx].label = e.target.value;
                    const fcIdx = formComponents.findIndex(fc => fc.id === componentId);
                    if (fcIdx !== -1) formComponents[fcIdx].meta.options[idx].label = e.target.value;
                    renderFormComponents();
                });
            });

            Array.from(pane.querySelectorAll('.btn-remove-option')).forEach(function(btn) {
                btn.addEventListener('click', function() {
                    const idx = Number(btn.getAttribute('data-opt-idx'));
                    if (comp.meta.options.length > 1) {
                        comp.meta.options.splice(idx, 1);
                        const fcIdx = formComponents.findIndex(fc => fc.id === componentId);
                        if (fcIdx !== -1) formComponents[fcIdx].meta.options = comp.meta.options;
                        renderOptionPanel();
                        renderFormComponents();
                    }
                });
            });
        }

        renderOptionPanel();

        const addBtn = document.getElementById('add-option-btn');
        if (addBtn) {
            addBtn.addEventListener('click', function() {
                comp.meta.options.push({ id: generateOptionId(), label: `옵션 ${comp.meta.options.length + 1}` });
                const fcIdx = formComponents.findIndex(fc => fc.id === componentId);
                if (fcIdx !== -1) formComponents[fcIdx].meta.options = comp.meta.options;
                renderFormComponents();
                showComponentSettingPanel(componentId);
            });
        }
    }
}

// ===============================
// [ 추가 ] 문서 설정 패널 전역 토글 UI 에 바인딩 함수
// ===============================
function bindDocumentSettingsPanel() {
    const panel = document.getElementById('form-setting-panel');
    if (!panel) return;

    const settingTogglesHTML = `
      <div class="setting-row" style="margin-bottom:5px;align-items:center;display:flex;">
        <label for="toggle-auto-attendance" style="flex:1;">최종 승인 시 근태 자동 반영</label>
        <input type="checkbox" id="toggle-auto-attendance" ${documentSettings.autoReflectAttendance ? "checked" : ""} style="transform:scale(1.15);margin-left:7px;cursor:pointer;">
      </div>
      <div class="setting-row" style="margin-bottom:5px;align-items:center;display:flex;">
        <label for="toggle-auto-schedule" style="flex:1;">최종 승인 시 일정 자동 반영</label>
        <input type="checkbox" id="toggle-auto-schedule" ${documentSettings.autoReflectSchedule ? "checked" : ""} style="transform:scale(1.15);margin-left:7px;cursor:pointer;">
      </div>
    `;

    let insertedId = "document-global-toggles";
    if (!document.getElementById(insertedId)) {
        const wrapper = document.createElement('div');
        wrapper.id = insertedId;
        wrapper.innerHTML = settingTogglesHTML;

        const saveBtn = document.getElementById('save-form-btn');
        if (saveBtn) {
            panel.insertBefore(wrapper, saveBtn);
        } else {
            panel.appendChild(wrapper);
        }
    }

    const refToggle = document.getElementById('toggle-reference-document');
    if (refToggle) {
        refToggle.checked = documentSettings.allowReferenceDocument;
        refToggle.addEventListener('change', function (e) {
            documentSettings.allowReferenceDocument = !!e.target.checked;
            console.log('[documentSettings]', documentSettings);
        });
    }
    const attToggle = document.getElementById('toggle-attachment');
    if (attToggle) {
        attToggle.checked = documentSettings.allowAttachment;
        attToggle.addEventListener('change', function (e) {
            documentSettings.allowAttachment = !!e.target.checked;
            console.log('[documentSettings]', documentSettings);
        });
    }
    const autoAttendanceToggle = document.getElementById('toggle-auto-attendance');
    if (autoAttendanceToggle) {
        autoAttendanceToggle.checked = documentSettings.autoReflectAttendance;
        autoAttendanceToggle.addEventListener('change', function (e) {
            documentSettings.autoReflectAttendance = !!e.target.checked;
            console.log('[documentSettings]', documentSettings);
        });
    }
    const autoScheduleToggle = document.getElementById('toggle-auto-schedule');
    if (autoScheduleToggle) {
        autoScheduleToggle.checked = documentSettings.autoReflectSchedule;
        autoScheduleToggle.addEventListener('change', function (e) {
            documentSettings.autoReflectSchedule = !!e.target.checked;
            console.log('[documentSettings]', documentSettings);
        });
    }
}

async function loadFormTemplateGroupInfo() {
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

function buildAffectTags(settings) {
    const tags = [];

    if (settings.autoReflectAttendance) {
        tags.push("ATTENDANCE");
    }
    if (settings.autoReflectSchedule) {
        tags.push("SCHEDULE");
    }

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

/* ======================================================
   [SAVE] 문서 양식 구조 저장 기능 (PATCH)
   ====================================================== */

async function saveFormTemplateStructure(templateId) {
    if (!templateId) {
        alert('저장할 문서 양식 ID(templateId)가 존재하지 않습니다.');
        return;
    }

    // 1. fixed / 불필요 필드 제거
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
        return;
    }

    // 3. affectTags 생성
    const affectTags = buildAffectTags(documentSettings);

    // 4. 서버 계약에 맞는 payload 구성
    const payload = {
        categoryCode,
        affectTags,
        templateJson: {
            fields: convertComponentsToFields(filteredComponents)
        }
    };

    try {
        const res = await apiFetch(`/api/admin/form-templates/${templateId}/structure`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err?.message || '문서 양식 저장에 실패했습니다.');
        }

        alert('문서 양식이 저장되었습니다.');

    } catch (e) {
        console.error('[FormTemplate SAVE ERROR]', e);
        alert(e.message || '문서 양식 저장 중 오류가 발생했습니다.');
    }
}


/**
 * 저장 버튼 이벤트 바인딩 (PATCH API만 바인딩)
 */
function bindSaveFormButton() {
    const saveBtn = document.getElementById('save-form-btn');
    if (!saveBtn) return;

    // 기존 중복 기능 제거(PATCH 요청만 유지)
    saveBtn.addEventListener('click', async function () {
        saveBtn.disabled = true;
        try {
            // templateId는 선언돼있다고 가정(api 사용부와 동일)
            await saveFormTemplateStructure(typeof templateId !== "undefined" ? templateId : undefined);
        } finally {
            saveBtn.disabled = false;
        }
    });
}

