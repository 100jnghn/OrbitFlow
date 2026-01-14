/**
 * 직원 근태 종합 현황 대시보드 스크립트
 */

let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL',
    keyword: ''
};

document.addEventListener('DOMContentLoaded', function () {
    const today = new Date();
    const formattedToday = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;

    const modalReasonInput = document.getElementById('modalReason');
    const charCountElement = document.getElementById('charCount');

    if (modalReasonInput) {
        modalReasonInput.addEventListener('input', function () {
            clearFieldError('modalReasonError');
            const currentLength = this.value.length;
            const maxLength = 40;

            if (charCountElement) {
                charCountElement.textContent = `${currentLength} / ${maxLength}`;
                charCountElement.style.color = currentLength >= maxLength ? 'var(--danger-color)' : 'var(--text-muted)';
            }
        });
    }

    const searchInput = document.getElementById('searchKeyword');
    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') handleSearch();
        });
    }

    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (startDateInput && endDateInput) {
        startDateInput.addEventListener('change', function () {
            endDateInput.min = this.value;
            if (endDateInput.value && endDateInput.value < this.value) {
                endDateInput.value = '';
            }
        });
    }

    loadSummaryData();
    loadAttendanceList();
});

/**
 * 요약 데이터 로드
 */
async function loadSummaryData() {
    try {
        const response = await fetch('/api/admin/attendance/summary', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const result = await response.json();

        if (response.ok && result.data) {
            const d = result.data;
            document.getElementById('totalEmployees').innerText = d.totalEmployees || 0;
            document.getElementById('onTimeCount').innerText = d.onTimeCount || 0;
            document.getElementById('lateCount').innerText = d.lateCount || 0;
            document.getElementById('absentCount').innerText = d.absentCount || 0;

            if (document.getElementById('vacationCount')) document.getElementById('vacationCount').innerText = d.vacationCount || 0;
            if (document.getElementById('outsideCount')) document.getElementById('outsideCount').innerText = d.outsideCount || 0;
            if (document.getElementById('businessTripCount')) document.getElementById('businessTripCount').innerText = d.businessTripCount || 0;
        }
    } catch (e) {
        console.error("요약 데이터 로드 실패:", e);
    }
}

/**
 * 근태 목록 로드
 */
async function loadAttendanceList() {
    const { page, size, startDate, endDate, status, keyword } = currentSearchParams;
    const params = new URLSearchParams({
        page,
        size,
        startDate,
        endDate,
        status: status || 'ALL',
        keyword: keyword || ''
    });

    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        });
        const result = await response.json();

        if (response.ok && result.data) {
            renderAttendanceTable(result.data.content || []);
            renderPagination(result.data);
        } else {
            console.error("목록 로드 실패:", result);
        }
    } catch (e) {
        console.error("목록 로드 중 오류:", e);
    }
}

/**
 * 테이블 렌더링
 * - 정정 전: [정정] 버튼
 * - 정정 후: [정정됨] + [사유] 버튼(클릭 시 사유 확인)
 */
function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading-state">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const statusCode = item.statusCode || 'BEFORE_WORK';
        const statusName = item.statusName || '근무 예정';
        const commuteStyle = statusCode === 'LATE' ? 'color: var(--warning-color); font-weight:600;' : '';

        const isCorrected = (item.isCorrected === true || item.isCorrected === 1 || item.correctionYn === 'Y');
        const reason = item.correctionReason || '사유가 등록되지 않았습니다.';

        const hasAttendanceId = item.attendanceId !== null && item.attendanceId !== undefined && item.attendanceId !== '';

        let actionBtn = '';

        if (isCorrected) {
            // ✅ 정정됨 + 사유 버튼(동적으로)
            // data-reason에 사유 저장해두고, 클릭하면 showCorrectionReason()로 보여줌
            actionBtn = `
        <div class="corrected-actions">
          <span class="badge-corrected">
            <i class="fa-solid fa-circle-check"></i> 정정됨
          </span>
          <button class="btn-reason"
                  type="button"
                  data-reason="${escapeHtml(reason)}"
                  onclick="showCorrectionReason(this)">
            사유
          </button>
        </div>
      `;
        } else if (hasAttendanceId) {
            actionBtn = `
        <button class="btn-table-action"
                type="button"
                onclick="openCorrectionModal(${item.attendanceId}, '${escapeHtml(item.employeeName)}', '${statusCode}')">
          <i class="fa-solid fa-pen-to-square"></i> 정정
        </button>
      `;
        } else {
            actionBtn = `
        <button class="btn-table-action"
                type="button"
                disabled
                title="근태 기록이 없어 정정할 수 없습니다.">
          <i class="fa-solid fa-pen-to-square"></i> 정정
        </button>
      `;
        }

        return `
      <tr>
        <td>
          <div class="emp-info-cell">
            <div class="emp-name">${item.employeeName}</div>
            <div class="emp-num">${item.employeeNum}</div>
          </div>
        </td>
        <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
        <td>${item.leaveAt || '-'}</td>
        <td>${item.workingTime || '-'}</td>
        <td><span class="status-badge ${statusCode}">${statusName}</span></td>
        <td>${item.workDate}</td>
        <td>${actionBtn}</td>
      </tr>
    `;
    }).join('');
}

/**
 * 사유 보기 버튼 클릭 시: SweetAlert(있으면) / 없으면 alert 로 표시
 */
async function showCorrectionReason(btnEl) {
    const reason = btnEl?.getAttribute('data-reason') || '사유가 등록되지 않았습니다.';

    if (typeof Swal !== 'undefined') {
        await Swal.fire({
            icon: 'info',
            title: '정정 사유',
            text: reason,
            confirmButtonText: '확인'
        });
    } else {
        alert('정정 사유\n\n' + reason);
    }
}

/**
 * 모달 제어
 */
function openCorrectionModal(id, name, status) {
    document.getElementById('targetAttendanceId').value = id;
    document.getElementById('modalStatus').value = status;
    document.getElementById('modalReason').value = '';
    document.getElementById('charCount').textContent = '0 / 40';

    document.getElementById('correctionModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    document.getElementById('correctionModal').style.display = 'none';
    document.body.style.overflow = '';
}

/**
 * 정정 제출
 * - 성공 시 목록 재조회 -> isCorrected=true 내려오면 자동으로 "정정됨 + 사유"로 렌더링됨
 */
async function submitCorrection() {
    const reason = document.getElementById('modalReason').value.trim();
    if (!reason) {
        showError('modalReasonError', '정정 사유는 필수 입력값입니다.');
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;

    if (!id || id === 'null' || id === 'undefined') {
        if (typeof Swal !== 'undefined') {
            await Swal.fire({ icon: 'warning', title: '정정 불가', text: '근태 기록이 없어 정정할 수 없습니다.' });
        } else {
            alert('근태 기록이 없어 정정할 수 없습니다.');
        }
        return;
    }

    if (typeof Swal !== 'undefined') {
        const confirm = await Swal.fire({
            title: '근태 정정',
            text: '정정하시겠습니까?',
            icon: 'question',
            showCancelButton: true
        });
        if (!confirm.isConfirmed) return;
    }

    try {
        const response = await fetch(`/api/admin/attendance/update/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            },
            body: JSON.stringify({ status: status, correctionReason: reason })
        });

        if (response.ok) {
            closeModal();

            // ✅ 재조회 -> 서버에서 isCorrected/correctionReason 내려오면 화면이 "정정됨+사유"로 전환됨
            await loadAttendanceList();
            await loadSummaryData();

            if (typeof Swal !== 'undefined') {
                await Swal.fire({ icon: 'success', title: '정정 완료', text: '근태 정정이 완료되었습니다.' });
            }
        } else {
            const msg = await response.text();
            if (typeof Swal !== 'undefined') {
                await Swal.fire({ icon: 'error', title: '정정 실패', text: msg || '요청이 실패했습니다.' });
            } else {
                alert('정정 실패: ' + (msg || '요청이 실패했습니다.'));
            }
        }
    } catch (e) {
        console.error("정정 요청 실패:", e);
        if (typeof Swal !== 'undefined') {
            await Swal.fire({ icon: 'error', title: '정정 실패', text: '네트워크 오류가 발생했습니다.' });
        } else {
            alert('정정 실패: 네트워크 오류');
        }
    }
}

/**
 * 검색 및 필터
 */
function handleSearch() {
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.page = 0;
    loadAttendanceList();
}

function resetFilters() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('statusFilter').value = 'ALL';
    document.getElementById('searchKeyword').value = '';
    currentSearchParams = { page: 0, size: 10, startDate: '', endDate: '', status: 'ALL', keyword: '' };
    loadAttendanceList();
}

/**
 * 페이지네이션
 */
function renderPagination(pageData) {
    const pagination = document.getElementById('boardPagination');
    if (!pagination || !pageData) return;
    pagination.innerHTML = '';

    const page = pageData.number || 0;
    const totalPages = pageData.totalPages || 0;
    const wrapper = document.createElement('div');
    wrapper.className = 'pagination';

    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => { currentSearchParams.page = page - 1; loadAttendanceList(); };
    wrapper.appendChild(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        if (totalPages > 10 && (i > 2 && i < totalPages - 3 && Math.abs(i - page) > 2)) continue;
        const btn = document.createElement('button');
        btn.className = `page-number ${i === page ? 'active' : ''}`;
        btn.innerText = i + 1;
        btn.onclick = () => { currentSearchParams.page = i; loadAttendanceList(); };
        wrapper.appendChild(btn);
    }

    const nextBtn = document.createElement('button');
    nextBtn.className = 'page-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = page >= totalPages - 1;
    nextBtn.onclick = () => { currentSearchParams.page = page + 1; loadAttendanceList(); };
    wrapper.appendChild(nextBtn);

    pagination.appendChild(wrapper);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

function showError(id, msg) {
    const el = document.getElementById(id);
    if (el) { el.textContent = msg; el.style.display = 'block'; }
}

function clearFieldError(id) {
    const el = document.getElementById(id);
    if (el) { el.textContent = ''; el.style.display = 'none'; }
}

function handleModalBackdropClick(e) {
    if (e.target.id === 'correctionModal') closeModal();
}
