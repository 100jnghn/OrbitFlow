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
let hoverCloseTimer = null;

document.addEventListener('DOMContentLoaded', function () {
    // 1. 날짜 표시 로직
    const today = new Date();
    const formattedToday = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;

    // 2. 정정 모달 글자 수 카운터
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

    // 3. 이벤트 위임 방식으로 마우스 오버 핸들링
    const table = document.getElementById('attendanceTable');
    if (table) {
        table.addEventListener('mouseover', function (e) {
            const target = e.target.closest('.badge-corrected');
            if (target) {
                handleCorrectedMouseOver(e, target);
            }
        });
        table.addEventListener('mouseout', handleCorrectedMouseOut);
    }

    // 4. 검색창 엔터키 이벤트
    const searchInput = document.getElementById('searchKeyword');
    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') handleSearch();
        });
    }

    // [추가] 시작일 변경 시 종료일 최소값 설정
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (startDateInput && endDateInput) {
        startDateInput.addEventListener('change', function () {
            endDateInput.min = this.value;
            if (endDateInput.value && endDateInput.value < this.value) {
                endDateInput.value = ''; // 시작일보다 이전 날짜 선택 시 초기화
            }
        });
    }

    // 5. 초기 데이터 로드
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
    const params = new URLSearchParams({ page, size, startDate, endDate, status: status || 'ALL', keyword: keyword || '' });

    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        });
        const result = await response.json();

        if (response.ok && result.data) {
            renderAttendanceTable(result.data.content || []);
            renderPagination(result.data);
        }
    } catch (e) {
        console.error("목록 로드 중 오류:", e);
    }
}

/**
 * 테이블 렌더링 (정정 상태 로직 포함)
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

        // [핵심수정] DB의 tinyint(1) 및 Boolean 대응
        const isCorrected = (item.isCorrected === true || item.isCorrected === 1 || item.correctionYn === 'Y');
        const reason = item.correctionReason || '사유가 등록되지 않았습니다.';

        const actionBtn = isCorrected
            ? `<div class="badge-corrected" data-reason="${escapeHtml(reason)}">
                <i class="fa-solid fa-circle-info"></i> 정정됨
               </div>`
            : `<button class="btn-table-action" onclick="openCorrectionModal(${item.attendanceId}, '${item.employeeName}', '${statusCode}')">
                <i class="fa-solid fa-pen-to-square"></i> 정정
               </button>`;

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
            </tr>`;
    }).join('');
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
 */
async function submitCorrection() {
    const reason = document.getElementById('modalReason').value.trim();
    if (!reason) {
        showError('modalReasonError', '정정 사유는 필수 입력값입니다.');
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;

    // SweetAlert Confirm (존재 가정)
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
            loadAttendanceList();
            loadSummaryData();
        }
    } catch (e) {
        console.error("정정 요청 실패:", e);
    }
}

/**
 * 마우스 오버 툴팁 처리
 */
function handleCorrectedMouseOver(e, target) {
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
    hoverCloseTimer = setTimeout(closeReasonPopover, 150);
}

function closeReasonPopover() {
    if (reasonPopoverEl) {
        reasonPopoverEl.remove();
        reasonPopoverEl = null;
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

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => { currentSearchParams.page = page - 1; loadAttendanceList(); };
    wrapper.appendChild(prevBtn);

    // 페이지 숫자
    for (let i = 0; i < totalPages; i++) {
        if (totalPages > 10 && (i > 2 && i < totalPages - 3 && Math.abs(i - page) > 2)) continue;
        const btn = document.createElement('button');
        btn.className = `page-number ${i === page ? 'active' : ''}`;
        btn.innerText = i + 1;
        btn.onclick = () => { currentSearchParams.page = i; loadAttendanceList(); };
        wrapper.appendChild(btn);
    }

    // 다음 버튼
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