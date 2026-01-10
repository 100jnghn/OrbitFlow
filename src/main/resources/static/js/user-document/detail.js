let currentActionType = null;   // "approve" | "reject"
let currentDocumentId = null;
let currentBeforeDocumentId = null;

const MAX_COMMENT_LENGTH = 200;

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

    image: renderImage,

    "event-date-range": renderEventDateRange,
    notice: renderNotice,
    default: renderDefaultField
};


function updateApprovalSidebarSelection() {
    // 모든 no-sub 메뉴 선택 해제
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });

    // 결재 대기함 선택
    const inboxLink = document.getElementById('mySubmitLink');
    if (inboxLink) {
        const menuItem = inboxLink.closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}

function hideApprovalCommentHint() {
    const hint = document.getElementById("approvalCommentHint");
    if (!hint) return;

    hint.textContent = "";
    hint.classList.remove("error", "success");
    hint.style.display = "none";   // ⭐ 완전 숨김
}

function showApprovalCommentHint() {
    const textarea = document.getElementById("approvalComment");
    const hint = document.getElementById("approvalCommentHint");
    if (!textarea || !hint) return;

    const length = textarea.value.length;

    hint.textContent = `(${length} / ${MAX_COMMENT_LENGTH})`;
    hint.classList.remove("error", "success");
    hint.style.display = "block"; // ⭐ 다시 표시
}


function bindApprovalOutsideClickOnce() {
    const modal = document.getElementById("approvalModal");
    const textarea = document.getElementById("approvalComment");
    const modalContent = modal?.querySelector(".approval-modal");

    if (!modal || !textarea || !modalContent) return;

    document.addEventListener("mousedown", (e) => {
        // 모달 닫혀 있으면 무시
        if (modal.classList.contains("hidden")) return;

        // modal 내부 클릭이면 무시
        if (modalContent.contains(e.target)) return;

        // 진짜 바깥 클릭
        hideApprovalCommentHint();
    });
}

/* ===============================
   서명 업로드 UI 바인딩 (최종)
=============================== */
let selectedSignatureFile = null;

function bindSignatureUpload() {
    const box = document.getElementById("signaturePreviewBox");
    const input = document.getElementById("signatureInput");
    const img = document.getElementById("signaturePreviewImage");
    const placeholder = document.getElementById("signaturePlaceholder");
    const removeBtn = document.getElementById("signatureRemoveBtn");
    const hint = document.getElementById("signatureHint");

    if (!box || !input || !img || !placeholder) return;

    // 클릭 → 파일 선택
    box.addEventListener("click", () => input.click());

    // 파일 선택
    input.addEventListener("change", e => {
        const file = e.target.files[0];
        if (!file) return;

        if (!file.type.startsWith("image/")) {
            showSignatureError("이미지 파일만 업로드 가능합니다.");
            input.value = "";
            return;
        }

        selectedSignatureFile = file;

        const reader = new FileReader();
        reader.onload = ev => {
            img.src = ev.target.result;
            img.style.display = "block";
            placeholder.style.display = "none";
            removeBtn.style.display = "inline-flex";
        };
        reader.readAsDataURL(file);
    });

    // 제거
    removeBtn?.addEventListener("click", e => {
        e.stopPropagation();
        resetSignatureInput();
    });

    function resetSignatureInput() {
        selectedSignatureFile = null;
        input.value = "";
        img.src = "";
        img.style.display = "none";
        placeholder.style.display = "flex";
        removeBtn.style.display = "none";
        clearSignatureHint();
    }

    function showSignatureError(msg) {
        if (!hint) return;
        hint.textContent = msg;
        hint.classList.add("error");
        hint.style.display = "block";
    }

    function clearSignatureHint() {
        if (!hint) return;
        hint.textContent = "";
        hint.classList.remove("error", "success");
        hint.style.display = "none";
    }
}


function bindApprovalCommentInput() {
    const textarea = document.getElementById("approvalComment");
    const hint = document.getElementById("approvalCommentHint");
    if (!textarea || !hint) return;

    textarea.maxLength = MAX_COMMENT_LENGTH;

    textarea.addEventListener("focus", () => {
        showApprovalCommentHint();   // ⭐ 포커스 진입
    });

    textarea.addEventListener("input", () => {
        showApprovalCommentHint();   // 입력 시에도 갱신
    });

    textarea.addEventListener("blur", () => {
        hideApprovalCommentHint();   // ⭐ 포커스 이탈
    });
}


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

/* ===============================
   모달
=============================== */

function openApprovalModal(type) {
    currentActionType = type;

    const modal = document.getElementById("approvalModal");
    const title = document.getElementById("approvalModalTitle");
    const confirmBtn = document.getElementById("approvalConfirmBtn");
    const comment = document.getElementById("approvalComment");
    const hint = document.getElementById("approvalCommentHint");

    comment.value = "";
    confirmBtn.disabled = false;

    hint.textContent = `(0 / ${MAX_COMMENT_LENGTH})`;
    hint.classList.remove("error", "success");

    confirmBtn.classList.remove("approve", "reject");

    if (type === "approve") {
        title.textContent = "문서 승인";
        confirmBtn.textContent = "승인";
        confirmBtn.classList.add("approve");
        comment.placeholder = "의견을 입력하세요 (선택)";
    } else {
        title.textContent = "문서 반려";
        confirmBtn.textContent = "반려";
        confirmBtn.classList.add("reject");
        comment.placeholder = "반려 사유를 입력하세요 (필수)";
        comment.focus();
    }

    modal.classList.remove("hidden");

    bindApprovalCommentInput();
}


function closeApprovalModal() {
    const modal = document.getElementById("approvalModal");
    modal.classList.add("hidden");
}

/* ===============================
   서명 등록 팝업
=============================== */
function openSignatureModal() {
    const modal = document.getElementById("signatureModal");
    modal && modal.classList.remove("hidden");
}

function closeSignatureModal() {
    const modal = document.getElementById("signatureModal");
    modal?.classList.add("hidden");

    selectedSignatureFile = null;

    const img = document.getElementById("signaturePreviewImage");
    const placeholder = document.getElementById("signaturePlaceholder");
    const removeBtn = document.getElementById("signatureRemoveBtn");
    const input = document.getElementById("signatureInput");
    const hint = document.getElementById("signatureHint");

    if (img) {
        img.src = "";
        img.style.display = "none";
    }
    if (placeholder) placeholder.style.display = "flex";
    if (removeBtn) removeBtn.style.display = "none";
    if (input) input.value = "";
    if (hint) {
        hint.textContent = "";
        hint.classList.remove("error", "success");
        hint.style.display = "none";
    }
}


/* ===============================
   결재 서명 조회
=============================== */
async function fetchMySignatureExists() {
    const res = await apiFetch("/api/employee-signature", {
        method: "GET"
    });

    if (!res.ok) {
        throw new Error("서명 조회 실패");
    }

    const json = await res.json();
    return json.data === true;
}

async function submitSignature() {
    const hint = document.getElementById("signatureHint");

    if (!selectedSignatureFile) {
        if (hint) {
            hint.textContent = "서명 이미지를 선택해주세요.";
            hint.classList.add("error");
            hint.style.display = "block";
        }
        return;
    }

    const formData = new FormData();
    formData.append("file", selectedSignatureFile);

    try {
        const res = await apiFetch("/api/employee-signature", {
            method: "POST",
            body: formData
        });

        if (!res.ok) throw new Error();

        closeSignatureModal();
        openApprovalModal("approve");

    } catch (e) {
        console.error(e);
        if (hint) {
            hint.textContent = "서명 등록 중 오류가 발생했습니다.";
            hint.classList.add("error");
            hint.style.display = "block";
        }
    }
}


async function submitApprovalAction() {
    if (!currentDocumentId || !currentActionType) return;

    const comment = document
        .getElementById("approvalComment")
        .value
        .trim();

    const hint = document.getElementById("approvalCommentHint");

    if (currentActionType === "reject" && !comment) {
        const hint = document.getElementById("approvalCommentHint");
        hint.textContent = "반려 사유는 필수 입력입니다.";
        hint.classList.add("error");
        hint.style.display = "block";
        document.getElementById("approvalComment").focus();
        return;
    }

    const url =
        currentActionType === "approve"
            ? `/api/documents/${currentDocumentId}/approve`
            : `/api/documents/${currentDocumentId}/reject`;

    try {
        const res = await apiFetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({comment})
        });

        if (!res.ok) {
            throw new Error("결재 처리 실패");
        }

        await sweetSuccess(currentActionType === "approve"
            ? "승인되었습니다."
            : "반려되었습니다.");

        closeApprovalModal();
        location.reload(); // 상태 갱신

    } catch (e) {
        console.error(e);
        await sweetWarning("처리 중 오류가 발생했습니다.");
    }
}


/* =========================================================
   detail.js
   - 문서 상세 조회
   - 문서 내용 readonly 렌더
   - 결재선 표시
========================================================= */

document.addEventListener("DOMContentLoaded", async () => {
    await fetchVacationTypes();

    document
        .getElementById("approvalCancelBtn")
        ?.addEventListener("click", closeApprovalModal);

    document
        .getElementById("approvalConfirmBtn")
        ?.addEventListener("click", submitApprovalAction);

    document
        .getElementById("signatureCancelBtn")
        ?.addEventListener("click", closeSignatureModal);

    document
        .getElementById("signatureSaveBtn")
        ?.addEventListener("click", submitSignature);

    bindApprovalOutsideClickOnce();

    bindSignatureUpload();

    const documentId = getDocumentIdFromPath();
    if (!documentId) return;

    try {
        const data = await fetchDocumentDetail(documentId);
        await initDocumentDetailPage(data);
    } catch (e) {
        console.error(e);
        await sweetWarning("문서 정보를 불러오지 못했습니다.");
    }
});


/* ===============================
   초기화
=============================== */
async function initDocumentDetailPage(data) {
    currentDocumentId = data.documentId;

    renderDocumentHeader(data);
    renderDocumentContent(data.contentSchema);
    renderApprovalLines(data.approvalLines);

    loadAndRenderAttachments(data.documentId);

    updateApprovalSidebarSelection();
    controlActionButtons(data);

    await setupRevisionButtons(data);

    bindBackButton();
    addPdfDownloadButton(data);

    initAiPanels(data.documentId);
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


// ✅ 그대로 둔다 (수정 X)
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
            v.displayText ||      // 다른 타입 대비
            v.departmentName ||   // 혹시 있을 경우
            v.name ||
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


function renderImage(field) {
    const images = Array.isArray(field.value) ? field.value : [];

    if (!images.length) {
        const span = document.createElement("span");
        span.textContent = "-";
        span.classList.add("empty");
        return createFieldWrapper(field, span);
    }

    const container = document.createElement("div");
    container.className = "image-field readonly";

    images.forEach(img => {
        const row = document.createElement("div");
        row.className = "image-row readonly";

        // ✅ 스피너
        const spinner = document.createElement("div");
        spinner.className = "image-spinner";
        row.appendChild(spinner);

        const imageEl = document.createElement("img");
        imageEl.alt = "첨부 이미지";
        imageEl.style.display = "none"; // ⭐ 처음엔 숨김

        loadProtectedImageForDetail(img.fileId)
            .then(url => {
                imageEl.src = url;

                imageEl.onload = () => {
                    spinner.remove();              // ⭐ 스피너 제거
                    imageEl.style.display = "block";
                    URL.revokeObjectURL(url);
                };
            })
            .catch(() => {
                spinner.textContent = "이미지 로드 실패";
            });

        // 클릭 시 원본
        imageEl.addEventListener("click", async () => {
            const url = await loadProtectedImageForDetail(img.fileId);
            window.open(url, "_blank");
        });

        row.appendChild(imageEl);
        container.appendChild(row);
    });

    return createFieldWrapper(field, container);
}


async function loadProtectedImageForDetail(fileId) {
    const res = await apiFetch(
        `/api/document-file/${currentDocumentId}/images/${fileId}`,
        {method: "GET"}
    );

    if (!res.ok) {
        throw new Error("IMAGE_LOAD_FAILED");
    }

    const blob = await res.blob();
    return URL.createObjectURL(blob);
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
   첨부 / 참조 문서
=============================== */
async function loadAndRenderAttachments(documentId) {
    try {
        const files = await fetchDocumentFiles(documentId);

        // ✅ 첨부파일 패널만 렌더
        renderAttachments(files);

    } catch (e) {
        console.error("[ATTACHMENT] load failed", e);
    }
}


function renderAttachments(files = []) {
    const listEl = document.getElementById("attachmentList");
    if (!listEl) return;

    listEl.innerHTML = "";

    // fieldId가 null인 것만 (일반 첨부 + 참조 문서)
    const items = files.filter(f => f.fieldId == null);

    if (!items.length) {
        const li = document.createElement("li");
        li.className = "attachment-empty";
        li.textContent = "첨부된 파일이 없습니다.";
        listEl.appendChild(li);
        return;
    }

    items.forEach(file => {
        const li = document.createElement("li");
        li.className = "attachment-item";

        const isReference = file.referenceTargetId != null;

        // === 배지 ===
        const typeSpan = document.createElement("span");
        typeSpan.className = `attachment-type ${isReference ? "reference" : "attachment"}`;
        typeSpan.textContent = isReference ? "참조" : "첨부";

        // === 이름 ===
        const nameSpan = document.createElement("span");
        nameSpan.className = "attachment-name";

        if (isReference) {
            // 참조: 작성자 | 문서명
            nameSpan.textContent = `${file.writerName} | ${file.displayName}`;
        } else {
            // 첨부: 파일명(displayName)
            nameSpan.textContent = file.displayName;
        }

        // === 파일 크기 (첨부만 표시해도 되고, 공통 표시도 가능) ===
        const sizeSpan = document.createElement("span");
        sizeSpan.className = "attachment-size";
        sizeSpan.textContent = formatFileSize(file.fileSize);

        // === 다운로드 ===
        li.addEventListener("click", (e) => {
            e.stopPropagation();

            downloadAttachmentByFileId(
                file.fileId,
                file.displayName,
                e
            );
        });

        li.append(typeSpan, nameSpan, sizeSpan);
        listEl.appendChild(li);
    });
}


function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function formatFileSize(size) {
    if (size == null) return "-";
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}


/* ===============================
   하단 버튼 제어
=============================== */
function controlActionButtons(data) {
    const approveBtn = document.getElementById("approveBtn");
    const rejectBtn = document.getElementById("rejectBtn");

    currentDocumentId = data?.documentId;

    const canApproveOrReject =
        data?.myApprovalOrder === true &&
        data?.status !== "REJECTED";

    if (approveBtn) {
        approveBtn.style.display = canApproveOrReject ? "inline-block" : "none";
        approveBtn.onclick = async () => {
            try {
                const hasSignature = await fetchMySignatureExists();

                if (hasSignature) {
                    openApprovalModal("approve");
                } else {
                    openSignatureModal();
                }
            } catch (e) {
                console.error(e);
                await sweetWarning("서명 정보를 확인할 수 없습니다.");
            }
        };
    }

    if (rejectBtn) {
        rejectBtn.style.display = canApproveOrReject ? "inline-block" : "none";
        rejectBtn.onclick = () => openApprovalModal("reject");
    }
}


function addPdfDownloadButton(data) {
    if (!data.pdfFileId) return;

    const actionLeft = document.querySelector(".action-left");
    if (!actionLeft) return;

    const pdfBtn = document.createElement("button");
    pdfBtn.className = "action-btn secondary";
    pdfBtn.textContent = "PDF 다운로드";

    pdfBtn.onclick = async () => {
        if (pdfBtn.disabled) return;

        const MIN_DISPLAY_TIME = 800;
        const startTime = Date.now();

        try {
            pdfBtn.disabled = true;
            pdfBtn.textContent = "PDF 다운로드 중...";

            const url = await fetchPresignedDownloadUrlByFileId(
                data.pdfFileId
            );

            const a = document.createElement("a");
            a.href = url;
            a.download = "";
            document.body.appendChild(a);
            a.click();
            a.remove();

            // ⭐ 너무 빨리 끝나면 기다렸다가 복구
            const elapsed = Date.now() - startTime;
            if (elapsed < MIN_DISPLAY_TIME) {
                await delay(MIN_DISPLAY_TIME - elapsed);
            }

        } catch (e) {
            console.error(e);
            await sweetWarning("PDF 다운로드 중 오류가 발생했습니다.");
        } finally {
            pdfBtn.disabled = false;
            pdfBtn.textContent = "PDF 다운로드";
        }
    };

    actionLeft.appendChild(pdfBtn);
}


/* ===============================
   API
=============================== */
async function fetchRevisionInfo(documentId) {

    const res = await apiFetch(`/api/documents/${documentId}/revision`);
    if (!res.ok) {
        console.warn("[REVISION API] response not ok");
        return null;
    }

    const json = await res.json();

    return json.data;
}

async function fetchDocumentFiles(documentId) {

    const res = await apiFetch(`/api/document-file/${documentId}/files`);

    if (!res.ok) {
        console.error("response not ok");
        console.groupEnd();
        throw new Error("첨부 파일 목록 조회 실패");
    }

    const json = await res.json();

    console.groupEnd();

    return json.data ?? [];
}


async function fetchPresignedDownloadUrlByFileId(fileId) {
    const res = await apiFetch(`/api/files/${fileId}/presigned`);

    if (!res.ok) {
        throw new Error("다운로드 URL 생성 실패");
    }

    const json = await res.json();
    return json.data?.url;
}


let downloadingFileId = null;

async function downloadAttachmentByFileId(fileId, fileName, event) {
    const itemEl = event.currentTarget;
    if (!itemEl) return;

    if (downloadingFileId === fileId) return;
    downloadingFileId = fileId;

    const spinner = document.createElement("span");
    spinner.className = "attachment-spinner";

    const nameEl = itemEl.querySelector(".attachment-name");
    if (!nameEl) return;

    itemEl.insertBefore(spinner, nameEl);

    const startTime = Date.now();

    try {
        const url = await fetchPresignedDownloadUrlByFileId(fileId);

        const a = document.createElement("a");
        a.href = url;
        a.download = fileName ?? "";
        document.body.appendChild(a);
        a.click();
        a.remove();

        const elapsed = Date.now() - startTime;
        if (elapsed < 600) {
            await new Promise(r => setTimeout(r, 600 - elapsed));
        }

        spinner.remove();

        const check = document.createElement("span");
        check.className = "attachment-check";
        check.textContent = "✓";
        itemEl.insertBefore(check, nameEl);

        await new Promise(r => setTimeout(r, 2000));
        check.remove();

    } catch (e) {
        console.error(e);
        await sweetWarning("파일 다운로드 중 오류가 발생했습니다.");
        spinner.remove();
    } finally {
        downloadingFileId = null;
    }
}


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

async function setupRevisionButtons(docData) {
    const reviseBtn = document.getElementById("reviseBtn");
    const goRevisionBtn = document.getElementById("goRevisionBtn");
    const goOriginalBtn = document.getElementById("goOriginalBtn");

    reviseBtn && (reviseBtn.style.display = "none");
    goRevisionBtn && (goRevisionBtn.style.display = "none");
    goOriginalBtn && (goOriginalBtn.style.display = "none");

    const revisionInfo = await fetchRevisionInfo(docData.documentId);
    if (!revisionInfo) return;

    const {
        beforeDocumentId,
        nextDocumentId,
        nextDocumentStatus,
        isMine
    } = revisionInfo;

    // ✅⭐️ 여기!
    currentBeforeDocumentId = beforeDocumentId ?? null;

    if (!isMine) return;

    /* ===============================
       1️⃣ 원본 문서로 이동
    =============================== */
    if (goOriginalBtn && beforeDocumentId != null) {
        goOriginalBtn.style.display = "inline-flex";
        goOriginalBtn.onclick = () => {
            location.href = `/view/document/${beforeDocumentId}`;
        };
    }

    /* ===============================
       2️⃣ 재기안 관련 버튼
    =============================== */
    if (docData.status !== "REJECTED") return;

    if (nextDocumentId == null) {
        if (!reviseBtn) return;

        reviseBtn.style.display = "inline-flex";
        reviseBtn.onclick = async () => {
            if (!confirm("반려 문서를 재기안하시겠습니까?")) return;

            const res = await apiFetch(
                `/api/documents/${docData.documentId}/revise`,
                {method: "POST"}
            );

            const json = await res.json();
            location.href = `/view/document/write/${json.data.documentId}`;
        };
        return;
    }

    if (!goRevisionBtn) return;

    goRevisionBtn.style.display = "inline-flex";
    goRevisionBtn.onclick = () => {
        location.href =
            nextDocumentStatus === "DRAFT"
                ? `/view/document/write/${nextDocumentId}`
                : `/view/document/${nextDocumentId}`;
    };
}


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

/* ===============================
   AI 패널 공통
=============================== */

const AI_STATUS = {
    PROCESSING: "PROCESSING",
    COMPLETED: "COMPLETED"
};

const AI_TYPE = {
    CONTENT: "CONTENT",
    DIFF: "DIFF"
};

const AI_POLL_INTERVAL = 3000;

// 폴링 중복 방지용
const aiPollTimers = {
    [AI_TYPE.CONTENT]: null,
    [AI_TYPE.DIFF]: null
};

function show(el) {
    el && el.classList.remove("hidden");
}

function hide(el) {
    el && el.classList.add("hidden");
}

function stopPolling(type) {
    const t = aiPollTimers[type];
    if (t) clearInterval(t);
    aiPollTimers[type] = null;
}

async function fetchAi(documentId, type) {
    const res = await apiFetch(`/api/document-ai/${documentId}?summaryType=${type}`);
    if (!res.ok) throw new Error("AI 조회 실패");
    return await res.json(); // {status, message, data}
}

async function createAi(documentId, type) {
    const url =
        type === AI_TYPE.CONTENT
            ? `/api/document-ai/${documentId}/summary`
            : `/api/document-ai/${documentId}/diff`;

    const res = await apiFetch(url, {method: "POST"});
    if (!res.ok) throw new Error("AI 생성 요청 실패");
}

/**
 * panelEls = { empty, emptyText, loading, loadingText, result, createBtn }
 * mode:
 *  - "NONE": data=null (생성 전) → placeholder + 버튼
 *  - "NO_TARGET": 비교대상 없음 → placeholder만, 버튼 숨김
 *  - "PROCESSING": spinner + 버튼 숨김(비활성) + placeholder 문구 변경은 loadingText로
 *  - "COMPLETED": 결과만
 */
function applyAiPanelState(panelEls, mode, opts = {}) {
    const {emptyMsg, resultText, canCreate} = opts;

    // 초기화
    hide(panelEls.empty);
    hide(panelEls.result);

    if (panelEls.result) {
        panelEls.result.classList.remove("loading");
    }

    if (panelEls.createBtn) {
        panelEls.createBtn.disabled = true;
        hide(panelEls.createBtn);
    }

    // 문구 반영
    if (emptyMsg != null && panelEls.emptyText) {
        panelEls.emptyText.textContent = emptyMsg;
    }
    if (resultText != null && panelEls.result) {
        panelEls.result.textContent = resultText;
    }

    switch (mode) {
        case "NONE": {
            show(panelEls.empty);
            if (panelEls.createBtn && canCreate !== false) {
                show(panelEls.createBtn);
                panelEls.createBtn.disabled = false;
            }
            break;
        }

        case "NO_TARGET": {
            show(panelEls.empty);
            break;
        }

        case "PROCESSING": {
            show(panelEls.result);
            panelEls.result.classList.add("loading");
            panelEls.result.textContent = "";
            break;
        }

        case "COMPLETED": {
            show(panelEls.result);
            panelEls.result.classList.remove("loading");
            break;
        }
    }
}

function startPolling(documentId, type, panelEls) {
    stopPolling(type);

    aiPollTimers[type] = setInterval(async () => {
        try {
            const {data} = await fetchAi(documentId, type);

            // data=null이면 아직 생성 안된 상태로 되돌아간 것 → 폴링 중단 + NONE 처리
            if (!data) {
                stopPolling(type);
                applyAiPanelState(panelEls, "NONE", {
                    emptyMsg: "아직 생성되지 않았습니다.",
                    canCreate: true
                });
                return;
            }

            // 처리 중이면 계속
            if (data.aiStatus === AI_STATUS.PROCESSING) return;

            // 완료면 중단 + 완료 표시
            if (data.aiStatus === AI_STATUS.COMPLETED) {
                stopPolling(type);
                applyAiPanelState(panelEls, "COMPLETED", {
                    resultText: data.context
                });
            }
        } catch (e) {
            console.error(`[AI POLL ${type}]`, e);
            stopPolling(type);
        }
    }, AI_POLL_INTERVAL);
}

/* ===============================
   AI 요약 패널
=============================== */

function getSummaryPanelEls() {
    return {
        empty: document.getElementById("aiSummaryEmpty"),
        emptyText: document.getElementById("aiSummaryEmptyText"),
        result: document.getElementById("aiSummaryResult"),
        createBtn: document.getElementById("aiSummaryCreateBtn")
    };
}


async function initAiSummaryPanel(documentId) {
    const els = getSummaryPanelEls();
    if (!els.empty || !els.result || !els.createBtn) return;

    // ✅ init 순간에 절대 스피너가 돌면 안됨 → 일단 전부 숨김 후 서버 상태로 결정
    applyAiPanelState(els, "NONE", {emptyMsg: "AI 요약 상태를 불러오는 중...", canCreate: false});

    try {
        const {data} = await fetchAi(documentId, AI_TYPE.CONTENT);

        // 1) data=null → 생성 전(버튼 활성 + placeholder)
        if (!data) {
            applyAiPanelState(els, "NONE", {
                emptyMsg: "아직 생성되지 않았습니다.",
                canCreate: true
            });

            els.createBtn.onclick = async () => {
                // 버튼 누른 순간: placeholder 제거 + 스피너 + 폴링 시작
                applyAiPanelState(els, "PROCESSING", {loadingMsg: "AI 요약을 생성 중입니다..."});
                await createAi(documentId, AI_TYPE.CONTENT);
                startPolling(documentId, AI_TYPE.CONTENT, els);
            };
            return;
        }

        // 2) PROCESSING → 스피너 + 폴링
        if (data.aiStatus === AI_STATUS.PROCESSING) {
            applyAiPanelState(els, "PROCESSING", {loadingMsg: "AI 요약을 생성 중입니다..."});
            startPolling(documentId, AI_TYPE.CONTENT, els);
            return;
        }

        // 3) COMPLETED → 결과
        if (data.aiStatus === AI_STATUS.COMPLETED) {
            applyAiPanelState(els, "COMPLETED", {resultText: data.context});
        }
    } catch (e) {
        console.error("[AI SUMMARY INIT]", e);
        applyAiPanelState(els, "NONE", {emptyMsg: "AI 요약 정보를 불러오지 못했습니다.", canCreate: false});
    }
}

/* ===============================
   AI 비교 패널
=============================== */

function getDiffPanelEls() {
    return {
        empty: document.getElementById("aiDiffEmpty"),
        emptyText: document.getElementById("aiDiffEmptyText"),
        result: document.getElementById("aiDiffResult"),
        createBtn: document.getElementById("aiDiffCreateBtn")
    };
}


async function initAiDiffPanel(documentId, beforeDocumentId) {
    const els = getDiffPanelEls();
    if (!els.empty || !els.result || !els.createBtn) return;

    // ✅ 비교 대상 없으면: 스피너 절대 X, 버튼 숨김
    if (!beforeDocumentId) {
        stopPolling(AI_TYPE.DIFF);
        applyAiPanelState(els, "NO_TARGET", {emptyMsg: "비교 대상이 없습니다."});
        return;
    }

    // init 순간 노출 방지
    applyAiPanelState(els, "NONE", {emptyMsg: "AI 비교 상태를 불러오는 중...", canCreate: false});

    try {
        const {data} = await fetchAi(documentId, AI_TYPE.DIFF);

        // 1) data=null → 생성 전(버튼 활성)
        if (!data) {
            applyAiPanelState(els, "NONE", {
                emptyMsg: "아직 생성되지 않았습니다.",
                canCreate: true
            });

            els.createBtn.onclick = async () => {
                applyAiPanelState(els, "PROCESSING", {loadingMsg: "AI 비교를 생성 중입니다..."});
                await createAi(documentId, AI_TYPE.DIFF);
                startPolling(documentId, AI_TYPE.DIFF, els);
            };
            return;
        }

        // 2) PROCESSING → 스피너 + 폴링
        if (data.aiStatus === AI_STATUS.PROCESSING) {
            applyAiPanelState(els, "PROCESSING", {loadingMsg: "AI 비교를 생성 중입니다..."});
            startPolling(documentId, AI_TYPE.DIFF, els);
            return;
        }

        // 3) COMPLETED → 결과
        if (data.aiStatus === AI_STATUS.COMPLETED) {
            applyAiPanelState(els, "COMPLETED", {resultText: data.context});
        }
    } catch (e) {
        console.error("[AI DIFF INIT]", e);
        applyAiPanelState(els, "NONE", {emptyMsg: "AI 비교 정보를 불러오지 못했습니다.", canCreate: false});
    }
}

/* ===============================
   진입점
=============================== */

function initAiPanels(documentId) {
    initAiSummaryPanel(documentId);
    initAiDiffPanel(documentId, currentBeforeDocumentId);
}
