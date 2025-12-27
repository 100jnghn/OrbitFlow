const API_BASE = '/api/admin/ranks';

let rankList = [];
let selectedRankId = null;
let sortable = null;
let isOrderChanged = false;

const els = {
    tbody: () => document.getElementById('rankTableBody'),
    search: () => document.getElementById('searchKeyword'),
    btnSearch: () => document.getElementById('btnSearch'),
    includeInactive: () => document.getElementById('includeInactive'),
    btnOpenCreate: () => document.getElementById('btnOpenCreate'),
    btnSaveOrder: () => document.getElementById('btnSaveOrder'),

    modal: () => document.getElementById('rankModal'),
    modalTitle: () => document.getElementById('modalTitle'),
    rankId: () => document.getElementById('rankId'),
    rankName: () => document.getElementById('rankName'),
    parentRank: () => document.getElementById('parentRank'),
    rankActive: () => document.getElementById('rankActive'),
    toggleText: () => document.getElementById('toggleText'),
    nameHelp: () => document.getElementById('nameHelp'),

    btnSaveRank: () => document.getElementById('btnSaveRank'),
    btnCancel: () => document.getElementById('btnCancel'),
    btnCloseModal: () => document.getElementById('btnCloseModal'),
};

document.addEventListener('DOMContentLoaded', () => {
    bindEvents();
    loadRanks();
});

function bindEvents() {
    els.btnSearch().onclick = filterRanks;
    els.search().addEventListener('keydown', e => {
        e.stopPropagation();
        if (e.key === 'Enter') {
            e.preventDefault();
            filterRanks();
        }
    });
    els.includeInactive().onchange = loadRanks;

    els.btnOpenCreate().onclick = openCreateModal;
    els.btnSaveRank().onclick = saveRank;
    els.btnSaveOrder().onclick = saveOrder;

    els.btnCancel().onclick = closeModal;
    els.btnCloseModal().onclick = closeModal;

    els.rankActive().onchange = () =>
        els.toggleText().textContent = els.rankActive().checked ? '사용' : '미사용';
}

async function loadRanks() {
    const includeInactive = els.includeInactive().checked;
    const res = await apiFetch(`${API_BASE}?includeInactive=${includeInactive}`);
    const json = await res.json();
    rankList = json.data || [];
    filterRanks();
    resetOrder();
}

function filterRanks() {
    const keyword = els.search().value.trim().toLowerCase();
    const includeInactive = els.includeInactive().checked;

    const filtered = rankList.filter(r => {
        if (!includeInactive && !r.isActive) return false;
        return r.name.toLowerCase().includes(keyword);
    });

    renderTable(filtered);

    if (!isSearchMode()) initSortable();
    else destroySortable();
}

function isSearchMode() {
    return els.search().value.trim().length > 0 || els.includeInactive().checked;
}

function renderTable(list) {
    const tbody = els.tbody();
    tbody.innerHTML = '';

    const active = list.filter(r => r.isActive)
        .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
    const inactive = list.filter(r => !r.isActive);

    active.forEach(renderRow);

    if (inactive.length) {
        tbody.insertAdjacentHTML('beforeend',
            `<tr><td colspan="6" class="inactive-sep">비활성 직급</td></tr>`);
        inactive.forEach(renderRow);
    }
}

function renderRow(r) {
    const tr = document.createElement('tr');
    tr.dataset.id = r.id;

    tr.innerHTML = `
      <td>${r.isActive && !isSearchMode() ? dragHandle() : ''}</td>
      <td><strong>${r.name}</strong></td>
      <td>${r.parentRankName ?? '-'}</td>
      <td>${r.employeeCount}</td>
      <td>
        <span class="status-badge ${r.isActive ? 'status-active' : 'status-inactive'}">
          ${r.isActive ? '활성' : '비활성'}
        </span>
      </td>
      <td>
        <button class="table-btn">수정</button>
      </td>
    `;

    tr.querySelector('.table-btn').onclick = () => openEditModal(r.id);
    els.tbody().appendChild(tr);
}

function dragHandle() {
    return `
      <span class="drag-handle">
        <span class="drag-dots">
          <span></span><span></span><span></span>
          <span></span><span></span><span></span>
        </span>
      </span>`;
}

function initSortable() {
    destroySortable();
    sortable = Sortable.create(els.tbody(), {
        handle: '.drag-handle',
        animation: 150,
        onEnd: () => {
            isOrderChanged = true;
            els.btnSaveOrder().disabled = false;
        }
    });
}

function destroySortable() {
    sortable?.destroy();
    sortable = null;
}

async function saveOrder() {
    if (!isOrderChanged) return;

    const rows = [...els.tbody().querySelectorAll('tr[data-id]')];
    const payload = {
        orders: rows.map(r => ({id: Number(r.dataset.id)}))
    };

    await apiFetch(`${API_BASE}/order`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    loadRanks();
}

function openCreateModal() {
    selectedRankId = null;
    els.modalTitle().textContent = '직급 추가';
    els.rankName().value = '';
    els.rankActive().checked = true;
    els.rankActive().disabled = true;
    fillParentOptions();
    openModal();
}

function openEditModal(id) {
    const r = rankList.find(v => v.id === id);
    selectedRankId = id;

    els.modalTitle().textContent = '직급 수정';
    els.rankName().value = r.name;

    els.rankActive().checked = r.isActive;
    els.rankActive().disabled = r.employeeCount > 0;

    if (r.employeeCount > 0) {
        els.toggleText().textContent = '사용 (부여 인원 존재)';
    } else {
        els.toggleText().textContent = r.isActive ? '사용' : '미사용';
    }

    fillParentOptions(id);

    if (r.parentRankId) {
        els.parentRank().value = String(r.parentRankId);
    }

    openModal();
}

function fillParentOptions(excludeId) {
    els.parentRank().innerHTML = `<option value="">없음</option>`;
    rankList.filter(r => r.isActive && r.id !== excludeId)
        .forEach(r => {
            els.parentRank().insertAdjacentHTML(
                'beforeend',
                `<option value="${r.id}">${r.name}</option>`
            );
        });
}

async function saveRank() {
    const payload = {
        name: els.rankName().value.trim(),
        parentRankId: els.parentRank().value || null,
        isActive: els.rankActive().checked
    };

    const url = selectedRankId
        ? `${API_BASE}/${selectedRankId}`
        : API_BASE;

    await apiFetch(url, {
        method: selectedRankId ? 'PUT' : 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    closeModal();
    loadRanks();
}

function openModal() {
    els.modal().classList.remove('hidden');
}

function closeModal() {
    els.modal().classList.add('hidden');
}

function resetOrder() {
    isOrderChanged = false;
    els.btnSaveOrder().disabled = true;
}
