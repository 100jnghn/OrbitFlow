let currentParams = { page: 0, size: 10, status: 'ALL' }; // ✅ 기본값 변경

document.addEventListener('DOMContentLoaded', function() {
    const dropdown = document.getElementById('monthDropdown');
    const monthList = document.getElementById('monthList');
    const selectedText = document.getElementById('selectedMonthText');
    const searchType = document.getElementById('searchType');

    // 1. 방식 전환 (월별 vs 기간설정)
    searchType.addEventListener('change', function(e) {
        const isRange = e.target.value === 'RANGE';
        document.getElementById('monthPickerWrapper').style.display = isRange ? 'none' : 'block';
        document.getElementById('rangePickerWrapper').style.display = isRange ? 'flex' : 'none';
        // 검색 타입 변경 시 자동 검색하지 않음 (검색 버튼 클릭 시에만 검색)
    });

    // ✅ statusFilter 초기값을 currentParams에 동기화
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter && statusFilter.value) {
        currentParams.status = statusFilter.value;
    }

    // 2. 월 선택 초기화 및 데이터 로드
    initCustomMonthSelector(monthList, selectedText);
    
    // 3. 초기 페이지 로드 시 현재 월의 근태 목록 자동 로드
    const initialMonthValue = document.getElementById('monthSelect').value;
    if (initialMonthValue) {
        const [y, m] = initialMonthValue.split('-');
        loadAttendanceData(y, m);
    }

    // 4. 드롭다운 토글
    dropdown.addEventListener('click', (e) => {
        dropdown.classList.toggle('show');
        e.stopPropagation();
    });
    document.addEventListener('click', () => dropdown.classList.remove('show'));

    // 5. 상태 필터 변경 시 자동 검색 제거 (검색 버튼 클릭 시에만 적용)
    // 상태 필터는 검색 버튼 클릭 시 currentParams에 반영됨

    // 6. 검색 버튼 클릭 이벤트 (월별/기간 설정 + 상태 필터 함께 적용)
    document.getElementById('rangeSearchBtn').addEventListener('click', () => {
        // 상태 필터 값을 currentParams에 반영
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            currentParams.status = statusFilter.value;
        }
        currentParams.page = 0;
        executeSearch();
    });
});

/** 36개월 선택기 로직 및 데이터 매핑 */
function initCustomMonthSelector(list, text) {
    const now = new Date();
    const hidden = document.getElementById('monthSelect');
    const dropdown = document.getElementById('monthDropdown');

    for (let i = 0; i < 36; i++) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
        const y = d.getFullYear(), m = d.getMonth() + 1;
        const val = `${y}-${m < 10 ? '0' + m : m}`, txt = `${y}년 ${m}월`;

        const li = document.createElement('li');
        li.textContent = txt;

        if (i === 0) {
            li.classList.add('active');
            text.textContent = txt;
            hidden.value = val;
            // 초기 로드는 검색 버튼을 통해서만 실행
        }

        li.onclick = () => {
            list.querySelectorAll('li').forEach(el => el.classList.remove('active'));
            li.classList.add('active');
            text.textContent = txt;
            hidden.value = val;
            dropdown.classList.remove('show');
            // 월 선택 시 자동 검색하지 않음 (검색 버튼 클릭 시에만 검색)
        };
        list.appendChild(li);
    }
}

function executeSearch() {
    const mode = document.getElementById('searchType').value;
    if (mode === 'RANGE') {
        const start = document.getElementById('startDate').value;
        const end = document.getElementById('endDate').value;
        if (!start || !end) return;
        loadAttendanceData(null, null, start, end);
    } else {
        const val = document.getElementById('monthSelect').value;
        if(!val) return;
        const [y, m] = val.split('-');
        loadAttendanceData(y, m);
    }
}

async function loadAttendanceData(year, month, start = null, end = null) {
    const { page, size, status } = currentParams;

    let url = `/api/attendance/history/monthly?status=${status}&page=${page}&size=${size}`;
    if (start && end) url += `&startDate=${start}&endDate=${end}`;
    else url += `&year=${year}&month=${month}`;

    const response = await fetch(url, {
        headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
    });

    const res = await response.json();
    const data = res.data;

    if (data) {
        document.getElementById('totalWorkHours').innerText = data.summary.totalWorkTimeDisplay || '0h 00m';
        document.getElementById('lateCount').innerText = data.summary.lateCount || 0;
        document.getElementById('absentCount').innerText = data.summary.leaveAbsentCount || 0;
        renderTable(data.pagedData.content);
        renderPagination(data.pagedData);
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
            <td style="font-weight:600;">${r.date}</td>
            <td>${r.commuteAt || '-'}</td>
            <td>${r.leaveAt || '-'}</td>
            <td>${r.workingTime || '0h 00m'}</td>
            <td><span class="status-badge ${getBadgeClass(r.statusCode)}">${r.statusName}</span></td>
        </tr>`).join('');
}

function getBadgeClass(c) {
    if (c === 'LATE') return 'badge-late';
    if (c === 'ABSENT') return 'badge-absent';
    if (c === 'ON_TIME') return 'badge-normal'; // 정상 출근 스타일
    if (c === 'VACATION') return 'badge-vacation'; // 휴가 스타일 (필요 시 CSS 추가)
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
