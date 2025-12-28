/**
 * 내 연차 현황 데이터 로드 및 렌더링
 *
 */
document.addEventListener('DOMContentLoaded', function() {
    loadLeaveBalance();
    loadLeaveHistory();
});

/**
 * 1. 상단 요약 정보 로드
 * API: GET /api/leave/my
 */
async function loadLeaveBalance() {
    const token = sessionStorage.getItem('accessToken');
    const currentYear = new Date().getFullYear();

    try {
        // 컨트롤러의 @GetMapping("/my") 경로와 일치시킴
        const response = await fetch(`/api/leave/my?year=${currentYear}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const balance = result.data; // ResponseDto.data 추출

            // 데이터 바인딩 (null 방지)
            document.getElementById('totalGranted').innerText = (balance.totalGranted || 0).toFixed(1);
            document.getElementById('remainingDays').innerText = (balance.remainingDays || 0).toFixed(1);
            document.getElementById('usedDays').innerText = (balance.usedDays || 0).toFixed(1);
        } else {
            console.error("잔합 조회 실패: ", response.status);
        }
    } catch (error) {
        console.error("연차 잔합 로드 중 시스템 오류:", error);
    }
}

/**
 * 2. 하단 상세 내역 로드 (연차 및 반차 전용)
 * API: GET /api/leave/history/annual
 */
async function loadLeaveHistory() {
    const token = sessionStorage.getItem('accessToken');
    const tbody = document.getElementById('leaveHistoryList');

    try {
        // [수정] 컨트롤러에서 새로 만든 연차 전용 API를 호출합니다.
        const response = await fetch(`/api/leave/history/annual`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const history = result.data;

            if (!history || history.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="padding: 50px; text-align: center;">데이터가 없습니다.</td></tr>';
                return;
            }

            // [수정] 도안에 맞춰 렌더링 로직 보완
            tbody.innerHTML = history.map(item => {
                const isGrant = item.type === 'GRANT';
                const countClass = isGrant ? 'count-plus' : 'count-minus';
                // 부여는 +, 사용은 - 기호를 붙입니다.
                const sign = isGrant ? '+' : '-';

                // statusCode를 소문자로 변환하여 CSS 클래스로 사용 (approved, rejected, submitted)
                const statusClass = (item.statusCode || 'submitted').toLowerCase();

                return `
                    <tr class="${isGrant ? 'row-grant' : ''}">
                        <td>${item.title}</td> <td>${item.actionDate}</td> <td>${item.period || '-'}</td> <td class="${countClass}">${sign}${parseFloat(item.days).toFixed(1)}</td>
                        <td>
                            <div class="status-wrapper">
                                <span class="status-dot ${statusClass}"></span>
                                <span class="status-text">${item.statusName}</span>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }
    } catch (error) {
        console.error("내역 로드 중 시스템 오류:", error);
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: red;">내역을 불러오는 중 오류가 발생했습니다.</td></tr>';
    }
}