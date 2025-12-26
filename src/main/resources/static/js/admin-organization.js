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

const expandedSet = new Set();

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
    btnCloseIcon: () => document.getElementById('btnModalCloseIcon'),
    btnCancel: () => document.getElementById('btnModalCancel'),
};

/* ======================
   Init
====================== */
document.addEventListener('DOMContentLoaded', async () => {
    bindEvents();
    await loadOrgCategories();
    await loadOrganizations();
});

/* ======================
   Load
====================== */
async function loadOrganizations() {
    const includeInactive = els.includeInactive().checked;

    const res = await apiFetch(
        `${API_BASE}?includeInactive=${includeInactive}`
    );

    const json = await res.json();
    allOrgList = json.data || [];
    applyFilterAndRender();
    resetOrderChanged();
}

async function loadOrgCategories() {
    const res = await apiFetch('/api/admin/org-categories');
    const json = await res.json();
    orgCategories = (json.data || []).filter(c => c.isActive === true);
}

/* ======================
   Filter + Render
====================== */
function applyFilterAndRender() {
    const keyword = els.search().value.trim().toLowerCase();
    const includeInactive = els.includeInactive().checked;

    // 검색어 없을 때 → 기존 전체 트리
    if (!keyword) {
        filteredOrgList = allOrgList.filter(o =>
            includeInactive || normalizeActive(o)
        );

        renderTree();
        initSortable();
        return;
    }

    // 검색어 있을 때
    const includeDescendants = els.includeDescendants().checked;
    const visibleIds = collectMatchedOrgIds(keyword, includeDescendants);

    filteredOrgList = allOrgList.filter(o => {
        // 회사(최상위 parent=null)는 제외
        if (o.parentOrgId === null) return false;

        if (!includeInactive && !normalizeActive(o)) return false;

        return visibleIds.has(o.id);
    });

    // 부모 자동 펼침
    filteredOrgList.forEach(o => expandParents(o));

    renderTree();

    // 검색 중 정렬 비활성화
    destroySortable();
    els.btnSaveOrder().disabled = true;
    isOrderChanged = false;
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

    const node = document.createElement('div');
    node.className = 'org-node';
    node.dataset.id = org.id;
    node.dataset.parentId = org.parentOrgId ?? 'root';
    node.dataset.active = normalizeActive(org);
    node.style.marginLeft = `${depth * 20}px`;

    const keyword = els.search().value.trim();

    node.innerHTML = `
      <div class="org-row">
        ${normalizeActive(org) ? dragHandleHtml() : ''}
        ${
        hasChildren
            ? `<span class="org-toggle">${isExpanded ? '▾' : '▸'}</span>`
            : `<span class="org-toggle-placeholder"></span>`
    }
        <span class="org-label">${highlight(org.name, keyword)}</span>
        <span class="status-badge ${normalizeActive(org) ? 'status-active' : 'status-inactive'}">
            ${normalizeActive(org) ? '활성' : '비활성'}
        </span>
        <button class="table-btn">수정</button>
      </div>
    `;

    container.appendChild(node);

    node.querySelector('.table-btn').onclick = () => openEdit(org.id);

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
        groups[pid].push({ id: Number(n.dataset.id) });
    });

    for (const orders of Object.values(groups)) {
        await apiFetch(`${API_BASE}/order`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ orders })
        });
    }

    resetOrderChanged();
    await loadOrganizations();
}

/* ======================
   Events
====================== */
function bindEvents() {
    els.search().addEventListener('input', applyFilterAndRender);
    els.includeInactive().addEventListener('change', loadOrganizations);
    els.btnSearch().addEventListener('click', applyFilterAndRender);

    els.btnCreate().addEventListener('click', openCreate);
    els.btnSaveOrder().addEventListener('click', saveOrder);
    els.btnCloseIcon().addEventListener('click', closeModal);
    els.btnCancel().addEventListener('click', closeModal);
    els.btnSave().addEventListener('click', saveOrg);

    els.includeDescendants().addEventListener('change', applyFilterAndRender);
}

/* ======================
   CRUD / Utils
====================== */
function openCreate() {
    isEditMode = false;
    selectedOrgId = null;

    els.modalTitle().textContent = '조직 생성';
    els.orgName().value = '';

    buildCategorySelect(null);
    buildParentSelect(null);

    // 생성 시: 카테고리 / 부모조직 활성화
    document.getElementById('categorySelect').disabled = false;
    els.parentSelect().disabled = false;

    // 생성 시: 사용 여부는 항상 활성 + 잠금
    const toggle = document.getElementById('activeToggle');
    toggle.checked = true;
    toggle.disabled = true;

    document.getElementById('inactiveHelp').style.display = 'none';

    openModal();
}

async function openEdit(id) {
    isEditMode = true;

    const org = allOrgList.find(o => o.id === id);
    if (!org) return;

    selectedOrgId = id;

    els.modalTitle().textContent = '조직 수정';
    els.orgName().value = org.name ?? '';

    buildCategorySelect(org.categoryId);
    buildParentSelect(org.parentOrgId ?? null);

    // 수정 시: 카테고리 / 부모조직 수정 불가
    document.getElementById('categorySelect').disabled = true;
    els.parentSelect().disabled = true;

    const toggle = document.getElementById('activeToggle');
    const help = document.getElementById('inactiveHelp');

    toggle.checked = normalizeActive(org);
    toggle.disabled = false;
    help.style.display = 'none';

    // 활성 상태일 때만 "비활성화 가능 여부" 체크
    if (normalizeActive(org)) {
        const res = await apiFetch(`/api/admin/organizations/${id}/deactivate-check`);
        const json = await res.json();
        const check = json.data;

        if (!check.canDeactivate) {
            toggle.disabled = true;
            help.textContent = check.reason;
            help.style.display = 'block';
        }
    }

    openModal();
}

async function saveOrg() {
    const name = els.orgName().value.trim();
    if (!name) return alert('조직명을 입력해주세요.');

    const payload = {
        name,
        isActive: isEditMode ? document.getElementById('activeToggle').checked : true
    };

    const url = selectedOrgId ? `${API_BASE}/${selectedOrgId}` : API_BASE;
    const method = selectedOrgId ? 'PUT' : 'POST';

    await apiFetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    closeModal();
    await loadOrganizations();
}

function buildCategorySelect(selectedId) {
    const select = document.getElementById('categorySelect');
    select.innerHTML = orgCategories.map(c =>
        `<option value="${c.id}" ${c.id === selectedId ? 'selected' : ''}>${c.name}</option>`
    ).join('');
}

function buildParentSelect(selected) {
    els.parentSelect().innerHTML = `
      <option value="">상위 조직 없음</option>
      ${allOrgList
        .filter(o => normalizeActive(o) && o.id !== selectedOrgId)
        .map(o => `<option value="${o.id}" ${o.id === selected ? 'selected' : ''}>${escapeHtml(o.name)}</option>`)
        .join('')}
    `;
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

function groupHasKeyword(root, keyword) {
    const stack = [root];

    while (stack.length) {
        const cur = stack.pop();

        if (cur.name.toLowerCase().includes(keyword)) {
            return true;
        }

        const children = allOrgList.filter(o => o.parentOrgId === cur.id);
        stack.push(...children);
    }

    return false;
}

function expandParentsByKeyword(root, keyword) {
    const stack = [root];

    while (stack.length) {
        const cur = stack.pop();

        if (cur.name.toLowerCase().includes(keyword)) {
            expandParents(cur);
        }

        const children = allOrgList.filter(o => o.parentOrgId === cur.id);
        stack.push(...children);
    }
}

function collectMatchedOrgIds(keyword, includeDescendants) {
    const result = new Set();

    allOrgList.forEach(org => {
        if (!org.name.toLowerCase().includes(keyword)) return;

        // 1. 자기 자신
        result.add(org.id);

        // 2. 부모 체인
        let cur = org;
        while (cur.parentOrgId) {
            const parent = allOrgList.find(o => o.id === cur.parentOrgId);
            if (!parent) break;
            result.add(parent.id);
            cur = parent;
        }

        // 3. 하위 조직 (옵션 ON일 때만)
        if (includeDescendants) {
            collectDescendants(org.id, result);
        }
    });

    return result;
}

function collectDescendants(parentId, set) {
    allOrgList
        .filter(o => o.parentOrgId === parentId)
        .forEach(child => {
            if (set.has(child.id)) return;
            set.add(child.id);
            collectDescendants(child.id, set);
        });
}
