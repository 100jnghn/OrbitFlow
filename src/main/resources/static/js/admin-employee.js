/**
 * admin-employee.js
 * - 사원 목록 / 생성 / 수정
 */

document.addEventListener('DOMContentLoaded', async () => {
    await loadOrgOptions();
    loadEmployees();
});

async function loadOrgOptions() {
    const select = document.getElementById('empOrgId');
    if (!select) return;

    const res = await apiFetch('/api/admin/organizations');
    const result = await res.json().catch(() => null);

    // 우선 data가 리스트라고 가정하고 동일하게 처리.
    const orgs = result?.data ?? [];

    select.innerHTML = `<option value="">선택</option>`;
    orgs.forEach(o => {
        select.insertAdjacentHTML('beforeend', `<option value="${o.id}">${o.name}</option>`);
    });
}

async function loadLookupsForCreate() {
    const rankSelect = document.getElementById('empRankId');
    const posSelect = document.getElementById('empPositionCategoryId');

    if (!rankSelect || !posSelect) return;

    // 직급은 전체
    rankSelect.innerHTML = `<option value="">선택</option>`;
    posSelect.innerHTML = `<option value="">조직을 먼저 선택하세요</option>`;
    posSelect.disabled = true;

    const res = await apiFetch('/api/admin/ranks');
    const ranks = (await res.json())?.data ?? [];

    ranks.forEach(r => {
        rankSelect.insertAdjacentHTML(
            'beforeend',
            `<option value="${r.id}">${r.name}</option>`
        );
    });
}

document.getElementById('empOrgId')?.addEventListener('change', async e => {
    const orgId = e.target.value;

    // 직책 초기화 + UX 안내
    resetPositionSelect();
    document.getElementById('createPositionResetNotice').style.display = 'block';

    if (!orgId) return;

    await loadPositionsByOrg(orgId);
});



async function loadPositionsByOrg(orgId) {
    const select = document.getElementById('empPositionCategoryId');
    if (!select) return;

    select.innerHTML = `<option value="">불러오는 중…</option>`;
    select.disabled = true;

    const res = await apiFetch(`/api/admin/org-position-policies/${orgId}`);
    const result = await res.json().catch(() => null);
    const positions = result?.data ?? [];

    select.innerHTML = '';

    if (positions.length === 0) {
        select.innerHTML = `<option value="">사용 가능한 직책이 없습니다</option>`;
        select.disabled = true;
        return;
    }

    select.insertAdjacentHTML(
        'beforeend',
        `<option value="">선택</option>`
    );

    positions.forEach(p => {
        select.insertAdjacentHTML(
            'beforeend',
            `<option value="${p.positionCategoryId}">
                ${p.positionCategoryName}
             </option>`
        );
    });

    select.disabled = false;
}

function resetPositionSelect() {
    const select = document.getElementById('empPositionCategoryId');
    if (!select) return;

    select.innerHTML = `<option value="">조직을 먼저 선택하세요</option>`;
    select.disabled = true;
}

async function loadEmployees(page = 0) {
    const keyword = document.getElementById('keyword')?.value ?? '';
    const status = document.getElementById('status')?.value ?? '';

    try {
        const res = await apiFetch(
            `/api/admin/employees?keyword=${keyword}&status=${status}&page=${page}&size=10`
        );
        const result = await res.json();

        const tbody = document.getElementById('employeeTbody');
        tbody.innerHTML = '';

        if (!result.data || !result.data.content?.length) {
            tbody.innerHTML = `
              <tr>
                <td colspan="6" style="padding:24px; color:#98a2b3;">
                  조회된 사원이 없습니다.
                </td>
              </tr>
            `;
            document.getElementById('pagination').innerHTML = '';
            return;
        }

        result.data.content.forEach(e => {
            tbody.insertAdjacentHTML('beforeend', `
              <tr onclick="goDetail(${e.id})">
                <td>${e.name}</td>
                <td>${e.email}</td>
                <td>${e.orgPath}</td>
                <td>${e.rankName ?? '-'}</td>
                <td>${e.positionName ?? '-'}</td>
                <td class="status ${e.status}">${e.status}</td>
              </tr>
            `);
        });

        renderPagination(result.data);

    } catch (e) {
        console.error(e);
        alert('사원 목록 조회 중 오류가 발생했습니다.');
    }
}

function goDetail(id) {
    location.href = `/view/admin/employees/${id}`;
}

/* =========================
   Modal
========================= */

async function openCreate() {
    document.getElementById('employeeModal').classList.remove('hidden');

    // 입력값 초기화
    empName.value = '';
    empEmail.value = '';
    empNo.value = '';
    empHireDate.value = '';
    empGender.value = '';
    empOrgId.value = '';
    empEmploymentType.value = '';
    empRole.value = '';
    empRankId.value = '';

    resetPositionSelect();
    document.getElementById('createPositionResetNotice').style.display = 'none';

    await loadLookupsForCreate();
}

function closeEmployeeModal() {
    document.getElementById('employeeModal').classList.add('hidden');
}

/* =========================
   Create
========================= */
async function saveEmployee() {
    const name = empName.value?.trim();
    const email = empEmail.value?.trim();
    const employeeNo = empNo.value?.trim();

    const hireDate = document.getElementById('empHireDate').value;
    const gender = document.getElementById('empGender').value;
    const orgId = document.getElementById('empOrgId').value;
    const employmentType = document.getElementById('empEmploymentType').value;
    const role = document.getElementById('empRole').value;

    if (!name || !email || !employeeNo || !hireDate || !gender || !orgId || !employmentType || !role) {
        alert('필수 항목(*)을 모두 입력해 주세요.');
        return;
    }

    const payload = {
        name,
        email,
        employeeNo,
        hireDate,
        gender,
        orgId: Number(orgId),
        rankId: empRankId.value || null,
        positionCategoryId: empPositionCategoryId.value || null,
        employmentType,
        role
    };

    const res = await apiFetch('/api/admin/employees', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        const err = await res.json().catch(() => null);
        alert(err?.message || '사원 생성 실패');
        return;
    }

    closeEmployeeModal();
    loadEmployees(0);
}



function renderPagination(pageData) {
    const el = document.getElementById('pagination');
    const wrapper = el.closest('.pagination-container');
    el.innerHTML = '';

    if (!pageData || pageData.totalPages <= 1) {
        wrapper.style.display = 'none';
        return;
    }

    wrapper.style.display = 'flex';

    for (let i = 0; i < pageData.totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        btn.className = '';

        if (i === pageData.number) {
            btn.classList.add('active');
        }

        btn.onclick = () => loadEmployees(i);
        el.appendChild(btn);
    }
}
