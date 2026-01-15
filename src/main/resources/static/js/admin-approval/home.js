// ========================
// 상수 및 상태 변수
// ========================
const API_URL = '/api/admin/form-templates/all';

const approvalStatusMap = {
    DRAFT:    { text: '임시',   class: 'status-badge status-pending' },
    ACTIVE:   { text: '활성',   class: 'status-badge status-approved' },
    INACTIVE: { text: '비활성', class: 'status-badge status-rejected' }
};
const BASE_ROLE_OPTIONS = {
    SCHEDULE: [
        {value: 'COMPANY_EVENT', label: '회사 일정'}
    ],
    ATTENDANCE: [
        {value: 'VACATION', label: '휴가'},
        {value: 'BUSINESS_TRIP', label: '출장'},
        {value: 'OUTWORK', label: '외근'}
    ]
};

let approvalState = {
    keyword: '',
    status: '',
    categoryCode: '',
    page: 0,
    pageSize: 10
};

let currentPage = 0;
let totalPages = 1;
let isTemplateNameAvailable = false;
let nameCheckDebounce = null;
let isCheckingTemplateName = false;

let templateNameCheckState = 'idle';
// idle | checking | valid | invalid


// ========================
// 업데이트 팝업 상태
// ========================
let updatePopupState = {
    groupId: null,
    groupActive: false,        // dto.active
    hasActiveTemplate: false,  // dto.hasActiveTemplate
    isEditMode: false
};



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
const popupCategory = document.getElementById('popupCategory');
const popupBaseRole = document.getElementById('popupBaseRole');

const popupCategoryMsg =
    popupCategory
        ? popupCategory.closest('.modal-form-group')?.querySelector('.hint')
        : null;

const popupBaseRoleMsg =
    popupBaseRole
        ? popupBaseRole.closest('.modal-form-group')?.querySelector('.hint')
        : null;


// 힌트 엘리먼트
const popupTemplateNameMsg =
    popupTemplateName
        ? popupTemplateName.closest('.modal-form-group')?.querySelector('.hint')
        : null;

const popupTemplateDescMsg =
    popupTemplateDesc
        ? popupTemplateDesc.closest('.modal-form-group')?.querySelector('.hint')
        : null;

const updateTemplateSearchMsg =
    document.getElementById('updateTemplateSearchHint');
const keywordInputMsg =
    keywordInput
        ? keywordInput.closest('.search-group')?.querySelector('.hint')
        : null;


function handleCategoryChange() {
    if (!popupCategory || !popupBaseRole) return;

    const category = popupCategory.value;

    // 초기화
    popupBaseRole.innerHTML = '<option value="">선택하세요</option>';
    popupBaseRole.value = '';
    popupBaseRole.disabled = true;

    const options = BASE_ROLE_OPTIONS[category];
    if (!options) {
        // GENERAL 등 → 일정 유형 사용 안 함
        if (popupBaseRoleMsg) popupBaseRoleMsg.textContent = '';
        return;
    }

    // 옵션 채우기
    options.forEach(opt => {
        const optionEl = document.createElement('option');
        optionEl.value = opt.value;
        optionEl.textContent = opt.label;
        popupBaseRole.appendChild(optionEl);
    });

    popupBaseRole.disabled = false;
}


function initPopupMaxLength() {
    if (popupTemplateName) {
        popupTemplateName.setAttribute('maxlength', TEMPLATE_NAME_MAX);
    }

    if (popupTemplateDesc) {
        popupTemplateDesc.setAttribute('maxlength', TEMPLATE_DESC_MAX);
    }
}


function validateCreatePopup() {
    let valid = true;

    if (!validateTemplateName()) valid = false;
    if (!validateTemplateDesc()) valid = false;

    if (!popupCategory.value) {
        showMsg(popupCategoryMsg, '카테고리를 선택해주세요.', 'error');
        valid = false;
    }

    if (
        (popupCategory.value === 'SCHEDULE' ||
            popupCategory.value === 'ATTENDANCE') &&
        !popupBaseRole.value
    ) {
        showMsg(popupBaseRoleMsg, '일정 유형을 선택해주세요.', 'error');
        valid = false;
    }

    return valid;
}


// ========================
// API 호출 함수
// ========================
async function fetchApprovalDocs({
                                     status,
                                     keyword,
                                     categoryCode,
                                     page,
                                     pageSize
                                 }) {
    const params = new URLSearchParams();

    if (typeof page === 'number') params.set('offset', page);
    if (typeof pageSize === 'number') params.set('size', pageSize);
    if (status) params.set('status', status);
    if (keyword) params.set('keyword', keyword);
    if (categoryCode) params.set('templateCategoryCode', categoryCode);

    const url = API_URL + '?' + params.toString();

    const res = await apiFetch(url, {method: 'GET'});

    if (!res.ok) throw new Error('데이터 불러오기 실패');

    const result = await res.json();
    return result?.data ?? {content: [], totalPages: 1, number: 0};
}


// 업데이트 팝업 검색 ajax
async function fetchUpdateTemplates(keyword) {
    const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
    const res = await apiFetch(`/api/form-template-groups${query}`);
    if (!res.ok) return [];

    const result = await res.json();
    return Array.isArray(result.data) ? result.data : [];
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

        const isDraft = doc.formTemplateStatus === 'DRAFT';

        if (doc.formTemplateStatus === 'ACTIVE') {
            tr.classList.add('row-active');
        } else if (doc.formTemplateStatus === 'INACTIVE') {
            tr.classList.add('row-inactive');
        } else if (isDraft) {
            tr.classList.add('row-draft');
        }

        tr.style.cursor = 'pointer';

        if (!isDraft) {
            tr.classList.add('row-view-only');
        }

        // 행 클릭 (삭제 버튼 클릭 제외)
        tr.addEventListener('click', (e) => {
            if (e.target.closest('.row-delete-btn')) return;

            const templateId = doc.formTemplateId;
            if (!templateId) return;

            if (isDraft) {
                window.location.href =
                    `/view/admin/create-template?templateId=${templateId}`;
            } else {
                window.location.href =
                    `/view/admin/preview-template/${templateId}`;
            }
        });

        tr.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                tr.click();
            }
        });

        tr.innerHTML = `
            <!-- 1. 양식명 -->
            <td class="col-title" title="${escapeHTML(doc.formTemplateGroupName)}">
                <div class="title-wrap">
                    <span class="title-text">
                        ${escapeHTML(truncateText(doc.formTemplateGroupName, LIST_TITLE_MAX))}
                    </span>
                    ${renderActionIcon(doc.formTemplateStatus)}
                </div>
            </td>

            <!-- 2. 카테고리 -->
            <td class="col-category">
                ${renderTemplateCategoryText(doc.templateCategoryCode)}
            </td>

            <!-- 3. 버전 -->
            <td class="col-version">${doc.formTemplateVersion}</td>

            <!-- 4. 사용 문서 수 -->
            <td class="col-usedoc">${doc.useDocument || 0}</td>

            <!-- 5. 근태 연동 -->
            <td class="col-attend">
                ${renderAttend(doc.templateCategoryCode)}
            </td>

            <!-- 6. 일정 연동 -->
            <td class="col-schedule">
                ${renderSchedule(doc.templateCategoryCode)}
            </td>

            <!-- 7. 상태 -->
            <td class="col-status">
                ${renderStatusBadge(doc.formTemplateStatus)}
            </td>

            <!-- 8. 최종 수정일 + 삭제 버튼 -->
            <td class="col-updated progress-cell ellipsis">
                <span class="progress-text">
                    ${formatDateTime(doc.updatedAt)}
                </span>
                ${
            isDraft
                ? `<button
                               type="button"
                               class="row-delete-btn"
                               data-id="${doc.formTemplateId}">
                               삭제
                           </button>`
                : ''
        }
            </td>
        `;

        tbody.appendChild(tr);
    });
}

function bindDraftTemplateDeleteEvent() {
    const tbody = document.getElementById('approvalTableBody');
    if (!tbody) return;

    tbody.addEventListener('click', (e) => {
        const btn = e.target.closest('.row-delete-btn');
        if (!btn) return;

        e.stopPropagation();

        const templateId = btn.dataset.id;
        if (!templateId) return;

        confirmDeleteDraftTemplate(templateId);
    });
}

async function confirmDeleteDraftTemplate(templateId) {
    const result = await sweetConfirm(
        '임시 양식 삭제',
        '삭제한 양식은 복구할 수 없습니다.'
    );

    if (!result.isConfirmed) return;

    try {
        const res = await apiFetch(
            `/api/admin/form-templates/draft/${templateId}`,
            {method: 'DELETE'}
        );

        // ❌ 실패 응답
        if (!res.ok) {
            const body = await res.json().catch(() => ({}));
            const message =
                body?.message ||
                '양식을 삭제할 수 없습니다.';

            // 정책 위반 → warning
            if (res.status === 400) {
                await Swal.fire({
                    icon: 'warning',
                    title: '삭제 불가',
                    text: message
                });
                return;
            }

            // 그 외 → error
            throw new Error(message);
        }

        // ✅ 성공
        await Swal.fire({
            icon: 'success',
            title: '삭제 완료',
            text: '임시 결재 양식이 삭제되었습니다.',
            timer: 1200,
            showConfirmButton: false
        });

        // 목록 갱신 (현재 페이지 유지)
        loadAndRender({page: approvalState.page});

    } catch (e) {
        await Swal.fire({
            icon: 'error',
            title: '삭제 실패',
            text: e.message || '삭제 중 오류가 발생했습니다.'
        });
    }
}


function renderUpdateDropdown(items) {
    const dropdown = document.getElementById('updateDropdown');
    dropdown.innerHTML = '';

    if (!items || items.length === 0) {
        dropdown.style.display = 'none';
        return;
    }

    items.forEach(item => {
        const div = document.createElement('div');
        div.className = 'modal-dropdown-item';

        const groupStatusClass = item.active ? 'active' : 'inactive';
        const groupStatusText = item.active ? '활성' : '비활성';

        const docStatusClass = item.hasActiveTemplate ? 'ok' : 'none';
        const docStatusText = item.hasActiveTemplate ? '사용 중' : '미사용';

        div.innerHTML = `
            <div class="dropdown-item-inner">
                <span class="dropdown-title">
                    ${escapeHTML(truncateText(item.name, TEMPLATE_NAME_MAX))}
                </span>
                <div class="dropdown-meta">
                    <span class="group-status ${groupStatusClass}">
                        ${groupStatusText}
                    </span>
                    <span class="doc-status ${docStatusClass}">
                        ${docStatusText}
                    </span>
                </div>
            </div>
        `;

        div.addEventListener('mousedown', () => {
            updateTemplateSearch.value = item.name;

            updatePopupState.groupId = item.id;
            updatePopupState.groupActive = !!item.active;
            updatePopupState.hasActiveTemplate = !!item.hasActiveTemplate;
            updatePopupState.isEditMode = false;

            const descEl = document.getElementById('updateSelectedDescText');
            descEl.textContent = item.description || '설명이 없습니다.';
            descEl.contentEditable = 'false';

            const toggleEl = document.getElementById('groupStatusToggle');
            toggleEl.checked = updatePopupState.groupActive;
            toggleEl.disabled = true;

            dropdown.style.display = 'none';

            updateUpdateButtonState();
        });


        dropdown.appendChild(div);
    });

    dropdown.style.display = 'block';
}

function renderTemplateCategoryText(code) {
    switch (code) {
        case 'GENERAL':
            return '일반';
        case 'ATTENDANCE':
            return '근태';
        case 'SCHEDULE':
            return '일정';
        default:
            return '-';
    }
}

function formatDateTime(isoString) {
    const d = new Date(isoString);

    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');

    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
}


function renderActionIcon(status) {
    if (status === 'DRAFT') {
        return `<span class="action-icon edit-icon" title="편집">✏️</span>`;
    }
    return `<span class="action-icon view-icon" title="상세 보기">🔍</span>`;
}

function getApplyStatusByCategory(categoryCode) {
    if (!categoryCode) {
        return {schedule: false, attendance: false};
    }

    switch (categoryCode) {
        case 'ATTENDANCE':
            return {schedule: true, attendance: true};

        case 'SCHEDULE':
            return {schedule: true, attendance: false};

        case 'GENERAL':
        default:
            return {schedule: false, attendance: false};
    }
}


function renderAttend(categoryCode) {
    const {attendance} = getApplyStatusByCategory(categoryCode);
    return attendance
        ? '<span class="tag-on">O</span>'
        : '<span class="tag-off">X</span>';
}

function renderSchedule(categoryCode) {
    const {schedule} = getApplyStatusByCategory(categoryCode);
    return schedule
        ? '<span class="tag-on">O</span>'
        : '<span class="tag-off">X</span>';
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
// 페이지네이션
// ========================
function renderPagination(page) {
    const pagination = document.getElementById('approvalPagination');
    pagination.innerHTML = '';

    // 이전
    const prev = document.createElement('button');
    prev.className = 'page-btn';
    prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prev.disabled = page === 0;
    prev.onclick = () => loadAndRender({page: page - 1});
    pagination.appendChild(prev);

    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);

    if (end - start < maxVisible - 1) {
        start = Math.max(0, end - maxVisible + 1);
    }

    if (start > 0) {
        addPageBtn(pagination, 0);
        if (start > 1) addEllipsis(pagination);
    }

    for (let i = start; i <= end; i++) {
        addPageBtn(pagination, i, i === page);
    }

    if (end < totalPages - 1) {
        if (end < totalPages - 2) addEllipsis(pagination);
        addPageBtn(pagination, totalPages - 1);
    }

    // 다음
    const next = document.createElement('button');
    next.className = 'page-btn';
    next.innerHTML = '<i class="fas fa-chevron-right"></i>';
    next.disabled = page >= totalPages - 1;
    next.onclick = () => loadAndRender({page: page + 1});
    pagination.appendChild(next);
}


function addPageBtn(container, page, active = false) {
    const btn = document.createElement('button');
    btn.className = 'page-number';
    if (active) btn.classList.add('active');
    btn.textContent = page + 1;
    btn.onclick = () => loadAndRender({page});
    container.appendChild(btn);
}

function addEllipsis(container) {
    const span = document.createElement('span');
    span.className = 'ellipsis';
    span.textContent = '...';
    container.appendChild(span);
}


async function loadAndRender(override = {}) {
    const req = {
        keyword: override.keyword !== undefined
            ? override.keyword
            : approvalState.keyword,

        status: override.status !== undefined
            ? override.status
            : approvalState.status,

        categoryCode: override.categoryCode !== undefined
            ? override.categoryCode
            : approvalState.categoryCode,

        page: override.page !== undefined
            ? override.page
            : approvalState.page,

        pageSize: override.pageSize !== undefined
            ? override.pageSize
            : approvalState.pageSize
    };

    try {
        const {
            content = [],
            totalPages: tp = 1,
            number = 0
        } = await fetchApprovalDocs(req);

        // ✅ inbox와 동일한 전역 상태 갱신
        currentPage = number;
        totalPages = tp;

        renderTableRows(content);
        renderPagination(currentPage);

        // 검색 상태 유지를 위해 page만 반영
        approvalState.page = number;

    } catch (e) {
        currentPage = 0;
        totalPages = 1;

        renderTableRows([]);
        renderPagination(0);

        console.error('[fetch error]', e);
    }
}


// ========================
// 팝업 (생성/업데이트) 열기/닫기 함수
// ========================
function showCreatePopup(initName = '', initDesc = '') {
    const dom = getCreatePopupDom();
    if (!dom) return;

    const {name, desc, createBtn, category, baseRole} = dom;

    name.value = initName;
    desc.value = initDesc;

    category.value = '';
    baseRole.value = '';
    baseRole.disabled = true;

    clearHint(popupTemplateNameMsg);
    clearHint(popupTemplateDescMsg);
    clearHint(popupCategoryMsg);
    clearHint(popupBaseRoleMsg);

    createBtn.disabled = true;

    document.getElementById('createPopup').style.display = 'flex';
    templateNameCheckState = 'idle';
    isTemplateNameAvailable = false;
    clearTimeout(nameCheckDebounce);
}


function hideCreatePopup() {
    document.getElementById('createPopup').style.display = 'none';
}

function showUpdatePopup() {
    lastSelectedUpdateTemplateId = null;
    lastSelectedUpdateId = null;

    // 팝업 표시
    document.getElementById('updatePopup').style.display = 'flex';

    // 입력/드롭다운 초기화
    updateTemplateSearch.value = '';
    document.getElementById('updateDropdown').style.display = 'none';

    // 상태 초기화
    updatePopupState.groupId = null;
    updatePopupState.groupActive = false;
    updatePopupState.hasActiveTemplate = false;
    updatePopupState.isEditMode = false;

    // 설명 초기화
    const descEl = document.getElementById('updateSelectedDescText');
    descEl.textContent = '선택된 양식의 설명이 표시됩니다.';
    descEl.contentEditable = 'false';

    // 토글 초기화
    const toggleEl = document.getElementById('groupStatusToggle');
    toggleEl.checked = false;
    toggleEl.disabled = true;

    // 버튼 상태 반영
    updateUpdateButtonState();

    // 포커스
    updateTemplateSearch.focus();
}


function hideUpdatePopup() {
    clearTimeout(updatePopupDebounce);

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
async function checkTemplateNameDuplicate(name) {
    const res = await apiFetch(
        `/api/admin/form-template-groups/check-name?name=${encodeURIComponent(name)}`,
        {method: 'GET'}
    );

    if (!res.ok) {
        throw new Error('중복 체크 실패');
    }

    const result = await res.json();

   return result?.data
}


function validateTemplateName(showSuccess = true) {
    const v = popupTemplateName.value.trim();

    if (!v) {
        templateNameCheckState = 'idle';
        showMsg(popupTemplateNameMsg, '양식명을 입력해주세요.', 'error');
        return false;
    }

    if (v.length > TEMPLATE_NAME_MAX) {
        templateNameCheckState = 'idle';
        showMsg(
            popupTemplateNameMsg,
            `양식명은 ${TEMPLATE_NAME_MAX}자 이내여야 합니다.`,
            'error'
        );
        return false;
    }

    if (templateNameCheckState === 'checking') {
        showMsg(
            popupTemplateNameMsg,
            '양식명 중복 확인 중입니다...',
            'info'
        );
        return false;
    }

    if (templateNameCheckState === 'invalid') {
        showMsg(
            popupTemplateNameMsg,
            '이미 사용 중인 양식명입니다.',
            'error'
        );
        return false;
    }

    if (templateNameCheckState === 'valid' && showSuccess) {
        showMsg(
            popupTemplateNameMsg,
            '사용 가능한 양식명입니다.',
            'success'
        );
        return true;
    }

    return false;
}


function validateTemplateDesc() {
    const v = popupTemplateDesc.value.trim();

    if (!v) {
        showMsg(
            popupTemplateDescMsg,
            '설명을 입력해주세요.',
            'error'
        );
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
// 이벤트 바인딩 메인/팝업
// ========================
function bindEventHandlers() {
    /* ======================
       Create Popup 전용
    ====================== */
    if (popupTemplateName && popupTemplateDesc && popupCreateBtn) {
        popupTemplateName.addEventListener('input', () => {
            enforceMaxLength(popupTemplateName, TEMPLATE_NAME_MAX);

            clearTimeout(nameCheckDebounce);

            const name = popupTemplateName.value.trim();

            // 1. 기본 검증 실패 → idle
            if (!name || name.length > TEMPLATE_NAME_MAX) {
                templateNameCheckState = 'idle';
                isTemplateNameAvailable = false;

                validateTemplateName(false);
                updateCreateButtonState();
                return;
            }

            // 2. 서버 체크 시작
            templateNameCheckState = 'checking';
            isTemplateNameAvailable = false;

            showMsg(
                popupTemplateNameMsg,
                '양식명 중복 확인 중입니다...',
                'info'
            );

            nameCheckDebounce = setTimeout(async () => {
                try {
                    const currentValue = name;
                    const available = await checkTemplateNameDuplicate(name);

                    // 🔒 race-condition 방어
                    if (popupTemplateName.value.trim() !== currentValue) return;

                    if (available) {
                        templateNameCheckState = 'valid';
                        isTemplateNameAvailable = true;
                    } else {
                        templateNameCheckState = 'invalid';
                        isTemplateNameAvailable = false;
                    }

                } catch (e) {
                    templateNameCheckState = 'invalid';
                    isTemplateNameAvailable = false;
                } finally {
                    validateTemplateName(true);
                    updateCreateButtonState();
                }
            }, 300);
        });


        popupTemplateDesc.addEventListener('input', () => {
            enforceMaxLength(popupTemplateDesc, TEMPLATE_DESC_MAX);
            updateCreateButtonState();
        });

        popupCategory?.addEventListener('change', () => {
            handleCategoryChange();

            clearHint(popupCategoryMsg);
            clearHint(popupBaseRoleMsg);

            updateCreateButtonState();
        });


        popupBaseRole?.addEventListener('change', () => {
            clearHint(popupBaseRoleMsg);
            updateCreateButtonState();
        });

        document
            .getElementById('popupCancelBtn')
            ?.addEventListener('click', hideCreatePopup);

        document
            .getElementById('popupCreateBtn')
            ?.addEventListener('click', async function () {

                if (!validateCreatePopup()) return;

                const name = popupTemplateName.value.trim();
                const desc = popupTemplateDesc.value.trim();
                const categoryCode = popupCategory.value;
                const baseRole =
                    categoryCode === 'SCHEDULE'
                        ? popupBaseRole.value
                        : null;

                try {
                    popupCreateBtn.disabled = true;

                    // 1️⃣ 그룹 생성 (수정본)
                    const res = await apiFetch('/api/admin/form-template-groups', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({
                            name,
                            description: desc,
                            categoryCode,
                            baseRole: popupBaseRole.disabled ? null : popupBaseRole.value
                        })
                    });


                    if (!res.ok) {
                        const errMsg =
                            (await res.json())?.message ||
                            '문서 양식 그룹 생성에 실패했습니다.';
                        await sweetWarning(errMsg);
                        return;
                    }

                    const result = await res.json();
                    const groupId = result?.data?.createdFormTemplateGroupId;
                    if (!groupId) {
                        await sweetWarning('생성된 양식 그룹 ID를 가져오지 못했습니다.');
                        return;
                    }


                    // 2️⃣ 템플릿 생성 (수정본)
                    const res2 = await apiFetch(
                        `/api/admin/form-templates?templateGroupId=${groupId}`,
                        {method: 'POST'}
                    );


                    if (!res2.ok) {
                        const err = await res2.json().catch(() => ({}));
                        throw new Error(err?.message || '결재 양식 생성 실패');
                    }

                    const result2 = await res2.json();
                    const templateId = result2?.data?.formTemplateId;

                    hideCreatePopup();
                    window.location.href =
                        `/view/admin/create-template?templateId=${templateId}`;

                } catch (e) {
                    console.error(e);
                    await sweetWarning(e.message || '문서 양식 생성 중 오류가 발생했습니다.');
                } finally {
                    updateCreateButtonState();
                }
            });
    }

    /* ======================
       검색 / 페이징 (공통)
    ====================== */
    keywordInput?.addEventListener('input', () => {
        enforceMaxLength(keywordInput, TEMPLATE_NAME_MAX);
        validateKeywordSearch();
    });

    document
        .getElementById('statusFilter')
        ?.addEventListener('change', function () {
            approvalState.status = this.value;
            approvalState.page = 0;
            loadAndRender({status: this.value, page: 0});
        });

    document
        .getElementById('categoryFilter')
        ?.addEventListener('change', function () {
            approvalState.categoryCode = this.value;
            approvalState.page = 0;

            loadAndRender({
                categoryCode: this.value,
                page: 0
            });
        });

    document
        .getElementById('searchBtn')
        ?.addEventListener('click', function () {
            const keyword = keywordInput.value.trim();
            if (keyword && !validateKeywordSearch()) return;

            approvalState.keyword = keyword;
            approvalState.page = 0;
            loadAndRender({keyword, page: 0});
            clearHint(keywordInputMsg);
        });

    keywordInput?.addEventListener('keyup', function (e) {
        if (e.key !== 'Enter') return;

        const keyword = this.value.trim();
        if (keyword && !validateKeywordSearch()) return;

        approvalState.keyword = keyword;
        approvalState.page = 0;
        loadAndRender({keyword, page: 0});
        clearHint(keywordInputMsg);
    });


    document
        .getElementById('createTemplateBtn')
        ?.addEventListener('click', () => showCreatePopup());


    document
        .getElementById('updateTemplateBtn')
        ?.addEventListener('click', showUpdatePopup);
}

function bindUpdatePopupEvents() {
    const input = document.getElementById('updateTemplateSearch');
    const dropdown = document.getElementById('updateDropdown');
    const cancelBtn = document.getElementById('popupUpdateCancelBtn');
    const okBtn = document.getElementById('popupUpdateOkBtn');

    if (!input || !dropdown || !cancelBtn || !okBtn) return;

    // 닫기
    cancelBtn.addEventListener('click', hideUpdatePopup);

    /* =========================
       ⭐ focus: 입력 없어도 최신 10개
       ========================= */
    input.addEventListener('focus', async () => {
        clearTimeout(updatePopupDebounce);

        const items = await fetchUpdateTemplates(null);
        renderUpdateDropdown(items);
    });

    /* =========================
       input: 검색 or 전체
       ========================= */
    input.addEventListener('input', () => {
        enforceMaxLength(input, UPDATE_SEARCH_MAX);
        validateUpdateTemplateSearch();

        clearTimeout(updatePopupDebounce);
        const keyword = input.value.trim();

        updatePopupDebounce = setTimeout(async () => {
            const items = await fetchUpdateTemplates(keyword || null);
            renderUpdateDropdown(items);
        }, 300);
    });

    /* =========================
       blur: 드롭다운 닫기
       ========================= */
    input.addEventListener('blur', (e) => {
        setTimeout(() => {
            if (!dropdown.matches(':hover')) {
                dropdown.style.display = 'none';
            }
        }, 150);
    });


    /* =========================
       업데이트 실행
       ========================= */
    okBtn.addEventListener('click', async () => {
        if (!updatePopupState.groupId) {
            await sweetWarning('업데이트할 문서를 선택해주세요.');
            return;
        }

        try {
            okBtn.disabled = true;

            const res = await apiFetch(
                `/api/admin/form-templates/${updatePopupState.groupId}/revise`,
                {method: 'POST'}
            );

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err?.message || '문서 업데이트 실패');
            }

            const result = await res.json();
            const createdTemplateId = result?.data?.createdTemplateId;

            if (!createdTemplateId) {
                throw new Error('생성된 문서 ID 없음');
            }

            hideUpdatePopup();
            window.location.href =
                `/view/admin/create-template?templateId=${createdTemplateId}`;

        } catch (e) {
            console.error(e);
            await sweetWarning(e.message || '문서 업데이트 중 오류');
        } finally {
            okBtn.disabled = false;
        }
    });
}

function updateCreateButtonState() {
    const dom = getCreatePopupDom();
    if (!dom) return;

    dom.createBtn.disabled = !canEnableCreateButton();
}


function canEnableUpdateButton() {
    return (
        updatePopupState.groupActive === true &&
        updatePopupState.isEditMode === false
    );
}


function bindUpdateEditButton() {
    const editBtn = document.getElementById('popupEditBtn');
    const descEl = document.getElementById('updateSelectedDescText');
    const toggleEl = document.getElementById('groupStatusToggle');
    const searchInput = document.getElementById('updateTemplateSearch');

    editBtn.addEventListener('click', async () => {
        if (!updatePopupState.groupId) return;

        /* ======================
           수정 진입
        ====================== */
        if (!updatePopupState.isEditMode) {
            updatePopupState.isEditMode = true;

            descEl.contentEditable = 'true';
            toggleEl.disabled = false;
            editBtn.textContent = '수정 완료';

            // ⭐ 양식 이름 검색 비활성
            if (searchInput) {
                searchInput.disabled = true;
            }

            updateUpdateButtonState();
            return;
        }

        /* ======================
           수정 완료
        ====================== */
        try {
            const newDesc = descEl.innerText.trim();
            const newActive = toggleEl.checked;

            const res = await apiFetch(
                `/api/admin/form-template-groups/${updatePopupState.groupId}`,
                {
                    method: 'PATCH',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        description: newDesc,
                        active: newActive
                    })
                }
            );

            if (!res.ok) {
                throw new Error('PATCH failed');
            }

            // 상태 반영
            updatePopupState.groupActive = newActive;
            updatePopupState.isEditMode = false;

            // UI 복구
            descEl.contentEditable = 'false';
            toggleEl.disabled = true;
            editBtn.textContent = '수정';

            // ⭐ 검색 input 다시 활성
            if (searchInput) {
                searchInput.disabled = false;
            }

            updateUpdateButtonState();

        } catch (e) {
            await sweetWarning('수정 중 오류가 발생했습니다.');
            console.error(e);

            updatePopupState.isEditMode = false;

            descEl.contentEditable = 'false';
            toggleEl.disabled = true;
            editBtn.textContent = '수정';

            if (searchInput) {
                searchInput.disabled = false;
            }

            updateUpdateButtonState();
        }
    });
}


function updateUpdateButtonState() {
    const btn = document.getElementById('popupUpdateOkBtn');
    if (!btn) return;

    btn.disabled = !canEnableUpdateButton();
}


function canEnableCreateButton() {
    const category = popupCategory?.value;
    const baseRole = popupBaseRole?.value;

    if (templateNameCheckState !== 'valid') return false;

    if (!category) return false;

    if (category === 'GENERAL') return true;

    return !!baseRole;
}


function getCreatePopupDom() {
    const name = document.getElementById('popupTemplateName');
    const desc = document.getElementById('popupTemplateDesc');
    const createBtn = document.getElementById('popupCreateBtn');
    const category = document.getElementById('popupCategory');
    const baseRole = document.getElementById('popupBaseRole');

    if (!name || !desc || !createBtn || !category || !baseRole) {
        return null;
    }

    return {name, desc, createBtn, category, baseRole};
}

document
    .getElementById('groupStatusToggle')
    ?.addEventListener('change', () => {
        if (!updatePopupState.isEditMode) return;

        updatePopupState.groupActive = document.getElementById('groupStatusToggle').checked;
        updateUpdateButtonState();
    });


// ========================
// DOMContentLoaded 진입점
// ========================
document.addEventListener('DOMContentLoaded', function () {
    initPopupMaxLength();

    if (popupCategory) {
        handleCategoryChange();
    }

    bindEventHandlers();
    bindUpdatePopupEvents();
    bindUpdateEditButton();
    bindDraftTemplateDeleteEvent();

    loadAndRender({});
});


