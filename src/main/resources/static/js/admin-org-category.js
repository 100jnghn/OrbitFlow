/* ==================================================
   admin-org-category.js (FINAL)
   - 조회 (비활성 포함 옵션)
   - 생성 (항상 활성)
   - 수정 (활성/비활성 전환 가능)
   - 순서 저장 (활성만)
================================================== */

const API_BASE = '/api/admin/org-categories';

let categoryList = [];
let selectedCategoryId = null;

let sortableInstance = null;
let isOrderChanged = false;

/* ======================
   Elements
====================== */
const els = {
    tbody: () => document.getElementById('categoryTableBody'),

    search: () => document.getElementById('searchKeyword'),
    btnSearch: () => document.getElementById('btnSearch'),
    includeInactive: () => document.getElementById('includeInactive'),
    btnOpenCreate: () => document.getElementById('btnOpenCreate'),
    btnSaveOrder: () => document.getElementById('btnSaveOrder'),

    modal: () => document.getElementById('categoryModal'),
    modalTitle: () => document.getElementById('modalTitle'),
    categoryId: () => document.getElementById('categoryId'),
    categoryName: () => document.getElementById('categoryName'),
    categoryActive: () => document.getElementById('categoryActive'),
    toggleText: () => document.getElementById('toggleText'),
    nameHelp: () => document.getElementById('nameHelp'),

    btnCloseModal: () => document.getElementById('btnCloseModal'),
    btnCancel: () => document.getElementById('btnCancel'),
    btnSaveCategory: () => document.getElementById('btnSaveCategory'),
};

/* ======================
   Init
====================== */
document.addEventListener('DOMContentLoaded', () => {
    bindEvents();
    loadCategories();
});

function filterCategories() {
    const keyword = els.search()?.value.trim().toLowerCase() ?? '';
    const includeInactive = els.includeInactive()?.checked ?? false;

    const filtered = categoryList.filter(c => {
        if (!includeInactive && !normalizeActive(c)) return false;
        return (c.name ?? '').toLowerCase().includes(keyword);
    });

    const activeCount = renderTable(filtered);

    const hint = document.getElementById('orderHint');
    if (hint) {
        hint.style.display = activeCount > 1 ? 'block' : 'none';
    }

    initSortable();
}

function bindEvents() {
    els.includeInactive()?.addEventListener('change', loadCategories);

    els.btnOpenCreate()?.addEventListener('click', openCreateModal);
    els.btnSaveOrder()?.addEventListener('click', saveOrder);
    els.btnSearch()?.addEventListener('click', filterCategories);
    els.search()?.addEventListener('keydown', e => { // 엔터키 검색 지원
        if (e.key === 'Enter') {
            filterCategories();
        }
    });
    els.btnCloseModal()?.addEventListener('click', closeModal);
    els.btnCancel()?.addEventListener('click', closeModal);
    els.btnSaveCategory()?.addEventListener('click', saveCategory);

    els.categoryActive()?.addEventListener('change', syncToggleText);

    els.categoryName()?.addEventListener('input', () => {
        els.nameHelp().textContent = '';
        els.categoryName().classList.remove('is-invalid');
    });

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && !els.modal().classList.contains('hidden')) {
            closeModal();
        }
    });
}

/* ======================
   Load
====================== */
async function loadCategories() {
    try {
        const includeInactive = els.includeInactive()?.checked ?? false;

        const res = await apiFetch(
            `${API_BASE}?includeInactive=${includeInactive}`
        );
        const result = await res.json();

        if (!res.ok) {
            alert(result.message || '목록 조회 실패');
            return;
        }

        categoryList = Array.isArray(result.data) ? result.data : [];

        filterCategories();
        resetOrderChanged();

        initSortable();
        resetOrderChanged();

    } catch (e) {
        console.error(e);
        alert('조직 카테고리 조회 중 오류 발생');
    }
}

/* ======================
   Render
====================== */
function renderTable(list) {
    const tbody = els.tbody();
    tbody.innerHTML = '';

    if (!list.length) {
        tbody.innerHTML = `
          <tr>
            <td colspan="4" style="padding:24px; color:#98a2b3;">
              등록된 카테고리가 없습니다.
            </td>
          </tr>`;
        return 0; // activeCount = 0
    }

    const active = list
        .filter(c => normalizeActive(c))
        .sort((a, b) => toNumber(a.orderIndex) - toNumber(b.orderIndex));

    const inactive = list.filter(c => !normalizeActive(c));

    active.forEach(renderRow);

    if (inactive.length) {
        const sep = document.createElement('tr');
        sep.innerHTML = `
          <td colspan="4"
              style="background:#f9fafb; font-weight:800; color:#667085;">
            비활성 카테고리
          </td>`;
        tbody.appendChild(sep);

        inactive.forEach(renderRow);
    }

    return active.length;
}


function renderRow(category) {
    const tr = document.createElement('tr');
    tr.dataset.id = category.id;

    const isActive = normalizeActive(category);

    tr.innerHTML = `
      <td>${isActive ? dragHandleHtml() : ''}</td>
      <td><strong>${escapeHtml(category.name)}</strong></td>
      <td>
        <span class="status-badge ${isActive ? 'status-active' : 'status-inactive'}">
          ${isActive ? '활성' : '비활성'}
        </span>
      </td>
      <td>
        <button class="table-btn" data-edit="${category.id}">수정</button>
      </td>
    `;

    els.tbody().appendChild(tr);

    tr.querySelector('[data-edit]').addEventListener('click', () => {
        openEditModal(category.id);
    });
}

function dragHandleHtml() {
    return `
      <span class="drag-handle">
        <span class="drag-dots">
          <span></span><span></span>
          <span></span><span></span>
          <span></span><span></span>
        </span>
      </span>`;
}

/* ======================
   Sortable (활성만)
====================== */
function initSortable() {
    const tbody = els.tbody();
    if (!tbody) return;

    if (sortableInstance) sortableInstance.destroy();

    sortableInstance = Sortable.create(tbody, {
        handle: '.drag-handle',
        filter: 'tr:not([data-id])', // separator 제외
        preventOnFilter: false,
        animation: 160,

        onMove: evt => {
            const id = evt.related?.dataset?.id;
            const c = categoryList.find(v => String(v.id) === id);
            return c && normalizeActive(c);
        },

        onEnd: markOrderChanged
    });

}

/* ======================
   Save Order
====================== */
async function saveOrder() {
    if (!isOrderChanged) return;

    const rows = [...document.querySelectorAll('#categoryTableBody tr[data-id]')]
        .filter(r => {
            const c = categoryList.find(v => v.id === toNumber(r.dataset.id));
            return c && normalizeActive(c);
        });

    const payload = {
        orders: rows.map(r => ({id: toNumber(r.dataset.id)}))
    };

    const res = await apiFetch(`${API_BASE}/order`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    const result = await res.json();

    if (!res.ok) {
        alert(result.message || '순서 저장 실패');
        return;
    }

    resetOrderChanged();
    loadCategories();
    toast('순서가 저장되었습니다.');
}

/* ======================
   Modal
====================== */
function openCreateModal() {
    selectedCategoryId = null;

    els.modalTitle().textContent = '조직 카테고리 생성';
    els.categoryId().value = '';
    els.categoryName().value = '';

    els.categoryActive().checked = true;
    els.categoryActive().disabled = true;

    syncToggleText();
    els.nameHelp().textContent = '';

    openModal();
    setTimeout(() => els.categoryName().focus(), 0);
}

function openEditModal(id) {
    const category = categoryList.find(c => c.id === id);
    if (!category) return;

    selectedCategoryId = id;

    els.modalTitle().textContent = '조직 카테고리 수정';
    els.categoryId().value = id;
    els.categoryName().value = category.name ?? '';

    els.categoryActive().checked = normalizeActive(category);
    els.categoryActive().disabled = false;

    syncToggleText();
    els.nameHelp().textContent = '';

    openModal();
}

async function saveCategory() {
    const name = els.categoryName().value.trim();
    const isActive = els.categoryActive().checked;

    if (!name) {
        els.nameHelp().textContent = '카테고리명을 입력해주세요.';
        return;
    }

    const payload = {name, isActive};

    const isEdit = !!selectedCategoryId;
    const url = isEdit ? `${API_BASE}/${selectedCategoryId}` : API_BASE;

    const res = await apiFetch(url, {
        method: isEdit ? 'PUT' : 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    const result = await res.json();

    if (!res.ok) {
        alert(result.message || '저장 실패');
        return;
    }

    closeModal();
    loadCategories();
    toast('저장되었습니다.');
}

/* ======================
   Utils
====================== */
function openModal() {
    els.modal().classList.remove('hidden');
    els.modal().setAttribute('aria-hidden', 'false');
}

function closeModal() {
    els.modal().classList.add('hidden');
    els.modal().setAttribute('aria-hidden', 'true');
}

function markOrderChanged() {
    isOrderChanged = true;
    els.btnSaveOrder().disabled = false;
}

function resetOrderChanged() {
    isOrderChanged = false;
    els.btnSaveOrder().disabled = true;
}

function syncToggleText() {
    els.toggleText().textContent =
        els.categoryActive().checked ? '사용' : '미사용';
}

function normalizeActive(c) {
    const v = c.isActive ?? c.active;
    return v === true || v === 1 || v === 'true';
}

function toNumber(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;');
}

function toast(msg) {
    const el = document.createElement('div');
    el.textContent = msg;
    el.style.cssText = `
      position:fixed; right:18px; bottom:18px;
      background:#111827; color:#fff;
      padding:10px 14px; border-radius:12px;
      font-weight:800; z-index:3000;
    `;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 1400);
}
