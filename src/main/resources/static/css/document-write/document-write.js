/**
 * document-write.js
 * - 문서 초안 + 결재선 초안 조회
 * - 문서 필드 렌더링
 * - 임시저장
 *
 * 전제:
 *  - documentId는 Thymeleaf에서 DOCUMENT_ID로 주입됨
 *  - 문서 / 결재선은 이미 DRAFT 상태로 생성되어 있음
 */

let hasUnsavedChanges = false;
let isInitializing = true;
let documentFieldDefinitions = [];

const MAX_LENGTH = {
    /* 단일 필드 */
    documentTitle: 50,
    text: 100,
    textarea: 500,
    number: 10,
    currency: 15,

    /* table 전용 */
    table: {
        text: 20,
        number: 10,
        currency: 15   // ⭐ 추가/확정
    }
};

const SCHEDULE_REASON_LABEL_MAP = {
    VACATION: '휴가 사유',
    BUSINESS_TRIP: '출장 사유',
    OUTWORK: '외근 사유',
    COMPANY_EVENT: '일정 사유'
};


let leaveTypeCache = null;

async function loadLeaveTypes() {
    if (leaveTypeCache) return leaveTypeCache;

    const res = await apiFetch('/api/leave-types/all');
    if (!res.ok) {
        showToast('휴가 유형을 불러오지 못했습니다.', 'error');
        return [];
    }

    const result = await res.json();
    leaveTypeCache = result.data ?? [];
    return leaveTypeCache;
}


// 문서 필드 변경 감지
document.addEventListener('input', (e) => {
    if (isInitializing) return;

    if (
        e.target.matches(
            'input[data-field-id], textarea[data-field-id], select[data-field-id]'
        )
    ) {
        hasUnsavedChanges = true;

        const wrapper = getFieldWrapperByFieldId(e.target.dataset.fieldId);

        if (wrapper?.dataset.hasError === 'true') {
            wrapper.dataset.hasError = 'false';
            clearFieldError(e.target);
        }
    }
});

// 페이지 이탈 경고
window.addEventListener('beforeunload', (e) => {
    if (!hasUnsavedChanges) return;
    e.preventDefault();
    e.returnValue = '';
});

document.addEventListener('DOMContentLoaded', async () => {

    if (typeof DOCUMENT_ID === 'undefined' || !DOCUMENT_ID) {
        alert('문서 정보가 올바르지 않습니다.');
        return;
    }

    try {
        await loadDocumentDraft(DOCUMENT_ID);
        await loadApprovalLines(DOCUMENT_ID);
        bindEvents(DOCUMENT_ID);

        hasUnsavedChanges = false;
        isInitializing = false;
    } catch (e) {
        console.error(e);
        alert('문서 정보를 불러오는 중 오류가 발생했습니다.');
    }
});

/* =====================================================
   유틸 함수
   ===================================================== */

function labeled(labelText, inputEl) {
    const wrapper = document.createElement('div');
    wrapper.className = 'labeled-field';

    const label = document.createElement('span');
    label.className = 'inline-label';
    label.textContent = labelText;

    wrapper.append(label, inputEl);
    return wrapper;
}

function confirmLeaveIfDirty() {
    if (!hasUnsavedChanges) return true;

    return confirm(
        '저장되지 않은 변경 사항이 있습니다.\n' +
        '정말로 이동하시겠습니까?'
    );
}


function showToast(message, type = 'info', duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toast-out 0.25s ease forwards';
        toast.addEventListener('animationend', () => {
            toast.remove();
        });
    }, duration);
}

function showTableCellHint(input, current, max) {
    hideTableCellHint(input);

    const hint = document.createElement('div');
    hint.className = 'table-cell-hint';
    hint.textContent = `(${current} / ${max})`;

    const rect = input.getBoundingClientRect();

    hint.style.top = `${rect.bottom + window.scrollY + 4}px`; // ⬇ 하단
    hint.style.left = `${rect.left + window.scrollX}px`;

    document.body.appendChild(hint);
    input._cellHint = hint;
}

function hideTableCellHint(input) {
    if (input._cellHint) {
        input._cellHint.remove();
        delete input._cellHint;
    }
}


function hideCellTooltip(input) {
    if (input._tooltip) {
        input._tooltip.remove();
        delete input._tooltip;
    }
}


function showLengthHint(inputEl, current, max) {
    const scheduleGroup = inputEl.closest('.schedule-group');
    const fieldWrapper = inputEl.closest('.doc-field');

    const hintHost = scheduleGroup ?? fieldWrapper;
    if (!hintHost) return;

    // 에러 힌트가 있으면 info 힌트 표시 안 함
    if (hintHost.querySelector('.field-hint.error')) return;

    // 기존 info 힌트 제거
    hintHost.querySelector('.field-hint.info')?.remove();

    const hint = document.createElement('div');
    hint.className = 'field-hint info';
    hint.textContent = `(${current}/${max})`;

    hintHost.appendChild(hint);
}


function hideLengthHint(inputEl) {
    const scheduleGroup = inputEl.closest('.schedule-group');
    const fieldWrapper = inputEl.closest('.doc-field');

    const hintHost = scheduleGroup ?? fieldWrapper;
    if (!hintHost) return;

    hintHost.querySelector('.field-hint.info')?.remove();
}


function bindLengthCounter(inputEl, maxLength) {
    inputEl.maxLength = maxLength;

    inputEl.addEventListener('input', () => {
        const length = inputEl.value.length;
        showLengthHint(inputEl, length, maxLength);
    });

    inputEl.addEventListener('blur', () => {
        hideLengthHint(inputEl);
    });
}


function showFieldError(inputEl, message) {
    if (!inputEl) return;

    const fieldWrapper = inputEl.closest('.doc-field');
    if (!fieldWrapper) return;

    // ⭐ 검증 에러 플래그
    fieldWrapper.dataset.hasError = 'true';

    fieldWrapper.querySelector('.field-hint.error')?.remove();

    fieldWrapper.classList.add('field-error');
    inputEl.classList.add('field-error');

    const hint = document.createElement('div');
    hint.className = 'field-hint error';
    hint.textContent = message;
    fieldWrapper.appendChild(hint);

    fieldWrapper.scrollIntoView({behavior: 'smooth', block: 'center'});
    requestAnimationFrame(() => inputEl.focus({preventScroll: true}));
}


function clearFieldError(inputEl) {
    if (!inputEl) return;

    const fieldId = inputEl.dataset?.fieldId;
    if (!fieldId) return;

    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    if (fieldWrapper.dataset.hasError === 'true') return;

    const scheduleGroup = inputEl.closest('.schedule-group');
    const scheduleInput = inputEl.closest('.schedule-input');

    // ❗ hint 제거
    fieldWrapper.querySelector('.field-hint.error')?.remove();

    // range
    if (inputEl.dataset.rangeType) {
        clearRangeFieldErrorByFieldId(fieldId);
    }

    // radio
    if (inputEl.type === 'radio') {
        clearRadioGroupErrorByFieldId(fieldId);
    }

    // checkbox
    if (inputEl.type === 'checkbox') {
        clearCheckboxGroupErrorByFieldId(fieldId);
    }

    // 일반 input / textarea / select
    inputEl.classList.remove('field-error');
    scheduleGroup?.classList.remove('field-error');
    scheduleInput?.classList.remove('field-error');

    // schedule 내부가 아닐 때만 wrapper 정리
    if (!scheduleInput) {
        fieldWrapper.classList.remove('field-error');
    }
}


function showRadioGroupError(fieldId, message) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    const radioGroup = fieldWrapper.querySelector('.radio-group');
    if (!radioGroup) return;

    // 기존 hint 제거
    fieldWrapper.querySelector('.field-hint')?.remove();

    // 에러 클래스
    fieldWrapper.classList.add('field-error');
    radioGroup.classList.add('field-error');


    // hint는 doc-field 아래
    const hint = document.createElement('div');
    hint.className = 'field-hint';
    hint.textContent = message;
    fieldWrapper.appendChild(hint);

    // 스크롤
    fieldWrapper.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    // 포커스 → 첫 번째 radio
    const firstRadio = radioGroup.querySelector(
        'input[type="radio"]:not([disabled])'
    );
    if (firstRadio) {
        requestAnimationFrame(() =>
            firstRadio.focus({preventScroll: true})
        );
    }
}


function showCheckboxGroupError(fieldId, message) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    const checkboxGroup = fieldWrapper.querySelector('.checkbox-group');
    if (!checkboxGroup) return;

    // 기존 hint 제거
    fieldWrapper.querySelector('.field-hint')?.remove();

    // 에러 클래스
    fieldWrapper.classList.add('field-error');
    checkboxGroup.classList.add('field-error');

    // hint
    const hint = document.createElement('div');
    hint.className = 'field-hint';
    hint.textContent = message;
    fieldWrapper.appendChild(hint);

    // 스크롤
    fieldWrapper.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    // 포커스 → 첫 번째 checkbox
    const firstCheckbox = checkboxGroup.querySelector(
        'input[type="checkbox"]:not([disabled])'
    );
    if (firstCheckbox) {
        requestAnimationFrame(() =>
            firstCheckbox.focus({preventScroll: true})
        );
    }
}


function showCurrencyFieldError(fieldId, message) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    const currencyWrapper = fieldWrapper.querySelector('.currency-field');
    const input = currencyWrapper?.querySelector('input');

    // 기존 hint 제거
    fieldWrapper.querySelector('.field-hint')?.remove();

    // 에러 클래스
    fieldWrapper.classList.add('field-error');
    currencyWrapper?.classList.add('field-error');
    input?.classList.add('field-error');

    // hint는 doc-field 아래
    const hint = document.createElement('div');
    hint.className = 'field-hint';
    hint.textContent = message;
    fieldWrapper.appendChild(hint);

    // 스크롤
    fieldWrapper.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    // 포커스
    if (input) {
        requestAnimationFrame(() =>
            input.focus({preventScroll: true})
        );
    }
}


function clearRadioGroupErrorByFieldId(fieldId) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    fieldWrapper.classList.remove('field-error');
    fieldWrapper.querySelector('.field-hint')?.remove();

    const radioGroup = fieldWrapper.querySelector('.radio-group');
    radioGroup?.classList.remove('field-error');
}

function clearCheckboxGroupErrorByFieldId(fieldId) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    fieldWrapper.classList.remove('field-error');
    fieldWrapper.querySelector('.field-hint')?.remove();

    const checkboxGroup = fieldWrapper.querySelector('.checkbox-group');
    checkboxGroup?.classList.remove('field-error');
}


function showApprovalLineError(rowEl, message) {
    if (!rowEl) return;

    // 기존 hint 제거
    rowEl.querySelector('.approval-line-hint')?.remove();

    rowEl.classList.add('error');

    const hint = document.createElement('div');
    hint.className = 'approval-line-hint';
    hint.textContent = message;

    rowEl.appendChild(hint);

    rowEl.scrollIntoView({behavior: 'smooth', block: 'center'});
}

function clearApprovalLineError(rowEl) {
    if (!rowEl) return;

    rowEl.classList.remove('error');
    rowEl.querySelector('.approval-line-hint')?.remove();
}

function clearTableFieldError(fieldId) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    fieldWrapper.classList.remove('field-error');
    fieldWrapper.querySelector('.field-hint')?.remove();

    fieldWrapper
        .querySelectorAll('input.field-error')
        .forEach(el => el.classList.remove('field-error'));
}


/* =====================================================
   문서 초안 조회
   ===================================================== */

async function loadDocumentDraft(documentId) {
    const res = await apiFetch(`/api/document-contents/${documentId}`);
    if (!res.ok) throw new Error('Failed to load document content');

    const result = await res.json();
    const data = result.data;

    documentFieldDefinitions = data.fields ?? [];

    // document-title 바인딩
    const titleField = documentFieldDefinitions.find(
        f => f.fieldType === 'document-title'
    );
    renderDocumentTitleFromField(titleField);

    // 나머지 필드 렌더링
    await renderFields(documentFieldDefinitions);
}


function renderDocumentTitleFromField(titleField) {
    const titleInput = document.getElementById('documentTitle');
    const titleLabel = document.getElementById('documentTitleLabel');

    if (!titleInput || !titleField) return;

    // label
    if (titleLabel) {
        titleLabel.textContent = titleField.label ?? '문서 제목';
        titleLabel.classList.toggle('required', titleField.required === true);
    }

    // input
    titleInput.placeholder =
        titleField.meta?.placeholder ?? '문서 제목을 입력하세요';

    titleInput.value =
        titleField.value ?? titleField.meta?.value ?? '';

    titleInput.dataset.fieldId = titleField.fieldId;
    bindLengthCounter(titleInput, MAX_LENGTH.documentTitle);
}


/* =====================================================
   문서 필드 렌더링
   ===================================================== */

async function renderFields(fields) {
    const container = document.getElementById('documentFields');
    container.innerHTML = '';

    const renderTargets = fields.filter(
        f => f.fieldType !== 'document-title'
    );

    for (const field of renderTargets) {
        const fieldEl = await createFieldComponent(field);
        container.appendChild(fieldEl);
    }
}


/**
 * fieldType 별 컴포넌트 생성
 */
async function createFieldComponent(field) {

    // ✅ divider는 완전히 분리
    if (field.fieldType === 'divider') {
        const wrapper = document.createElement('div');
        wrapper.className = 'doc-divider';

        const hr = document.createElement('hr');
        wrapper.appendChild(hr);

        return wrapper;
    }

    if (field.fieldType === 'image') {
        return createImageField(field);
    }

    const wrapper = document.createElement('div');
    wrapper.className = 'doc-field';

    // label
    const label = document.createElement('label');
    label.textContent = field.label;
    if (field.required) label.classList.add('required');
    wrapper.appendChild(label);

    let input;

    switch (field.fieldType) {

        case 'text':
            input = document.createElement('input');
            input.type = 'text';
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            input.placeholder = field.meta?.placeholder ?? '';
            bindLengthCounter(input, MAX_LENGTH.text);
            break;

        case 'number':
            input = document.createElement('input');
            input.type = 'text';              // ⭐ 핵심
            input.inputMode = 'decimal';
            input.value = normalizeInputValue(field.value);

            bindLengthCounter(input, MAX_LENGTH.number);

            input.addEventListener('input', () => {
                // 숫자만 허용
                let v = input.value.replace(/[^0-9.-]/g, '');

                // 길이 제한 강제
                if (v.length > MAX_LENGTH.number) {
                    v = v.slice(0, MAX_LENGTH.number);
                }

                input.value = v;
            });
            break;


        case 'textarea':
            input = document.createElement('textarea');
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            input.placeholder = field.meta?.placeholder ?? '';
            bindLengthCounter(input, MAX_LENGTH.textarea);
            break;

        case 'date':
            input = document.createElement('input');
            input.type = 'date';
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            break;

        case 'time':
            input = document.createElement('input');
            input.type = 'time';
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            break;

        case 'date-range':
            input = createDateRange(field);
            break;

        case 'event-date-range':
            wrapper.classList.add('schedule-field');
            input = await createEventDateRange(field);
            break;


        case 'time-range':
            input = createTimeRange(field);
            break;

        case 'radio':
            input = createRadioGroup(field);
            break;

        case 'checkbox':
            input = createCheckboxGroup(field);
            break;

        case 'notice':
            input = document.createElement('div');
            input.className = `notice ${field.meta?.style || ''}`;
            input.textContent = field.meta?.message || '';
            break;

        case 'table':
            input = createTableField(field);
            break;

        case 'currency':
            input = createCurrencyField(field);
            break;

        default:
            input = createPlaceholder(field);
    }

    if (input && input.dataset !== undefined) {
        input.dataset.fieldId = field.fieldId;
        if (field.editable === false) {
            input.disabled = true;
        }
    }

    wrapper.appendChild(input);
    return wrapper;
}

function createPlaceholder(field) {
    const div = document.createElement('div');
    div.className = 'field-placeholder';
    div.textContent = `${field.label} (추후 구현 예정: ${field.fieldType})`;
    return div;
}

function createTimeRange(field) {
    const container = document.createElement('div');
    container.className = 'time-range';

    const start = document.createElement('input');
    start.type = 'time';
    start.value = field.value?.start || '';
    start.dataset.fieldId = field.fieldId;
    start.dataset.rangeType = 'start';

    const end = document.createElement('input');
    end.type = 'time';
    end.value = field.value?.end || '';
    end.dataset.fieldId = field.fieldId;
    end.dataset.rangeType = 'end';

    container.append(start, document.createTextNode(' ~ '), end);
    return container;
}


function createRadioGroup(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'radio-group'; // ✅ 추가
    wrapper.dataset.fieldId = field.fieldId; // ✅ 그룹 대표 ID

    (field.meta?.options || []).forEach(opt => {
        const label = document.createElement('label');
        const input = document.createElement('input');

        input.type = 'radio';
        input.name = field.fieldId;
        input.value = opt.id;
        input.dataset.fieldId = field.fieldId;

        input.checked = field.value === opt.id;

        label.append(input, document.createTextNode(opt.label));
        wrapper.appendChild(label);
    });

    return wrapper;
}


function createCurrencyField(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'currency-field';

    // 통화 단위
    const unit = document.createElement('span');
    unit.className = 'currency-unit';
    unit.textContent = field.meta?.unit ?? '₩';

    // 입력 필드
    const input = document.createElement('input');
    input.type = 'text';              // number ❌ (포맷 제어 불가)
    input.inputMode = 'decimal';      // 모바일 키패드
    input.placeholder = '0';
    input.value = formatCurrency(field.value);

    if (field.value !== null && field.value !== undefined && field.value !== '') {
        input.dataset.rawValue = String(field.value);
    }

    input.dataset.fieldId = field.fieldId;

    bindLengthCounter(input, MAX_LENGTH.currency);

    input.addEventListener('input', () => {
        const raw = extractNumber(input.value);

        if (raw === '') {
            input.value = '';
            delete input.dataset.rawValue;
        } else {
            input.value = formatCurrency(raw);
            input.dataset.rawValue = raw;
        }

        // ✅ 다른 input과 동일하게 에러 제거
        clearFieldError(input);
    });


    wrapper.append(unit, input);
    return wrapper;
}

function extractNumber(value) {
    if (!value) return '';

    // 숫자 + 소수점만 허용
    const cleaned = value.replace(/[^0-9.]/g, '');

    // 소수점 1개만 유지
    const parts = cleaned.split('.');
    if (parts.length > 2) {
        return parts[0] + '.' + parts.slice(1).join('');
    }

    return cleaned;
}

function formatCurrency(value) {
    if (value === '' || value === null || value === undefined) return '';

    const num = Number(value);
    if (isNaN(num)) return '';

    return num.toLocaleString(undefined, {
        minimumFractionDigits: 0,
        maximumFractionDigits: 10
    });
}


function createCheckboxGroup(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'checkbox-group';
    wrapper.dataset.fieldId = field.fieldId;

    const values = Array.isArray(field.value) ? field.value : [];

    (field.meta?.options || []).forEach(opt => {
        const label = document.createElement('label');
        const input = document.createElement('input');

        input.type = 'checkbox';
        input.value = opt.id;
        input.dataset.fieldId = field.fieldId;

        input.checked = values.includes(opt.id);

        label.append(input, document.createTextNode(opt.label));
        wrapper.appendChild(label);
    });

    return wrapper;
}


function createTableField(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'doc-table';
    wrapper.dataset.fieldId = field.fieldId;

    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const tbody = document.createElement('tbody');

    /* header */
    const headerRow = document.createElement('tr');
    field.meta.columns.forEach(col => {
        const th = document.createElement('th');
        th.textContent = col.label;

        if (col.required === true) {
            th.classList.add('required');
        }

        headerRow.appendChild(th);
    });


    if (field.meta.rowPolicy?.removable) {
        headerRow.appendChild(document.createElement('th'));
    }

    thead.appendChild(headerRow);

    /* rows */
    const initialRows = field.value ?? [];
    if (initialRows.length === 0) {
        addTableRow(tbody, field);
    } else {
        initialRows.forEach(row =>
            addTableRow(tbody, field, row)
        );
    }

    table.append(thead, tbody);
    wrapper.appendChild(table);

    /* add row 버튼 */
    let addBtn = null;
    if (field.meta.rowPolicy?.addable) {
        addBtn = document.createElement('button');
        addBtn.type = 'button';
        addBtn.className = 'add-row-btn';
        addBtn.textContent = '행 추가';

        addBtn.addEventListener('click', () => {
            addTableRow(tbody, field);
            updateTableRowControls(tbody, field, addBtn);
        });

        wrapper.appendChild(addBtn);
    }

    // 초기 상태 반영
    updateTableRowControls(tbody, field, addBtn);

    return wrapper;
}

function createImageField(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'doc-image-wrapper';

    const label = document.createElement('label');
    label.className = 'info-label';
    label.textContent = field.label;
    wrapper.appendChild(label);

    const uploadContainer = document.createElement('div');
    uploadContainer.className = 'image-upload-container';

    const inputFile = document.createElement('input');
    inputFile.type = 'file';
    inputFile.accept = 'image/*';
    inputFile.style.display = 'none';
    inputFile.dataset.fieldId = field.fieldId;

    const preview = document.createElement('div');
    preview.className = 'image-preview';

    const img = document.createElement('img');
    img.alt = '미리보기';
    img.style.display = 'none';

    const placeholder = document.createElement('div');
    placeholder.className = 'upload-placeholder';
    placeholder.innerHTML = `
        <i class="fas fa-cloud-upload-alt"></i>
        <p>클릭하여 이미지 업로드</p>
        <span class="upload-hint">JPG, PNG, GIF</span>
    `;

    preview.append(img, placeholder);
    uploadContainer.append(inputFile, preview);
    wrapper.appendChild(uploadContainer);

    // 클릭 시 파일 선택
    preview.addEventListener('click', () => {
        inputFile.click();
    });

    // 파일 선택 시 미리보기
    inputFile.addEventListener('change', () => {
        const file = inputFile.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = e => {
            img.src = e.target.result;
            img.style.display = 'block';
            placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);
    });

    return wrapper;
}

function getTableCellPlaceholderByType(type) {
    switch (type) {
        case 'number':
            return '숫자 입력';
        case 'currency':
            return '금액 입력';
        case 'text':
        default:
            return '텍스트 입력';
    }
}


function addTableRow(tbody, field, rowData = {}) {
    const tr = document.createElement('tr');

    field.meta.columns.forEach(col => {
        const td = document.createElement('td');
        const input = document.createElement('input');

        const type = col.type; // text | number | currency
        const maxLength =
            type === 'number'
                ? MAX_LENGTH.table.number
                : type === 'currency'
                    ? MAX_LENGTH.table.currency
                    : MAX_LENGTH.table.text;

        input.type = 'text';
        input.inputMode =
            type === 'number' || type === 'currency'
                ? 'decimal'
                : 'text';

        input.value = rowData[col.id] ?? '';
        input.placeholder = getTableCellPlaceholderByType(type);
        input.dataset.colId = col.id;

        /* =========================
           입력 제어
        ========================= */
        input.addEventListener('input', () => {
            if (!isInitializing) {
                hasUnsavedChanges = true;
            }

            let v = input.value;

            // 숫자 / 통화 → 숫자만
            if (type === 'number' || type === 'currency') {
                v = v.replace(/[^0-9.]/g, '');
            }

            // 길이 제한
            if (v.length > maxLength) {
                v = v.slice(0, maxLength);
            }

            if (v === '') {
                input.value = '';
                delete input.dataset.rawValue;
                hideTableCellHint(input);
                clearTableFieldError(field.fieldId);
                return;
            }

            if (type === 'currency') {
                input.value = formatCurrency(v);
                input.dataset.rawValue = v;
            } else {
                input.value = v;
            }

            showTableCellHint(input, v.length, maxLength);
            clearTableFieldError(field.fieldId);
        });


        input.addEventListener('blur', () => {
            input.removeAttribute('title');
            hideTableCellHint(input);
        });

        td.appendChild(input);
        tr.appendChild(td);
    });

    /* =========================
       행 삭제 버튼
    ========================= */
    if (field.meta.rowPolicy?.removable) {
        const td = document.createElement('td');
        const btn = document.createElement('button');

        btn.textContent = '삭제';
        btn.className = 'remove-row-btn';

        btn.addEventListener('click', () => {
            tr.remove();

            const tbody = tr.closest('tbody');
            const fieldId = tbody.closest('.doc-table')?.dataset.fieldId;
            const field = documentFieldDefinitions.find(f => f.fieldId === fieldId);
            const addBtn = tbody.closest('.doc-table')?.querySelector('.add-row-btn');

            updateTableRowControls(tbody, field, addBtn);
        });

        td.appendChild(btn);
        tr.appendChild(td);
    }

    tbody.appendChild(tr);
}

/**
 * 날짜 범위 필드
 */
function createDateRange(field) {
    const container = document.createElement('div');
    container.className = 'date-range';

    const start = document.createElement('input');
    start.type = 'date';
    start.value = field.value?.start || '';

    const end = document.createElement('input');
    end.type = 'date';
    end.value = field.value?.end || '';

    start.dataset.fieldId = field.fieldId;
    start.dataset.rangeType = 'start';
    end.dataset.fieldId = field.fieldId;
    end.dataset.rangeType = 'end';

    container.appendChild(start);
    container.appendChild(document.createTextNode(' ~ '));
    container.appendChild(end);

    return container;
}


function normalizeInputValue(value) {
    if (value === null || value === undefined) return '';
    if (typeof value === 'object') return '';
    return String(value);
}

/* =====================================================
   결재선 조회
   ===================================================== */

async function loadApprovalLines(documentId) {
    const res = await apiFetch(`/api/approval-lines/${documentId}`);
    if (!res.ok) throw new Error('Failed to load approval lines');

    const result = await res.json();
    renderApprovalLines(result.data ?? result);
}

function renderApprovalLines(lines) {
    const container = document.getElementById('approvalLineContainer');
    container.innerHTML = '';

    lines.forEach(line => {
        container.appendChild(createApprovalLineRow(line));
    });
}

function createApprovalLineRow(line) {
    const row = document.createElement('div');
    row.className = 'approval-line-row';
    row.dataset.approvalLineId = line.approvalLineId;

    const approverText = line.approverId
        ? `${line.organizationName} / ${line.positionName} · (${line.approverEmployeeNo}) ${line.approverName}`
        : `${line.organizationName} / ${line.positionName} · 결재자 미지정`;

    row.innerHTML = `
        <span class="order">STEP ${line.orderNo}</span>

        <span class="approver-display">
            ${approverText}
        </span>

        <span class="approver-editor hidden">
            <select class="approver-select" disabled></select>
        </span>

        <button type="button" class="change-approver-btn">
            변경
        </button>
    `;

    // 버튼 이벤트
    row.querySelector('.change-approver-btn')
        .addEventListener('click', () => {
            toggleApproverEditor(row, line);
        });

    return row;
}

function canOpenApproverEditor(line, employees) {
    if (!employees || employees.length === 0) return false;

    // 미지정이면 무조건 열어야 함 (지정 강제)
    if (line.approverId == null) return true;

    // 이미 지정된 경우: 다른 후보가 있어야 열 수 있음
    return employees.length > 1;
}


async function toggleApproverEditor(row, line) {
    const displayEl = row.querySelector('.approver-display');
    const editorEl = row.querySelector('.approver-editor');
    const selectEl = row.querySelector('.approver-select');
    const btn = row.querySelector('.change-approver-btn');

    if (btn.dataset.loading === 'true') return;
    btn.dataset.loading = 'true';
    btn.disabled = true;

    try {
        const employees = await fetchEmployeesForApproval(line);

        // CASE 1: 미지정 + 후보 1명 → 자동 지정
        if (line.approverId == null && employees.length === 1) {
            const emp = employees[0];

            await patchApprovalLineApprover(
                line.approvalLineId,
                String(emp.id)
            );

            clearApprovalLineError(row);

            showToast(
                `STEP ${line.orderNo} · (${emp.employeeNo}) ${emp.name} 님이 자동 지정되었습니다.`,
                'success'
            );

            await reloadApprovalLines();
            return;
        }

        // CASE 2: 변경 불가
        if (!canOpenApproverEditor(line, employees)) {
            clearApprovalLineError(row);
            editorEl.classList.add('hidden');
            displayEl.classList.remove('hidden');

            showToast('변경 가능한 다른 사원이 없습니다.', 'warning');
            return;
        }

        // CASE 3: 일반 변경 UI
        clearApprovalLineError(row);

        displayEl.classList.add('hidden');
        editorEl.classList.remove('hidden');
        selectEl.disabled = false;

        renderEmployeeOptions(selectEl, employees, line.approverId);

        selectEl.onchange = async () => {
            clearApprovalLineError(row);

            const selectedId = String(selectEl.value);

            editorEl.classList.add('hidden');
            displayEl.classList.remove('hidden');

            await patchApprovalLineApprover(
                line.approvalLineId,
                selectedId
            );
            await reloadApprovalLines();
        };

    } finally {
        btn.disabled = false;
        btn.dataset.loading = 'false';
    }
}


async function fetchEmployeesForApproval(line) {
    const params = new URLSearchParams({
        orgId: line.organizationId,
        positionCategoryId: line.positionCategoryId
    });

    const res = await apiFetch(`/api/admin/employees/by-org-and-position?${params}`);
    if (!res.ok) {
        alert('사원 목록을 불러오지 못했습니다.');
        return [];
    }

    const result = await res.json();
    return result.data ?? [];
}

function renderEmployeeOptions(selectEl, employees, selectedId) {
    selectEl.innerHTML = ''; // ⭐ 기존 옵션 완전 제거

    employees.forEach(emp => {
        const opt = document.createElement('option');
        opt.value = String(emp.id);
        opt.textContent = `(${emp.employeeNo}) ${emp.name}`;
        selectEl.appendChild(opt);
    });

    // 항상 하나는 선택되도록
    if (selectedId != null) {
        selectEl.value = String(selectedId);
    } else if (selectEl.options.length > 0) {
        selectEl.selectedIndex = 0;
    }
}


async function patchApprovalLineApprover(approvalLineId, approverId) {
    const res = await apiFetch(`/api/approval-lines/${approvalLineId}`, {
        method: 'PATCH',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            approverId
        })
    });

    if (!res.ok) {
        showToast('결재자 변경에 실패했습니다.', 'error');
    }
}

async function reloadApprovalLines() {
    const res = await apiFetch(`/api/approval-lines/${DOCUMENT_ID}`);
    const result = await res.json();
    renderApprovalLines(result.data ?? []);
}


function createVacationDateUI(field) {
    const wrapper = document.createElement('div');
    wrapper.className = 'vacation-date-ui';

    const start = document.createElement('input');
    start.type = 'date';

    const end = document.createElement('input');
    end.type = 'date';

    start.dataset.fieldId = field.fieldId;
    start.dataset.rangeType = 'start';
    end.dataset.fieldId = field.fieldId;
    end.dataset.rangeType = 'end';

    wrapper.append(
        labeled('시작일', start),
        labeled('종료일', end)
    );

    return wrapper;
}

async function createEventDateRange(field) {
    const baseRole = field.meta?.baseRole;
    const ui = field.meta?.ui ?? {};

    const inputWrapper = document.createElement('div');
    inputWrapper.className = 'schedule-input';

    /* 1️⃣ 시작일 - 종료일 */
    const rangeGroup = document.createElement('div');
    rangeGroup.className = 'schedule-group';

    const rangeLabel = document.createElement('div');
    rangeLabel.className = 'schedule-group-label';
    rangeLabel.textContent = '시작일 - 종료일';

    const dateRange = document.createElement('div');
    dateRange.className = 'date-range';

    const start = document.createElement('input');
    start.type = 'date';
    start.dataset.fieldId = field.fieldId;
    start.dataset.rangeType = 'start';
    start.value = field.value?.start || '';

    const end = document.createElement('input');
    end.type = 'date';
    end.dataset.fieldId = field.fieldId;
    end.dataset.rangeType = 'end';
    end.value = field.value?.end || '';

    dateRange.append(start, document.createTextNode(' ~ '), end);
    rangeGroup.append(rangeLabel, dateRange);
    inputWrapper.appendChild(rangeGroup);

    /* 2️⃣ 휴가 유형 (VACATION만) */
    if (baseRole === 'VACATION') {
        const leaveTypes = await loadLeaveTypes();

        const typeGroup = document.createElement('div');
        typeGroup.className = 'schedule-group';

        const typeLabel = document.createElement('div');
        typeLabel.className = 'schedule-group-label';
        typeLabel.textContent = '휴가 유형';

        const select = document.createElement('select');
        select.dataset.fieldId = field.fieldId;
        select.dataset.subKey = 'vacationTypeId';

        const empty = document.createElement('option');
        empty.value = '';
        empty.textContent = '선택';
        select.appendChild(empty);

        leaveTypes.forEach(t => {
            const opt = document.createElement('option');
            opt.value = String(t.typeId);
            opt.textContent = t.typeName;
            opt.selected =
                String(field.value?.vacationTypeId) === String(t.typeId);
            select.appendChild(opt);
        });

        typeGroup.append(typeLabel, select);
        inputWrapper.appendChild(typeGroup);
    }

    /* 3️⃣ 일정명 */
    if (ui.requireTitle) {
        const titleGroup = document.createElement('div');
        titleGroup.className = 'schedule-group';

        const titleLabel = document.createElement('div');
        titleLabel.className = 'schedule-group-label';
        titleLabel.textContent = '일정명';

        const titleInput = document.createElement('input');
        titleInput.type = 'text';
        titleInput.placeholder = '일정 제목을 입력하세요';
        titleInput.dataset.fieldId = field.fieldId;
        titleInput.dataset.subKey = 'title';
        titleInput.value = field.value?.title || '';

        bindLengthCounter(titleInput, MAX_LENGTH.text);

        titleGroup.append(titleLabel, titleInput);
        inputWrapper.appendChild(titleGroup);
    }

    /* 4️⃣ 사유 / 설명 (하나의 textarea, 정책 분기) */
    if (ui.requireReason || ui.requireDescription) {
        const textGroup = document.createElement('div');
        textGroup.className = 'schedule-group';

        const textLabel = document.createElement('div');
        textLabel.className = 'schedule-group-label';

        const textarea = document.createElement('textarea');
        textarea.dataset.fieldId = field.fieldId;
        textarea.placeholder = '내용을 입력하세요.';

        if (ui.requireReason) {
            textLabel.textContent = SCHEDULE_REASON_LABEL_MAP[baseRole] ?? '사유';
            textarea.dataset.subKey = 'reason';
            textarea.value = field.value?.reason || '';
        } else {
            textLabel.textContent = '일정 설명';
            textarea.dataset.subKey = 'description';
            textarea.value = field.value?.description || '';
        }

        bindLengthCounter(textarea, MAX_LENGTH.textarea);

        textGroup.append(textLabel, textarea);
        inputWrapper.appendChild(textGroup);
    }

    return inputWrapper;
}


function validateTableField(fieldDef, rows) {
    const {required, label, meta} = fieldDef;
    const columns = meta?.columns ?? [];
    const rowPolicy = meta?.rowPolicy ?? {};

    const minRows = rowPolicy.min ?? 0;
    const maxRows = rowPolicy.max ?? Infinity;

    /* =========================
       1️⃣ 테이블 자체 required
    ========================= */
    if (required && (!Array.isArray(rows) || rows.length === 0)) {
        return {
            valid: false,
            message: `${label}에 최소 1행 이상 입력해주세요.`,
            reason: 'REQUIRED'
        };
    }

    // required=false && 행 없음 → 통과
    if (!rows || rows.length === 0) {
        return {valid: true};
    }

    /* =========================
       2️⃣ 행 개수 검증
    ========================= */
    if (rows.length < minRows) {
        return {
            valid: false,
            message: `${label}에 최소 ${minRows}행 이상 입력해주세요.`,
            reason: 'MIN_ROWS'
        };
    }

    if (rows.length > maxRows) {
        return {
            valid: false,
            message: `${label}는 최대 ${maxRows}행까지 입력할 수 있습니다.`,
            reason: 'MAX_ROWS'
        };
    }

    /* =========================
       3️⃣ 행 · 컬럼 단위 검증
    ========================= */
    for (let rowIndex = 0; rowIndex < rows.length; rowIndex++) {
        const row = rows[rowIndex];

        // 🛡 완전히 빈 row 방어 (이론상 없어야 하지만 안전장치)
        const hasAnyValue = Object.values(row).some(
            v => v !== null && v !== undefined && String(v).trim() !== ''
        );
        if (!hasAnyValue) continue;

        for (const col of columns) {
            const value = row[col.id];
            const colType = col.type;

            const isEmpty =
                value === null ||
                value === undefined ||
                (typeof value === 'string' && value.trim() === '');

            /* --- required 컬럼 --- */
            if (col.required === true && isEmpty) {
                return {
                    valid: false,
                    message: `${label} ${rowIndex + 1}행의 "${col.label}"을(를) 입력해주세요.`,
                    rowIndex,
                    colId: col.id,
                    reason: 'REQUIRED_COLUMN'
                };
            }

            // 값이 없으면 타입 검증은 스킵
            if (isEmpty) continue;

            /* =========================
               타입별 검증
            ========================= */

            // 🔢 number
            if (colType === 'number') {
                if (isNaN(Number(value))) {
                    return {
                        valid: false,
                        message: `${label} ${rowIndex + 1}행의 "${col.label}"은 숫자여야 합니다.`,
                        rowIndex,
                        colId: col.id,
                        reason: 'INVALID_NUMBER'
                    };
                }

                if (
                    String(value).length >
                    (MAX_LENGTH.table?.number ?? Infinity)
                ) {
                    return {
                        valid: false,
                        message: `${label} ${rowIndex + 1}행의 "${col.label}"은 최대 ${MAX_LENGTH.table.number}자리까지 입력할 수 있습니다.`,
                        rowIndex,
                        colId: col.id,
                        reason: 'MAX_LENGTH'
                    };
                }
            }

            // 💰 currency
            if (colType === 'currency') {
                if (isNaN(Number(value))) {
                    return {
                        valid: false,
                        message: `${label} ${rowIndex + 1}행의 "${col.label}"은 올바른 금액이 아닙니다.`,
                        rowIndex,
                        colId: col.id,
                        reason: 'INVALID_CURRENCY'
                    };
                }

                if (
                    String(value).length >
                    (MAX_LENGTH.table?.currency ?? Infinity)
                ) {
                    return {
                        valid: false,
                        message: `${label} ${rowIndex + 1}행의 "${col.label}"은 최대 ${MAX_LENGTH.table.currency}자리까지 입력할 수 있습니다.`,
                        rowIndex,
                        colId: col.id,
                        reason: 'MAX_LENGTH'
                    };
                }
            }

            // 🔤 text
            if (colType === 'text') {
                if (
                    String(value).length >
                    (MAX_LENGTH.table?.text ?? Infinity)
                ) {
                    return {
                        valid: false,
                        message: `${label} ${rowIndex + 1}행의 "${col.label}"은 최대 ${MAX_LENGTH.table.text}자까지 입력할 수 있습니다.`,
                        rowIndex,
                        colId: col.id,
                        reason: 'MAX_LENGTH'
                    };
                }
            }
        }
    }

    return {valid: true};
}


function validateApprovalLines(lines) {
    const invalid = lines.find(line => !line.approverId);
    if (!invalid) return true;

    const row = document.querySelector(
        `.approval-line-row[data-approval-line-id="${invalid.approvalLineId}"]`
    );

    showApprovalLineError(
        row,
        '결재자가 지정되지 않았습니다.'
    );

    return false;
}


function isEmptyValue(value) {
    if (value === null || value === undefined) return true;

    if (typeof value === 'string') {
        return value.trim() === '';
    }

    if (Array.isArray(value)) {
        return value.length === 0;
    }

    if (typeof value === 'object') {
        return Object.values(value).every(v => isEmptyValue(v));
    }

    return false;
}

function validateRequiredField(field, value) {
    if (!field.required) return true;

    switch (field.fieldType) {

        case 'currency':
            return value !== null &&
                value !== undefined &&
                value !== '';

        case 'checkbox':
            // checkbox는 기본적으로 0개 선택 허용
            return true;

        case 'address':
        case 'employee-search':
        case 'department-search':
        case 'image':
            // 아직 기능 미구현
            return true;

        case 'event-date-range': {
            if (!value?.start || !value?.end) return false;
            if (value.start > value.end) return false;

            if (field.meta?.baseRole === 'VACATION') {
                if (!value?.vacationTypeId) return false;
            }

            if (field.meta?.ui?.requireTitle) {
                const title = value?.title;
                if (!title || !title.trim()) return false;
                if (title.length > MAX_LENGTH.text) return false;
            }

            if (field.meta?.ui?.requireReason) {
                const reason = value?.reason;
                if (!reason || !reason.trim()) return false;
            }

            if (field.meta?.ui?.requireDescription) {
                const desc = value?.description;
                if (!desc || !desc.trim()) return false;
            }

            return true;
        }

        case 'date-range':
        case 'time-range':
            return Boolean(value?.start && value?.end);

        case 'table':
            return true; // table은 schema 기반 검증에서만 처리

        default:
            return !isEmptyValue(value);
    }
}


function validateDocumentTitle() {
    const titleInput = document.getElementById('documentTitle');
    const fieldId = titleInput?.dataset.fieldId;

    if (!titleInput || !fieldId) return true;

    if (titleInput.value.trim() === '') {
        showFieldError(
            titleInput,
            '문서 제목은 필수 항목입니다.'
        );
        return false;
    }

    return true;
}


function updateTableRowControls(tbody, field, addBtn) {
    const rowPolicy = field.meta?.rowPolicy ?? {};
    const min = rowPolicy.min ?? 0;
    const max = rowPolicy.max ?? Infinity;

    const rows = tbody.querySelectorAll('tr');
    const rowCount = rows.length;

    // ▶ add 버튼
    if (addBtn) {
        const canAdd =
            rowPolicy.addable !== false &&
            rowCount < max;

        addBtn.disabled = !canAdd;
        addBtn.title = !canAdd && rowCount >= max
            ? `최대 ${max}행까지 입력할 수 있습니다.`
            : '';
    }

    // ▶ remove 버튼
    rows.forEach(row => {
        const removeBtn = row.querySelector('.remove-row-btn');
        if (!removeBtn) return;

        const canRemove =
            rowPolicy.removable !== false &&
            rowCount > min;

        removeBtn.disabled = !canRemove;
        removeBtn.title = !canRemove && rowCount <= min
            ? `최소 ${min}행은 유지해야 합니다.`
            : '';
    });
}


function validateFieldsByTypeWithSchema(fieldDefs, valuesById) {
    for (const field of fieldDefs) {
        const value = valuesById.get(field.fieldId);

        switch (field.fieldType) {

            case 'date-range':
            case 'time-range':
                if (!validateRangeField(field, value)) {
                    return false;
                }
                break;

            case 'table': {
                const result = validateTableField(field, value);

                if (!result.valid) {
                    const fieldWrapper = getFieldWrapperByFieldId(field.fieldId);
                    const table = fieldWrapper?.querySelector('table');
                    const tbody = table?.querySelector('tbody');

                    // 1️⃣ 기존 table 에러 정리
                    fieldWrapper?.classList.add('field-error');
                    fieldWrapper?.querySelector('.field-hint')?.remove();
                    table?.querySelectorAll('input.field-error')
                        .forEach(el => el.classList.remove('field-error'));

                    // 2️⃣ hint 표시 (doc-field 하단)
                    const hint = document.createElement('div');
                    hint.className = 'field-hint';
                    hint.textContent = result.message;
                    fieldWrapper?.appendChild(hint);

                    // 3️⃣ 특정 셀 하이라이트 + 포커스
                    if (
                        result.rowIndex != null &&
                        result.colId &&
                        tbody
                    ) {
                        const row = tbody.querySelectorAll('tr')[result.rowIndex];
                        const cellInput = row?.querySelector(
                            `input[data-col-id="${result.colId}"]`
                        );

                        if (cellInput) {
                            cellInput.classList.add('field-error');

                            requestAnimationFrame(() =>
                                cellInput.focus({preventScroll: true})
                            );
                        }
                    }

                    fieldWrapper?.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });

                    return false;
                }
                break;
            }
            case 'number': {
                if (value == null) break;

                if (!/^\d+$/.test(String(value))) {
                    showFieldError(
                        document.querySelector(`[data-field-id="${field.fieldId}"]`),
                        `${field.label}에는 숫자만 입력할 수 있습니다.`
                    );
                    return false;
                }
                break;
            }
            case 'currency': {
                if (value == null) break;

                if (isNaN(Number(value))) {
                    showCurrencyFieldError(
                        field.fieldId,
                        `${field.label}은(는) 올바른 금액이 아닙니다.`
                    );
                    return false;
                }
                break;
            }
            case 'checkbox': {
                const opts = field.meta ?? {};
                const min = opts.minSelected ?? 0;
                const max = opts.maxSelected ?? Infinity;

                const count = Array.isArray(value) ? value.length : 0;

                if (count < min) {
                    showCheckboxGroupError(
                        field.fieldId,
                        `${field.label}은 최소 ${min}개 이상 선택해야 합니다.`
                    );
                    return false;
                }

                if (count > max) {
                    showCheckboxGroupError(
                        field.fieldId,
                        `${field.label}은 최대 ${max}개까지 선택할 수 있습니다.`
                    );
                    return false;
                }

                break;
            }


        }
    }
    return true;
}

function validateRangeField(fieldDef, value) {
    if (!value) {
        showRangeFieldError(fieldDef.fieldId, '값을 입력해주세요.', 'start');
        return false;
    }

    const {start, end} = value;

    if (!start) {
        showRangeFieldError(fieldDef.fieldId, '시작 값을 입력해주세요.', 'start');
        return false;
    }

    if (!end) {
        showRangeFieldError(fieldDef.fieldId, '종료 값을 입력해주세요.', 'end');
        return false;
    }

    // ✅ start <= end 검증
    if (start > end) {
        showRangeFieldError(
            fieldDef.fieldId,
            '시작 값은 종료 값보다 클 수 없습니다.',
            'start'
        );
        return false;
    }

    clearRangeFieldErrorByFieldId(fieldDef.fieldId);
    return true;
}


function validateRequiredFieldsWithSchema(fieldDefs, valuesById) {
    for (const field of fieldDefs) {
        if (!field.required) continue;

        const value = valuesById.get(field.fieldId);

        const valid = validateRequiredField(field, value);

        if (!valid) {

            // ✅ range류는 wrapper 기준 처리
            if (
                field.fieldType === 'date-range' ||
                field.fieldType === 'time-range'
            ) {
                // start/end 중 뭘 비웠는지에 따라 포커스도 맞춤
                const focusType = !value?.start ? 'start' : 'end';
                showRangeFieldError(
                    field.fieldId,
                    `필수 항목입니다. ${field.label}을(를) 입력해주세요.`,
                    focusType
                );
                return false;
            }

            if (field.fieldType === 'event-date-range') {
                if (!validateEventDateRange(field, value)) return false;
                continue;
            }


            if (field.fieldType === 'radio') {
                showRadioGroupError(
                    field.fieldId,
                    `필수 항목입니다. ${field.label}을(를) 선택해주세요.`
                );
                return false;
            }

            if (field.fieldType === 'currency') {
                showCurrencyFieldError(
                    field.fieldId,
                    `필수 항목입니다. ${field.label}을(를) 입력해주세요.`
                );
                return false;
            }

            const inputEl = document.querySelector(
                `[data-field-id="${field.fieldId}"]`
            );

            showFieldError(
                inputEl,
                `필수 항목입니다. ${field.label}을(를) 입력해주세요.`
            );
            return false;
        }
    }
    return true;
}


function validateEventDateRange(field, value) {

    // 1️⃣ 날짜
    if (!value?.start || !value?.end) {
        showRangeFieldError(
            field.fieldId,
            '필수 항목입니다. 날짜를 입력해주세요.',
            !value?.start ? 'start' : 'end'
        );
        return false;
    }

    if (value.start > value.end) {
        showRangeFieldError(
            field.fieldId,
            '시작일은 종료일보다 클 수 없습니다.',
            'start'
        );
        return false;
    }

    // 2️⃣ 휴가 유형
    if (field.meta?.baseRole === 'VACATION' && !value?.vacationTypeId) {
        const selectEl = document.querySelector(
            `select[data-field-id="${field.fieldId}"][data-subKey="vacationTypeId"]`
        );
        showFieldError(selectEl, '휴가 유형을 선택해주세요.');
        return false;
    }

    // 3️⃣ 일정명
    if (field.meta?.ui?.requireTitle && (!value?.title || !value.title.trim())) {
        const input = document.querySelector(
            `input[data-field-id="${field.fieldId}"][data-subKey="title"]`
        );
        showFieldError(input, '필수 항목입니다. 일정명을 입력해주세요.');
        return false;
    }

    // 4️⃣ 사유
    if (field.meta?.ui?.requireReason && (!value?.reason || !value.reason.trim())) {
        const textarea = document.querySelector(
            `textarea[data-field-id="${field.fieldId}"][data-subKey="reason"]`
        );
        showFieldError(textarea, '필수 항목입니다. 사유를 입력해주세요.');
        return false;
    }

    // 5️⃣ 설명
    if (field.meta?.ui?.requireDescription && (!value?.description || !value.description.trim())) {
        const textarea = document.querySelector(
            `textarea[data-field-id="${field.fieldId}"][data-subKey="description"]`
        );
        showFieldError(textarea, '필수 항목입니다. 일정 설명을 입력해주세요.');
        return false;
    }

    return true;
}


function getFieldWrapperByFieldId(fieldId) {
    return document
        .querySelector(`[data-field-id="${fieldId}"]`)
        ?.closest('.doc-field') || null;
}


function showRangeFieldError(fieldId, message, focusRangeType = 'start') {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    // 🔹 실제 range input wrapper만 타겟
    const rangeWrapper =
        fieldWrapper.querySelector('.date-range, .time-range');

    if (!rangeWrapper) return;

    // 기존 hint 제거
    fieldWrapper.querySelector('.field-hint')?.remove();

    // 에러 클래스
    fieldWrapper.classList.add('field-error');
    rangeWrapper.classList.add('field-error');

    // start / end input 하이라이트
    rangeWrapper
        .querySelectorAll('input[data-range-type]')
        .forEach(inp => inp.classList.add('field-error'));

    // 메시지
    const hint = document.createElement('div');
    hint.className = 'field-hint';
    hint.textContent = message;
    fieldWrapper.appendChild(hint);

    // 스크롤
    fieldWrapper.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    // 포커스
    const focusTarget = rangeWrapper.querySelector(
        `input[data-range-type="${focusRangeType}"]:not([disabled])`
    );
    focusTarget?.focus({preventScroll: true});
}


function clearRangeFieldErrorByFieldId(fieldId) {
    const fieldWrapper = getFieldWrapperByFieldId(fieldId);
    if (!fieldWrapper) return;

    fieldWrapper.classList.remove('field-error');
    fieldWrapper.querySelector('.field-hint')?.remove();

    const rangeWrapper =
        fieldWrapper.querySelector(
            '.date-range, .time-range, .event-date-range'
        );

    rangeWrapper?.classList.remove('field-error');
    rangeWrapper
        ?.querySelectorAll('input[data-range-type]')
        .forEach(inp => inp.classList.remove('field-error'));
}


/* =====================================================
   이벤트 바인딩
   ===================================================== */

function bindEvents(documentId) {

    // 임시 저장
    document.getElementById('tempSaveBtn')
        ?.addEventListener('click', async () => {
            await tempSave(documentId);
        });

    // 이전
    document.getElementById('prevBtn')
        ?.addEventListener('click', () => {
            if (!confirmLeaveIfDirty()) return;

            history.back();
        });


    // 다음
    document.getElementById('nextBtn')
        ?.addEventListener('click', async () => {

            // 0️⃣ 문서 제목 검증
            if (!validateDocumentTitle()) return;

            // 1️⃣ 결재선 검증
            const res = await apiFetch(`/api/approval-lines/${DOCUMENT_ID}`);
            const result = await res.json();
            const approvalLines = result.data ?? [];

            if (!validateApprovalLines(approvalLines)) return;

            // 2️⃣ 문서 값 수집
            const payload = collectDocumentValues();
            const valuesById = new Map(
                payload.fields.map(f => [f.fieldId, f.value])
            );

            // 3️⃣ required 검증
            if (!validateRequiredFieldsWithSchema(
                documentFieldDefinitions,
                valuesById
            )) return;

            // 4️⃣ 타입별 검증
            if (!validateFieldsByTypeWithSchema(
                documentFieldDefinitions,
                valuesById
            )) return;

            alert('검증 모두 통과.');
            return;

            try {
                // 5️⃣ 최종 저장
                await apiFetch(`/api/document-contents/${DOCUMENT_ID}`, {
                    method: 'PATCH',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload)
                });

                hasUnsavedChanges = false;

                // 6️⃣ 제출
                await apiFetch(`/api/documents/${DOCUMENT_ID}/submit`, {
                    method: 'POST'
                });

                // 7️⃣ 이동
                alert('문서가 상신되었습니다.');
                location.href = '/view/document/my-documents';

            } catch (e) {
                console.error(e);
                alert('문서 상신 중 오류가 발생했습니다.');
            }
        });


}

/* =====================================================
   임시 저장
   ===================================================== */

async function tempSave(documentId) {
    const payload = collectDocumentValues();

    const res = await apiFetch(`/api/document-contents/${documentId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        alert('임시 저장에 실패했습니다.');
        return;
    }
    hasUnsavedChanges = false;
    showToast('임시 저장되었습니다.', 'success');
}

/**
 * 화면에서 field 값 수집
 */
function collectDocumentValues() {
    const fieldMap = new Map();
    const processedFieldIds = new Set();

    /* =====================================================
       TABLE
       ===================================================== */
    document.querySelectorAll('.doc-table').forEach(tableEl => {
        const fieldId = tableEl.dataset.fieldId;
        if (!fieldId) return;

        const rows = [];

        tableEl.querySelectorAll('tbody tr').forEach(tr => {
            const row = {};
            let hasValue = false;

            tr.querySelectorAll('input[data-col-id]').forEach(input => {
                const colId = input.dataset.colId;

                let value;

                if (input.dataset.rawValue !== undefined) {
                    value = input.dataset.rawValue === ''
                        ? null
                        : Number(input.dataset.rawValue);
                } else {
                    value = input.value === '' ? null : input.value;
                }

                if (value !== null) hasValue = true;
                row[colId] = value;
            });


            // 완전히 빈 행은 제외
            if (hasValue) {
                rows.push(row);
            }
        });

        fieldMap.set(fieldId, {
            fieldId,
            value: rows
        });
        processedFieldIds.add(fieldId);
    });

    /* =====================================================
       OTHER FIELDS
       ===================================================== */
    document.querySelectorAll(
        'input[data-field-id], textarea[data-field-id], select[data-field-id]'
    ).forEach(el => {

        const fieldId = el.dataset.fieldId;
        if (!fieldId || el.disabled) return;

        // table에서 이미 처리된 fieldId는 스킵
        if (processedFieldIds.has(fieldId)) return;

        /* ===== range (date / time range) ===== */
        if (el.dataset.rangeType) {
            const existing = fieldMap.get(fieldId) ?? {
                fieldId,
                value: {}
            };

            existing.value[el.dataset.rangeType] = el.value;
            fieldMap.set(fieldId, existing);
            return;
        }

        /* ===== currency ===== */
        if (el.dataset.rawValue !== undefined) {
            fieldMap.set(fieldId, {
                fieldId,
                value:
                    el.dataset.rawValue === ''
                        ? null
                        : Number(el.dataset.rawValue)
            });
            return;
        }

        /* ===== radio ===== */
        if (el.type === 'radio') {
            if (el.checked) {
                fieldMap.set(fieldId, {
                    fieldId,
                    value: el.value
                });
            }
            return;
        }

        /* ===== checkbox ===== */
        if (el.type === 'checkbox') {
            const existing = fieldMap.get(fieldId) ?? {
                fieldId,
                value: []
            };

            if (el.checked) {
                existing.value.push(el.value);
            }

            fieldMap.set(fieldId, existing);
            return;
        }

        /* ===== object field (subKey 기반 값) ===== */
        if (el.dataset.subKey) {
            const existing = fieldMap.get(fieldId) ?? {
                fieldId,
                value: {}
            };

            let value = el.value;
            if (value === '') value = null;

            existing.value[el.dataset.subKey] = value;
            fieldMap.set(fieldId, existing);
            return;
        }


        /* ===== default (text, number, textarea, date, time, select) ===== */
        fieldMap.set(fieldId, {
            fieldId,
            value: el.value
        });
    });

    return {
        fields: Array.from(fieldMap.values())
    };
}

