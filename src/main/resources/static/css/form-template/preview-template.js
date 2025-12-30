/* =====================================================
 * Constants
 * ===================================================== */

const PAGE_HEIGHT = 1123;
const PAGE_PADDING = 40;
const PAGE_CONTENT_HEIGHT = PAGE_HEIGHT - PAGE_PADDING * 2;
const OVERFLOW_TOLERANCE = 2;

/* =====================================================
 * API
 * ===================================================== */

async function fetchPreviewData() {
    const res = await apiFetch(`/api/admin/form-templates/${templateId}/preview`);
    if (!res.ok) throw new Error("미리보기 데이터 조회 실패");
    return (await res.json()).data;
}

/* =====================================================
 * Page Utils
 * ===================================================== */

function createPage() {
    const page = document.createElement("div");
    page.className = "page";

    const content = document.createElement("div");
    content.className = "page-content";

    page.appendChild(content);
    return page;
}

function createMeasurePage() {
    const page = createPage();
    page.classList.add("measure");
    document.body.appendChild(page);
    return page;
}

function getContentHeight() {
    return PAGE_CONTENT_HEIGHT;
}

function canAppend(page, block) {
    const measurePage = createMeasurePage();
    const measureContent = measurePage.querySelector(".page-content");
    const realContent = page.querySelector(".page-content");

    measureContent.innerHTML = realContent.innerHTML;
    measureContent.appendChild(block.cloneNode(true));

    const overflow =
        measureContent.scrollHeight >
        getContentHeight() + OVERFLOW_TOLERANCE;

    document.body.removeChild(measurePage);
    return !overflow;
}

function flowAppend(block, page, root) {
    if (canAppend(page, block)) {
        page.querySelector(".page-content").appendChild(block);
        return page;
    }

    const newPage = createPage();
    root.appendChild(newPage);
    newPage.querySelector(".page-content").appendChild(block);
    return newPage;
}

/* =====================================================
 * Entry
 * ===================================================== */

document.addEventListener("DOMContentLoaded", async () => {
    const {schema, previewData} = await fetchPreviewData();
    const root = document.getElementById("document-root");

    let currentPage = createPage();
    root.appendChild(currentPage);

    for (const field of schema.fields) {
        currentPage = renderFieldFlow(
            field,
            previewData[field.fieldId],
            currentPage,
            root
        );
    }
});

/* =====================================================
 * Field Dispatcher
 * ===================================================== */

function renderFieldFlow(field, value, page, root) {
    switch (field.fieldType) {
        case "divider":
            return flowAppend(renderDivider(field), page, root);

        case "notice":
            return flowAppend(renderNotice(field, value), page, root);

        case "image":
            return flowAppend(renderImage(field, value), page, root);

        case "table":
            return renderTableFlow(field, value || [], page, root);

        case "radio":
        case "checkbox":
            return renderOptionGroupFlow(field, value, page, root);

        default:
            return flowAppend(renderSimpleField(field, value), page, root);
    }
}

/* =====================================================
 * Renderers
 * ===================================================== */

function renderSimpleField(field, value) {
    const wrap = document.createElement("div");
    wrap.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label || field.fieldType;

    const val = document.createElement("div");
    val.className = "field-value";
    val.innerText = formatValue(value);

    wrap.append(label, val);
    return wrap;
}

/**
 * Divider – 관리자 미리보기에서는 label 표시
 */
function renderDivider(field) {
    const wrap = document.createElement("div");
    wrap.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label || "구분선";

    const line = document.createElement("div");
    line.className = "field-value";

    const hr = document.createElement("hr");
    hr.className = "field-divider";
    line.appendChild(hr);

    wrap.append(label, line);z
    return wrap;
}

/**
 * Notice – label 항상 표시
 */
function renderNotice(field, value) {
    const wrap = document.createElement("div");
    wrap.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label || "안내 문구";

    const val = document.createElement("div");
    val.className = `field-value notice ${field.meta?.style || "info"}`;
    val.innerText = formatValue(value) || field.meta?.message || "";

    wrap.append(label, val);
    return wrap;
}


/**
 * Image – 좌 label / 우 이미지 (밑줄 문제 해결)
 */
function renderImage(field, value) {
    const wrap = document.createElement("div");
    wrap.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label || "이미지";

    const val = document.createElement("div");
    val.className = "field-value field-image";

    const img = document.createElement("img");
    img.src = value?.src || "";
    img.alt = value?.alt || "";

    val.appendChild(img);
    wrap.append(label, val);
    return wrap;
}

/* =====================================================
 * Radio / Checkbox
 * ===================================================== */

function renderOptionGroupFlow(field, value, page, root) {
    const wrap = document.createElement("div");
    wrap.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label;

    const options = document.createElement("div");
    options.className = "field-value";

    field.meta.options.forEach(opt => {
        const line = document.createElement("div");
        const checked = Array.isArray(value)
            ? value.includes(opt.id)
            : value === opt.id;

        line.innerText = `${checked ? "☑" : "☐"} ${opt.label}`;
        options.appendChild(line);
    });

    wrap.append(label, options);

    if (canAppend(page, wrap)) {
        page.querySelector(".page-content").appendChild(wrap);
        return page;
    }

    page = flowAppend(label, page, root);

    for (const opt of field.meta.options) {
        const line = document.createElement("div");
        line.className = "field-value";
        const checked = Array.isArray(value)
            ? value.includes(opt.id)
            : value === opt.id;

        line.innerText = `${checked ? "☑" : "☐"} ${opt.label}`;
        page = flowAppend(line, page, root);
    }

    return page;
}

/* =====================================================
 * Table – Row Flow (중복 방지)
 * ===================================================== */

function renderTableFlow(field, rows, page, root) {
    let tableBlock = null;
    let tbody = null;

    for (const row of rows) {
        if (!tableBlock) {
            tableBlock = createTableBlock(field);
            tbody = tableBlock.tbody;
        }

        const tr = document.createElement("tr");

        // 🔹 행 번호
        const noTd = document.createElement("td");
        noTd.innerText = String(tbody.children.length + 1);
        noTd.className = "table-no";
        tr.appendChild(noTd);

        // 기존 데이터
        field.meta.columns.forEach(col => {
            const td = document.createElement("td");
            td.innerText = row[col.id] ?? "";
            tr.appendChild(td);
        });

        tbody.appendChild(tr);

        if (!canAppend(page, tableBlock.wrapper)) {
            tbody.removeChild(tr);

            page = createPage();
            root.appendChild(page);

            tableBlock = createTableBlock(field);
            tbody = tableBlock.tbody;
            tbody.appendChild(tr);

            page.querySelector(".page-content")
                .appendChild(tableBlock.wrapper);
        } else {
            if (!tableBlock.wrapper.parentNode) {
                page.querySelector(".page-content")
                    .appendChild(tableBlock.wrapper);
            }
        }
    }

    return page;
}

function createTableBlock(field) {
    const wrapper = document.createElement("div");
    wrapper.className = "field field-row";

    const label = document.createElement("div");
    label.className = "field-label";
    label.innerText = field.label;

    const container = document.createElement("div");
    container.className = "field-value";

    const table = document.createElement("table");
    table.className = "table";

    const thead = document.createElement("thead");
    const tr = document.createElement("tr");

    const noTh = document.createElement("th");
    noTh.className = "table-no";
    noTh.innerText = "No";
    tr.appendChild(noTh);

    field.meta.columns.forEach(col => {
        const th = document.createElement("th");
        th.innerText = col.label;
        tr.appendChild(th);
    });

    thead.appendChild(tr);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    table.appendChild(tbody);

    container.appendChild(table);
    wrapper.append(label, container);

    return {wrapper, tbody};
}

/* =====================================================
 * Utils
 * ===================================================== */

function formatValue(value) {
    if (value == null) return "";

    // preview 전용 display 객체
    if (typeof value === "object" && value.display) {
        return value.display;
    }

    // (안전망 – 혹시 이전 구조 섞여와도 대응)
    if (typeof value === "object" && value.start && value.end) {
        return `${value.start} ~ ${value.end}`;
    }

    if (typeof value === "object" && value.name) {
        return value.name;
    }

    if (Array.isArray(value)) {
        return value.join(", ");
    }

    return String(value);
}


/* =====================================================
 * Preview Navigation
 * ===================================================== */

function handlePreviewBack() {
    // 히스토리가 있으면 이전 페이지
    if (window.history.length > 1) {
        window.history.back();
    } else {
        // fallback (필요 시 수정)
        window.location.href = "/admin/form-templates";
    }
}
