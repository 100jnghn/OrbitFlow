// ===============================
// 상태 관리 (offset는 0-based)
// ===============================
let mydocState = {
    status: "ALL",
    searchType: "TITLE",
    searchParams: {
        keyword: "",
        startDate: "",
        endDate: ""
    },
    offset: 0,
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

// ===============================
// 초기화
// ===============================
function initMyDocumentPage() {
    updateApprovalSidebarSelection();
    bindFilterEvents();
    bindWriteButton();
    fetchAndRender();
}

// ===============================
// 이벤트 바인딩
// ===============================
function executeSearch() {
    const input = document.getElementById('keyword');
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

    updateSearchParams();
    mydocState.offset = 0;
    fetchAndRender();
}


function bindFilterEvents() {
    const statusEl = document.getElementById("statusFilter");
    const searchTypeEl = document.getElementById("searchType");
    const searchBtnEl = document.getElementById("searchBtn");
    const keywordEl = document.getElementById("keyword");
    const startDateEl = document.getElementById("startDate");
    const endDateEl = document.getElementById("endDate");

    /* =========================
       ✅ 기간 필터 제약 (추가)
    ========================= */
    if (startDateEl && endDateEl) {

        // 초기 상태 반영 (뒤로가기 / 새로고침 대비)
        if (startDateEl.value) {
            endDateEl.min = startDateEl.value;
        }

        startDateEl.addEventListener("change", () => {
            if (startDateEl.value) {
                endDateEl.min = startDateEl.value;

                // 이미 선택된 종료일이 시작일보다 이전이면 초기화
                if (endDateEl.value && endDateEl.value < startDateEl.value) {
                    endDateEl.value = "";
                }
            } else {
                endDateEl.removeAttribute("min");
            }
        });
    }

    /* =========================
       기존 로직
    ========================= */

    if (statusEl) {
        statusEl.addEventListener("change", function () {
            mydocState.status = this.value;
            mydocState.offset = 0;
            fetchAndRender();
        });
    }

    if (searchTypeEl) {
        searchTypeEl.addEventListener("change", function () {
            mydocState.searchType = this.value;
        });
    }

    if (startDateEl) {
        startDateEl.addEventListener("change", () => {
            updateSearchParams();
            mydocState.offset = 0;
            fetchAndRender();
        });
    }

    if (endDateEl) {
        endDateEl.addEventListener("change", () => {
            updateSearchParams();
            mydocState.offset = 0;
            fetchAndRender();
        });
    }

    if (searchBtnEl) {
        searchBtnEl.addEventListener("click", executeSearch);
    }

    if (keywordEl) {
        keywordEl.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                executeSearch();
            }
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

function bindDraftDeleteEvent() {
    const tbody = document.getElementById("mydocTbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const btn = e.target.closest(".row-delete-btn");
        if (!btn) return;

        e.stopPropagation(); // 행 클릭 방지

        confirmDeleteDraft(btn.dataset.id);
    });
}


async function confirmDeleteDraft(documentId) {
    const result = await sweetConfirm(
        '임시 문서 삭제',
        '삭제한 문서는 복구할 수 없습니다.'
    );

    if (!result.isConfirmed) return;

    try {
        const res = await deleteDraftDocument(documentId);
        if (!res.ok) throw new Error();

        await Swal.fire({
            icon: 'success',
            title: '삭제 완료',
            text: '임시 문서가 삭제되었습니다.',
            timer: 1200,
            showConfirmButton: false
        });

        fetchAndRender();
    } catch {
        Swal.fire({
            icon: 'error',
            title: '삭제 실패',
            text: '문서를 삭제할 수 없습니다.'
        });
    }
}


async function deleteDraftDocument(documentId) {
    return apiFetch(
        `/api/documents/draft/${documentId}`,
        {method: 'DELETE'}
    );
}


// ===============================
// 검색 UI
// ===============================


function updateSearchParams() {
    mydocState.searchParams = {
        keyword: document.getElementById("keyword")?.value?.trim() || "",
        startDate: document.getElementById("startDate")?.value || "",
        endDate: document.getElementById("endDate")?.value || ""
    };
}


// ===============================
// API 호출
// ===============================
function fetchMyDocuments(params) {
    const query = new URLSearchParams();

    if (params.status && params.status !== "ALL") {
        query.append("documentStatus", params.status);
    }

    query.append("offset", String(params.offset));
    query.append("size", String(params.size));

    const {keyword, startDate, endDate} = params;

    if (keyword) {
        query.append("keyword", keyword);
        const type = params.searchType || "TITLE";
        query.append("searchType", type);
    }

    if (startDate) query.append("startDate", startDate);
    if (endDate) query.append("endDate", endDate);

    return apiFetch(`/api/documents/my-written?${query.toString()}`)
        .then(res => res.json())
        .then(json => json.data);
}


// ===============================
// 렌더링
// ===============================
function fetchAndRender() {
    const params = {
        status: mydocState.status,
        searchType: mydocState.searchType,
        ...mydocState.searchParams,
        offset: mydocState.offset,
        size: mydocState.size
    };

    fetchMyDocuments(params).then(pageData => {
        renderTable(pageData.content ?? []);
        mydocState.totalPages = pageData.totalPages ?? 1;
        renderPagination();
    });
}


/* =========================
   초기화 버튼
========================= */

function clearSearchFilters() {
    // 1. UI 초기화
    document.getElementById("statusFilter").value = "ALL";
    document.getElementById("searchType").value = "TITLE";
    document.getElementById("keyword").value = "";
    document.getElementById("startDate").value = "";
    document.getElementById("endDate").value = "";

    // 2. 상태 초기화
    mydocState.status = "ALL";
    mydocState.searchType = "TITLE";
    mydocState.searchParams = {
        keyword: "",
        startDate: "",
        endDate: ""
    };
    mydocState.offset = 0;

    // 3. 정상 재조회
    fetchAndRender(); // ⭐ 이것만 호출
}


function renderTable(docs) {
    const tbody = document.getElementById("mydocTbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (!docs.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="approval-empty-row">
                    조회된 문서가 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    docs.forEach((doc, index) => {
        const tr = document.createElement("tr");

        // ⭐ 번호 계산
        const number = mydocState.offset * mydocState.size + index + 1;

        const revisionBadge = doc.hasRevision
            ? `<span class="revision-badge">재기안됨</span>`
            : '';

        tr.innerHTML = `
            <td class="index-col">${number}</td>
            <td class="title-col ellipsis" title="${doc.title}">
                <span class="title-text">${doc.title}</span>
                ${revisionBadge}
            </td>
            <td class="title-col ellipsis">${doc.templateGroupName}</td>
            <td>v${doc.templateVersion}</td>
            <td>${formatDateTime(doc.createdAt)}</td>
            <td>${renderDocumentStatusBadge(doc.status)}</td>
            <td>${renderApprovalStep(doc)}</td>
            <td class="progress-cell ellipsis">
                <span class="progress-text">
                    ${renderCurrentApprover(doc)}
                </span>
                ${
            doc.status === 'DRAFT'
                ? `<button
                               type="button"
                               class="row-delete-btn"
                               data-id="${doc.documentId}">
                               삭제
                           </button>`
                : ''
        }
            </td>
        `;

        tr.classList.add("clickable");

        // ✅ 행 클릭 시 상세 이동 (삭제 버튼 클릭은 제외)
        tr.addEventListener("click", (e) => {
            if (e.target.closest(".row-delete-btn")) {
                return; // 삭제 버튼 클릭 → 이동 금지
            }

            location.href = doc.status === "DRAFT"
                ? `/view/document/write/${doc.documentId}`
                : `/view/document/${doc.documentId}`;
        });

        tbody.appendChild(tr);
    });
}


function renderApprovalStep(doc) {
    if (doc.status === 'DRAFT') {
        return '-';
    }

    if (
        doc.currentApprovalOrder == null ||
        doc.totalApprovalCount == null
    ) {
        return '-';
    }

    return `${doc.currentApprovalOrder} / ${doc.totalApprovalCount}`;
}


function renderCurrentApprover(doc) {
    if (doc.status === 'DRAFT') {
        return '-';
    }

    if (doc.status === 'APPROVED') {
        return '결재 완료';
    }

    if (doc.status === 'REJECTED') {
        return '반려';
    }

    const {
        currentApproverOrgName,
        currentApproverPositionName,
        currentApproverName
    } = doc;

    if (
        currentApproverOrgName &&
        currentApproverPositionName &&
        currentApproverName
    ) {
        return `${currentApproverOrgName}/${currentApproverPositionName}/${currentApproverName}`;
    }

    return '-';
}


function renderDocumentStatusBadge(status) {
    let text = '-';
    let colorClass = 'status-gray';

    switch (status) {
        case 'DRAFT':
            text = '작성중';
            break;
        case 'SUBMITTED':
            text = '상신됨';
            colorClass = 'status-blue';
            break;
        case 'IN_PROGRESS':
            text = '결재중';
            colorClass = 'status-blue';
            break;
        case 'APPROVED':
            text = '승인완료';
            colorClass = 'status-green';
            break;
        case 'REJECTED':
            text = '반려';
            colorClass = 'status-red';
            break;
    }

    return `<span class="status-badge ${colorClass}">${text}</span>`;
}


// ===============================
// 페이지네이션
// ===============================
function renderPagination() {
    const pagination = document.getElementById('approvalPagination');
    if (!pagination) return;

    pagination.innerHTML = '';

    const offset = mydocState.offset;
    const total = mydocState.totalPages;

    const prev = document.createElement('button');
    prev.className = 'page-btn';
    prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prev.disabled = offset === 0;
    prev.onclick = () => {
        mydocState.offset--;
        fetchAndRender();
    };
    pagination.appendChild(prev);

    const maxVisible = 5;
    let start = Math.max(0, offset - Math.floor(maxVisible / 2));
    let end = Math.min(total - 1, start + maxVisible - 1);

    if (start > 0) {
        addPageBtn(pagination, 0);
        if (start > 1) addEllipsis(pagination);
    }

    for (let i = start; i <= end; i++) {
        addPageBtn(pagination, i, i === offset);
    }

    if (end < total - 1) {
        if (end < total - 2) addEllipsis(pagination);
        addPageBtn(pagination, total - 1);
    }

    const next = document.createElement('button');
    next.className = 'page-btn';
    next.innerHTML = '<i class="fas fa-chevron-right"></i>';
    next.disabled = offset >= total - 1;
    next.onclick = () => {
        mydocState.offset++;
        fetchAndRender();
    };
    pagination.appendChild(next);
}


function addPageBtn(container, page, active = false) {
    const btn = document.createElement('button');
    btn.className = 'page-number';
    if (active) btn.classList.add('active');
    btn.textContent = page + 1;
    btn.onclick = () => {
        mydocState.offset = page;
        fetchAndRender();
    };
    container.appendChild(btn);
}


function addEllipsis(container) {
    const span = document.createElement('span');
    span.className = 'ellipsis';
    span.textContent = '...';
    container.appendChild(span);
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


function formatDateTime(isoString) {
    if (!isoString) return "-";
    const d = new Date(isoString);
    return d.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

// ===============================
// fragment 안전 실행
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    const clearBtn = document.getElementById("clearBtn");
    if (clearBtn) {
        clearBtn.addEventListener("click", clearSearchFilters);
    }

    if (document.querySelector(".my-documents-page")) {
        initMyDocumentPage();
        bindWritePopupEvents();
        bindDraftDeleteEvent();
    }
});

