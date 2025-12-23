let currentParams = { page: 0, size: 10, status: 'ALL' };

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

        currentParams.page = 0;
        executeSearch();
    });

    // 2. 월 선택 초기화 및 데이터 로드
    initCustomMonthSelector(monthList, selectedText);

    // 3. 드롭다운 토글
    dropdown.addEventListener('click', (e) => {
        dropdown.classList.toggle('show');
        e.stopPropagation();
    });
    document.addEventListener('click', () => dropdown.classList.remove('show'));

    // 4. 상태 필터 이벤트
    document.getElementById('statusFilter').addEventListener('change', (e) => {
        currentParams.status = e.target.value;
        currentParams.page = 0;
        executeSearch();
    });

    // 5. 기간 조회 버튼
    document.getElementById('rangeSearchBtn').addEventListener('click', () => {
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
            executeSearch();
        }

        li.onclick = () => {
            list.querySelectorAll('li').forEach(el => el.classList.remove('active'));
            li.classList.add('active');
            text.textContent = txt;
            hidden.value = val;
            dropdown.classList.remove('show');
            currentParams.page = 0;
            executeSearch();
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

/**
 * ✅ 핵심 수정:
 * - 백엔드 DTO는 commuteAt / leaveAt 인데
 * - 기존 프론트는 checkInTime / checkOutTime 을 보고 있어서 값이 안 나왔음
 * - commuteAt / leaveAt 으로 맞춰서 출력
 */
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
    return 'badge-normal';
}

function renderPagination(pageInfo) {
    const container = document.getElementById('pagination');
    container.innerHTML = '';
    if (pageInfo.totalPages <= 1) return;

    for (let i = 0; i < pageInfo.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `page-btn ${i === pageInfo.number ? 'active' : ''}`;
        btn.onclick = () => { currentParams.page = i; executeSearch(); };
        container.appendChild(btn);
    }
}
