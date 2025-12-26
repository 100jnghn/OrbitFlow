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

document.addEventListener('DOMContentLoaded', async () => {

    if (typeof DOCUMENT_ID === 'undefined' || !DOCUMENT_ID) {
        alert('문서 정보가 올바르지 않습니다.');
        return;
    }

    try {
        await loadDocumentDraft(DOCUMENT_ID);
        await loadApprovalLines(DOCUMENT_ID);
        bindEvents(DOCUMENT_ID);
    } catch (e) {
        console.error(e);
        alert('문서 정보를 불러오는 중 오류가 발생했습니다.');
    }
});

/* =====================================================
   문서 초안 조회
   ===================================================== */

async function loadDocumentDraft(documentId) {
    const res = await apiFetch(`/api/document-contents/${documentId}`);
    if (!res.ok) throw new Error('Failed to load document content');

    const result = await res.json();
    const data = result.data;

    // fields 중 document-title 찾아서 상단 입력에 바인딩
    const titleField = (data.fields || []).find(f => f.fieldType === 'document-title');
    renderDocumentTitleFromField(titleField);

    // 나머지 필드 렌더링
    renderFields(data.fields || []);
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
}


/* =====================================================
   문서 필드 렌더링
   ===================================================== */

function renderFields(fields) {
    const container = document.getElementById('documentFields');
    container.innerHTML = '';

    fields
        .filter(f => f.fieldType !== 'document-title')   // ⭐ 제목은 상단에서만 처리
        .forEach(field => {
            const fieldEl = createFieldComponent(field);
            container.appendChild(fieldEl);
        });
}


/**
 * fieldType 별 컴포넌트 생성
 */
function createFieldComponent(field) {

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
            break;

        case 'number':
            input = document.createElement('input');
            input.type = 'number';
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            input.inputMode = 'decimal';
            break;

        case 'textarea':
            input = document.createElement('textarea');
            input.value = normalizeInputValue(field.value);   // ✅ 수정
            input.placeholder = field.meta?.placeholder ?? '';
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
        case 'schedule-date-range':
        case 'leave-date-range':
            input = createDateRange(field);
            break;

        case 'leave-reason':
            input = createLeaveReason(field);
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

    // 입력 이벤트
    input.addEventListener('input', () => {
        const raw = extractNumber(input.value);
        input.value = formatCurrency(raw);
        input.dataset.rawValue = raw;
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

    // header
    const headerRow = document.createElement('tr');
    field.meta.columns.forEach(col => {
        const th = document.createElement('th');
        th.textContent = col.label;
        headerRow.appendChild(th);
    });

    if (field.meta.rowPolicy?.removable) {
        headerRow.appendChild(document.createElement('th'));
    }

    thead.appendChild(headerRow);

    // 초기 row
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

    // add row 버튼
    if (field.meta.rowPolicy?.addable) {
        const addBtn = document.createElement('button');
        addBtn.type = 'button';
        addBtn.className = 'add-row-btn';
        addBtn.textContent = '행 추가';

        addBtn.addEventListener('click', () => {
            addTableRow(tbody, field);
        });

        wrapper.appendChild(addBtn);
    }

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


function addTableRow(tbody, field, rowData = {}) {
    const tr = document.createElement('tr');

    field.meta.columns.forEach(col => {
        const td = document.createElement('td');
        const input = document.createElement('input');

        input.type = col.type === 'number' ? 'number' : 'text';
        input.value = rowData[col.id] ?? '';
        input.placeholder = col.type;
        input.dataset.colId = col.id;


        td.appendChild(input);
        tr.appendChild(td);
    });

    if (field.meta.rowPolicy?.removable) {
        const td = document.createElement('td');
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = '삭제';

        btn.addEventListener('click', () => {
            tr.remove();
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

function createLeaveReason(field) {
    const container = document.createElement('div');
    container.className = 'leave-reason';

    const select = document.createElement('select');
    const textarea = document.createElement('textarea');

    // 기본 옵션
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '휴가 유형 선택';
    select.appendChild(defaultOption);

    // 휴가 유형 옵션
    (field.meta?.options || []).forEach(opt => {
        const option = document.createElement('option');
        option.value = opt.code;
        option.textContent = opt.name;
        select.appendChild(option);
    });

    // 기존 값 복원
    if (field.value?.vacationTypeCode) {
        select.value = field.value.vacationTypeCode;
    }

    textarea.placeholder = '상세 사유 입력';
    textarea.value = field.value?.detailReason ?? '';

    // ⭐ 하나의 fieldId로 묶음
    select.dataset.fieldId = field.fieldId;
    select.dataset.subKey = 'vacationTypeCode';

    textarea.dataset.fieldId = field.fieldId;
    textarea.dataset.subKey = 'detailReason';

    container.append(select, textarea);
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

    row.innerHTML = `
        <span class="order">STEP ${line.orderNo}</span>
        <span class="approver">
            ${line.approverId ? `사원 #${line.approverId}` : '결재자 미지정'}
        </span>
        <button class="change-approver-btn">변경</button>
    `;

    return row;
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
            history.back();
        });

    // 다음
    document.getElementById('nextBtn')
        ?.addEventListener('click', () => {
            // TODO: 결재 시작 단계
            alert('다음 단계로 이동');
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

    alert('임시 저장되었습니다.');
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
                const value = input.value;

                if (value !== '') hasValue = true;
                row[colId] = value === '' ? null : value;
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

        /* ===== leave-reason (subKey object) ===== */
        if (el.dataset.subKey) {
            const existing = fieldMap.get(fieldId) ?? {
                fieldId,
                value: {}
            };

            existing.value[el.dataset.subKey] = el.value;
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

