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
    <td class="col-order">
      ${v.isActive && !isSearch() ? drag() : ''}
    </td>

    <td class="col-name">
      <strong>${v.name}</strong>
    </td>

    <td class="col-parent">
      ${v.parentPositionName ?? '-'}
    </td>

    <td class="col-head">
      ${v.isHead ? `<span class="badge badge-head">HEAD</span>` : ''}
    </td>

    <td class="col-status">
      <span class="status-badge ${v.isActive ? 'status-active' : 'status-inactive'}">
        ${v.isActive ? '활성' : '비활성'}
      </span>
    </td>

    <td class="col-action">
      <button class="table-btn" data-edit>수정</button>
    </td>
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

    // 혹시 남아 있을 수 있는 help 숨김
    $('#inactiveHelp')?.classList.add('hidden');

    buildParentPositionSelect(null, false);
    showModal();
    updateHeadHelp();
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

    $('#headHelp').classList.add('hidden');

    // 사용 여부만 수정 가능
    $('#isActive').checked = v.isActive;
    $('#isActive').disabled = false;

    // 1. 부모 후보 재구성 (자기 자신만 제외)
    buildParentPositionSelect(selectedId, true);

    // 2. 기존 parent를 명시적으로 선택
    const parentSelect = $('#parentPositionSelect');
    parentSelect.value = parentPositionId ?? '';


    $('#inactiveHelp').classList.add('hidden');

    showModal();
    updateHeadHelp();
    bindActiveToggle();
}



/* ================= Save ================= */
async function save() {

    const isCreate = !selectedId;

    const name = $('#positionName').value.trim();
    const orgCategoryId = Number($('#orgCategorySelect').value);
    const parentPositionId = $('#parentPositionSelect').value
        ? Number($('#parentPositionSelect').value)
        : null;
    const isHead = $('#isHead').checked;
    const isActive = $('#isActive').checked;

    const noParent = !parentPositionId;

    // 생성 + HEAD + parent 없음 경고
    if (isCreate && noParent && isHead) {
        const ok = confirm(
            '상위 직책이 없는 최상위 결재처리자로 생성됩니다.\n계속 진행하시겠습니까?'
        );
        if (!ok) return;
    }

    const payload = isCreate
        ? { name, orgCategoryId, parentPositionId, isHead }
        : { name, isActive };

    const method = isCreate ? 'POST' : 'PUT';
    const url = isCreate ? API : `${API}/${selectedId}`;

    try {
        await apiFetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        hideModal();
        load();
    } catch (e) {
        alert(e.message || '요청 처리 중 오류가 발생했습니다.');
    }
}

/* ================= Bind ================= */
function bind() {
    $('#btnOpenCreate').onclick = openCreate;
    $('#btnCancel').onclick = hideModal;
    $('#btnSave').onclick = save;
    $('#btnSearch').onclick = render;
    $('#includeInactive').onchange = load;
    $('#btnSaveOrder').onclick = saveOrder;
}

/* ================= Utils ================= */
const $ = s => document.querySelector(s);
const drag = () => `
  <span class="drag-handle">
    <span class="drag-dots">
      <span></span><span></span>
      <span></span><span></span>
      <span></span><span></span>
    </span>
  </span>
`;
const isSearch = () => $('#searchKeyword').value.trim().length > 0;

function showModal() {
    $('#modal').classList.remove('hidden');
}

function hideModal() {
    $('#modal').classList.add('hidden');
}

/* ================= 상위 직책 셀렉트 빌드 함수 ================= */
function buildParentPositionSelect(excludeId, disabled = false) {
    const select = $('#parentPositionSelect');

    const currentOrgCategoryId =
        Number($('#orgCategorySelect').value);

    const allowedOrgCategoryIds =
        collectAllowedOrgCategoryIds(currentOrgCategoryId);

    const candidates = list.filter(v =>
        v.isActive &&
        v.id !== excludeId &&
        allowedOrgCategoryIds.has(v.orgCategoryId)
    );

    select.innerHTML = `
      <option value="">(상위 직책 없음)</option>
      ${candidates.map(v => `
        <option value="${v.id}">
          ${v.name} (${v.orgCategoryName})${v.isHead ? ' · HEAD' : ''}
        </option>
      `).join('')}
    `;

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

function collectAllowedOrgCategoryIds(orgCategoryId) {
    const current = orgCategories.find(c => c.id === orgCategoryId);
    if (!current) return new Set();

    return new Set(
        orgCategories
            .filter(c => c.orderIndex <= current.orderIndex)
            .map(c => c.id)
    );
}

function updateHeadHelp() {
    const noParent = !$('#parentPositionSelect').value;
    const isHead = $('#isHead').checked;

    $('#headHelp').classList.toggle(
        'hidden',
        !(noParent && isHead)
    );
}

$('#isHead')?.addEventListener('change', updateHeadHelp);
$('#parentPositionSelect')?.addEventListener('change', updateHeadHelp);

function bindActiveToggle() {
    const toggle = $('#isActive');
    const help = $('#inactiveHelp');

    if (!toggle || !help) return;

    toggle.onchange = () => {
        if (selectedId && !toggle.checked) {
            help.classList.remove('hidden');
        } else {
            help.classList.add('hidden');
        }
    };

}

