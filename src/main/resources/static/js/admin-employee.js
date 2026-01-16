/**
 * admin-employee.js
 * - 사원 목록 / 생성 / 수정
 */

let emailChecked = false;
// let internalPhoneChecked = true; // optional
let isSavingEmployee = false;

const saveEmployeeBtn =
    document.querySelector('#employeeModal .modal-actions .btn-primary');

function showMsg(el, message, type) {
    el.textContent = message;
    el.className = 'hint ' + type;
}

function validateEmpName() {
    const v = empName.value.trim();

    if (!v) {
        empNameMsg.textContent = '';
        return false;
    }

    showMsg(empNameMsg, `${v.length}/50`, 'success');
    return true;
}

empName.addEventListener('input', validateEmpName);


function validateEmpNo() {
    const v = empNo.value.trim();

    if (!v) {
        empNoMsg.textContent = '';
        // employeeNoChecked = false;
        return false;
    }

    showMsg(empNoMsg, `${v.length}/20`, 'success');
    // employeeNoChecked = false; // 입력 바뀌면 다시 체크 필요
    return true;
}

empNo.addEventListener('input', validateEmpNo);

function validateEmpEmail() {
    const v = empEmail.value.trim();

    if (!v) {
        empEmailMsg.textContent = '';
        emailChecked = false;
        return false;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
        showMsg(empEmailMsg, '이메일 형식이 올바르지 않습니다.', 'error');
        emailChecked = false;
        return false;
    }

    showMsg(empEmailMsg, '형식이 올바릅니다. 중복 확인 필요', 'success');
    return true;
}

empEmail.addEventListener('input', () => {
    emailChecked = false;
    validateEmpEmail();
});


empEmail.addEventListener('blur', checkEmpEmailDuplicate);

async function checkEmpEmailDuplicate() {
    if (!validateEmpEmail()) return;

    try {
        const res = await apiFetch(
            `/api/admin/employees/check-email?email=${encodeURIComponent(empEmail.value)}`
        );
        const json = await res.json();

        if (!json.data.available) {
            showMsg(empEmailMsg, '이미 사용 중인 이메일입니다.', 'error');
            emailChecked = false;
        } else {
            showMsg(empEmailMsg, '사용 가능한 이메일입니다.', 'success');
            emailChecked = true;
        }

    } catch {
        showMsg(empEmailMsg, '이메일 확인 중 오류 발생', 'error');
        emailChecked = false;
    }
}

function validatePhone(v, msgEl, max = 20) {
    if (!v) {
        msgEl.textContent = '';
        return true;
    }

    showMsg(msgEl, `입력됨 (${v.length}/${max})`, 'success');
    return true;
}

function validateInternalPhone(v, msgEl, max = 10) {
    if (!v) {
        msgEl.textContent = '';
        return true;
    }

    showMsg(msgEl, `입력됨 (${v.length}/${max})`, 'success');
    return true;
}


empPhone.addEventListener('input', () => {
    empPhone.value = empPhone.value.replace(/[^0-9]/g, '');
    validatePhone(empPhone.value, empPhoneMsg, 20);
});

empInternalPhone.addEventListener('input', () => {
    empInternalPhone.value = empInternalPhone.value.replace(/[^0-9]/g, '');
    validateInternalPhone(empInternalPhone.value, empInternalPhoneMsg, 10);
});



function resetCreateValidation() {
    [
        empNameMsg,
        empNoMsg,
        empEmailMsg,
        empPhoneMsg,
        empInternalPhoneMsg
    ].forEach(el => el.textContent = '');

    emailChecked = false;
}


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
                <td class="col-org" title="${e.orgPath}"><span class="employee-org-path">${e.orgPath}</span></td>
                <td>${e.rankName ?? '-'}</td>
                <td>${e.positionName ?? '-'}</td>
                <td class="col-status"><span class="status-badge ${e.status}">${statusLabel(e.status)}</span></td>
              </tr>
            `);
        });

        renderPagination(result.data);

    } catch (e) {
        console.error(e);
        await sweetError('사원 목록 조회 중 오류가 발생했습니다.');
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

    resetCreateForm();
    await loadLookupsForCreate();
}

function resetCreateForm() {
    // text / date / select 값 초기화
    [
        empName,
        empEmail,
        empNo,
        empHireDate,
        empGender,
        empPhone,
        empInternalPhone,
        empBirthDate,
        empOrgId,
        empEmploymentType,
        empRole,
        empRankId
    ].forEach(el => {
        if (el) el.value = '';
    });

    // 직책 초기화
    resetPositionSelect();

    // 검증 메시지 초기화
    resetCreateValidation();

    // UX 안내 숨김
    document.getElementById('createPositionResetNotice').style.display = 'none';
}



function closeEmployeeModal() {
    document.getElementById('employeeModal').classList.add('hidden');
}

/* =========================
   Create
========================= */
async function saveEmployee() {
    // 연타 방지
    if (isSavingEmployee) return;
    isSavingEmployee = true;
    saveEmployeeBtn.disabled = true;

    try {
        if (!emailChecked) {
            showMsg(empEmailMsg, '이메일 중복 확인이 필요합니다.', 'error');
            empEmail.focus();
            return;
        }

        if (
            !validateEmpName() ||
            !validateEmpNo() ||
            !validateEmpEmail()
        ) {
            return sweetWarning('입력값을 다시 확인해주세요.');
        }

        if (
            !empHireDate.value ||
            !empGender.value ||
            !empOrgId.value ||
            !empEmploymentType.value ||
            !empRole.value ||
            !empRankId.value
        ) {
            return sweetWarning('필수 항목(*)을 모두 입력해 주세요.');
        }

        const payload = {
            name: empName.value.trim(),
            employeeNo: empNo.value.trim(),
            email: empEmail.value.trim(),
            hireDate: empHireDate.value,
            gender: empGender.value,
            phone: empPhone.value?.trim() || null,
            internalPhone: empInternalPhone.value?.trim() || null,
            birthDate: empBirthDate.value || null,
            orgId: Number(empOrgId.value),
            rankId: empRankId.value || null,
            positionCategoryId: empPositionCategoryId.value || null,
            employmentType: empEmploymentType.value,
            role: empRole.value
        };

        const res = await apiFetch('/api/admin/employees', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => null);
            throw new Error(err?.message);
        }

        closeEmployeeModal();
        await sweetSuccess('사원이 생성되었습니다.');
        loadEmployees(0);

    } catch (e) {
        sweetError(e.message || '사원 생성 실패');

    } finally {
        // 복구
        isSavingEmployee = false;
        saveEmployeeBtn.disabled = false;
    }
}



function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    const wrapper = container.closest('.pagination-container');
    container.innerHTML = '';

    if (!pageData || pageData.totalPages <= 1) {
        wrapper.style.display = 'none';
        return;
    }

    wrapper.style.display = 'flex';

    const {number, totalPages, first, last} = pageData;

    // 이전 버튼
    const prev = document.createElement('button');
    prev.textContent = '<';
    prev.disabled = first;
    prev.onclick = () => loadEmployees(number - 1);
    container.appendChild(prev);

    // 페이지 번호
    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;

        if (i === number) {
            btn.classList.add('active');
        }

        btn.onclick = () => loadEmployees(i);
        container.appendChild(btn);
    }

    // 다음 버튼
    const next = document.createElement('button');
    next.textContent = '>';
    next.disabled = last;
    next.onclick = () => loadEmployees(number + 1);
    container.appendChild(next);
}


function statusLabel(s) {
    return {
        ACTIVE: '재직',
        SUSPENDED: '정지',
        RESIGNED: '퇴사',
        TEMP: '임시'
    }[s] ?? s;
}

document.addEventListener('DOMContentLoaded', () => {
    const today = new Date().toISOString().split('T')[0];
    const birth = document.getElementById('empBirthDate');
    if (birth) birth.max = today;
});
