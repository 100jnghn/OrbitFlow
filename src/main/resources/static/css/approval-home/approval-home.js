// ========================
// 상수 및 상태 변수
// ========================
const API_URL = '/api/admin/form-templates/all';

const approvalStatusMap = {
    DRAFT:    { text: '임시 저장',   class: 'status-badge status-pending' },
    ACTIVE:   { text: '활성',   class: 'status-badge status-approved' },
    INACTIVE: { text: '비활성', class: 'status-badge status-rejected' }
};

let approvalState = {
    keyword: '',
    status: '',
    page: 0,
    pageSize: 10
};

let lastSelectedUpdateTemplateId = null;
let lastSelectedUpdateId = null;
let lastUpdateDropdownItems = [];
let lastUpdateDropdownIdMap = {};
let updatePopupDebounce;

const LIST_TITLE_MAX = 30; // 또는 35
const TEMPLATE_NAME_MAX = 50;
const TEMPLATE_DESC_MAX = 200;
const UPDATE_SEARCH_MAX = TEMPLATE_NAME_MAX;

// ========================
// DOM 참조 및 힌트 엘리먼트
// ========================
const popupTemplateName = document.getElementById('popupTemplateName');
const popupTemplateDesc = document.getElementById('popupTemplateDesc');
const popupCreateBtn = document.getElementById('popupCreateBtn');
const updateTemplateSearch = document.getElementById('updateTemplateSearch');
const keywordInput = document.getElementById('keywordInput');

/* maxlength 설정 */
popupTemplateName.setAttribute('maxlength', TEMPLATE_NAME_MAX);
popupTemplateDesc.setAttribute('maxlength', TEMPLATE_DESC_MAX);

// 힌트 엘리먼트
const popupTemplateNameMsg =
    popupTemplateName.closest('.modal-form-group').querySelector('.hint');
const popupTemplateDescMsg =
    popupTemplateDesc.closest('.modal-form-group').querySelector('.hint');
const updateTemplateSearchMsg =
    document.getElementById('updateTemplateSearchHint');
const keywordInputMsg =
    keywordInput.closest('.search-group').querySelector('.hint');

// ========================
// API 호출 함수
// ========================
async function fetchApprovalDocs({ status, keyword, page, pageSize }) {
    const params = [];
    if (typeof page === 'number') params.push(`offset=${page}`);
    if (typeof pageSize === 'number') params.push(`size=${pageSize}`);
    if (status) params.push(`status=${encodeURIComponent(status)}`);
    if (keyword) params.push(`keyword=${encodeURIComponent(keyword)}`);
    const url = API_URL + (params.length ? '?' + params.join('&') : '');
    const res = await apiFetch(url, {});
    if (!res.ok) throw new Error('데이터 불러오기 실패');
    const result = await res.json();
    return result && result.data ? result.data : { content: [], totalPages: 1, number: 0, totalElements: 0 };
}

/**
 * 선택한 양식 그룹(groupId)을 기반으로 결재 양식 임시 저장 생성
 * @param {number} groupId - FormTemplateGroup ID
 * @param {string} categoryCode - TemplateCategoryCode (예: 'ATTENDANCE', 'SCHEDULE', 'GENERAL')
 */
async function createFormTemplateByGroupId(groupId, categoryCode) {
    if (!groupId) {
        alert('양식 그룹 ID가 유효하지 않습니다.');
        return null;
    }
    if (!categoryCode) {
        alert('양식 카테고리를 선택해주세요.');
        return null;
    }

    try {
        const params = new URLSearchParams({
            templateGroupId: groupId,
            categoryCode: categoryCode
        });

        const res = await apiFetch(`/api/admin/form-templates?${params.toString()}`, {
            method: 'POST'
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err?.message || '결재 양식 생성에 실패했습니다.');
        }

        const result = await res.json();
        const formTemplateId = result?.data?.formTemplateId;

        if (!formTemplateId) {
            throw new Error('생성된 결재 양식 ID를 받지 못했습니다.');
        }

        return formTemplateId;

    } catch (e) {
        console.error('[createFormTemplateByGroupId error]', e);
        alert(e.message || '결재 양식 생성 중 오류가 발생했습니다.');
        return null;
    }
}

// 업데이트 팝업 검색 ajax
async function fetchUpdateTemplates(keyword) {
    if (!keyword) return [];
    const res = await apiFetch(
        `/api/form-templates/active?keyword=${encodeURIComponent(keyword)}`
    );
    if (!res.ok) return [];

    const result = await res.json();
    if (Array.isArray(result.data)) return result.data;
    if (result.data && Array.isArray(result.data.content)) return result.data.content;
    return [];
}

// ========================
// 테이블/팝업 렌더링 및 유틸 함수
// ========================
function renderTableRows(docs) {
    const tbody = document.getElementById('approvalTableBody');
    const emptyMsg = document.getElementById('approvalTableEmpty');
    tbody.innerHTML = '';

    if (!docs || docs.length === 0) {
        emptyMsg.style.display = 'block';
        return;
    }
    emptyMsg.style.display = 'none';

    docs.forEach(doc => {
        const tr = document.createElement('tr');
        tr.className = 'approval-row';
        tr.tabIndex = 0;
        tr.setAttribute('role', 'button');

        if (doc.formTemplateStatus === 'ACTIVE') {
            tr.classList.add('row-active');
        } else if (doc.formTemplateStatus === 'INACTIVE') {
            tr.classList.add('row-inactive');
        } else if (doc.formTemplateStatus === 'DRAFT') {
            tr.classList.add('row-draft');
        }

        const isDraft = doc.formTemplateStatus === 'DRAFT';

        tr.style.cursor = 'pointer';

        if (!isDraft) {
            tr.classList.add('row-view-only');
        }

        tr.addEventListener('click', () => {
            const templateId = doc.formTemplateId;

            if (!templateId) return;

            if (isDraft) {
                // 임시 저장 → 편집
                window.location.href =
                    `/view/admin/create-template?templateId=${templateId}`;
            } else {
                // 활성 / 비활성 → 미리보기
                window.location.href =
                    `/view/admin/preview-template/${templateId}`;
            }
        });

        tr.innerHTML = `
        <td class="col-title" title="${escapeHTML(doc.formTemplateGroupName)}">
            <div class="title-wrap">
            <span class="title-text">
                ${escapeHTML(truncateText(doc.formTemplateGroupName, LIST_TITLE_MAX))}
            </span>
            ${renderActionIcon(doc.formTemplateStatus)}
            </div>
        </td>
        <td class="col-version">${doc.formTemplateVersion}</td>
        <td class="col-usedoc">${doc.useDocument || 0}</td>
        <td class="col-updated">${formatDate(doc.updatedAt)}</td>
        <td class="col-attend">${renderAttend(doc.affectTags)}</td>
        <td class="col-schedule">${renderSchedule(doc.affectTags)}</td>
        <td class="col-status">${renderStatusBadge(doc.formTemplateStatus)}</td>
    `;
        tbody.appendChild(tr);
    });
}

function renderUpdateDropdown(items) {
    const dropdown = document.getElementById('updateDropdown');
    dropdown.innerHTML = '';
    if (!items.length) {
        dropdown.style.display = 'none';
        return;
    }
    lastUpdateDropdownItems = items;
    lastUpdateDropdownIdMap = {};
    items.forEach(item => {
        const div = document.createElement('div');
        div.className = 'modal-dropdown-item';

        const name =
            item.formTemplateGroupName ||
            item.formTemplateName ||
            item.name ||
            '';

        const groupId = item.formTemplateGroupId || item.groupId;
        const templateId = item.formTemplateId || item.templateId;

        div.textContent = truncateText(name, TEMPLATE_NAME_MAX);
        div.title = name;

        div.addEventListener('mousedown', function () {
            updateTemplateSearch.value = name;

            lastSelectedUpdateId = groupId;
            lastSelectedUpdateTemplateId = templateId;

            dropdown.style.display = 'none';
            showSelectedUpdateDescription(groupId);
            showMsg(
                updateTemplateSearchMsg,
                `선택됨 (${name.length}/${UPDATE_SEARCH_MAX})`,
                'success'
            );
        });

        dropdown.appendChild(div);
    });

    dropdown.style.display = 'block';
}

async function showSelectedUpdateDescription(groupId) {
    const descTextEl = document.getElementById('updateSelectedDescText');
    if (!groupId || !descTextEl) return;

    descTextEl.textContent = '조회 중...';

    try {
        const res = await apiFetch(`/api/form-template-groups/${groupId}`);
        if (!res.ok) throw new Error();

        const result = await res.json();
        descTextEl.textContent =
            result?.data?.description ||
            result?.data?.desc ||
            result?.data?.summary ||
            '(설명 없음)';
    } catch {
        descTextEl.textContent = '(설명 조회 실패)';
    }
}

function renderActionIcon(status) {
    if (status === 'DRAFT') {
        return `<span class="action-icon edit-icon" title="편집">✏️</span>`;
    }
    return `<span class="action-icon view-icon" title="상세 보기">🔍</span>`;
}

function renderAttend(tags) {
    if (Array.isArray(tags) && tags.includes('ATTENDANCE')) {
        return '<span class="tag-on">O</span>';
    }
    return '<span class="tag-off">X</span>';
}

function renderSchedule(tags) {
    if (Array.isArray(tags) && tags.includes('SCHEDULE')) {
        return '<span class="tag-on">O</span>';
    }
    return '<span class="tag-off">X</span>';
}

function renderStatusBadge(status) {
    const map = approvalStatusMap[status] || { text: status, class: 'status-badge' };
    return `<span class="${map.class}">${map.text}</span>`;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0');
}

function escapeHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function truncateText(text, max) {
    if (!text) return '';
    if (text.length <= max) return text;
    return text.slice(0, max) + '…';
}

// ========================
// 렌더/페이지네이션/데이터로드
// ========================
function renderPagination(page, totalPages) {
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const indicator = document.getElementById('pageIndicator');
    prevBtn.disabled = (page <= 0);
    nextBtn.disabled = (page >= totalPages - 1);
    indicator.textContent = `${page + 1} / ${totalPages}`;
}

async function loadAndRender(override = {}) {
    const req = {
        keyword: override.keyword !== undefined ? override.keyword : approvalState.keyword,
        status:  override.status  !== undefined ? override.status  : approvalState.status,
        page:    override.page    !== undefined ? override.page    : approvalState.page,
        pageSize:override.pageSize !== undefined ? override.pageSize : approvalState.pageSize
    };
    try {
        const { content, totalPages = 1, number = 0 } = await fetchApprovalDocs(req);
        renderTableRows(content);
        renderPagination(number, totalPages);
        approvalState.page = number;
    } catch (e) {
        renderTableRows([]);
        renderPagination(0, 1);
        console.error('[fetch error]', e);
    }
}

// ========================
// 팝업 (생성/업데이트) 열기/닫기 함수
// ========================
function showCreatePopup(initName = '', initDesc = '') {
    popupTemplateName.value = initName;
    popupTemplateDesc.value = initDesc;

    popupTemplateNameMsg.textContent = '';
    popupTemplateDescMsg.textContent = '';
    validateTemplateName();
    validateTemplateDesc();
    updateCreateButtonState();

    document.getElementById('createPopup').style.display = 'flex';
}

function hideCreatePopup() {
    document.getElementById('createPopup').style.display = 'none';
}

function showUpdatePopup() {
    lastSelectedUpdateTemplateId = null;
    lastSelectedUpdateId = null;

    document.getElementById('updatePopup').style.display = 'flex';
    document.getElementById('updateTemplateSearch').value = '';
    document.getElementById('updateSelectedDescText').textContent = '';
    document.getElementById('updateDropdown').style.display = 'none';
    updateTemplateSearch.focus();
}

function hideUpdatePopup() {
    lastSelectedUpdateTemplateId = null;
    lastSelectedUpdateId = null;

    document.getElementById('updatePopup').style.display = 'none';
    document.getElementById('updateDropdown').style.display = 'none';
    document.getElementById('updateTemplateSearch').value = '';
    document.getElementById('updateSelectedDescText').textContent = '';
}

// ========================
// 메시지/히든/텍스트 보조 함수
// ========================
function showMsg(el, message, type) {
    if (!el) return;
    el.textContent = message;
    el.className = 'hint ' + type;
}

function clearHint(el) {
    if (el) el.textContent = '';
}

function enforceMaxLength(inputEl, max) {
    if (inputEl.value.length > max) {
        inputEl.value = inputEl.value.slice(0, max);
    }
}

// ========================
// 폼 검증 함수
// ========================
function validateTemplateName() {
    const v = popupTemplateName.value.trim();

    if (!v) {
        popupTemplateNameMsg.textContent = '';
        return false;
    }

    if (v.length > TEMPLATE_NAME_MAX) {
        showMsg(
            popupTemplateNameMsg,
            `양식명은 ${TEMPLATE_NAME_MAX}자 이내여야 합니다. (${v.length}/${TEMPLATE_NAME_MAX})`,
            'error'
        );
        return false;
    }

    showMsg(
        popupTemplateNameMsg,
        `사용 가능한 양식명입니다. (${v.length}/${TEMPLATE_NAME_MAX})`,
        'success'
    );
    return true;
}

function validateTemplateDesc() {
    const v = popupTemplateDesc.value.trim();

    if (!v) {
        popupTemplateDescMsg.textContent = '';
        return false;
    }

    if (v.length > TEMPLATE_DESC_MAX) {
        showMsg(
            popupTemplateDescMsg,
            `설명은 ${TEMPLATE_DESC_MAX}자 이내여야 합니다. (${v.length}/${TEMPLATE_DESC_MAX})`,
            'error'
        );
        return false;
    }

    showMsg(
        popupTemplateDescMsg,
        `입력됨 (${v.length}/${TEMPLATE_DESC_MAX})`,
        'success'
    );
    return true;
}

function validateUpdateTemplateSearch() {
    const v = updateTemplateSearch.value.trim();

    if (!v) {
        updateTemplateSearchMsg.textContent = '';
        return false;
    }

    if (v.length > UPDATE_SEARCH_MAX) {
        showMsg(
            updateTemplateSearchMsg,
            `양식명은 ${UPDATE_SEARCH_MAX}자 이내여야 합니다. (${v.length}/${UPDATE_SEARCH_MAX})`,
            'error'
        );
        return false;
    }

    showMsg(
        updateTemplateSearchMsg,
        `검색어 입력됨 (${v.length}/${UPDATE_SEARCH_MAX})`,
        'success'
    );
    return true;
}

function validateKeywordSearch() {
    const v = keywordInput.value.trim();

    if (!v) {
        keywordInputMsg.textContent = '';
        return false;
    }

    if (v.length > TEMPLATE_NAME_MAX) {
        showMsg(
            keywordInputMsg,
            `검색어는 ${TEMPLATE_NAME_MAX}자 이내여야 합니다. (${v.length}/${TEMPLATE_NAME_MAX})`,
            'error'
        );
        return false;
    }

    showMsg(
        keywordInputMsg,
        `검색어 입력됨 (${v.length}/${TEMPLATE_NAME_MAX})`,
        'success'
    );
    return true;
}

// ========================
// 버튼 상태 업데이트
// ========================
function updateCreateButtonState() {
    const isNameValid = popupTemplateName.value.trim().length > 0 &&
        popupTemplateName.value.length <= TEMPLATE_NAME_MAX;

    const isDescValid = popupTemplateDesc.value.trim().length > 0 &&
        popupTemplateDesc.value.length <= TEMPLATE_DESC_MAX;

    popupCreateBtn.disabled = !(isNameValid && isDescValid);
}

// ========================
// 이벤트 바인딩 메인/팝업
// ========================
function bindEventHandlers() {
    popupTemplateName.addEventListener('input', () => {
        enforceMaxLength(popupTemplateName, TEMPLATE_NAME_MAX);
        validateTemplateName();
        updateCreateButtonState();
    });

    popupTemplateDesc.addEventListener('input', () => {
        enforceMaxLength(popupTemplateDesc, TEMPLATE_DESC_MAX);
        validateTemplateDesc();
        updateCreateButtonState();
    });

    keywordInput.addEventListener('input', () => {
        enforceMaxLength(keywordInput, TEMPLATE_NAME_MAX);
        validateKeywordSearch();
    });

    document.getElementById('statusFilter').addEventListener('change', function () {
        approvalState.status = this.value;
        approvalState.page = 0;
        loadAndRender({ status: this.value, page: 0 });
    });

    document.getElementById('searchBtn').addEventListener('click', function () {
        if (!validateKeywordSearch() && keywordInput.value.trim()) return;
        approvalState.keyword = keywordInput.value;
        approvalState.page = 0;
        loadAndRender({keyword: keywordInput.value, page: 0});
        clearHint(keywordInputMsg);
    });

    keywordInput.addEventListener('keyup', function (e) {
        if (e.key === 'Enter') {
            if (!validateKeywordSearch() && this.value.trim()) return;
            approvalState.keyword = this.value;
            approvalState.page = 0;
            loadAndRender({keyword: this.value, page: 0});
            clearHint(keywordInputMsg);
        }
    });

    document.getElementById('prevPageBtn').addEventListener('click', function () {
        if (approvalState.page > 0) {
            approvalState.page -= 1;
            loadAndRender({ page: approvalState.page });
        }
    });

    document.getElementById('nextPageBtn').addEventListener('click', function () {
        approvalState.page += 1;
        loadAndRender({ page: approvalState.page });
    });

    document.getElementById('createTemplateBtn').addEventListener('click', function () {
        showCreatePopup();
    });

    document.getElementById('updateTemplateBtn').addEventListener('click', function () {
        showUpdatePopup();
    });

    // 팝업 취소, 작성 버튼
    document.getElementById('popupCancelBtn').addEventListener('click', function(){
        hideCreatePopup();
    });

    document.getElementById('popupCreateBtn')
        .addEventListener('click', async function () {

            if (popupCreateBtn.disabled) return;

            const name = popupTemplateName.value.trim();
            const desc = popupTemplateDesc.value.trim();

            try {
                popupCreateBtn.disabled = true;

                const res = await apiFetch('/api/admin/form-template-groups', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({name, description: desc})
                });

                if (!res.ok) {
                    const errMsg =
                        (await res.json())?.message ||
                        '문서 양식 그룹 생성에 실패했습니다.';
                    alert(errMsg);
                    return;
                }

                const result = await res.json();
                const groupId = result?.data?.createdFormTemplateGroupId;
                if (!groupId) {
                    alert('생성된 양식 그룹 ID를 가져오지 못했습니다.');
                    return;
                }

                const templateId =
                    await createFormTemplateByGroupId(groupId, "GENERAL");

                hideCreatePopup();
                window.location.href =
                    `/view/admin/create-template?templateId=${templateId}`;

            } catch (e) {
                console.error(e);
                alert('문서 양식 그룹 생성 중 오류가 발생했습니다.');
            } finally {
                popupCreateBtn.disabled = false;
            }
        });
}

function bindUpdatePopupEvents() {
    const input = document.getElementById('updateTemplateSearch');
    const dropdown = document.getElementById('updateDropdown');
    const cancelBtn = document.getElementById('popupUpdateCancelBtn');
    const okBtn = document.getElementById('popupUpdateOkBtn');

    // 팝업 열기
    document.getElementById('updateTemplateBtn')
        .addEventListener('click', showUpdatePopup);

    // 취소
    cancelBtn.addEventListener('click', hideUpdatePopup);

    // 검색 입력
    input.addEventListener('input', () => {
        enforceMaxLength(input, UPDATE_SEARCH_MAX);
        validateUpdateTemplateSearch();

        clearTimeout(updatePopupDebounce);
        const keyword = input.value.trim();

        if (!keyword) {
            dropdown.style.display = 'none';
            updateTemplateSearchMsg.textContent = '';
            document.getElementById('updateSelectedDescText').textContent = '';
            return;
        }

        updatePopupDebounce = setTimeout(async () => {
            const items = await fetchUpdateTemplates(keyword);
            renderUpdateDropdown(items);
        }, 400);
    });

    input.addEventListener('blur', () => {
        setTimeout(() => dropdown.style.display = 'none', 150);
    });

    // ✅ 업데이트 실행 (단일 이벤트)
    okBtn.addEventListener('click', async () => {
        if (!lastSelectedUpdateId) {
            alert('업데이트할 문서를 선택해주세요.');
            return;
        }

        try {
            okBtn.disabled = true;

            const res = await apiFetch(
                `/api/admin/form-templates/${lastSelectedUpdateId}/revise`,
                {method: 'POST'}
            );

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err?.message || '문서 업데이트에 실패했습니다.');
            }

            const result = await res.json();

            console.log('revise api result:', result);

            const createdTemplateId = result?.data?.createdTemplateId;

            if (!createdTemplateId) {
                throw new Error('생성된 새 문서 ID를 받지 못했습니다.');
            }

            hideUpdatePopup();

            window.location.href =
                `/view/admin/create-template?templateId=${createdTemplateId}`;

        } catch (e) {
            console.error('[revise error]', e);
            alert(e.message || '문서 업데이트 중 오류가 발생했습니다.');
        } finally {
            okBtn.disabled = false;
        }
    }); 
}

// ========================
// DOMContentLoaded 진입점
// ========================
document.addEventListener('DOMContentLoaded', function () {
    bindEventHandlers();
    loadAndRender({});
    bindUpdatePopupEvents();
});
