const API = '/api/admin/position-categories';
const ORG_CAT_API = '/api/admin/org-categories';

let list = [];
let sortable = null;
let orderChanged = false;
let selectedId = null;
let parentPositionId = null;
let orgCategories = [];

async function loadOrgCategories() {
    const res = await apiFetch(ORG_CAT_API);
    const json = await res.json();
    orgCategories = json.data || [];

    const select = $('#orgCategorySelect');
    select.innerHTML = orgCategories.map(c =>
        `<option value="${c.id}">${c.name}</option>`
    ).join('');
}


/* ================= Init ================= */
document.addEventListener('DOMContentLoaded', () => {
    bind();
    load();
});

/* ================= Load ================= */
async function load() {
    const includeInactive = $('#includeInactive').checked;
    const res = await apiFetch(`${API}?includeInactive=${includeInactive}`);
    const json = await res.json();
    list = json.data || [];
    render();
}

/* ================= Render ================= */
function render() {
    const tbody = $('#tableBody');
    tbody.innerHTML = '';

    const keyword = $('#searchKeyword').value.trim().toLowerCase();
    const filtered = list.filter(v =>
        v.name.toLowerCase().includes(keyword)
    );

    const active = filtered.filter(v => v.isActive);
    const inactive = filtered.filter(v => !v.isActive);

    active.forEach(v => tbody.appendChild(row(v)));

    if (inactive.length) {
        tbody.insertAdjacentHTML('beforeend',
            `<tr><td colspan="6" style="background:#f9fafb;font-weight:800">비활성 직책</td></tr>`
        );
        inactive.forEach(v => tbody.appendChild(row(v)));
    }

    initSortable(active.length);
}

/* ================= Row ================= */
function row(v) {
    const tr = document.createElement('tr');
    tr.dataset.id = v.id;
    tr.innerHTML = `
    <td>${v.isActive ? drag() : ''}</td>
    <td>${v.name}</td>
    <td>${v.parentPositionName ?? '-'}</td>
    <td>${v.isHead ? `<span class="badge badge-head">HEAD</span>` : ''}</td>
    <td>${v.isActive ? '활성' : '비활성'}</td>
    <td><button data-edit>수정</button></td>
  `;
    tr.querySelector('[data-edit]').onclick = () => openEdit(v.id);
    return tr;
}

/* ================= Sortable ================= */
function initSortable(count) {
    destroySortable();
    if (count < 2 || isSearch()) return;

    sortable = Sortable.create($('#tableBody'), {
        handle: '.drag-handle',
        onEnd: () => {
            orderChanged = true;
            $('#btnSaveOrder').disabled = false;
        }
    });
}

function destroySortable() {
    if (sortable) sortable.destroy();
    sortable = null;
}

/* ================= Modal ================= */
async function openCreate() {
    await loadOrgCategories();

    selectedId = null;
    parentPositionId = null;

    const orgCategorySelect = $('#orgCategorySelect');
    orgCategorySelect.disabled = false;
    orgCategorySelect.value = orgCategories[0]?.id ?? '';

    $('#modalTitle').textContent = '직책 생성';
    $('#positionName').value = '';
    $('#isHead').checked = false;
    $('#isHead').disabled = false;

    $('#isActive').checked = true;
    $('#isActive').disabled = true;

    buildParentPositionSelect(null, false);
    showModal();
}



async function openEdit(id) {
    await loadOrgCategories();

    const v = list.find(x => x.id === id);
    if (!v) return;

    selectedId = id;
    parentPositionId = v.parentPositionId ?? null;

    $('#modalTitle').textContent = '직책 수정';
    $('#positionName').value = v.name;

    // 조직 유형 기준: 값만 세팅 + 수정 불가
    const orgCategorySelect = $('#orgCategorySelect');
    orgCategorySelect.value = v.orgCategoryId;
    orgCategorySelect.disabled = true;

    // HEAD 여부: 조회만
    $('#isHead').checked = v.isHead;
    $('#isHead').disabled = true;

    // 사용 여부만 수정 가능
    $('#isActive').checked = v.isActive;
    $('#isActive').disabled = false;

    // 상위 직책: 조회만
    buildParentPositionSelect(parentPositionId, true);

    showModal();
}



/* ================= Save ================= */
async function save() {
    const parentValue = $('#parentPositionSelect').value;


    const payload = {
        name: $('#positionName').value.trim(),
        orgCategoryId: Number($('#orgCategorySelect').value),
        parentPositionId: $('#parentPositionSelect').value
            ? Number($('#parentPositionSelect').value)
            : null,
        isHead: $('#isHead').checked,
        isActive: $('#isActive').checked
    };


    const method = selectedId ? 'PUT' : 'POST';
    const url = selectedId ? `${API}/${selectedId}` : API;

    await apiFetch(url, {
        method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    hideModal();
    load();
}

/* ================= Bind ================= */
function bind() {
    $('#btnOpenCreate').onclick = openCreate;
    $('#btnCancel').onclick = hideModal;
    $('#btnCloseModal').onclick = hideModal;
    $('#btnSave').onclick = save;
    $('#btnSearch').onclick = render;
    $('#includeInactive').onchange = load;
    $('#btnSaveOrder').onclick = saveOrder;
}

/* ================= Utils ================= */
const $ = s => document.querySelector(s);
const drag = () => `<span class="drag-handle">⋮⋮</span>`;
const isSearch = () => $('#searchKeyword').value.trim().length > 0;

function showModal() {
    $('#modal').classList.remove('hidden');
}

function hideModal() {
    $('#modal').classList.add('hidden');
}

/* ================= 상위 직책 셀렉트 빌드 함수 ================= */
function buildParentPositionSelect(selectedId, disabled = false) {
    const select = $('#parentPositionSelect');

    const currentOrgCategoryId =
        Number($('#orgCategorySelect').value);

    const allowedOrgCategoryIds =
        collectAllowedOrgCategoryIds(currentOrgCategoryId);

    const candidates = list.filter(v =>
        v.isActive &&
        v.id !== selectedId &&
        allowedOrgCategoryIds.has(v.orgCategoryId)
    );

    select.innerHTML = `
  ${candidates.map(v =>
        `<option value="${v.id}" ${v.id === selectedId ? 'selected' : ''}>
        ${v.name} (${v.orgCategoryName})${v.isHead ? ' · HEAD' : ''}
    </option>`
    ).join('')}
`;
// --> 부모 직책을 반드시 하나 선택해야 함 (“최상위 직책”이라는 개념이 사라짐, 모든 직책은 명시적 트리 안에 존재)

    select.disabled = disabled;
}

/* ================= 정렬 저장 ================= */
async function saveOrder() {
    if (!orderChanged) return;

    const ids = [...$('#tableBody').children]
        .filter(tr => tr.dataset.id)
        .map((tr, idx) => ({
            id: Number(tr.dataset.id),
            orderIndex: idx + 1
        }));

    await apiFetch(`${API}/order`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({orders: ids})
    });

    orderChanged = false;
    $('#btnSaveOrder').disabled = true;
    load();
}

document
    .getElementById('orgCategorySelect')
    .addEventListener('change', () => {
        buildParentPositionSelect(parentPositionId, false);
    });


// function collectAllowedOrgCategoryIds(orgCategoryId) {
//     const allowed = new Set();
//     let currentId = orgCategoryId;
//
//     while (currentId) {
//         allowed.add(currentId);
//         const current = orgCategories.find(c => c.id === currentId);
//         currentId = current?.parentId ?? null;
//     }
//
//     return allowed;
// }

function collectAllowedOrgCategoryIds(orgCategoryId) {
    const current = orgCategories.find(c => c.id === orgCategoryId);
    if (!current) return new Set();

    return new Set(
        orgCategories
            .filter(c => c.orderIndex <= current.orderIndex)
            .map(c => c.id)
    );
}
