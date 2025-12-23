// 1. 전역 변수: 현재 조회 중인 상태 보존
let currentParams = { page: 0, size: 10, status: 'ALL' };

document.addEventListener('DOMContentLoaded', function() {
    const dropdown = document.getElementById('monthDropdown');
    const monthList = document.getElementById('monthList');
    const selectedText = document.getElementById('selectedMonthText');
    const statusFilter = document.getElementById('statusFilter');

    // 2. 커스텀 드롭다운 목록 생성 및 초기 데이터 로드
    initCustomMonthSelector(monthList, selectedText);

    // 3. 드롭다운 열기/닫기 토글
    if (dropdown) {
        dropdown.addEventListener('click', (e) => {
            dropdown.classList.toggle('show');
            e.stopPropagation(); // 클릭 이벤트 전파 방지
        });
    }

    // 화면 다른 곳 클릭 시 드롭다운 닫기
    document.addEventListener('click', () => {
        if (dropdown) dropdown.classList.remove('show');
    });

    // 4. 상태 필터(전체, 지각 등) 변경 이벤트
    if (statusFilter) {
        statusFilter.addEventListener('change', (e) => {
            currentParams.status = e.target.value;
            currentParams.page = 0; // 필터 변경 시 1페이지부터

            // 현재 선택된 월 정보 가져오기
            const hiddenInput = document.getElementById('monthSelect');
            const [y, m] = hiddenInput.value.split('-');
            loadMonthlyHistory(y, m);
        });
    }
});

/**
 * 36개월 목록 생성 및 클릭 이벤트 바인딩
 */
function initCustomMonthSelector(listElement, textElement) {
    if (!listElement) return;

    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth();
    const hiddenInput = document.getElementById('monthSelect');

    listElement.innerHTML = '';
    const fragment = document.createDocumentFragment();

    for (let i = 0; i < 36; i++) {
        const targetDate = new Date(currentYear, currentMonth - i, 1);
        const year = targetDate.getFullYear();
        const month = targetDate.getMonth() + 1;

        const valMonth = month < 10 ? `0${month}` : month;
        const value = `${year}-${valMonth}`;
        const text = `${year}년 ${month}월`;

        const li = document.createElement('li');
        li.textContent = text;
        li.dataset.value = value;

        // 초기 설정: 현재 월 선택 상태로 시작
        if (i === 0) {
            li.classList.add('active');
            textElement.textContent = text;
            if (hiddenInput) hiddenInput.value = value;
            // 페이지 로드 시 첫 번째 데이터 불러오기 호출
            loadMonthlyHistory(year, valMonth);
        }

        // 목록 클릭 시 기능 작동 핵심 로직
        li.addEventListener('click', function(e) {
            e.stopPropagation(); // 드롭다운 닫히기 전 클릭 처리

            // UI 변경
            listElement.querySelectorAll('li').forEach(item => item.classList.remove('active'));
            this.classList.add('active');
            textElement.textContent = text;
            if (hiddenInput) hiddenInput.value = value;

            // 드롭다운 닫기
            document.getElementById('monthDropdown').classList.remove('show');

            // [핵심] 실제 데이터 로드 함수 호출
            currentParams.page = 0; // 월 변경 시 1페이지부터
            loadMonthlyHistory(year, valMonth);
        });

        fragment.appendChild(li);
    }
    listElement.appendChild(fragment);
}

/**
 * 서버 API 호출 및 화면 업데이트
 */
async function loadMonthlyHistory(year, month) {
    const token = sessionStorage.getItem('accessToken');
    const { page, size, status } = currentParams;

    // API 경로 및 파라미터 확인
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
            const res = await response.json();
            const result = res.data; // ResponseDto의 data 필드

            if (result) {
                // 상단 요약 카드 업데이트
                updateSummaryUI(result);
                // 테이블 목록 업데이트
                renderHistoryTable(result.attendanceList || []);
                // 하단 페이지네이션 업데이트
                renderPagination(result.pageInfo);
            }
        } else {
            console.error("서버 응답 에러:", response.status);
        }
    } catch (error) {
        console.error("네트워크 에러:", error);
    }
}

// 요약 카드 업데이트
function updateSummaryUI(data) {
    // 서버 응답 필드명에 맞춰 수정 (totalWorkHours, lateCount 등)
    document.getElementById('totalWorkHours').innerText = data.totalWorkHours || 0;
    document.getElementById('lateCount').innerText = data.lateCount || 0;
    document.getElementById('absentCount').innerText = data.leaveAbsentCount || 0;
}

// 테이블 렌더링
function renderHistoryTable(records) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 50px;">기록이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(record => `
        <tr>
            <td>${record.workDate}</td>
            <td>${record.commuteAt || '-'}</td>
            <td>${record.leaveAt || '-'}</td>
            <td>${record.workingTime || '0'}</td>
            <td><span class="status-badge ${getBadgeClass(record.statusCode)}">${record.statusName}</span></td>
        </tr>
    `).join('');
}

function getBadgeClass(code) {
    switch(code) {
        case 'LATE': return 'badge-late';
        case 'ABSENT': return 'badge-absent';
        case 'BEFORE_WORK': return 'badge-before';
        default: return 'badge-normal';
    }
}

// 페이지네이션 렌더링
function renderPagination(pageInfo) {
    const container = document.getElementById('pagination');
    if (!container || !pageInfo) return;

    container.innerHTML = '';

    for (let i = 0; i < pageInfo.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `page-btn ${i === pageInfo.currentPage ? 'active' : ''}`;
        btn.onclick = () => {
            currentParams.page = i;
            const hiddenInput = document.getElementById('monthSelect');
            const [y, m] = hiddenInput.value.split('-');
            loadMonthlyHistory(y, m);
        };
        container.appendChild(btn);
    }
}