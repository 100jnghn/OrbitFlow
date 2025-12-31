/**
 * admin-employee.js
 * - 사원 목록 / 생성 / 수정
 */

document.addEventListener('DOMContentLoaded', () => {
    loadEmployees();
});

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

function openCreate() {
    document.getElementById('employeeModal').classList.remove('hidden');
}

function closeEmployeeModal() {
    document.getElementById('employeeModal').classList.add('hidden');
}

async function saveEmployee() {
    const id = document.getElementById('employeeId').value;

    const payload = {
        name: empName.value,
        email: empEmail.value,
        employeeNo: empNo.value,
        status: empStatus.value
    };

    try {
        const res = await apiFetch(
            `/api/admin/employees${id ? '/' + id : ''}`,
            {
                method: id ? 'PUT' : 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            }
        );

        await res.json();
        closeEmployeeModal();
        loadEmployees();

    } catch (e) {
        console.error(e);
        alert('사원 저장 실패');
    }
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
