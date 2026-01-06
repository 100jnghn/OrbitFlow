/**
 * 전역 상태 관리
 */
let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL',
    keyword: ''
};

document.addEventListener('DOMContentLoaded', function() {
    // 오늘 날짜 표시
    const today = new Date();
    const formattedToday = today.getFullYear() + '.' +
        String(today.getMonth() + 1).padStart(2, '0') + '.' +
        String(today.getDate()).padStart(2, '0');

    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) {
        todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;
    }

    // 정정 모달 정정 사유 실시간 검증 및 글자 수 카운터
    const modalReasonInput = document.getElementById('modalReason');
    const charCountElement = document.getElementById('charCount');
    if (modalReasonInput) {
        modalReasonInput.addEventListener('input', function() {
            clearFieldError('modalReasonError', 'modalReason');
            const reason = this.value;
            const currentLength = reason.length;
            const maxLength = 40;

            if (charCountElement) {
                charCountElement.textContent = `${currentLength} / ${maxLength}`;
                if (currentLength >= maxLength) {
                    charCountElement.style.color = 'var(--danger-color)';
                } else if (currentLength >= maxLength * 0.8) {
                    charCountElement.style.color = '#f59e0b';
                } else {
                    charCountElement.style.color = 'var(--neutral-500)';
                }
            }

            if (reason.trim() && reason.length > maxLength) {
                showError('modalReasonError', `정정 사유는 ${maxLength}자 이하여야 합니다.`);
            }
        });
    }

    // 초기 데이터 로드
    loadSummaryData();
    loadAttendanceList();
});

/**
 * 상단 통계 데이터 로드
 */
async function loadSummaryData() {
    try {
        const response = await fetch('/api/admin/attendance/summary', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        if (response.ok) {
            const data = await response.json();
            document.getElementById('totalEmployees').innerText = data.totalEmployees || 0;
            document.getElementById('onTimeCount').innerText = data.onTimeCount || 0;
            document.getElementById('lateCount').innerText = data.lateCount || 0;
            document.getElementById('notLeavingCount').innerText = data.notLeavingCount || 0;
        }
    } catch (error) {
        console.error("통계 데이터 로드 실패:", error);
    }
}

/**
 * 근태 목록 데이터 로드
 */
async function loadAttendanceList() {
    const { page, size, startDate, endDate, status, keyword } = currentSearchParams;
    const params = new URLSearchParams({ page, size, startDate, endDate, status, keyword });

    try {
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            const list = data.content || (Array.isArray(data) ? data : []);
            renderAttendanceTable(list);
            renderPagination(data);
        }
    } catch (error) {
        console.error("목록 로드 실패:", error);
    }
}

/**
 * 테이블 렌더링 (정정됨 버튼 + 사유 버튼 구조 적용)
 */
function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const commuteStyle = item.statusCode === 'LATE' ? 'color: #ff9800; font-weight: bold;' : '';
        let leaveTimeDisplay = item.leaveAt || '-';
        if (item.commuteAt && item.commuteAt !== '-' && (!item.leaveAt || item.leaveAt === '-')) {
            leaveTimeDisplay = '<span style="color: #2196F3; font-weight: bold;">근무 중</span>';
        }

        // 정정 상태에 따른 버튼 처리
        let actionButtons = '';
        if (item.isCorrected) {
            actionButtons = `
                <div class="action-group">
                    <span class="badge-corrected">정정됨</span>
                    <button class="btn-view-reason" onclick="showReasonPopup('${escapeHtml(item.correctionReason)}')">사유</button>
                </div>
            `;
        } else {
            actionButtons = `
                <button class="btn-table-action" onclick="openCorrectionModal(${item.attendanceId}, '${item.statusCode}')">정정</button>
            `;
        }

        return `
            <tr>
                <td><strong>${item.employeeName}</strong><br><small>${item.employeeNum}</small></td>
                <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
                <td>${leaveTimeDisplay}</td>
                <td>${item.workingTime || '-'}</td>
                <td><span class="status-badge ${item.statusCode}">${item.statusName}</span></td>
                <td>${item.workDate || '-'}</td>
                <td>${actionButtons}</td>
            </tr>
        `;
    }).join('');
}

/**
 * 정정 사유 팝업창 표시
 */
function showReasonPopup(reason) {
    if (!reason || reason === 'undefined') {
        alert("등록된 정정 사유가 없습니다.");
        return;
    }
    alert(`[근태 정정 사유]\n\n${reason}`);
}

/**
 * 모달 제어 및 서브밋
 */
function openCorrectionModal(id, status) {
    document.getElementById('targetAttendanceId').value = id;
    document.getElementById('modalStatus').value = status;
    document.getElementById('modalReason').value = '';
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
        showError('modalReasonError', '정정 사유를 입력해주세요.');
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
        if (response.ok) {
            alert("정정되었습니다.");
            closeModal();
            loadAttendanceList();
            loadSummaryData();
        }
    } catch (error) { console.error(error); }
}

/**
 * 검색/필터/유틸리티
 */
function handleSearch() {
    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
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

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showError(id, msg) {
    const err = document.getElementById(id);
    if (err) { err.textContent = msg; err.style.display = 'block'; }
}

function clearFieldError(errId, inputId) {
    const err = document.getElementById(errId);
    if (err) { err.textContent = ''; err.style.display = 'none'; }
}

function renderPagination(pageData) {
    const pagination = document.getElementById('boardPagination');
    if (!pagination || !pageData.totalPages) return;
    pagination.innerHTML = '';
    const page = pageData.number;
    for (let i = 0; i < pageData.totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = i === page ? 'page-number active' : 'page-number';
        btn.textContent = i + 1;
        btn.onclick = () => { currentSearchParams.page = i; loadAttendanceList(); };
        pagination.appendChild(btn);
    }
}