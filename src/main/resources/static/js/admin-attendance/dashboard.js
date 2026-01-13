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

// 툴팁(정정 사유) 전역 상태
let reasonPopoverEl = null;
let reasonPopoverAnchorEl = null;
let hoverCloseTimer = null;

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

    const table = document.getElementById('attendanceTable');
    if (table) {
        table.addEventListener('mouseover', handleCorrectedMouseOver);
        table.addEventListener('mouseout', handleCorrectedMouseOut);
    }

    const searchInput = document.getElementById('searchKeyword');
    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') handleSearch();
        });
    }

    loadSummaryData();
    loadAttendanceList();
});

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
            if (document.getElementById('beforeWorkCount')) document.getElementById('beforeWorkCount').innerText = d.beforeWorkCount || 0;
        }
    } catch (e) {
        console.error("요약 데이터 로드 실패:", e);
    }
}

async function loadAttendanceList() {
    const { page, size, startDate, endDate, status, keyword } = currentSearchParams;
    const params = new URLSearchParams({ page, size, startDate, endDate, status, keyword });

    try {
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const result = await response.json();

        if (response.ok && result.data) {
            renderAttendanceTable(result.data.content || []);
            renderPagination(result.data);
        }
    } catch (e) {
        console.error("근태 목록 로드 실패:", e);
    }
}

function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading-state">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const statusCode = item.statusCode || 'BEFORE_WORK';
        let statusName = item.statusName;

        if (statusCode === 'BEFORE_WORK') statusName = '근무 예정';

        const commuteStyle = statusCode === 'LATE' ? 'color: var(--warning-color); font-weight:600;' : '';

        let leaveDisplay = item.leaveAt || '-';
        if (item.commuteAt && item.commuteAt !== '-' && !item.leaveAt) {
            leaveDisplay = '<span style="color: var(--primary-color); font-weight:600;">근무 중</span>';
        }

        const corrected = item.correctionYn === 'Y' || item.isCorrected;

        let actionBtn = corrected
            ? `<span class="badge-corrected" data-reason="${escapeHtml(item.correctionReason)}">정정됨</span>`
            : `<button class="btn-table-action" onclick="openCorrectionModal(${item.attendanceId}, '${statusCode}')">
                <i class="fa-solid fa-pen-to-square" style="margin-right:4px;"></i>정정
               </button>`;

        return `
            <tr>
                <td>
                    <div style="text-align:left; padding-left:15px;">
                        <div style="font-weight:600; color:var(--text-main);">${item.employeeName}</div>
                        <div style="font-size:12px; color:var(--text-muted);">${item.employeeNum}</div>
                    </div>
                </td>
                <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
                <td>${leaveDisplay}</td>
                <td>${item.workingTime || '-'}</td>
                <td><span class="status-badge ${statusCode}">${statusName}</span></td>
                <td>${item.workDate}</td>
                <td>${actionBtn}</td>
            </tr>`;
    }).join('');
}

function openCorrectionModal(id, status) {
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
 * [수정: SweetAlert2 적용] 정정 사유 제출
 */
async function submitCorrection() {
    const reason = document.getElementById('modalReason').value.trim();
    if (!reason) {
        showError('modalReasonError', '정정 사유는 필수 입력값입니다.');
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;

    // SweetAlert Confirm 추가
    const confirmResult = await sweetConfirm("근태 정정", "입력하신 사유로 근태 정보를 정정하시겠습니까?");
    if (!confirmResult.isConfirmed) return;

    try {
        const response = await fetch(`/api/admin/attendance/update/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            },
            body: JSON.stringify({ status: status, correctionReason: reason })
        });

        const result = await response.json();

        if (response.ok) {
            await sweetSuccess(result.message || "성공적으로 정정되었습니다.");
            closeModal();
            loadAttendanceList();
            loadSummaryData();
        } else {
            sweetError(result.message || "정정 처리에 실패했습니다.");
        }
    } catch (e) {
        sweetError("네트워크 오류가 발생했습니다.");
    }
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
 * [수정: SweetAlert2 적용] 검색 핸들러
 */
async function handleSearch() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (startDate && endDate && startDate > endDate) {
        sweetWarning("종료일은 시작일보다 빠를 수 없습니다.");
        return;
    }

    currentSearchParams.startDate = startDate;
    currentSearchParams.endDate = endDate;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.page = 0;
    loadAttendanceList();
}

function handleCorrectedMouseOver(e) {
    const target = e.target.closest('.badge-corrected');
    if (!target) return;

    clearTimeout(hoverCloseTimer);
    const reason = target.getAttribute('data-reason') || '사유 없음';

    closeReasonPopover();
    const pop = document.createElement('div');
    pop.className = 'reason-popover';
    pop.innerHTML = `<strong>정정 사유</strong><p style="margin:5px 0 0 0;">${escapeHtml(reason)}</p>`;
    document.body.appendChild(pop);

    const rect = target.getBoundingClientRect();
    pop.style.left = `${rect.left}px`;
    pop.style.top = `${rect.bottom + window.scrollY + 5}px`;
    reasonPopoverEl = pop;
}

function handleCorrectedMouseOut() {
    hoverCloseTimer = setTimeout(closeReasonPopover, 100);
}

function closeReasonPopover() {
    if (reasonPopoverEl) {
        reasonPopoverEl.remove();
        reasonPopoverEl = null;
    }
}

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
    prevBtn.onclick = () => {
        currentSearchParams.page = page - 1;
        loadAttendanceList();
    };
    wrapper.appendChild(prevBtn);

    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);
    if (end - start < maxVisible - 1) start = Math.max(0, end - maxVisible + 1);

    for (let i = start; i <= end; i++) {
        const btn = document.createElement('button');
        btn.className = `page-number ${i === page ? 'active' : ''}`;
        btn.innerText = i + 1;
        btn.onclick = () => {
            currentSearchParams.page = i;
            loadAttendanceList();
        };
        wrapper.appendChild(btn);
    }

    const nextBtn = document.createElement('button');
    nextBtn.className = 'page-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = page >= totalPages - 1;
    nextBtn.onclick = () => {
        currentSearchParams.page = page + 1;
        loadAttendanceList();
    };
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