// 전역 변수 설정
let currentParams = { page: 0, size: 10, status: 'ALL' };

document.addEventListener('DOMContentLoaded', function() {
    const monthSelect = document.getElementById('monthSelect');
    const statusFilter = document.getElementById('statusFilter');

    // 1. 초기 연월 설정 및 데이터 로드
    if (monthSelect) {
        const [year, month] = monthSelect.value.split('-');
        loadMonthlyHistory(year, month);

        // 연월 변경 시 호출
        monthSelect.addEventListener('change', function() {
            const [y, m] = this.value.split('-');
            currentParams.page = 0; // 페이지 초기화
            loadMonthlyHistory(y, m);
        });
    }

    // 2. 상태 필터 변경 시 호출 (서버 사이드 필터링)
    if (statusFilter) {
        statusFilter.addEventListener('change', (e) => {
            currentParams.status = e.target.value;
            currentParams.page = 0; // 필터 변경 시 첫 페이지로 리셋
            const [y, m] = monthSelect.value.split('-');
            loadMonthlyHistory(y, m);
        });
    }
});

/**
 * 월별 근태 현황 로드 (인증 토큰 및 서버 필터 파라미터 포함)
 */
async function loadMonthlyHistory(year, month) {
    const token = sessionStorage.getItem('accessToken');
    const { page, size, status } = currentParams;

    // API URL에 필터(status)와 페이징 정보를 포함
    const url = `/api/attendance/history/monthly?year=${year}&month=${month}&status=${status}&page=${page}&size=${size}&sort=workDate,desc`;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();

            // 상단 요약 업데이트 (MonthlyHistoryResDto.summary 참조)
            if (data.summary) {
                updateSummaryUI(data.summary);
            }

            // 하단 목록 업데이트 (MonthlyHistoryResDto.pagedData 참조)
            if (data.pagedData) {
                renderHistoryTable(data.pagedData.content);
                renderPagination(data.pagedData);
            }
        } else if (response.status === 401 || response.status === 403) {
            alert("인증이 만료되었습니다. 다시 로그인해주세요.");
            location.href = "/login";
        }
    } catch (error) {
        console.error("데이터 로드 실패:", error);
    }
}

/**
 * 테이블 목록 렌더링 (상태별 뱃지 스타일 적용)
 */
function renderHistoryTable(records) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 50px;">해당 조건의 기록이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(record => {
        // DB 상태값(statusCode)에 따른 CSS 클래스 매핑
        let badgeClass = 'badge-normal';
        if (record.statusCode === 'LATE') badgeClass = 'badge-late';
        if (record.statusCode === 'ABSENT') badgeClass = 'badge-absent';
        if (record.statusCode === 'BEFORE_WORK') badgeClass = 'badge-before';

        return `
            <tr>
                <td>${record.date}</td>
                <td>${record.commuteAt}</td>
                <td>${record.leaveAt}</td>
                <td>${record.workingTime}</td>
                <td><span class="status-badge ${badgeClass}">${record.statusName}</span></td>
            </tr>
        `;
    }).join('');
}

/**
 * 상단 요약 UI 업데이트
 */
function updateSummaryUI(summary) {
    document.getElementById('totalWorkHours').innerText = summary.totalWorkHours || 0;
    document.getElementById('lateCount').innerText = summary.lateCount || 0;
    document.getElementById('absentCount').innerText = summary.leaveAbsentCount || 0;
}

/**
 * 페이징 버튼 렌더링
 */
function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    if (!container) return;
    container.innerHTML = '';

    for (let i = 0; i < pageData.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `page-btn ${i === pageData.number ? 'active' : ''}`;
        btn.onclick = () => {
            currentParams.page = i;
            const monthSelect = document.getElementById('monthSelect');
            const [y, m] = monthSelect.value.split('-');
            loadMonthlyHistory(y, m);
        };
        container.appendChild(btn);
    }
}