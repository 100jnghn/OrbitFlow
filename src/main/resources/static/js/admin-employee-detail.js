/**
 * admin-employee-detail.js
 * - CSR 방식 사원 상세
 * - 상태 변경
 * - Audit Log 타임라인 + diff
 */


// 현재 상태
let currentEmployeeStatus = null;


/* =========================
   Lookup Maps
========================= */
let orgMap = {};
let rankMap = {};
let positionMap = {};

/* =========================
   Edit Modal Elements
========================= */
const editName = document.getElementById('editName');
const editGender = document.getElementById('editGender');
const editPhone = document.getElementById('editPhone');
const editInternalPhone = document.getElementById('editInternalPhone');
const editBirthDate = document.getElementById('editBirthDate');

const editOrgId = document.getElementById('editOrgId');
const editRankId = document.getElementById('editRankId');
const editPositionCategoryId = document.getElementById('editPositionCategoryId');
const editEmploymentType = document.getElementById('editEmploymentType');
const editRole = document.getElementById('editRole');

const positionResetNotice = document.getElementById('positionResetNotice');
const editHireDate = document.getElementById('editHireDate');


async function loadLookups() {
    const [orgRes, rankRes, posRes] = await Promise.all([
        apiFetch('/api/admin/organizations'),
        apiFetch('/api/admin/ranks'),
        apiFetch('/api/admin/position-categories')
    ]);

    const orgs = await safeJson(orgRes);
    const ranks = await safeJson(rankRes);
    const positions = await safeJson(posRes);

    orgs?.data?.forEach(o => orgMap[o.id] = o.name);
    ranks?.data?.forEach(r => rankMap[r.id] = r.name);
    positions?.data?.forEach(p => positionMap[p.id] = p.name);
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadLookups();

    const employeeId = document.getElementById('employeeId')?.value;
    if (!employeeId) return;

    bindStatusButtons(employeeId);
    loadEmployeeDetail(employeeId);
    loadAuditLogs(employeeId);
});

/* =========================
   Employee Detail
========================= */
async function loadEmployeeDetail(employeeId) {
    const res = await apiFetch(`/api/admin/employees/${employeeId}`);
    const result = await safeJson(res);

    if (!res.ok) {
        await sweetError(result?.message || '사원 상세 조회에 실패했습니다.');
        return;
    }

    const e = result.data;

    currentEmployeeStatus = e.status;

    const btnEdit = document.getElementById('btnEditEmployee');
    if (btnEdit) {
        btnEdit.disabled = (e.status === 'RESIGNED');
    }

    /* avatar */
    const avatar = document.getElementById('empAvatar');
    if (avatar) {
        avatar.src = e.gender === 'FEMALE'
            ? '/images/female.png'
            : '/images/male.png';

        avatar.classList.toggle('grayscale', e.status === 'RESIGNED');
    }

    /* basic info */
    setText('empName', e.name);
    setText('empEmail', e.email);
    setText('empEmpNo', e.employeeNo);

    setText('empOrg', e.orgPath);
    setText('empRank', e.rankName);
    setText('empPosition', e.positionName);

    setText('empHireDate', e.hireDate);
    setText('empEmploymentType', employmentLabel(e.employmentType));

    setText('empPhone', e.phone);
    setText('empInternalPhone', e.internalPhone);
    setText('empBirthDate', e.birthDate);

    renderStatusBadge(e.status);
    highlightStatusButton(e.status);

    /* summary */
    const summary = [];
    if (e.orgPath) summary.push(e.orgPath.split(' > ').pop());
    if (e.positionName) summary.push(e.positionName);
    if (e.rankName) summary.push(e.rankName);

    let summaryHtml = summary.join(' · ');
    if (e.employmentType) {
        summaryHtml += ` <span class="emp-badge">${employmentLabel(e.employmentType)}</span>`;
    }

    const summaryEl = document.getElementById('empSummary');
    if (summaryEl) summaryEl.innerHTML = summaryHtml;

    updateStatusButtons(e.status);
}

function updateStatusButtons(status) {
    btnActive.disabled   = (status === 'ACTIVE' || status === 'RESIGNED');
    btnSuspend.disabled = (status === 'SUSPENDED' || status === 'RESIGNED');
    btnResign.disabled  = (status === 'RESIGNED');

    btnResendActivate.style.display = 'none';
    tempNotice.style.display = 'none';

    if (status === 'TEMP') {
        btnActive.disabled = true;
        btnSuspend.disabled = true;
        btnResendActivate.style.display = 'inline-flex';
        tempNotice.style.display = 'block';
    }
}

btnResendActivate.onclick = async () => {
    const result = await sweetConfirm(
        '메일 전송',
        '활성화 메일을 보내시겠습니까?'
    );
    if (!result.isConfirmed) return;

    const employeeId = document.getElementById('employeeId').value;

    const res = await apiFetch(`/api/email/activate/resend?employeeId=${employeeId}`, {
        method: 'POST'
    });

    if (!res.ok) {
        await sweetError('메일 전송에 실패했습니다.');
        return;
    }

    await sweetSuccess('활성화 메일을 전송했습니다.');
};


function employmentLabel(v) {
    const map = {
        REGULAR: '정규직',
        NON_REGULAR: '비정규직',
        CONTRACT: '계약직'
    };
    return map[v] ?? v;
}

/* =========================
   Audit Logs
========================= */
async function loadAuditLogs(employeeId) {
    const list = document.getElementById('auditLogList');
    list.innerHTML = `<li class="audit-empty">이력을 불러오는 중입니다…</li>`;

    const res = await apiFetch(`/api/admin/employees/${employeeId}/logs`);
    const result = await safeJson(res);

    if (!res.ok || !result?.data?.length) {
        list.innerHTML = `<li class="audit-empty">변경 이력이 없습니다.</li>`;
        return;
    }

    list.innerHTML = '';

    result.data.forEach(log => {

        let body;

        if (log.eventType === 'CREATE') {
            body = renderCreateLog(log.afterData);

        } else if (log.eventType === 'STATUS_CHANGE') {
            body = renderStatusChange(log.beforeData, log.afterData);

        } else if (log.eventType === 'UPDATE') {
            if (!hasDiff(log.beforeData, log.afterData)) return;
            body = renderSideBySideDiff(log.beforeData, log.afterData);

        } else {
            return;
        }

        list.insertAdjacentHTML('beforeend', `
<li class="audit-item audit-${log.eventType.toLowerCase()}">
    <span class="timeline-dot"></span>
    <div class="audit-top">
        <div class="audit-left">
            <span class="badge ${auditBadgeClass(log.eventType)}">
                ${auditTypeLabel(log.eventType)}
            </span>
            <span class="audit-time">${formatRelativeTime(log.createdAt)}</span>
            <span class="audit-actor">· ${log.actorName ?? '알 수 없음'}</span>
        </div>
    </div>
    <details class="audit-detail">
    <summary>${auditSummaryLabel(log.eventType)}</summary>
    ${body}
    </details>
</li>
    `);
    });

}

function auditSummaryLabel(type) {
    if (type === 'CREATE') return '사원 생성 정보';
    if (type === 'UPDATE') return '사원 정보 변경';
    if (type === 'STATUS_CHANGE') return '재직 상태 변경';
    return '변경 상세';
}


function renderSideBySideDiff(beforeData, afterData) {
    const before = toObj(beforeData);
    const after = toObj(afterData);

    const keys = Object.keys({ ...before, ...after });

    if (!keys.length) {
        return `<div class="audit-empty">변경 내용이 없습니다.</div>`;
    }

    return `
<div class="diff-table">
    <div class="diff-head">
        <div>항목</div>
        <div>이전</div>
        <div>이후</div>
    </div>
    ${keys.map(k => {
        const b = before[k];
        const a = after[k];

        const changed = JSON.stringify(b) !== JSON.stringify(a);

        return `
        <div class="diff-row ${changed ? 'changed' : ''}">
            <div class="diff-key">${diffKeyLabel(k)}</div>
            <div class="diff-before">${formatValueByKey(k, b)}</div>
            <div class="diff-after">${formatValueByKey(k, a)}</div>
        </div>`;
    }).join('')}
</div>`;
}


/* =========================
   Status Change
========================= */
function bindStatusButtons(id) {
    btnActive.onclick = () => changeStatus(id, 'ACTIVE');
    btnSuspend.onclick = () => changeStatus(id, 'SUSPENDED');
    btnResign.onclick = () => changeStatus(id, 'RESIGNED');
}

async function changeStatus(id, status) {

    // 이미 같은 상태면 아무 것도 안 함
    if (currentEmployeeStatus === status) {
        return;
    }

    if (status === 'RESIGNED') {
        const result = await sweetConfirm(
            '퇴사 처리',
            '퇴사 처리 시 되돌릴 수 없습니다. 진행하시겠습니까?'
        );
        if (!result.isConfirmed) return;
    }

    const res = await apiFetch(`/api/admin/employees/${id}/status`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({status})
    });

    if (!res.ok) {
        await sweetError('상태 변경에 실패했습니다.');
        return;
    }

    await sweetSuccess('상태가 변경되었습니다.');
    loadEmployeeDetail(id);
    loadAuditLogs(id);
}

/* =========================
   Helpers
========================= */
function renderStatusBadge(status) {
    const map = {
        ACTIVE: ['재직', 'status-active'],
        SUSPENDED: ['정지', 'status-suspended'],
        RESIGNED: ['퇴사', 'status-resigned'],
        TEMP: ['임시', 'status-temp']
    };
    const [text, cls] = map[status] || [status, 'status-temp'];
    empStatus.innerHTML = `<span class="status-pill ${cls}">${text}</span>`;
}

function highlightStatusButton(status) {
    ['btnActive', 'btnSuspend', 'btnResign']
        .forEach(id => document.getElementById(id)?.classList.remove('is-selected'));

    if (status === 'ACTIVE') btnActive.classList.add('is-selected');
    if (status === 'SUSPENDED') btnSuspend.classList.add('is-selected');
    if (status === 'RESIGNED') btnResign.classList.add('is-selected');
}

function formatValueByKey(key, value) {
    if (value == null) return '-';
    if (key === 'orgId') return escapeHtml(orgMap[value] ?? value);
    if (key === 'rankId') return escapeHtml(rankMap[value] ?? value);
    if (key === 'positionCategoryId') return escapeHtml(positionMap[value] ?? value);
    if (key === 'employmentType') return escapeHtml(employmentLabel(value));
    return escapeHtml(String(value));
}

function auditBadgeClass(type) {
    if (type === 'CREATE') return 'badge-create';
    if (type === 'STATUS_CHANGE') return 'badge-status';
    if (type === 'UPDATE') return 'badge-update';
    return 'badge-default';
}

function auditTypeLabel(type) {
    if (type === 'CREATE') return '생성';
    if (type === 'STATUS_CHANGE') return '상태 변경';
    if (type === 'UPDATE') return '정보 변경';
    return type;
}

function statusLabel(s) {
    return {TEMP: '임시', ACTIVE: '재직', SUSPENDED: '정지', RESIGNED: '퇴사'}[s] ?? s;
}

function normalizeStatus(v) {
    return String(v).replaceAll('"', '');
}

function formatRelativeTime(iso) {
    const diff = Date.now() - new Date(iso);
    const m = Math.floor(diff / 60000);
    if (m < 1) return '방금 전';
    if (m < 60) return `${m}분 전`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}시간 전`;
    return `${Math.floor(h / 24)}일 전`;
}

function setText(id, v) {
    const el = document.getElementById(id);
    if (el) el.textContent = v ?? '-';
}

function safeJson(res) {
    return res.json().catch(() => null);
}

function escapeHtml(s) {
    return String(s).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
}

function toast(msg) {
    const el = document.createElement('div');
    el.className = 'toast show';
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 1500);
}

/* =========================
   Edit Modal
========================= */
async function openEditModal() {

    // 퇴사 사원은 수정 모달 진입 불가
    if (currentEmployeeStatus === 'RESIGNED') {
        await sweetError('퇴사한 사원의 정보는 수정할 수 없습니다.');
        return;
    }

    const modal = document.getElementById('editEmployeeModal');
    const employeeId = document.getElementById('employeeId').value;

    const res = await apiFetch(`/api/admin/employees/${employeeId}/edit`);
    const result = await safeJson(res);
    if (!res.ok) {
        await sweetError('사원 정보 조회에 실패했습니다.');
        return;
    }

    const e = result.data;

    // input
    editName.value = e.name ?? '';
    editPhone.value = e.phone ?? '';
    editInternalPhone.value = e.internalPhone ?? '';
    editBirthDate.value = e.birthDate ?? '';
    editHireDate.value = e.hireDate ?? '';
    editGender.value = e.gender ?? '';

    // lookup 먼저
    await loadEditLookupsFromDetail();

    // select value 세팅
    editOrgId.value = String(e.orgId ?? '');
    editRankId.value = String(e.rankId ?? '');
    editEmploymentType.value = e.employmentType ?? '';
    console.log('API role raw:', e.role);
    console.log('API role type:', typeof e.role);
    console.log('API role length:', e.role?.length);

    editRole.value = e.role ?? '';

    console.log('select value after set:', editRole.value);

    // 직책은 org 기준으로 option 만든 후 value
    await loadEditPositionsByOrg(e.orgId);
    editPositionCategoryId.value = String(e.positionCategoryId ?? '');

    modal.classList.remove('hidden');
}


async function loadEditLookupsFromDetail() {
    const [orgRes, rankRes] = await Promise.all([
        apiFetch('/api/admin/organizations'),
        apiFetch('/api/admin/ranks')
    ]);

    const orgs = (await orgRes.json())?.data ?? [];
    const ranks = (await rankRes.json())?.data ?? [];

    editOrgId.innerHTML = `<option value="">선택</option>`;
    orgs.forEach(o =>
        editOrgId.insertAdjacentHTML(
            'beforeend',
            `<option value="${o.id}">${o.name}</option>`
        )
    );

    editRankId.innerHTML = `<option value="">선택</option>`;
    ranks.forEach(r =>
        editRankId.insertAdjacentHTML(
            'beforeend',
            `<option value="${r.id}">${r.name}</option>`
        )
    );
}



editOrgId.addEventListener('change', async e => {
    const newOrgId = e.target.value;

    // 직책 초기화
    editPositionCategoryId.value = '';
    if (positionResetNotice) {
        positionResetNotice.style.display = 'block';
    }

    await loadEditPositionsByOrg(newOrgId, null);
});





async function loadEditPositionsByOrg(orgId) {
    const select = editPositionCategoryId;
    select.innerHTML = `<option value="">선택</option>`;

    if (!orgId) return;

    const res = await apiFetch(`/api/admin/org-position-policies/${orgId}`);
    const result = await res.json().catch(() => null);
    const positions = result?.data ?? [];

    positions.forEach(p => {
        select.insertAdjacentHTML(
            'beforeend',
            `<option value="${String(p.positionCategoryId)}">
                ${p.positionCategoryName}
            </option>`
        );
    });
}



function closeEditModal() {
    const modal = document.getElementById('editEmployeeModal');
    if (!modal) return;
    modal.classList.add('hidden');
}

async function saveEdit() {
    const employeeId = document.getElementById('employeeId').value;

    const payload = {
        name: editName.value || null,
        phone: editPhone.value || null,
        internalPhone: editInternalPhone.value || null,
        birthDate: editBirthDate.value || null,
        hireDate: editHireDate.value || null,
        gender: editGender.value || null,

        orgId: editOrgId.value ? Number(editOrgId.value) : null,
        rankId: editRankId.value ? Number(editRankId.value) : null,
        positionCategoryId: editPositionCategoryId.value
            ? Number(editPositionCategoryId.value)
            : null,

        employmentType: editEmploymentType.value || null,
        role: editRole.value || null
    };

    const res = await apiFetch(`/api/admin/employees/${employeeId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        await sweetError('사원 정보 수정에 실패했습니다.');
        return;
    }

    await sweetSuccess('사원 정보가 수정되었습니다.');
    closeEditModal();
    loadEmployeeDetail(employeeId);
    loadAuditLogs(employeeId);
}


function toObj(v) {
    if (v == null) return {};
    if (typeof v === 'object') return v;
    if (typeof v === 'string') {
        try {
            return JSON.parse(v);
        } catch {
            return {};
        }
    }
    return {};
}

function hasDiff(beforeData, afterData) {
    const b = toObj(beforeData);
    const a = toObj(afterData);
    return Object.keys({...b, ...a})
        .some(k => JSON.stringify(b[k]) !== JSON.stringify(a[k]));
}

function renderStatusChange(before, after) {
    const b = toObj(before).status ?? before;
    const a = toObj(after).status ?? after;

    const bs = String(b).replaceAll('"', '');
    const as = String(a).replaceAll('"', '');

    return `
<div class="status-diff">
  <span class="status-pill status-${bs.toLowerCase()}">${statusLabel(bs)}</span>
  <span class="status-arrow">→</span>
  <span class="status-pill status-${as.toLowerCase()}">${statusLabel(as)}</span>
</div>`;
}

function renderCreateLog(afterData) {
    const a = toObj(afterData);

    return `
<div class="diff-table">
    <div class="diff-head">
        <div>항목</div>
        <div>값</div>
    </div>
    ${Object.entries(a).map(([k, v]) => `
        <div class="diff-row">
            <div class="diff-key">${diffKeyLabel(k)}</div>
            <div class="diff-after">${formatValueByKey(k, v)}</div>
        </div>
    `).join('')}
</div>`;
}

function diffKeyLabel(k) {
    const map = {
        orgId: '조직',
        rankId: '직급',
        positionCategoryId: '직책',
        employmentType: '고용형태',
        role: '권한',
        hireDate: '입사일',
        phone: '연락처',
        internalPhone: '내선번호',
        gender: '성별',
        birthDate: '생년월일'
    };
    return map[k] ?? k;
}

const map = {

};
