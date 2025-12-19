const API_URL = '/api/admin/form-templates/all';

const approvalStatusMap = {
    DRAFT:    { text: '초안',   class: 'status-badge status-pending' },
    ACTIVE:   { text: '활성',   class: 'status-badge status-approved' },
    INACTIVE: { text: '비활성', class: 'status-badge status-rejected' }
};

let approvalState = {
    keyword: '',
    status: '',
    page: 0,
    pageSize: 15
};

// fetch -> apiFetch 치환
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
        tr.addEventListener('click', () => {
            console.log('결재 양식 상세로 이동:', doc.formTemplateId);
        });
        tr.innerHTML = `
            <td class="col-title">${escapeHTML(doc.formTemplateGroupName)}</td>
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

function showCreatePopup(initName = '', initDesc = '') {
    document.getElementById('popupTemplateName').value = initName;
    document.getElementById('popupTemplateDesc').value = initDesc;
    document.getElementById('createPopup').style.display = 'flex';
}
function hideCreatePopup() {
    document.getElementById('createPopup').style.display = 'none';
}

/**
 * 선택한 양식 그룹(groupId)을 기반으로 결재 양식 초안 생성
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

function bindEventHandlers() {
    document.getElementById('statusFilter').addEventListener('change', function () {
        approvalState.status = this.value;
        approvalState.page = 0;
        loadAndRender({ status: this.value, page: 0 });
    });
    const keywordInput = document.getElementById('keywordInput');
    document.getElementById('searchBtn').addEventListener('click', function () {
        approvalState.keyword = keywordInput.value;
        approvalState.page = 0;
        loadAndRender({ keyword: keywordInput.value, page: 0 });
    });
    keywordInput.addEventListener('keyup', function (e) {
        if (e.key === 'Enter') {
            approvalState.keyword = this.value;
            approvalState.page = 0;
            loadAndRender({ keyword: this.value, page: 0 });
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
    document.getElementById('popupCreateBtn').addEventListener('click', async function () {
        const name = document.getElementById('popupTemplateName').value.trim();
        const desc = document.getElementById('popupTemplateDesc').value.trim();

        if (!name) {
            alert('문서 양식명을 입력해주세요.');
            return;
        }

        try {
            const btn = document.getElementById('popupCreateBtn');
            btn.disabled = true;

            const res = await apiFetch('/api/admin/form-template-groups', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name,
                    description: desc
                })
            });

            if (!res.ok) {
                const errMsg = (await res.json())?.message || '문서 양식 그룹 생성에 실패했습니다.';
                alert(errMsg);
                return;
            }

            const result = await res.json();

            const groupId = result?.data?.createdFormTemplateGroupId;

            if (!groupId) {


                alert('생성된 양식 그룹 ID를 가져오지 못했습니다.');
                return;
            }

            const templateId = await createFormTemplateByGroupId(
                groupId,
                "GENERAL"
            );


            hideCreatePopup();

            window.location.href = `/view/admin/create-template?groupId=${groupId}&templateId=${templateId}`;

        } catch (e) {
            console.error(e);
            alert('문서 양식 그룹 생성 중 오류가 발생했습니다.');
        } finally {
            document.getElementById('popupCreateBtn').disabled = false;
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    bindEventHandlers();
    loadAndRender({});
    bindUpdatePopupEvents();
});

// [추가: 결재 양식 업데이트 팝업 로직]
let updatePopupDebounce;

function showUpdatePopup() {
    document.getElementById('updatePopup').style.display = 'flex';
    document.getElementById('updateTemplateSearch').value = '';
    document.getElementById('updateDropdown').style.display = 'none';
}
function hideUpdatePopup() {
    // 양식 설명 영역 비우기 (팝업 닫을 때마다)
    const descTextEl = document.getElementById('updateSelectedDescText');
    if (descTextEl) descTextEl.textContent = '';

    document.getElementById('updatePopup').style.display = 'none';
    document.getElementById('updateDropdown').style.display = 'none';
}
async function fetchUpdateTemplates(keyword) {
    if (!keyword) return [];
    const url = '/api/form-templates/active?keyword=' + encodeURIComponent(keyword);
    const res = await apiFetch(url, {});
    if (!res.ok) return [];
    const result = await res.json();
    if (Array.isArray(result.data)) return result.data;
    if (result.data && Array.isArray(result.data.content)) return result.data.content;
    return [];
}
let lastUpdateDropdownItems = [];
let lastUpdateDropdownIdMap = {};
let lastSelectedUpdateId = null;

function renderUpdateDropdown(items, inputValue) {
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
        let name = item.formTemplateGroupName
            || item.FormTemplateGroupName
            || item.formTemplateName
            || item.FormTemplateName
            || item.name
            || '';
        let groupId = item.formTemplateGroupId || item.FormTemplateGroupId || item.id;
        div.textContent = name;
        if (typeof groupId !== 'undefined') div.dataset.groupId = groupId;
        lastUpdateDropdownIdMap[name] = groupId;
        if (div.textContent === inputValue) div.classList.add('selected');
        div.tabIndex = 0;
        div.addEventListener('mousedown', function() {
            document.getElementById('updateTemplateSearch').value = div.textContent;
            lastSelectedUpdateId = groupId;
            dropdown.style.display = 'none';
            showSelectedUpdateDescription(groupId);
        });
        dropdown.appendChild(div);
    });

// 입력에 일치하는 값이 있으면 description 즉시 요청/출력
    const autoId = lastUpdateDropdownIdMap[inputValue];
    if (autoId) {
        lastSelectedUpdateId = autoId;
        showSelectedUpdateDescription(autoId);
    }
    dropdown.style.display = 'block';
}
async function showSelectedUpdateDescription(groupId) {
    const descBoxEl = document.getElementById('updateSelectedDesc');
    const descTextEl = document.getElementById('updateSelectedDescText');
    if (!descBoxEl || !descTextEl || !groupId) return;
    descTextEl.textContent = '조회 중...';
    try {
        const res = await apiFetch(`/api/form-template-groups/${groupId}`);
        if (res.ok) {
            const result = await res.json();
            // description or fallback 처리
            let desc = (result.data && result.data.description) ||
                       (result.data && result.data.desc) ||
                       (result.data && result.data.summary) || '';
            descTextEl.textContent = desc || '(설명 없음)';
        } else {
            descTextEl.textContent = '(설명 조회 실패)';
        }
    } catch (e) {
        descTextEl.textContent = '(설명 조회 실패)';
    }
}

function bindUpdatePopupEvents() {
    document.getElementById('updateTemplateBtn').addEventListener('click', showUpdatePopup);
    document.getElementById('popupUpdateCancelBtn').addEventListener('click', hideUpdatePopup);
    if (document.getElementById('popupUpdateOkBtn')) {
        document.getElementById('popupUpdateOkBtn').addEventListener('click', function() {
            const selectedName = document.getElementById('updateTemplateSearch').value;
            if (!selectedName) {
                alert('업데이트할 양식을 선택해주세요.');
                return;
            }
            // TODO: 실제 업데이트 처리 로직 구현 위치
            console.log('업데이트 요청:', selectedName);
            hideUpdatePopup();
        });
    }
    document.getElementById('updatePopup').addEventListener('click', function(e) {
        if (e.target === this) hideUpdatePopup();
    });
    const input = document.getElementById('updateTemplateSearch');
    const dropdown = document.getElementById('updateDropdown');
    input.addEventListener('input', function() {
        clearTimeout(updatePopupDebounce);
        const keyword = input.value.trim();
        if (!keyword) {
            dropdown.style.display = 'none';
            const descTextEl = document.getElementById('updateSelectedDescText');
            if (descTextEl) descTextEl.textContent = '';
            return;
        }
        updatePopupDebounce = setTimeout(async () => {
            const items = await fetchUpdateTemplates(keyword);
            renderUpdateDropdown(items, input.value);
        }, 500);
    });
    input.addEventListener('focus', function() {
        const keyword = input.value.trim();
        if (keyword) fetchUpdateTemplates(keyword).then(items => renderUpdateDropdown(items, input.value));
    });
    input.addEventListener('blur', function() {
        setTimeout(() => { dropdown.style.display = 'none'; }, 180);
    });
    // 인풋 변경 시 자동으로 descr 업데이트 자동 요청
    input.addEventListener('change', function() {
        const groupId = lastUpdateDropdownIdMap[input.value];
        if (groupId) showSelectedUpdateDescription(groupId);
    });



}
