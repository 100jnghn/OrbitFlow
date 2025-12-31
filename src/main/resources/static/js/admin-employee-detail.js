/**
 * admin-employee-detail.js (FINAL)
 * - CSR 방식 상세 조회
 * - 상태 변경 + 이력 갱신
 */

document.addEventListener('DOMContentLoaded', () => {
    const employeeId = document.getElementById('employeeId')?.value;
    if (!employeeId) return;

    bindStatusButtons(employeeId);
    loadEmployeeDetail(employeeId);
    loadAuditLogs(employeeId);
});

/* =========================
   Detail
========================= */
async function loadEmployeeDetail(employeeId) {
    const res = await apiFetch(`/api/admin/employees/${employeeId}`);
    const result = await safeJson(res);

    if (!res.ok) {
        alert(result?.message || '사원 상세 조회 실패');
        return;
    }

    const e = result.data;

    setText('empName', e.name);
    setText('empEmail', e.email);
    setText('empEmpNo', e.employeeNo);
    setText('empOrg', e.orgPath);
    setText('empRank', e.rankName);
    setText('empPosition', e.positionName);
    setText('empHireDate', e.hireDate);
    setText('empEmploymentType', e.employmentType);

    renderStatusBadge(e.status);
    highlightStatusButton(e.status);
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
        list.insertAdjacentHTML('beforeend', `
          <li class="audit-item">
            <div class="audit-top">
              <div class="audit-left">
                <span class="badge">${log.eventType}</span>
                <span class="audit-time">${formatDateTime(log.createdAt)}</span>
              </div>
            </div>

            ${log.afterData ? `
              <details class="audit-detail" open>
                <summary>변경 내용</summary>
                <pre class="audit-json">${escapeHtml(log.afterData)}</pre>
              </details>` : ''}
          </li>
        `);
    });

    formatAuditJson();
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
    if (status === 'RESIGNED' && !confirm('퇴사 처리 시 되돌릴 수 없습니다. 진행할까요?')) return;

    const res = await apiFetch(`/api/admin/employees/${id}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ status })
    });

    const result = await safeJson(res);
    if (!res.ok) {
        alert(result?.message || '상태 변경 실패');
        return;
    }

    toast('상태가 변경되었습니다.');
    loadEmployeeDetail(id);
    loadAuditLogs(id);
}

/* =========================
   UI Helpers
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
    ['btnActive','btnSuspend','btnResign'].forEach(id =>
        document.getElementById(id)?.classList.remove('is-selected')
    );
    if (status === 'ACTIVE') btnActive.classList.add('is-selected');
    if (status === 'SUSPENDED') btnSuspend.classList.add('is-selected');
    if (status === 'RESIGNED') btnResign.classList.add('is-selected');
}

function setText(id, v) {
    const el = document.getElementById(id);
    if (el) el.textContent = v ?? '-';
}

function formatAuditJson() {
    document.querySelectorAll('.audit-json').forEach(el => {
        try {
            el.textContent = JSON.stringify(JSON.parse(el.textContent), null, 2);
        } catch {}
    });
}

function safeJson(res) { return res.json().catch(() => null); }
function formatDateTime(v) { return v?.replace('T',' ').slice(0,19) ?? '-'; }
function escapeHtml(s) {
    return String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
}

function toast(msg) {
    const el = document.createElement('div');
    el.className = 'toast';
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.classList.add('show'), 10);
    setTimeout(() => el.remove(), 1600);
}
