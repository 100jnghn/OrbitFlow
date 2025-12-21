/**
 * 파일명: history.js
 */
const API_HISTORY_URL = '/api/attendance/history/monthly';

document.addEventListener('DOMContentLoaded', function() {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    const monthSelect = document.getElementById('monthSelect');
    if (monthSelect) {
        // 기본값 설정 (YYYY-MM 형식)
        const yearMonthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`;
        monthSelect.value = yearMonthStr;

        monthSelect.addEventListener('change', function(e) {
            const [year, month] = e.target.value.split('-');
            loadMonthlyHistory(parseInt(year), parseInt(month));
        });
    }

    // 초기 로드
    loadMonthlyHistory(currentYear, currentMonth);
});

async function loadMonthlyHistory(year, month) {
    // 세션에서 토큰 가져오기 (Spring Security 설정에 따름)
    const token = sessionStorage.getItem('accessToken');

    try {
        const response = await fetch(`${API_HISTORY_URL}?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {
                // 토큰이 필요한 경우 포함, 없다면 제거 가능
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            updateSummaryUI(data);      // 상단 요약 카드 데이터 반영
            renderHistoryTable(data.dailyRecords); // 하단 상세 테이블 데이터 반영
        } else {
            const errorMsg = await response.text();
            console.error('데이터 로드 실패:', errorMsg);
        }
    } catch (error) {
        console.error('네트워크 오류:', error);
    }
}

function updateSummaryUI(data) {
    // MonthlyAttHistoryResDto의 필드명과 일치하게 수정
    document.querySelector('.value-blue').textContent = data.totalWorkHours || 0;
    document.querySelector('.value-orange').textContent = data.lateCount || 0;
    document.querySelector('.value-red').textContent = data.leaveAbsentCount || 0;
}

function renderHistoryTable(records) {
    const tbody = document.querySelector('.history-table tbody');
    if (!tbody) return;

    if (!records || records.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 50px;">해당 월의 출퇴근 기록이 없습니다.</td></tr>`;
        return;
    }

    tbody.innerHTML = records.map(record => {
        // DailyAttRecordResDto의 statusCode 필드로 스타일 조건 처리
        const isLate = record.statusCode === 'LATE';
        const rowClass = isLate ? 'highlight-late' : '';
        const timeClass = isLate ? 'text-orange' : '';

        // 퇴근 기록이 없는 경우 처리 (Service에서 "-" 또는 "미기록"으로 줌)
        const isNoLeave = record.leaveAt === '-' || record.leaveAt === '미기록';
        const leaveTimeClass = isNoLeave ? 'text-red' : '';
        const workTimeClass = record.workingTime.includes('0h 00m') ? 'text-blue' : 'work-time-text';

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