/* =========================================================
   document-detail.js
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
    controlActionButtons();
}

const vacationTypeMap = new Map();

async function fetchVacationTypes() {
    const res = await apiFetch('/api/leave-types/all');
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
    const titleEl = document.getElementById("documentTitle");
    const badgeEl = document.getElementById("documentStatusBadge");

    if (titleEl) titleEl.textContent = data.title ?? "-";
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

    const fields = schema.fields
        .slice()
        .sort((a, b) => a.order - b.order);

    for (let i = 0; i < fields.length; i++) {
        const field = fields[i];

        if (field.fieldType === "leave-reason") {
            const row = renderLeaveReasonRow(field);
            if (row) container.appendChild(row);
            continue;
        }

        const el = renderReadonlyField(field);
        if (el) container.appendChild(el);
    }

}

function renderLeaveReasonRow(field) {
    const value = field.value || {};

    const typeLabel =
        vacationTypeMap.get(String(value.vacationTypeCode)) ?? "-";
    const detail = value.detailReason?.trim() || "-";

    // 바깥 row는 1개만 생성
    const row = createFieldRow(field.label ?? "휴가 사유", "");
    row.classList.add("leave-reason-row"); // 단일 표용 클래스

    const valueEl = row.querySelector(".doc-field-value");
    valueEl.innerHTML = ""; // createFieldRow의 기본 텍스트 제거(안전)

    // 내부 표 컨테이너
    const inner = document.createElement("div");
    inner.className = "leave-reason-inner";

    // 내부 1행: 휴가 유형
    inner.appendChild(createLeaveInnerRow("휴가 유형", typeLabel));

    // 내부 2행: 상세 사유
    inner.appendChild(createLeaveInnerRow("상세 사유", detail));

    valueEl.appendChild(inner);

    return row;
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


/* ===============================
   readonly 필드 렌더러 (임시)
=============================== */
function renderReadonlyField(field) {

    /* ===== 구분선 (hr) ===== */
    if (field.fieldType === "divider") {
        const hr = document.createElement("hr");
        hr.className = "doc-divider";
        return hr;
    }

    /* ===== radio ===== */
    if (field.fieldType === "radio") {
        return renderRadioField(field);
    }

    /* ===== checkbox ===== */
    if (field.fieldType === "checkbox") {
        return renderCheckboxField(field);
    }

    /* ===== 기본 필드 ===== */
    const row = document.createElement("div");
    row.className = "doc-field-row";

    /* 라벨 */
    const label = document.createElement("div");
    label.className = "doc-field-label";
    label.textContent = field.label ?? "";

    /* 값 */
    const value = document.createElement("div");
    value.className = "doc-field-value";


    if (
        field.value === null ||
        field.value === "" ||
        (Array.isArray(field.value) && field.value.length === 0)
    ) {
        value.textContent = "-";
        value.classList.add("empty");
    } else if (typeof field.value === "object") {
        const formatted = formatObjectValue(field);

        if (formatted && typeof formatted === "object" && formatted.__html) {
            value.innerHTML = formatted.__html;
        } else {
            value.textContent = formatted;
        }
    } else {
        value.textContent = field.value;
    }

    row.appendChild(label);
    row.appendChild(value);

    return row;
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
        div.className = "approval-line-item";

        div.innerHTML = `
            <div class="approver-name">
                ${line.orderNo}차 결재 · ${line.approverDisplay}
            </div>
            <div class="approver-status ${line.status.toLowerCase()}">
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
function controlActionButtons() {
    const approveBtn = document.getElementById("approveBtn");
    const rejectBtn = document.getElementById("rejectBtn");

    // 🔹 화면 구성 확인용: 항상 표시
    if (approveBtn) approveBtn.style.display = "inline-block";
    if (rejectBtn) rejectBtn.style.display = "inline-block";
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
