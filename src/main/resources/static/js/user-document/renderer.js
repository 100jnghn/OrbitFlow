/* =========================================================
 * Document Renderer (Common)
 * ========================================================= */

/* ---------- entry ---------- */

export function renderDocumentContent(schema, container) {
    if (!container || !schema?.fields) return;

    container.innerHTML = "";

    // 문서 제목
    const titleField = schema.fields.find(
        f => f.fieldType === "document-title"
    );

    if (titleField?.value) {
        const titleEl = document.createElement("h2");
        titleEl.className = "doc-content-title";
        titleEl.textContent = titleField.value;
        container.appendChild(titleEl);
    }

    const fields = schema.fields
        .filter(f => f.fieldType !== "document-title")
        .sort((a, b) => a.order - b.order);

    fields.forEach(field => {
        const renderer =
            fieldRenderers[field.fieldType] ?? renderDefaultField;

        const el = renderer(field);
        if (el) container.appendChild(el);
    });
}

/* ---------- field wrapper ---------- */

function createFieldWrapper(field, valueEl) {
    const row = document.createElement("div");
    row.className = `doc-field field-${field.fieldType}`;

    const label = document.createElement("div");
    label.className = "doc-field-label";
    label.textContent = field.label ?? "";

    const value = document.createElement("div");
    value.className = "doc-field-value";

    if (valueEl == null || valueEl === "-") {
        value.textContent = "-";
        value.classList.add("empty");
    } else {
        value.appendChild(valueEl);
    }

    row.append(label, value);
    return row;
}

/* =========================================================
 * Renderers
 * ========================================================= */

function renderSimpleRange(field) {
    const v = field.value ?? {};
    const span = document.createElement("span");
    span.textContent = `${v.start || "-"} ~ ${v.end || "-"}`;
    return createFieldWrapper(field, span);
}

function renderEventDateRange(field) {
    const v = field.value ?? {};
    const wrapper = document.createElement("div");
    wrapper.className = "sub-field-table";

    wrapper.appendChild(createSubRow(
        "기간",
        `${v.start || "-"} ~ ${v.end || "-"}`
    ));

    if (v.vacationTypeName) {
        wrapper.appendChild(createSubRow("휴가 유형", v.vacationTypeName));
    } else if (v.title) {
        wrapper.appendChild(createSubRow("일정 이름", v.title));
    } else {
        wrapper.appendChild(createSubRow("일정", "-"));
    }

    if (v.reason) {
        wrapper.appendChild(createMultilineSubRow("휴가 사유", v.reason));
    } else if (v.description) {
        wrapper.appendChild(createMultilineSubRow("일정 설명", v.description));
    } else {
        wrapper.appendChild(createSubRow("비고", "-"));
    }

    return createFieldWrapper(field, wrapper);
}

function renderTable(field) {
    if (!Array.isArray(field.value) || !field.value.length) {
        return createFieldWrapper(field, "-");
    }

    const table = document.createElement("table");
    table.className = "doc-table";

    const thead = document.createElement("thead");
    const tr = document.createElement("tr");

    const noTh = document.createElement("th");
    noTh.className = "col-no";
    noTh.textContent = "No";
    tr.appendChild(noTh);

    field.meta.columns.forEach(col => {
        const th = document.createElement("th");
        th.textContent = col.label;
        tr.appendChild(th);
    });

    thead.appendChild(tr);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    field.value.forEach((row, idx) => {
        const tr = document.createElement("tr");

        const noTd = document.createElement("td");
        noTd.className = "col-no";
        noTd.textContent = String(idx + 1);
        tr.appendChild(noTd);

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
        return createFieldWrapper(field, "-");
    }

    const container = document.createElement("div");
    container.className = "image-field readonly";

    images.forEach(img => {
        const row = document.createElement("div");
        row.className = "image-row readonly";

        const imageEl = document.createElement("img");
        imageEl.alt = "첨부 이미지";

        // preview / pdf → url
        if (img.url) {
            imageEl.src = img.url;
        }
        // detail → protected loader
        else if (img.fileId && typeof loadProtectedImageForDetail === "function") {
            loadProtectedImageForDetail(img.fileId)
                .then(url => imageEl.src = url);
        }

        row.appendChild(imageEl);
        container.appendChild(row);
    });

    return createFieldWrapper(field, container);
}

function renderNotice(field) {
    const box = document.createElement("div");
    box.className = `notice-box ${field.meta?.style ?? "info"}`;
    box.textContent = field.meta?.message ?? "";
    return createFieldWrapper(field, box);
}

function renderDefaultField(field) {
    const span = document.createElement("span");
    span.textContent =
        field.value == null || field.value === "" ? "-" : String(field.value);
    return createFieldWrapper(field, span);
}

/* =========================================================
 * Sub Row Utils
 * ========================================================= */

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

function createMultilineSubRow(label, value) {
    const row = createSubRow(label, value);
    row.querySelector(".sub-field-value").style.whiteSpace = "pre-wrap";
    return row;
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

function renderCurrency(field) {
    const v = field.value;
    const span = document.createElement("span");

    if (v == null || v === "" || isNaN(Number(v))) {
        span.textContent = "-";
    } else {
        const formatted = Number(v).toLocaleString("ko-KR");
        const unit = field.meta?.unit ?? "";
        span.textContent = unit ? `${formatted} ${unit}` : formatted;
    }

    return createFieldWrapper(field, span);
}

function renderDivider() {
    const hr = document.createElement("hr");
    hr.className = "doc-divider";
    return hr;
}

function renderEmployee(field) {
    const v = field.value;
    const span = document.createElement("span");

    if (!v) {
        span.textContent = "-";
        return createFieldWrapper(field, span);
    }

    const dept = v.departmentName?.trim();

    const personParts = [];
    if (v.positionName) personParts.push(v.positionName);
    if (v.name) personParts.push(v.name);

    const empNo = v.employeeNo ? `(${v.employeeNo})` : "";

    let text = "-";
    if (dept || personParts.length || empNo) {
        const personText = personParts.join(" ");
        text = [dept, personText + (empNo ? ` ${empNo}` : "")]
            .filter(Boolean)
            .join(" / ");
    }

    span.textContent = text;
    return createFieldWrapper(field, span);
}

function renderDepartment(field) {
    const v = field.value;
    const span = document.createElement("span");

    span.textContent =
        v?.displayText ||
        v?.departmentName ||
        v?.name ||
        "-";

    return createFieldWrapper(field, span);
}

function renderAddress(field) {
    const v = field.value ?? {};

    const wrapper = document.createElement("div");
    wrapper.className = "sub-field-table";

    wrapper.appendChild(createSubRow("우편번호", v.postcode || "-"));
    wrapper.appendChild(createSubRow("도로명 주소", v.roadAddress || "-"));
    wrapper.appendChild(createSubRow("상세 주소", v.detailAddress || "-"));

    return createFieldWrapper(field, wrapper);
}


/* =========================================================
 * Renderer Map
 * ========================================================= */

const fieldRenderers = {
    "date-range": renderSimpleRange,
    "time-range": renderSimpleRange,
    "event-date-range": renderEventDateRange,

    "employee-search": renderEmployee,
    "department-search": renderDepartment,
    address: renderAddress,

    radio: renderRadio,
    checkbox: renderCheckbox,
    currency: renderCurrency,
    table: renderTable,
    image: renderImage,
    notice: renderNotice,
    divider: renderDivider
};
