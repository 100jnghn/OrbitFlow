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
    // [기능: 금일 요약 현황 날짜 표시]
    const today = new Date();
    const formattedToday = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;

    // [기능: 정정 사유 글자수 제한 및 카운터]
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

    // [기능: 근태 규칙 반영 - 정정됨 호버 이벤트 위임]
    const table = document.getElementById('attendanceTable');
    if (table) {
        table.addEventListener('mouseover', handleCorrectedMouseOver);
        table.addEventListener('mouseout', handleCorrectedMouseOut);
    }

    // [기능: 직원 검색 엔터키 지원]
    const searchInput = document.getElementById('searchKeyword');
    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') handleSearch();
        });
    }

    // 초기 데이터 로드
    loadSummaryData();
    loadAttendanceList();
});

/**
 * [기능: 출근 완료/지각/결근/기타 현황 집계]
 */
async function loadSummaryData() {
    try {
        const response = await fetch('/api/admin/attendance/summary', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const result = await response.json();

        if (response.ok && result.data) {
            const d = result.data;
            // 상단 메인 카드
            document.getElementById('totalEmployees').innerText = d.totalEmployees || 0;
            document.getElementById('onTimeCount').innerText = d.onTimeCount || 0;
            document.getElementById('lateCount').innerText = d.lateCount || 0;
            document.getElementById('absentCount').innerText = d.absentCount || 0;

            // 기타 현황 바 (휴가/외근/출장/예정)
            if (document.getElementById('vacationCount')) document.getElementById('vacationCount').innerText = d.vacationCount || 0;
            if (document.getElementById('outsideCount')) document.getElementById('outsideCount').innerText = d.outsideCount || 0;
            if (document.getElementById('businessTripCount')) document.getElementById('businessTripCount').innerText = d.businessTripCount || 0;
            if (document.getElementById('beforeWorkCount')) document.getElementById('beforeWorkCount').innerText = d.beforeWorkCount || 0;
        }
    } catch (e) {
        console.error("요약 데이터 로드 실패:", e);
    }
}

/**
 * [기능: 기간별/상태별/사원 검색 근태 내역 조회]
 */
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
            renderPagination(result.data); // [기능: 페이징 처리]
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

        // ✅ 정정 버튼 디자인 수정: 일정 관리의 버튼 스타일 적용
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

/**
 * [기능: 정정버튼 작동 및 모달 제어]
 */
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

async function submitCorrection() {
    const reason = document.getElementById('modalReason').value.trim();
    if (!reason) {
        showError('modalReasonError', '정정 사유는 필수 입력값입니다.');
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;

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
            alert(result.message || "성공적으로 정정되었습니다.");
            closeModal();
            loadAttendanceList(); // [기능: 실시간 상태 업데이트]
            loadSummaryData();    // 통계 동기화
        } else {
            alert(result.message || "정정 처리에 실패했습니다.");
        }
    } catch (e) {
        alert("네트워크 오류가 발생했습니다.");
    }
}

/**
 * [기능: 초기화 버튼 - 필터링 항목 전체 초기화]
 */
function resetFilters() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('statusFilter').value = 'ALL';
    document.getElementById('searchKeyword').value = '';

    currentSearchParams = { page: 0, size: 10, startDate: '', endDate: '', status: 'ALL', keyword: '' };
    loadAttendanceList();
}

function handleSearch() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // [유효성 검사] 시작일이 종료일보다 늦을 경우 경고
    if (startDate && endDate && startDate > endDate) {
        alert("종료일은 시작일보다 빠를 수 없습니다.");
        return;
    }

    currentSearchParams.startDate = startDate;
    currentSearchParams.endDate = endDate;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.page = 0;
    loadAttendanceList();
}

/**
 * [기능: 근태 규칙 반영 - 정정 사유 팝오버]
 */
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

/**
 * 유틸리티 함수
 */
/**
 * [기능: 심플 페이지네이션 렌더링]
 */
function renderPagination(pageData) {
    const pagination = document.getElementById('boardPagination');
    if (!pagination || !pageData) return;

    pagination.innerHTML = '';

    const page = pageData.number || 0;
    const totalPages = pageData.totalPages || 0;

    const wrapper = document.createElement('div');
    wrapper.className = 'pagination';

    // 1. [이전] 화살표
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => {
        currentSearchParams.page = page - 1;
        loadAttendanceList();
    };
    wrapper.appendChild(prevBtn);

    // 2. 숫자 버튼 (간결하게 현재 페이지 주변 노출)
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

    // 3. [다음] 화살표
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