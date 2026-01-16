/* ==================================================
   admin-organization.js (FINAL)
   - 정렬 모드 제거
   - 항상 Drag & Drop 가능
   - 형제 단위 정렬 + 서브트리 동반 이동
   - DOM 순서 기준 저장
================================================== */

const API_BASE = '/api/admin/organizations';

let allOrgList = [];
let filteredOrgList = [];
let selectedOrgId = null;
let isEditMode = false;
let isOrderChanged = false;
let sortable = null;
let orgCategories = [];
let orgCategoryMap = new Map();

const expandedSet = new Set();
const MAX_ORG_NAME_LENGTH = 100;


/* ======================
   직책 정책 상태
====================== */
let positionCategories = [];
let selectedPositionIds = new Set();
let currentOrgCategoryId = null;

/* ======================
   Elements
====================== */
const els = {
    tree: () => document.getElementById('orgTree'),
    search: () => document.getElementById('searchKeyword'),
    includeInactive: () => document.getElementById('includeInactive'),
    includeDescendants: () => document.getElementById('includeDescendants'),
    btnSearch: () => document.getElementById('btnSearch'),
    btnCreate: () => document.getElementById('btnOpenCreate'),
    btnSaveOrder: () => document.getElementById('btnSaveOrder'),

    modal: () => document.getElementById('orgModal'),
    modalTitle: () => document.getElementById('modalTitle'),
    orgName: () => document.getElementById('orgName'),
    parentSelect: () => document.getElementById('parentOrgSelect'),
    btnSave: () => document.getElementById('btnSaveOrg'),
    btnCancel: () => document.getElementById('btnModalCancel'),
};

/* ======================
   Init
====================== */
document.addEventListener('DOMContentLoaded', async () => {
    bindEvents();
    bindOrgNameCounter();
    await loadOrgCategories();
    await loadOrganizations();
});


/* ======================
   Load
====================== */
async function loadOrganizations() {
    const includeInactive = els.includeInactive().checked;

    const res = await apiFetch(
        `${API_BASE}/trees?includeInactive=${includeInactive}`
    );

    const json = await res.json();
    allOrgList = json.data || [];
    filteredOrgList = allOrgList;

    expandedSet.clear();
    isAllExpanded = false;
    updateToggleIcon();

    renderTree();
    initSortable();
    resetOrderChanged();
}

function updateToggleIcon() {
    toggleIcon.className = isAllExpanded
        ? 'fa-solid fa-compress-alt'
        : 'fa-solid fa-expand-alt';
}


async function loadOrgCategories() {
    const res = await apiFetch('/api/admin/org-categories/selectable');
    const json = await res.json();
    orgCategories = json.data || [];

    // 검증용 (비활성 포함 전체)
    const res2 = await apiFetch('/api/admin/org-categories?includeInactive=true');
    const json2 = await res2.json();

    orgCategoryMap.clear();
    (json2.data || []).forEach(c => {
        orgCategoryMap.set(c.id, c);
    });

}

/* ======================
   Render Tree
====================== */
function renderTree() {
    const container = els.tree();
    container.innerHTML = '';

    const list = filteredOrgList;

    if (!list.length) {
        container.innerHTML = `
            <div class="org-empty">
                검색 결과가 없습니다.
            </div>
        `;
        return;
    }

    const visibleIds = new Set(list.map(o => o.id));

    // 검색 결과 기준 루트 노드 찾기
    const roots = list.filter(o => !visibleIds.has(o.parentOrgId));

    roots
        .sort((a, b) => a.orderIndex - b.orderIndex)
        .forEach(o => renderNode(o, 0, container));
}


function renderNode(org, depth, container) {
    const hasChildren = filteredOrgList.some(o => o.parentOrgId === org.id);
    const isExpanded = expandedSet.has(org.id);
    const isRoot = org.parentOrgId == null;

    const node = document.createElement('div');
    node.className = 'org-node';
    node.dataset.id = org.id;
    node.dataset.parentId = org.parentOrgId ?? 'root';
    node.dataset.active = normalizeActive(org);
    node.style.marginLeft = `${depth * 20}px`;

    const keyword = els.search().value.trim();

    node.innerHTML = `
        <div class="org-row">
            ${normalizeActive(org) && !isRoot ? dragHandleHtml() : ''}
            ${hasChildren ? `<span class="org-toggle">${isExpanded ? '▾' : '▸'}</span>` : `<span class="org-toggle-placeholder"></span>`}
            <span class="org-label">
                ${highlight(org.name, keyword)}
                <span class="org-meta-item" title="하위 조직 수">${org.childOrgCount ?? 0}</span>
            </span>
                
            <span class="org-meta">
                <span class="org-meta-item" title="소속 사원 수">소속 사원 수: ${org.employeeCount ?? 0}</span>
            </span>
            
            <span class="status-badge ${normalizeActive(org) ? 'status-active' : 'status-inactive'}">
                ${normalizeActive(org) ? '활성' : '비활성'}
            </span>
            
            <button class="table-btn"
                title="${isRoot ? '회사명 수정' : '조직 수정'}">
                수정
            </button>      
        </div>
    `;

    container.appendChild(node);

    const editBtn = node.querySelector('.table-btn');
    editBtn.onclick = () => openEdit(org.id);

    const toggle = node.querySelector('.org-toggle');
    if (toggle) {
        toggle.onclick = e => {
            e.stopPropagation();
            expandedSet.has(org.id) ? expandedSet.delete(org.id) : expandedSet.add(org.id);
            renderTree();
            initSortable();
        };
    }

    if (hasChildren && isExpanded) {
        filteredOrgList
            .filter(o => o.parentOrgId === org.id)
            .sort((a, b) => a.orderIndex - b.orderIndex)
            .forEach(child => renderNode(child, depth + 1, container));
    }
}

/* ======================
   Sortable
====================== */
function initSortable() {
    destroySortable();

    sortable = Sortable.create(els.tree(), {
        handle: '.drag-handle',
        draggable: '.org-node',
        animation: 160,

        onStart(evt) {
            const subtree = collectSubtreeNodes(evt.item);
            evt.item._subtree = subtree;
            subtree.forEach(n => n.classList.add('dragging-subtree'));
        },

        onMove(evt) {
            if (evt.dragged.dataset.parentId === 'root') return false;
            return evt.dragged.dataset.parentId === evt.related?.dataset.parentId;
        },

        onEnd(evt) {
            const subtree = evt.item._subtree || [evt.item];
            subtree.forEach(n => n.classList.remove('dragging-subtree'));

            const anchor = evt.item.nextElementSibling;
            subtree.forEach(n => els.tree().insertBefore(n, anchor));

            markOrderChanged();
        }
    });
}

function destroySortable() {
    if (sortable) {
        sortable.destroy();
        sortable = null;
    }
}

/* ======================
   Save Order
====================== */
async function saveOrder() {
    if (els.search().value.trim()) {
        alert('검색 중에는 순서를 변경할 수 없습니다.');
        return;
    }

    if (!isOrderChanged) return;

    const nodes = [...els.tree().querySelectorAll('.org-node')];
    const groups = {};

    nodes.forEach(n => {
        const pid = n.dataset.parentId ?? 'root';
        groups[pid] ??= [];
        groups[pid].push({id: Number(n.dataset.id)});
    });

    for (const orders of Object.values(groups)) {
        await apiFetch(`${API_BASE}/order`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({orders})
        });
    }

    resetOrderChanged();
    await loadOrganizations();
}

/* ======================
   Events
====================== */
function bindEvents() {
    els.search().addEventListener('input', loadOrganizationsWithSearch);
    els.btnSearch().addEventListener('click', loadOrganizationsWithSearch);
    els.includeDescendants().addEventListener('change', loadOrganizationsWithSearch);
    els.includeInactive().addEventListener('change', loadOrganizationsWithSearch);

    els.btnCreate().addEventListener('click', openCreate);
    els.btnSaveOrder().addEventListener('click', saveOrder);
    els.btnCancel().addEventListener('click', closeModal);
    els.btnSave().addEventListener('click', saveOrg);

    document.getElementById('activeToggle')
        ?.addEventListener('change', () => {
            document.getElementById('orgNameHelp').textContent = '';
        });

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && !els.modal().classList.contains('hidden')) {
            closeModal();
        }
    });
}

/* ======================
   CRUD / Utils
====================== */
function openCreate() {
    isEditMode = false;
    selectedOrgId = null;

    els.modalTitle().textContent = '조직 생성';
    els.orgName().value = '';
    document.getElementById('orgNameCount').textContent = '0';
    document.getElementById('orgNameHelp').textContent = '';
    document.getElementById('inactiveHelp').style.display = 'none';

    buildCategorySelect(orgCategories[0]?.id);
    buildParentSelect(null, {mode: 'create'});

    currentOrgCategoryId = orgCategories[0]?.id;
    loadPositionPolicies(null, currentOrgCategoryId);


    // 생성 시: 카테고리 / 부모조직 활성화
    document.getElementById('categorySelect').classList.remove('disabled');
    document.getElementById('parentOrgSelect').classList.remove('disabled');

    // 생성 시: 사용 여부는 항상 활성 + 잠금
    const toggle = document.getElementById('activeToggle');
    toggle.checked = true;
    toggle.disabled = true;

    document.getElementById('inactiveHelp').style.display = 'none';

    openModal();
}

async function openEdit(id) {
    isEditMode = true;
    document.getElementById('orgNameHelp').textContent = '';
    document.getElementById('inactiveHelp').style.display = 'none';

    const org = allOrgList.find(o => o.id === id);
    if (!org) return;

    const isRootOrg = org.isRootOrg === true;

    selectedOrgId = id;
    currentOrgCategoryId = org.categoryId;

    els.modalTitle().textContent = isRootOrg
        ? '회사명 수정'
        : '조직 수정';

    els.orgName().value = org.name ?? '';
    document.getElementById('orgNameCount').textContent = (org.name ?? '').length;

    buildCategorySelect(org.categoryId);
    buildParentSelect(org.parentOrgId ?? null, {mode: 'edit'});
    document.getElementById('parentOrgSelectValue').value = org.parentOrgId;

    // 회사 루트 조직 전용 제어
    if (isRootOrg) {
        // ===== 조직명 =====
        els.orgName().setAttribute('readonly', true);
        els.orgName().classList.add('readonly-input');

        const nameHelp = document.getElementById('orgNameHelp');
        nameHelp.textContent = '회사명은 조직 관리에서 수정할 수 없습니다.';
        nameHelp.style.display = 'block';

        // ===== 카테고리 / 상위 조직 =====
        document.getElementById('categorySelect').classList.add('disabled');
        document.getElementById('parentOrgSelect').classList.add('disabled');

        const readonlyHelp = document.getElementById('readonlyHelp');
        readonlyHelp.textContent =
            '회사 루트 조직은 조직 카테고리 및 상위 조직을 변경할 수 없습니다.';
        readonlyHelp.style.display = 'block';

        // ===== 활성 여부 =====
        const toggle = document.getElementById('activeToggle');
        toggle.checked = true;
        toggle.disabled = true;

        document.getElementById('inactiveHelp').style.display = 'none';
    } else {
        // 카테고리 / 부모 조직은 수정 불가
        document.getElementById('categorySelect').classList.add('disabled');
        document.getElementById('parentOrgSelect').classList.add('disabled');

        const readonlyHelp = document.getElementById('readonlyHelp');
        readonlyHelp.textContent =
            '조직 카테고리와 상위 조직은 생성 이후 변경할 수 없습니다.';
        readonlyHelp.style.display = 'block';

        const toggle = document.getElementById('activeToggle');
        let inactiveReason = '';

        // 카테고리 기준 활성화 차단
        const category = orgCategoryMap.get(org.categoryId);
        if (category && !category.isActive) {
            inactiveReason =
                '비활성 상태인 조직 카테고리에 속한 조직은 활성화할 수 없습니다. ' +
                '먼저 조직 카테고리를 활성화해주세요.';
        }

        // 상위 조직 기준 활성화 차단
        const parent = allOrgList.find(o => o.id === org.parentOrgId);
        if (!inactiveReason && parent && !normalizeActive(parent)) {
            inactiveReason =
                '상위 조직이 비활성 상태이므로 이 조직을 활성화할 수 없습니다.';
        }

        if (inactiveReason) {
            toggle.checked = false;
            toggle.disabled = true;

            const help = document.getElementById('inactiveHelp');
            help.textContent = inactiveReason;
            help.style.display = 'block';
        } else {
            toggle.checked = normalizeActive(org);
            toggle.disabled = false;
            document.getElementById('inactiveHelp').style.display = 'none';
        }
    }


    await loadPositionPolicies(id, currentOrgCategoryId);
    openModal();
}

async function saveOrg() {
    const name = els.orgName().value.trim();
    const help = document.getElementById('orgNameHelp');

    const categoryValue = document.getElementById('categorySelectValue').value;
    const parentOrgValue = document.getElementById('parentOrgSelectValue').value;

    const org = allOrgList.find(o => o.id === selectedOrgId);
    const activeToggle = document.getElementById('activeToggle');

    // 비활성화 사전 검증 (EDIT 모드)
    if (isEditMode && org && normalizeActive(org) && activeToggle.checked === false) {
        if ((org.childOrgCount ?? 0) > 0) {
            help.textContent = '하위 조직이 존재하여 비활성화할 수 없습니다.';
            activeToggle.checked = true;
            return;
        }

        if ((org.employeeCount ?? 0) > 0) {
            help.textContent = '소속 사원이 존재하여 비활성화할 수 없습니다.';
            activeToggle.checked = true;
            return;
        }
    }


    // 생성 시 상위 조직 필수
    if (!isEditMode && !parentOrgValue) {
        help.textContent = '상위 조직은 반드시 선택해야 합니다.';
        return;
    }


    if (!name) {
        help.textContent = '조직명을 입력해주세요.';
        return;
    }

    if (name.length > MAX_ORG_NAME_LENGTH) {
        help.textContent = `조직명은 최대 ${MAX_ORG_NAME_LENGTH}자까지 입력 가능합니다.`;
        return;
    }


    // CREATE 모드에서만 카테고리 선택 검증
    if (!isEditMode && !categoryValue) {
        help.textContent = '조직 카테고리를 선택해주세요.';
        return;
    }

    const payload = {name};

    // 생성일 때만 category / parent 전달
    if (!isEditMode) {
        payload.categoryId = Number(categoryValue);
        payload.parentOrgId = Number(parentOrgValue);
    }

    // 수정일 때는 활성 여부만 (가능한 경우에만)
    if (isEditMode && !activeToggle.disabled) {
        payload.isActive = activeToggle.checked;
    }

    const url = selectedOrgId ? `${API_BASE}/${selectedOrgId}` : API_BASE;
    const method = selectedOrgId ? 'PUT' : 'POST';

    const res = await apiFetch(url, {
        method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    const json = await res.json();
    const savedOrgId = selectedOrgId ?? json.data;

    // 정책이 하나라도 선택된 경우에만 호출
    if (selectedPositionIds.size > 0) {
        await apiFetch('/api/admin/org-position-policies', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                orgId: savedOrgId,
                positionCategoryIds: Array.from(selectedPositionIds)
            })
        });
    }

    closeModal();
    await loadOrganizations();
}

function buildCategorySelect(selectedId) {
    const select = document.getElementById('categorySelect');
    const options = select.querySelector('.custom-select-options');
    const label = select.querySelector('.custom-select-selected');
    const hidden = document.getElementById('categorySelectValue');

    //  초기화
    label.textContent = '조직 카테고리 선택';
    hidden.value = '';
    options.innerHTML = '';

    orgCategories.forEach(c => {
        const div = document.createElement('div');
        div.className = 'custom-select-option';
        div.textContent = c.name;

        if (c.id === selectedId) {
            div.classList.add('selected');
            label.textContent = c.name;
            hidden.value = c.id;
        }

        div.onclick = () => {
            options.querySelectorAll('.selected')
                .forEach(el => el.classList.remove('selected'));

            div.classList.add('selected');
            label.textContent = c.name;
            hidden.value = c.id;

            currentOrgCategoryId = c.id;
            selectedPositionIds.clear();
            renderPolicyTable(currentOrgCategoryId);

            select.classList.remove('active');
        };

        options.appendChild(div);
    });
}

function buildParentSelect(selectedParentId, {mode}) {
    const select = document.getElementById('parentOrgSelect');
    const options = select.querySelector('.custom-select-options');
    const label = select.querySelector('.custom-select-selected');
    const hidden = document.getElementById('parentOrgSelectValue');

    options.innerHTML = '';
    select.classList.remove('disabled');

    const isCreate = mode === 'create';
    const isEdit = mode === 'edit';

    // buildParentSelect에서는 "수정은 무조건 read-only"만 책임
    if (isEdit) {
        const parent = allOrgList.find(o => o.id === selectedParentId);

        if (parent) {
            label.textContent = parent.name;
            hidden.value = parent.id;
        } else {
            label.textContent = '상위 조직 없음';
            hidden.value = '';
        }

        select.classList.add('disabled');
        return;
    }

    // 생성 모드
    label.textContent = '상위 조직 선택';
    hidden.value = '';

    allOrgList
        .filter(o =>
            normalizeActive(o) &&
            o.id !== selectedOrgId    // 자기 자신만 제외
        )
        .forEach(o => {
            const div = document.createElement('div');
            div.className = 'custom-select-option';
            div.textContent = o.isRootOrg ? `${o.name} (회사)` : o.name;

            div.onclick = () => {
                options.querySelectorAll('.selected')
                    .forEach(el => el.classList.remove('selected'));

                div.classList.add('selected');
                label.textContent = div.textContent;
                hidden.value = o.id;

                select.classList.remove('active');
            };

            options.appendChild(div);
        });
}


function openModal() {
    els.modal().classList.remove('hidden');
}

function closeModal() {
    els.modal().classList.add('hidden');
}

function markOrderChanged() {
    isOrderChanged = true;
    els.btnSaveOrder().disabled = false;
}

function resetOrderChanged() {
    isOrderChanged = false;
    els.btnSaveOrder().disabled = true;
}

function normalizeActive(o) {
    return o.isActive === true || o.isActive === 1;
}

function escapeHtml(str) {
    return String(str).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
}

function dragHandleHtml() {
    return `<span class="drag-handle"><span class="drag-dots">
        <span></span><span></span><span></span><span></span><span></span><span></span>
    </span></span>`;
}

function collectSubtreeNodes(startNode) {
    const nodes = [startNode];
    const startId = startNode.dataset.id;

    let next = startNode.nextElementSibling;
    while (next && isDescendantOf(next, startId)) {
        nodes.push(next);
        next = next.nextElementSibling;
    }
    return nodes;
}

function isDescendantOf(node, ancestorId) {
    let pid = node.dataset.parentId;
    while (pid && pid !== 'root') {
        if (pid === ancestorId) return true;
        const parent = document.querySelector(`.org-node[data-id="${pid}"]`);
        pid = parent?.dataset.parentId;
    }
    return false;
}

function highlight(text, keyword) {
    if (!keyword) return escapeHtml(text);

    const escaped = escapeHtml(text);
    const reg = new RegExp(`(${keyword})`, 'ig');
    return escaped.replace(reg, '<mark class="org-highlight">$1</mark>');
}

function expandParents(org) {
    let cur = org;
    while (cur?.parentOrgId) {
        expandedSet.add(cur.parentOrgId);
        cur = allOrgList.find(o => o.id === cur.parentOrgId);
    }
}

function focusFirstMatch(keyword) {
    const match = allOrgList.find(o =>
        o.name.toLowerCase().includes(keyword)
    );
    if (!match) return;

    expandParents(match);

    // 다시 렌더링 (부모 열린 상태 반영)
    renderTree();

    // DOM에서 해당 노드 찾기
    const el = document.querySelector(`.org-node[data-id="${match.id}"]`);
    if (!el) return;

    el.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    el.classList.add('org-focus');
    setTimeout(() => el.classList.remove('org-focus'), 1500);
}


async function loadPositionPolicies(orgId, orgCategoryId) {

    // 1. 활성 직책 전체
    const res1 = await apiFetch(
        '/api/admin/position-categories?includeInactive=false'
    );
    const json1 = await res1.json();
    positionCategories = json1.data || [];

    selectedPositionIds.clear();

    // 2. 수정 시: 기존 정책
    if (orgId) {
        const res2 = await apiFetch(
            `/api/admin/org-position-policies/${orgId}`
        );
        const json2 = await res2.json();
        (json2.data || []).forEach(v =>
            selectedPositionIds.add(v.positionCategoryId)
        );
    }

    renderPolicyTable(orgCategoryId);
}

function renderPolicyTable(orgCategoryId) {

    const keyword =
        document.getElementById('policyKeyword').value.trim().toLowerCase();
    const headFilter =
        document.getElementById('policyHeadFilter').value;

    const tbody = document.getElementById('policyTableBody');
    tbody.innerHTML = '';

    const filtered = positionCategories
        .filter(p => p.isActive)
        .filter(p => !keyword || p.name.toLowerCase().includes(keyword))
        .filter(p => {
            if (headFilter === 'HEAD') return p.isHead;
            if (headFilter === 'NORMAL') return !p.isHead;
            return true;
        });

    if (!filtered.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="policy-empty">
                    선택 가능한 직책이 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    filtered.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                ${p.isHead ? `<span class="badge-head">HEAD</span>` : ''}
                ${escapeHtml(p.name)}
            </td>
            <td>${p.assignedCount ?? 0}</td>
            <td>
                <label class="switch">
                    <input type="checkbox"
                        ${selectedPositionIds.has(p.id) ? 'checked' : ''}
                        onchange="togglePolicy(${p.id}, this.checked)">
                    <span class="slider"></span>
                </label>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function togglePolicy(id, checked) {
    if (checked) selectedPositionIds.add(id);
    else selectedPositionIds.delete(id);
}


document.getElementById('policyKeyword')
    .addEventListener('input', () =>
        renderPolicyTable(currentOrgCategoryId)
    );

document.getElementById('policyHeadFilter')
    .addEventListener('change', () =>
        renderPolicyTable(currentOrgCategoryId)
    );


async function loadOrganizationsWithSearch() {
    const keyword = els.search().value.trim();
    const includeInactive = els.includeInactive().checked;
    const includeDescendants = els.includeDescendants().checked;

    const params = new URLSearchParams({
        includeInactive,
        includeDescendants
    });

    if (keyword) {
        params.append('keyword', keyword);
    }

    const res = await apiFetch(`${API_BASE}/trees?${params.toString()}`);
    const json = await res.json();

    allOrgList = json.data || [];
    filteredOrgList = allOrgList;

    expandedSet.clear();

    // 검색어가 있을 때만 자동 확장
    if (keyword) {
        allOrgList.forEach(o => expandParents(o));
    }

    renderTree();
    destroySortable();
    resetOrderChanged();
}


let isAllExpanded = false;
const toggleBtn = document.getElementById('btnToggleAll');
const toggleIcon = toggleBtn.querySelector('i');

toggleBtn.addEventListener('click', () => {
    const hasExpanded = expandedSet.size > 0;

    if (hasExpanded) {
        expandedSet.clear();
        isAllExpanded = false;
    } else {
        allOrgList.forEach(o => {
            if (o.parentOrgId) expandedSet.add(o.parentOrgId);
        });
        isAllExpanded = true;
    }

    updateToggleIcon();
    renderTree();
    initSortable();
});


function bindOrgNameCounter() {
    const input = els.orgName();
    const counter = document.getElementById('orgNameCount');
    const help = document.getElementById('orgNameHelp');

    if (!input || !counter || !help) return;

    const update = () => {
        let value = input.value;

        if (value.length > MAX_ORG_NAME_LENGTH) {
            value = value.slice(0, MAX_ORG_NAME_LENGTH);
            input.value = value;

            help.textContent = `조직명은 최대 ${MAX_ORG_NAME_LENGTH}자까지 입력 가능합니다.`;
        } else {
            help.textContent = '';
        }

        counter.textContent = value.length;
    };

    input.addEventListener('input', update);
    update(); // 초기 반영
}

document.querySelectorAll('.custom-select').forEach(select => {
    const label = select.querySelector('.custom-select-selected');

    label.addEventListener('click', () => {
        select.classList.toggle('active');
    });
});

document.addEventListener('click', e => {
    document.querySelectorAll('.custom-select.active').forEach(cs => {
        if (!cs.contains(e.target)) cs.classList.remove('active');
    });
});
