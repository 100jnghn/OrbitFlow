/**
 * 전역 상태 관리
 */
let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL',
    keyword: '' // 사원명/사번 검색 필드 추가
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

    const params = new URLSearchParams({
        page: page,
        size: size,
        startDate: startDate,
        endDate: endDate,
        status: status,
        keyword: keyword // 검색 키워드 포함
    });

    try {
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            // 서버 응답 구조(Page 객체 vs Array)에 따른 유연한 대처
            let list = data.content || (Array.isArray(data) ? data : []);

            // 클라이언트 사이드 최신순 정렬 (commuteAt 기준)
            if (list.length > 0) {
                list.sort((a, b) => {
                    const dateA = a.commuteAt ? new Date(a.commuteAt) : new Date(0);
                    const dateB = b.commuteAt ? new Date(b.commuteAt) : new Date(0);
                    return dateB - dateA; // 내림차순
                });
            }

            renderAttendanceTable(list);
            renderPagination(data);
        } else {
            const tbody = document.querySelector('#attendanceTable tbody');
            tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="color:red;">데이터를 불러오지 못했습니다. (Error: ' + response.status + ')</td></tr>';
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
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const commuteStyle = item.statusCode === 'LATE' ? 'color: #ff9800; font-weight: bold;' : '';
        // 출근은 했으나 퇴근 안 한 경우 "근무 중" 표시
        const leaveTime = item.leaveAt ? item.leaveAt : '<span style="color: #2196F3;">근무 중</span>';

        return `
            <tr>
                <td><strong>${item.employeeName}</strong><br><small>${item.employeeNum}</small></td>
                <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
                <td>${leaveTime}</td>
                <td>${item.workingTime || '-'}</td>
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
    currentSearchParams.keyword = document.getElementById('searchKeyword').value;
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.page = 0;

    const tbody = document.querySelector('#attendanceTable tbody');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">데이터를 검색 중입니다...</td></tr>';

    loadAttendanceList();
}

/**
 * 모달 및 정정 제출
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
            alert("성공적으로 정정되었습니다.");
            closeModal();
            loadAttendanceList();
            loadSummaryData();
        } else {
            alert("정정 처리에 실패했습니다.");
        }
    } catch (error) {
        console.error("정정 요청 중 오류:", error);
    }
}

/**
 * 페이징 렌더링
 */
function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    if (!container || pageData.totalPages === undefined) return;

    container.innerHTML = '';
    for (let i = 0; i < pageData.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `page-btn ${i === pageData.number ? 'active' : ''}`;
        btn.onclick = () => {
            currentSearchParams.page = i;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        };
        container.appendChild(btn);
    }
}