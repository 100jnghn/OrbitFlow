const fieldRenderers = {
    divider: renderDivider,
    radio: renderRadio,
    checkbox: renderCheckbox,
    table: renderTable,

    "time-range": renderSimpleRange,
    "date-range": renderSimpleRange,

    "employee-search": renderEmployee,
    "department-search": renderDepartment,

    currency: renderCurrency,

    address: renderAddress,

    "event-date-range": renderEventDateRange,
    notice: renderNotice,
    default: renderDefaultField
};

/* ===============================
   이전 버튼
=============================== */
function bindBackButton() {
    const backBtn = document.getElementById("backBtn");
    if (!backBtn) return;

    backBtn.addEventListener("click", () => {
        // 1️⃣ 히스토리가 있으면 뒤로
        if (window.history.length > 1) {
            window.history.back();
            return;
        }

        // 2️⃣ 없으면 문서 목록으로 fallback
        window.location.href = "/documents";
    });
}


/* =========================================================
   detail.js
   - 문서 상세 조회
   - 문서 내용 readonly 렌더
   - 결재선 표시
========================================================= */

document.addEventListener("DOMContentLoaded", async () => {
    await fetchVacationTypes();

    const documentId = getDocumentIdFromPath();
    if (!documentId) return;

    try {
        const data = await fetchDocumentDetail(documentId);
        initDocumentDetailPage(data);
    } catch (e) {
        console.error(e);
        alert("문서 정보를 불러오지 못했습니다.");
    }
});

/* ===============================
   초기화
=============================== */
function initDocumentDetailPage(data) {
    renderDocumentHeader(data);
    renderDocumentContent(data.contentSchema);
    renderApprovalLines(data.approvalLines);
    renderAttachments();
    controlActionButtons(data);
    bindBackButton();
}


const vacationTypeMap = new Map();

async function fetchVacationTypes() {
    const res = await apiFetch('/api/leave/types');
    if (!res.ok) return;

    const json = await res.json();

    json.data.forEach(v => {
        vacationTypeMap.set(String(v.typeId), v.typeName);
    });
}

/* ===============================
   문서 헤더
=============================== */
function renderDocumentHeader(data) {
    const badgeEl = document.getElementById("documentStatusBadge");

    if (badgeEl) {
        badgeEl.textContent = getStatusText(data.status);
        badgeEl.className = `status-badge ${data.status?.toLowerCase()}`;
    }
}


/* ===============================
   문서 본문 (document-content)
=============================== */
function renderDocumentContent(schema) {
    const container = document.getElementById("documentContentContainer");
    if (!container || !schema?.fields) return;

    container.innerHTML = "";

    // ✅ 문서 제목 (document-title)
    const titleField = schema.fields.find(
        f => f.fieldType === "document-title"
    );

    if (titleField?.value) {
        const titleEl = document.createElement("h2");
        titleEl.className = "doc-content-title";
        titleEl.textContent = titleField.value;
        container.appendChild(titleEl);
    }

    // 나머지 필드 렌더
    const fields = schema.fields
        .filter(f => f.fieldType !== "document-title")
        .sort((a, b) => a.order - b.order);

    fields.forEach(field => {
        const renderer =
            fieldRenderers[field.fieldType] ?? fieldRenderers.default;

        const el = renderer(field);
        if (el) container.appendChild(el);
    });
}


function createFieldWrapper(field, valueEl) {
    const row = document.createElement("div");
    row.className = `doc-field field-${field.fieldType}`;

    const label = document.createElement("div");
    label.className = "doc-field-label";
    label.textContent = field.label ?? "";

    const value = document.createElement("div");
    value.className = "doc-field-value";

    if (valueEl === null || valueEl === undefined || valueEl === "-") {
        value.textContent = "-";
        value.classList.add("empty");
    } else {
        value.appendChild(valueEl);
    }

    row.append(label, value);
    return row;
}

function renderSimpleRange(field) {
    const v = field.value ?? {};

    const text =
        (v.start || "-") + " ~ " + (v.end || "-");

    const span = document.createElement("span");
    span.textContent = text;

    return createFieldWrapper(field, span);
}

function renderEmployee(field) {
    const v = field.value;
    const span = document.createElement("span");

    if (!v) {
        span.textContent = "-";
        return createFieldWrapper(field, span);
    }

    // department
    const dept = v.departmentName?.trim();

    // position + name
    const personParts = [];
    if (v.positionName) personParts.push(v.positionName);
    if (v.name) personParts.push(v.name);

    // employee no
    const empNo = v.employeeNo ? `(${v.employeeNo})` : "";

    let text = "-";

    if (dept || personParts.length || empNo) {
        const personText = personParts.join(" ");
        text = [
            dept,
            personText + (empNo ? ` ${empNo}` : "")
        ]
            .filter(Boolean)
            .join(" / ");
    }

    span.textContent = text;
    return createFieldWrapper(field, span);
}


function renderDepartment(field) {
    const v = field.value;
    const span = document.createElement("span");

    if (!v) {
        span.textContent = "-";
    } else {
        span.textContent =
            v.displayText ||
            v.departmentName ||
            "-";
    }

    return createFieldWrapper(field, span);
}


function renderCurrency(field) {
    const v = field.value;
    const span = document.createElement("span");

    if (v === null || v === undefined || v === "") {
        span.textContent = "-";
        return createFieldWrapper(field, span);
    }

    const num = Number(v);
    if (Number.isNaN(num)) {
        span.textContent = "-";
        return createFieldWrapper(field, span);
    }

    const formatted = num.toLocaleString("ko-KR");

    const unit = field.meta?.unit || "KRW";

    span.textContent = `${formatted} ${unit}`;
    return createFieldWrapper(field, span);
}


function renderAddress(field) {
    const v = field.value ?? {};

    const wrapper = document.createElement("div");
    wrapper.className = "sub-field-table";

    wrapper.appendChild(createSubRow(
        "우편번호",
        v.postcode || "-"
    ));

    wrapper.appendChild(createSubRow(
        "도로명 주소",
        v.roadAddress || "-"
    ));

    wrapper.appendChild(createSubRow(
        "상세 주소",
        v.detailAddress || "-"
    ));

    return createFieldWrapper(field, wrapper);
}


function renderDefaultField(field) {
    const span = document.createElement("span");

    if (
        field.value === null ||
        field.value === "" ||
        (Array.isArray(field.value) && field.value.length === 0)
    ) {
        span.textContent = "-";
    } else {
        span.textContent = String(field.value);
    }

    return createFieldWrapper(field, span);
}

function renderDivider() {
    const hr = document.createElement("hr");
    hr.className = "doc-divider";
    return hr;
}


function renderRadio(field) {
    const selected = field.meta?.options
        ?.find(opt => opt.id === field.value);

    const span = document.createElement("span");
    span.textContent = selected?.label ?? "-";

    return createFieldWrapper(field, span);
}

function renderCheckbox(field) {
    const values = Array.isArray(field.value) ? field.value : [];
    const options = field.meta?.options ?? [];

    const labels = values
        .map(v => options.find(o => o.id === v)?.label)
        .filter(Boolean);

    const span = document.createElement("span");
    span.textContent = labels.length ? labels.join(", ") : "-";

    return createFieldWrapper(field, span);
}

function renderTable(field) {
    if (!Array.isArray(field.value) || !field.value.length) {
        return createFieldWrapper(field, document.createTextNode("-"));
    }

    const table = document.createElement("table");
    table.className = "doc-table";

    /* ===== thead ===== */
    const thead = document.createElement("thead");
    const headRow = document.createElement("tr");

    // ✅ No 컬럼
    const noTh = document.createElement("th");
    noTh.className = "col-no";
    noTh.textContent = "No";
    headRow.appendChild(noTh);

    // 기존 컬럼
    field.meta.columns.forEach(col => {
        const th = document.createElement("th");
        th.textContent = col.label;
        headRow.appendChild(th);
    });

    thead.appendChild(headRow);
    table.appendChild(thead);

    /* ===== tbody ===== */
    const tbody = document.createElement("tbody");

    field.value.forEach((row, index) => {
        const tr = document.createElement("tr");

        // ✅ No 값
        const noTd = document.createElement("td");
        noTd.className = "col-no";
        noTd.textContent = String(index + 1);
        tr.appendChild(noTd);

        // 기존 컬럼 값
        field.meta.columns.forEach(col => {
            const td = document.createElement("td");
            td.textContent = row[col.id] ?? "-";
            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);

    return createFieldWrapper(field, table);
}


function renderEventDateRange(field) {
    const v = field.value ?? {};

    const wrapper = document.createElement("div");
    wrapper.className = "sub-field-table";

    // 1️⃣ 기간 (항상 존재)
    wrapper.appendChild(createSubRow(
        "기간",
        `${v.start || "-"} ~ ${v.end || "-"}`
    ));

    // 2️⃣ 휴가 유형 OR 일정 이름
    if (v.vacationTypeId) {
        wrapper.appendChild(createSubRow(
            "휴가 유형",
            vacationTypeMap.get(String(v.vacationTypeId)) ?? "-"
        ));
    } else if (v.title) {
        wrapper.appendChild(createSubRow(
            "일정 이름",
            v.title
        ));
    } else {
        wrapper.appendChild(createSubRow(
            "일정",
            "-"
        ));
    }

    // 3️⃣ 휴가 사유 OR 일정 설명
    if (v.reason) {
        wrapper.appendChild(createSubRow(
            "휴가 사유",
            v.reason
        ));
    } else if (v.description) {
        wrapper.appendChild(createSubRow(
            "일정 설명",
            v.description
        ));
    } else {
        wrapper.appendChild(createSubRow(
            "비고",
            "-"
        ));
    }

    return createFieldWrapper(field, wrapper);
}

function createSubRow(label, value) {
    const row = document.createElement("div");
    row.className = "sub-field-row";

    const l = document.createElement("div");
    l.className = "sub-field-label";
    l.textContent = label;

    const v = document.createElement("div");
    v.className = "sub-field-value";
    v.textContent = value;

    row.append(l, v);
    return row;
}


function renderNotice(field) {
    const box = document.createElement("div");
    box.className = `notice-box ${field.meta?.style ?? "info"}`;
    box.textContent = field.meta?.message ?? "";

    return createFieldWrapper(field, box);
}


function createLeaveInnerRow(label, value) {
    const r = document.createElement("div");
    r.className = "leave-reason-inner-row";

    const l = document.createElement("div");
    l.className = "leave-reason-inner-label";
    l.textContent = label;

    const v = document.createElement("div");
    v.className = "leave-reason-inner-value";
    v.textContent = value;

    r.append(l, v);
    return r;
}


function renderRadioField(field) {
    const value = field.value;
    const options = field.meta?.options || [];

    let display = '-';

    if (value) {
        const matched = options.find(opt => opt.id === value);
        display = matched ? matched.label : '-';
    }

    return createFieldRow(field.label, display);
}

function renderCheckboxField(field) {
    const values = Array.isArray(field.value) ? field.value : [];
    const options = field.meta?.options || [];

    if (values.length === 0) {
        return createFieldRow(field.label, '-');
    }

    const labels = values
        .map(v => options.find(opt => opt.id === v))
        .filter(Boolean)
        .map(opt => opt.label);

    const display = labels.length > 0
        ? labels.join(', ')
        : '-';

    return createFieldRow(field.label, display);
}

function createFieldRow(label, value) {
    const row = document.createElement('div');
    row.className = 'doc-field-row';

    const labelEl = document.createElement('div');
    labelEl.className = 'doc-field-label';
    labelEl.textContent = label;

    const valueEl = document.createElement('div');
    valueEl.className = 'doc-field-value';
    valueEl.textContent = value ?? '';

    if ((value === null || value === undefined) || value === '-') {
        valueEl.classList.add('empty');
    }

    row.append(labelEl, valueEl);
    return row;
}


function formatObjectValue(field) {
    const v = field.value;

    switch (field.fieldType) {

        case "date-range":
        case "time-range":
        case "schedule-date-range":
        case "leave-date-range":
            return `${v.start || "-"} ~ ${v.end || "-"}`;

        case "checkbox":
            return Array.isArray(v) && v.length
                ? v.join(", ")
                : "-";

        case "table":
            return formatTableValue(field);

        default:
            return JSON.stringify(v);
    }
}

function formatTableValue(field) {
    if (!Array.isArray(field.value) || !field.value.length) {
        return "-";
    }

    return field.value
        .map((row, idx) => {
            const rowText = Object.values(row)
                .map(v => v ?? "-")
                .join(" / ");
            return `${idx + 1}. ${rowText}`;
        })
        .join("\n");
}



/* ===============================
   결재선 / 코멘트
=============================== */
function renderApprovalLines(lines) {
    const list = document.getElementById("approvalLineList");
    if (!list || !Array.isArray(lines)) return;

    list.innerHTML = "";

    lines.forEach(line => {
        const div = document.createElement("div");
        div.className = `approval-line-item ${line.status.toLowerCase()}`;

        div.innerHTML = `
            <div class="approver-name">
                ${line.orderNo}차 결재 · ${line.approverDisplay}
            </div>
            <div class="approver-status">
                ${getApprovalStatusText(line.status)}
            </div>
            ${line.comment
            ? `<div class="approver-comment">${line.comment}</div>`
            : ""}
        `;

        list.appendChild(div);
    });
}

/* ===============================
   첨부 / 참조 문서 (더미)
=============================== */
function renderAttachments() {
    const listEl = document.getElementById("attachmentList");
    if (!listEl) return;

    listEl.innerHTML = "";

    // TODO: 실제 API 연동 예정
    const attachments = [
        {name: "출장계획서.pdf", url: "#"},
        {name: "영수증.zip", url: "#"}
    ];

    attachments.forEach(file => {
        const li = document.createElement("li");
        li.innerHTML = `<a href="${file.url}">${file.name}</a>`;
        listEl.appendChild(li);
    });
}

/* ===============================
   하단 버튼 제어 (임시)
=============================== */
function controlActionButtons(data) {
    const approveBtn = document.getElementById("approveBtn");
    const rejectBtn = document.getElementById("rejectBtn");
    const aiSummaryBtn = document.getElementById("aiSummaryBtn");

    const isMyTurn = data?.myApprovalOrder === true;

    /* =========================
       승인 / 반려 버튼
    ========================= */
    if (approveBtn) {
        approveBtn.style.display = isMyTurn ? "inline-block" : "none";
    }

    if (rejectBtn) {
        rejectBtn.style.display = isMyTurn ? "inline-block" : "none";
    }

    /* =========================
       AI 요약 버튼
    ========================= */
    if (aiSummaryBtn) {
        if (isMyTurn) {
            aiSummaryBtn.disabled = false;
            aiSummaryBtn.classList.add("active");
            aiSummaryBtn.classList.remove("disabled");
        } else {
            aiSummaryBtn.disabled = true;
            aiSummaryBtn.classList.remove("active");
            aiSummaryBtn.classList.add("disabled");
        }
    }
}


/* ===============================
   API
=============================== */
async function fetchDocumentDetail(documentId) {
    const res = await apiFetch(`/api/documents/${documentId}/detail`);
    if (!res.ok) {
        throw new Error("문서 상세 조회 실패");
    }
    const json = await res.json();
    return json.data;
}

/* ===============================
   유틸
=============================== */
function getDocumentIdFromPath() {
    const parts = window.location.pathname.split("/");
    return parts[parts.length - 1];
}

function getStatusText(status) {
    switch (status) {
        case "DRAFT":
            return "작성중";
        case "SUBMITTED":
            return "상신";
        case "IN_PROGRESS":
            return "결재중";
        case "APPROVED":
            return "승인완료";
        case "REJECTED":
            return "반려";
        default:
            return status ?? "-";
    }
}

function getApprovalStatusText(status) {
    switch (status) {
        case "IN_PROGRESS":
            return "결재중";
        case "WAITING":
            return "대기중";
        case "APPROVED":
            return "승인";
        case "REJECTED":
            return "반려";
        default:
            return status ?? "-";
    }
}
