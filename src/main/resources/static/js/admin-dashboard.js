let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL'
};

document.addEventListener('DOMContentLoaded', function() {
    // 오늘 날짜 표시
    const today = new Date();
    const formattedToday = today.getFullYear() + '.' + (today.getMonth() + 1) + '.' + today.getDate();
    document.getElementById('todayLabel').innerText = `금일 요약 현황 (${formattedToday})`;

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
            // HTML에 존재하는 요소들만 업데이트
            document.getElementById('totalEmployees').innerText = data.totalEmployees;
            document.getElementById('onTimeCount').innerText = data.onTimeCount;
            document.getElementById('lateCount').innerText = data.lateCount;
            document.getElementById('notLeavingCount').innerText = data.notLeavingCount;
            // pendingRequestCount 업데이트 코드는 삭제
        }
    } catch (error) {
        console.error("통계 데이터 로드 실패:", error);
    }
}

/**
 * 근태 목록 데이터 로드
 */
async function loadAttendanceList() {
    const { page, size, startDate, endDate, status } = currentSearchParams;
    const url = `/api/admin/attendance/list?page=${page}&size=${size}&startDate=${startDate}&endDate=${endDate}&status=${status}`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            renderAttendanceTable(data.content);
            renderPagination(data);
        }
    } catch (error) {
        console.error("목록 로드 실패:", error);
    }
}

/**
 * 테이블 렌더링
 */
function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        // 지각일 경우 시각에 주황색 강조
        const commuteStyle = item.statusCode === 'LATE' ? 'color: #ff9800; font-weight: bold;' : '';

        return `
            <tr>
                <td>${item.employeeName} (${item.employeeNum})</td>
                <td style="${commuteStyle}">${item.commuteAt}</td>
                <td>${item.leaveAt}</td>
                <td>${item.workingTime}</td>
                <td><span class="status-badge ${item.statusCode}">${item.statusName}</span></td>
                <td>
                    <button class="btn-table-action" onclick="openCorrectionModal(${item.attendanceId}, '${item.statusCode}')">
                        ${item.isCorrected ? '수정됨' : '정정'}
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * 검색 처리
 */
function handleSearch() {
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.page = 0;
    loadAttendanceList();
}

/**
 * 모달 제어 및 정정 제출
 */
function openCorrectionModal(id, status) {
    document.getElementById('targetAttendanceId').value = id;
    document.getElementById('modalStatus').value = status;
    document.getElementById('correctionModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('correctionModal').style.display = 'none';
    document.getElementById('modalReason').value = '';
}

async function submitCorrection() {
    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;
    const reason = document.getElementById('modalReason').value;

    if (!reason.trim()) {
        alert("정정 사유를 입력해주세요.");
        return;
    }

    const response = await fetch(`/api/admin/attendance/update/${id}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
        },
        body: JSON.stringify({ status: status, correctionReason: reason })
    });

    if (response.ok) {
        alert("성공적으로 정정되었습니다.");
        closeModal();
        loadAttendanceList();
        loadSummaryData();
    }
}

/**
 * 페이징 렌더링
 */
function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    container.innerHTML = '';

    for (let i = 0; i < pageData.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `page-btn ${i === pageData.number ? 'active' : ''}`;
        btn.onclick = () => {
            currentSearchParams.page = i;
            loadAttendanceList();
        };
        container.appendChild(btn);
    }
}