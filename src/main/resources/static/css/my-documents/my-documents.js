// ===============================
// 상태 관리 (page는 0-based)
// ===============================
let mydocState = {
    status: "ALL",
    searchType: "date",
    searchParams: {},
    page: 0,
    size: 10,
    totalPages: 1
};

// ===============================
// 작성 팝업 상태
// ===============================
let writePopupState = {
    selectedTemplateId: null,
    debounce: null
};


// ===============================
// 입력 제한 상수
// ===============================
const SEARCH_TEXT_MAX = 50;


// ===============================
// 초기화
// ===============================
function initMyDocumentsPage() {
    bindFilterEvents();
    bindWriteButton();
    renderSearchInputs("date");
    fetchAndRender();
}

// ===============================
// 이벤트 바인딩
// ===============================
function bindFilterEvents() {
    const statusEl = document.getElementById("statusFilter");
    const searchTypeEl = document.getElementById("searchType");
    const searchBtnEl = document.getElementById("searchBtn");

    if (statusEl) {
        statusEl.addEventListener("change", function () {
            mydocState.status = this.value;
            mydocState.page = 0;
            fetchAndRender();
        });
    }

    if (searchTypeEl) {
        searchTypeEl.addEventListener("change", function () {
            mydocState.searchType = this.value;
            renderSearchInputs(this.value);
        });
    }

    if (searchBtnEl) {
        searchBtnEl.addEventListener("click", function () {
            if (mydocState.searchType !== 'date') {
                const input = document.getElementById('textInput');
                const hint = document.getElementById('searchTextHint');

                if (input) {
                    const v = input.value.trim();
                    if (v && v.length > SEARCH_TEXT_MAX) {
                        showHint(
                            hint,
                            `검색어는 ${SEARCH_TEXT_MAX}자 이내여야 합니다.`,
                            'error'
                        );
                        return;
                    }
                }
            }

            updateSearchParams();
            mydocState.page = 0;
            fetchAndRender();
        });
    }

}

// ===============================
// 문서 작성 버튼
// ===============================
function bindWriteButton() {
    const btn = document.getElementById('createDocBtn');
    if (!btn) return;

    btn.addEventListener('click', openWritePopup);
}

// ===============================
// 문서 작성 팝업
// ===============================
function openWritePopup() {
    const popup = document.getElementById('writePopup');
    if (!popup) return;

    popup.style.display = 'flex';
    resetWritePopup();
    document.getElementById('writeTemplateSearch')?.focus();
}

function closeWritePopup() {
    const popup = document.getElementById('writePopup');
    if (!popup) return;

    popup.style.display = 'none';
}


function resetWritePopup() {
    writePopupState.selectedTemplateId = null;

    const input = document.getElementById('writeTemplateSearch');
    const dropdown = document.getElementById('writeTemplateDropdown');
    const hint = document.getElementById('writeTemplateSearchHint');
    const desc = document.getElementById('writeSelectedDescText');
    const okBtn = document.getElementById('writePopupOkBtn');

    if (input) input.value = '';
    if (dropdown) dropdown.style.display = 'none';
    if (hint) hint.textContent = '';
    if (desc) desc.textContent = '선택한 양식의 설명이 표시됩니다.';
    if (okBtn) okBtn.disabled = true;
}


async function fetchTemplateGroupsByKeyword(keyword) {
    if (!keyword) return [];

    const res = await apiFetch(
        `/api/form-templates/active?keyword=${encodeURIComponent(keyword)}`
    );

    if (!res.ok) return [];

    const result = await res.json();
    return result.data ?? [];
}

function renderWriteTemplateDropdown(items) {
    const dropdown = document.getElementById('writeTemplateDropdown');
    dropdown.innerHTML = '';

    if (!items.length) {
        dropdown.style.display = 'none';
        return;
    }

    items.forEach(item => {
        const div = document.createElement('div');
        div.className = 'modal-dropdown-item';

        const name = item.formTemplateGroupName;
        const groupId = item.formTemplateGroupId;
        const templateId = item.formTemplateId;

        div.textContent = name;
        div.title = name;

        div.addEventListener('mousedown', () => {
            document.getElementById('writeTemplateSearch').value = name;
            writePopupState.selectedTemplateId = templateId;

            dropdown.style.display = 'none';
            showSelectedTemplateDescription(groupId);

            document.getElementById('writePopupOkBtn').disabled = false;
            document.getElementById('writeTemplateSearchHint').textContent =
                `선택됨`;
        });

        dropdown.appendChild(div);
    });

    dropdown.style.display = 'block';
}


async function showSelectedTemplateDescription(groupId) {
    const descEl = document.getElementById('writeSelectedDescText');
    if (!groupId || !descEl) return;

    descEl.textContent = '조회 중...';

    try {
        const res = await apiFetch(`/api/form-template-groups/${groupId}`);
        if (!res.ok) throw new Error();

        const result = await res.json();
        descEl.textContent =
            result?.data?.description ||
            '(설명 없음)';
    } catch {
        descEl.textContent = '(설명 조회 실패)';
    }
}

function bindSearchEnterKey() {
    const input = document.getElementById('textInput');
    if (!input) return;

    input.addEventListener('keyup', e => {
        if (e.key !== 'Enter') return;

        const v = input.value.trim();
        if (v && v.length > SEARCH_TEXT_MAX) return;

        updateSearchParams();
        mydocState.page = 0;
        fetchAndRender();
    });
}


function bindWritePopupEvents() {
    const input = document.getElementById('writeTemplateSearch');
    const dropdown = document.getElementById('writeTemplateDropdown');
    const okBtn = document.getElementById('writePopupOkBtn');
    const hint = document.getElementById('writeTemplateSearchHint');

    if (!input || !okBtn) return;

    // maxlength 설정 (안전)
    input.setAttribute('maxlength', SEARCH_TEXT_MAX);

    input.addEventListener('input', () => {
        enforceMaxLength(input, SEARCH_TEXT_MAX);

        const keyword = input.value.trim();
        writePopupState.selectedTemplateId = null;
        okBtn.disabled = true;

        clearTimeout(writePopupState.debounce);

        if (!keyword) {
            dropdown.style.display = 'none';
            clearHint(hint);
            document.getElementById('writeSelectedDescText').textContent =
                '선택한 양식의 설명이 표시됩니다.';
            return;
        }

        // 입력 현황 hint
        showHint(
            hint,
            `입력됨 (${keyword.length}/${SEARCH_TEXT_MAX})`,
            'success'
        );

        writePopupState.debounce = setTimeout(async () => {
            const items = await fetchTemplateGroupsByKeyword(keyword);
            renderWriteTemplateDropdown(items);
        }, 400);
    });

    // 포커스 아웃 시 드롭다운만 닫기
    input.addEventListener('blur', () => {
        setTimeout(() => {
            if (dropdown) dropdown.style.display = 'none';
        }, 150);
    });

    okBtn.addEventListener('click', async () => {
        if (!writePopupState.selectedTemplateId) return;

        const res = await apiFetch(
            `/api/documents/draft/${writePopupState.selectedTemplateId}`,
            {method: 'POST'}
        );

        const result = await res.json();
        const documentId = result.data.documentId;

        location.href = `/view/document/write/${documentId}`;
    });


    document.getElementById('writePopupCancelBtn')
        ?.addEventListener('click', closeWritePopup);
}


// ===============================
// 검색 UI
// ===============================
function renderSearchInputs(type) {
    const area = document.getElementById("searchInputs");
    if (!area) return;

    if (type === "date") {
        area.innerHTML = `
            <input type="date" id="startDate">
            <span>~</span>
            <input type="date" id="endDate">
        `;
        return;
    }

    const placeholder =
        type === "formName"
            ? "양식명을 입력하세요"
            : "문서 제목을 입력하세요";

    area.innerHTML = `
        <div class="search-group">
            <input type="text"
                   id="textInput"
                   placeholder="${placeholder}"
                   maxlength="${SEARCH_TEXT_MAX}">
            <div class="hint" id="searchTextHint"></div>
        </div>
    `;

    bindSearchTextValidation();
}

function bindSearchTextValidation() {
    const input = document.getElementById('textInput');
    const hint = document.getElementById('searchTextHint');
    if (!input || !hint) return;

    input.addEventListener('input', () => {
        enforceMaxLength(input, SEARCH_TEXT_MAX);

        const v = input.value.trim();
        if (!v) {
            clearHint(hint);
            return;
        }

        if (v.length > SEARCH_TEXT_MAX) {
            showHint(
                hint,
                `검색어는 ${SEARCH_TEXT_MAX}자 이내여야 합니다. (${v.length}/${SEARCH_TEXT_MAX})`,
                'error'
            );
            return;
        }

        showHint(
            hint,
            `입력됨 (${v.length}/${SEARCH_TEXT_MAX})`,
            'success'
        );
    });
}


function updateSearchParams() {
    if (mydocState.searchType === "date") {
        mydocState.searchParams = {
            startDate: document.getElementById("startDate")?.value,
            endDate: document.getElementById("endDate")?.value
        };
    } else {
        mydocState.searchParams = {
            keyword: document.getElementById("textInput")?.value
        };
    }
}

// ===============================
// API 호출
// ===============================
function fetchMyDocuments(params) {
    const query = new URLSearchParams();

    if (params.status && params.status !== "ALL") {
        query.append("documentStatus", params.status);
    }

    query.append("page", String(params.page));
    query.append("size", String(params.size));

    if (params.searchType === "date") {
        if (params.startDate) query.append("startDate", params.startDate);
        if (params.endDate) query.append("endDate", params.endDate);
    } else {
        const keyword = (params.keyword || "").trim();
        if (keyword.length > 0) {
            query.append("keyword", keyword);
            query.append("searchType", params.searchType);
        }
    }

    const url = `/api/documents/my-written?${query.toString()}`;

    return apiFetch(url)
        .then(res => res.json())
        .then(json => json.data); // ✅ 항상 data만 반환
}


// ===============================
// 렌더링
// ===============================
function fetchAndRender() {
    const params = {
        status: mydocState.status,
        searchType: mydocState.searchType,
        ...mydocState.searchParams,
        page: mydocState.page,
        size: mydocState.size
    };

    fetchMyDocuments(params).then(pageData => {
        renderTable(pageData.content ?? []);
        mydocState.totalPages = pageData.totalPages ?? 1;
        renderPagination();
    });
}

function getProgressText(status) {
    switch (status) {
        case "SUBMITTED":
            return "결재 대기";
        case "IN_PROGRESS":
            return "결재중";
        case "REJECTED":
            return "반려";
        case "APPROVED":
            return "결재 완료";
        default:
            return "-";
    }
}

function formatCurrentApprover(doc) {

    // ✅ 1️⃣ DRAFT는 결재자 개념 없음
    if (doc.status === "DRAFT") {
        return "-";
    }

    const org = doc.currentApproverOrgName;
    const position = doc.currentApproverPositionName;
    const name = doc.approvalName;

    // ✅ 2️⃣ 결재 완료 상태
    if (doc.status === "APPROVED") {
        return "결재 완료";
    }

    // ✅ 3️⃣ 반려
    if (doc.status === "REJECTED") {
        return "반려";
    }

    // ✅ 4️⃣ 결재 진행 중
    if (org && position && name) {
        return `${org}/${position}/${name}`;
    }

    // ✅ fallback
    return "-";
}

function renderTable(docs) {
    const tbody = document.getElementById("mydocTbody");
    const nodata = document.getElementById("nodataMsg");

    if (!tbody || !nodata) return;

    tbody.innerHTML = "";

    if (!docs.length) {
        nodata.style.display = "block";
        return;
    }

    nodata.style.display = "none";

    docs.forEach(doc => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${doc.title}</td>
            <td>${doc.templateGroupName}</td>
            <td>v${doc.templateVersion}</td>
            <td>${formatDate(doc.createdAt)}</td>
            <td>${getStatusText(doc.status)}</td>
            <td>${getProgressText(doc.status)}</td>
            <td>${formatCurrentApprover(doc)}</td>
        `;

        tr.classList.add("clickable");

        tr.addEventListener("click", () => {
            if (doc.status === "DRAFT") {
                location.href = `/view/document/write/${doc.documentId}`;
                return;
            }

            location.href = `/view/document/${doc.documentId}`;
        });
        tbody.appendChild(tr);
    });
}


// ===============================
// 페이지네이션
// ===============================
function renderPagination() {
    const prevBtn = document.getElementById("prevPageBtn");
    const nextBtn = document.getElementById("nextPageBtn");
    const indicator = document.getElementById("pageIndicator");

    if (!prevBtn || !nextBtn || !indicator) return;

    const cur = mydocState.page;
    const total = mydocState.totalPages;

    // 페이지 표시 (1-based)
    indicator.textContent = total > 0
        ? `${cur + 1} / ${total}`
        : "0 / 0";

    // 버튼 활성/비활성
    prevBtn.disabled = cur <= 0;
    nextBtn.disabled = cur >= total - 1;

    // 이벤트 (중복 방지 위해 onclick 사용)
    prevBtn.onclick = () => {
        if (cur > 0) {
            mydocState.page--;
            fetchAndRender();
        }
    };

    nextBtn.onclick = () => {
        if (cur < total - 1) {
            mydocState.page++;
            fetchAndRender();
        }
    };
}

// ===============================
// 힌트 유틸
// ===============================
function showHint(el, message, type) {
    if (!el) return;
    el.textContent = message;
    el.className = `hint ${type || ''}`.trim();
}

function clearHint(el) {
    if (el) el.textContent = '';
}

function enforceMaxLength(inputEl, max) {
    if (!inputEl) return;
    if (inputEl.value.length > max) {
        inputEl.value = inputEl.value.slice(0, max);
    }
}


// ===============================
// 유틸
// ===============================
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
            return status;
    }
}

function formatDate(isoString) {
    if (!isoString) return "-";
    const d = new Date(isoString);
    return d.toLocaleDateString("ko-KR");
}

// ===============================
// fragment 안전 실행
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    if (document.querySelector(".my-documents-page")) {
        initMyDocumentsPage();
        bindWritePopupEvents();
    }
});

