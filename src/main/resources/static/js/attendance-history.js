const API_HISTORY_URL = '/api/attendance/history/monthly';

document.addEventListener('DOMContentLoaded', function() {
    // 1. 초기 설정: 현재 연도와 월로 드롭다운 설정 및 데이터 로드
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    const monthSelect = document.getElementById('monthSelect');
    if (monthSelect) {
        // 현재 월을 기본 선택값으로 설정
        const yearMonthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`;
        monthSelect.value = yearMonthStr;

        // 드롭다운 변경 이벤트 리스너 등록
        monthSelect.addEventListener('change', function(e) {
            const [year, month] = e.target.value.split('-');
            loadMonthlyHistory(parseInt(year), parseInt(month));
        });
    }

    // 2. 초기 데이터 로드
    loadMonthlyHistory(currentYear, currentMonth);
});

/**
 * 서버에서 월별 근태 데이터 가져오기
 */
async function loadMonthlyHistory(year, month) {
    const token = sessionStorage.getItem('accessToken');
    if (!token) return;

    try {
        const response = await fetch(`${API_HISTORY_URL}?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            updateSummaryUI(data); // 3번 영역 업데이트
            renderHistoryTable(data.dailyRecords); // 4번 영역 업데이트
        } else {
            console.error('데이터를 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('네트워크 오류:', error);
    }
}

/**
 * 상단 요약 카드 업데이트 (3번 영역)
 */
function updateSummaryUI(data) {
    // 총 근무 시간
    const totalTimeEl = document.querySelector('.value-blue');
    if (totalTimeEl) totalTimeEl.textContent = data.totalWorkHours || 0;

    // 지각 횟수
    const lateCountEl = document.querySelector('.value-orange');
    if (lateCountEl) lateCountEl.textContent = data.lateCount || 0;

    // 휴가/결근 일수
    const absentCountEl = document.querySelector('.value-red');
    if (absentCountEl) absentCountEl.textContent = data.leaveAbsentCount || 0;
}

/**
 * 하단 일별 기록 테이블 렌더링 (4번 영역)
 */
function renderHistoryTable(records) {
    const tbody = document.querySelector('.history-table tbody');
    if (!tbody) return;

    if (!records || records.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 50px;">해당 월의 기록이 없습니다.</td></tr>`;
        return;
    }

    tbody.innerHTML = records.map(record => {
        // 지각인 경우 행 전체 배경 강조 및 출근 시간 색상 변경
        const isLate = record.statusCode === 'LATE';
        const rowClass = isLate ? 'highlight-late' : '';
        const timeClass = isLate ? 'text-orange' : '';

        // 퇴근 미기록 시 빨간색 강조
        const leaveTimeClass = record.leaveAt === '미기록' ? 'text-red' : '';
        const workTimeClass = record.workingTime === '계산불가' ? 'text-red' : 'work-time-text';

        return `
            <tr class="${rowClass}">
                <td>${record.date}</td>
                <td class="${timeClass}">${record.commuteAt}</td>
                <td class="${leaveTimeClass}">${record.leaveAt}</td>
                <td class="${workTimeClass}">${record.workingTime}</td>
            </tr>
        `;
    }).join('');
}