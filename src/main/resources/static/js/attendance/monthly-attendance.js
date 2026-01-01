let currentParams = { page: 0, size: 31, status: 'ALL' };

document.addEventListener('DOMContentLoaded', function() {
    // 초기 상태: 날짜 필드를 빈 값으로 설정 (연차 조회와 동일)
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    // 날짜 필드는 초기 상태(빈 값)로 설정
    startDateInput.value = '';
    endDateInput.value = '';
    
    // 초기 데이터 로드 (날짜가 없으므로 백엔드에서 현재 월로 처리하여 목록 표시)
    executeSearch();
    
    // 검색 버튼 클릭 이벤트
    document.getElementById('searchBtn').addEventListener('click', () => {
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            currentParams.status = statusFilter.value;
        }
        currentParams.page = 0;
        executeSearch();
    });

    // 초기화 버튼 클릭 이벤트
    document.getElementById('resetBtn').addEventListener('click', () => {
        resetFilters();
    });
});

function executeSearch() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const start = startDateInput.value || null;
    const end = endDateInput.value || null;
    
    // 시작일이 종료일보다 늦은 경우 검증 (잘못된 기간설정 알림)
    if (start && end && start > end) {
        alert('잘못된 기간설정입니다.');
        return;
    }
    
    loadAttendanceData(start, end);
}

function resetFilters() {
    // 필터 초기화 (연차 조회와 동일 - 기간을 완전히 빈 값으로 초기화)
    document.getElementById('statusFilter').value = 'ALL';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    
    // 파라미터 초기화
    currentParams.status = 'ALL';
    currentParams.page = 0;
    
    // 초기 상태로 되돌린 후 데이터 다시 로드 (연차 조회와 동일)
    executeSearch();
}

async function loadAttendanceData(start, end) {
    const { page, size, status } = currentParams;

    let url = `/api/attendance/history/monthly?status=${status}&page=${page}&size=${size}`;
    
    // 기간이 있으면 추가 (연차 조회와 동일한 방식)
    if (start) url += `&startDate=${start}`;
    if (end) url += `&endDate=${end}`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        const res = await response.json();
        
        // 에러 응답 처리
        if (!response.ok) {
            alert(res.message || '근태 내역을 조회하는 중 오류가 발생했습니다.');
            return;
        }

        const data = res.data;

        if (data) {
            document.getElementById('totalWorkHours').innerText = data.summary.totalWorkTimeDisplay || '0h 00m';
            document.getElementById('lateCount').innerText = data.summary.lateCount || 0;
            document.getElementById('absentCount').innerText = data.summary.leaveAbsentCount || 0;
            renderTable(data.pagedData.content);
            renderPagination(data.pagedData);
        }
    } catch (error) {
        console.error('근태 내역 조회 오류:', error);
        alert('근태 내역을 조회하는 중 오류가 발생했습니다.');
    }
}

function renderTable(recs) {
    const tb = document.querySelector('#attendanceTable tbody');
    if (!recs || recs.length === 0) {
        tb.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 100px; color:#999; font-size:15px;">조회된 기록이 없습니다.</td></tr>';
        return;
    }

    tb.innerHTML = recs.map(r => `
        <tr>
            <td style="font-weight:600; white-space: nowrap;">${r.date}</td>
            <td>${r.commuteAt || '-'}</td>
            <td>${r.leaveAt || '-'}</td>
            <td>${r.workingTime || '0h 00m'}</td>
            <td><span class="status-badge ${getBadgeClass(r.statusCode)}">${r.statusName}</span></td>
        </tr>`).join('');
}

function getBadgeClass(c) {
    if (c === 'LATE') return 'badge-late';
    if (c === 'ABSENT') return 'badge-absent';
    if (c === 'ON_TIME') return 'badge-normal';
    if (c === 'VACATION') return 'badge-vacation';
    return 'badge-normal';
}

function renderPagination(pageInfo) {
    const pagination = document.getElementById('boardPagination');
    pagination.innerHTML = '';

    const page = pageInfo.number;
    const totalPages = pageInfo.totalPages;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => {
        if (page > 0) {
            currentParams.page = page - 1;
            executeSearch();
        }
    };
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisible = 5;
    let startPage = Math.max(0, page - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }

    if (startPage > 0) {
        const firstBtn = document.createElement('button');
        firstBtn.className = 'page-number';
        firstBtn.textContent = '1';
        firstBtn.onclick = () => {
            currentParams.page = 0;
            executeSearch();
        };
        pagination.appendChild(firstBtn);

        if (startPage > 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = 'page-number';
        if (i === page) {
            pageBtn.classList.add('active');
        }
        pageBtn.textContent = i + 1;
        pageBtn.onclick = () => {
            currentParams.page = i;
            executeSearch();
        };
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }

        const lastBtn = document.createElement('button');
        lastBtn.className = 'page-number';
        lastBtn.textContent = totalPages;
        lastBtn.onclick = () => {
            currentParams.page = totalPages - 1;
            executeSearch();
        };
        pagination.appendChild(lastBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.className = 'page-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = page >= totalPages - 1;
    nextBtn.onclick = () => {
        if (page < totalPages - 1) {
            currentParams.page = page + 1;
            executeSearch();
        }
    };
    pagination.appendChild(nextBtn);
}
