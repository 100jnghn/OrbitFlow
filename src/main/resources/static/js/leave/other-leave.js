document.addEventListener('DOMContentLoaded', function() {
    loadOtherLeaveHistory();

    // 필터 변경 시 재로드 (데모용 이벤트 바인딩)
    document.getElementById('yearFilter').addEventListener('change', loadOtherLeaveHistory);
    document.getElementById('statusFilter').addEventListener('change', loadOtherLeaveHistory);
});

/**
 * 기타 휴가 신청 현황 로드
 * API: GET /api/leave/history/others
 */
async function loadOtherLeaveHistory() {
    const token = sessionStorage.getItem('accessToken');
    const tbody = document.getElementById('otherLeaveList');

    // 로딩 표시
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">데이터를 불러오는 중...</td></tr>';

    try {
        const response = await fetch(`/api/leave/history/others`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const history = result.data;

            if (!history || history.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="empty-row">신청 내역이 없습니다.</td></tr>';
                return;
            }

            // 테이블 렌더링
            tbody.innerHTML = history.map(item => {
                const statusClass = (item.statusCode || 'submitted').toLowerCase();
                const dayColorClass = item.days > 0 ? 'text-red' : ''; // 도안 상의 빨간색 텍스트 강조

                return `
                    <tr>
                        <td class="text-center">${item.title}</td>
                        <td class="text-center">${item.period}</td>
                        <td class="text-center ${dayColorClass}">${item.days}일</td>
                        <td class="text-center">${item.actionDate}</td>
                        <td class="text-center">
                            <div class="status-badge">
                                <span class="status-dot ${statusClass}"></span>
                                ${item.statusName}
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        } else {
            handleErrorResponse(response);
        }
    } catch (error) {
        console.error("시스템 오류:", error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center error">데이터 로드에 실패했습니다.</td></tr>';
    }
}

function handleErrorResponse(response) {
    if (response.status === 401) {
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        location.href = "/view/login";
    }
}