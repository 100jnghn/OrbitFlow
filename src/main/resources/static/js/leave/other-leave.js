document.addEventListener('DOMContentLoaded', function() {
    loadTotalLeaveHistory();

    document.getElementById('yearFilter').addEventListener('change', loadTotalLeaveHistory);
    document.getElementById('statusFilter').addEventListener('change', loadTotalLeaveHistory);
});

/**
 * 모든 휴가 신청 현황 통합 로드
 */
async function loadTotalLeaveHistory() {
    const token = sessionStorage.getItem('accessToken');
    const tbody = document.getElementById('totalLeaveList');
    const statusFilter = document.getElementById('statusFilter').value;

    tbody.innerHTML = '<tr><td colspan="5" class="text-center">데이터를 통합 로드 중...</td></tr>';

    try {
        // 1. 두 API 동시 호출
        const [annualRes, othersRes] = await Promise.all([
            fetch(`/api/leave/types`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (annualRes.ok && othersRes.ok) {
            const annualData = (await annualRes.json()).data || [];
            const othersData = (await othersRes.json()).data || [];

            // 2. 데이터 통합
            let combinedHistory = [...annualData, ...othersData];

            // 3. 필터링 (결제 상태)
            if (statusFilter !== 'ALL') {
                combinedHistory = combinedHistory.filter(item => item.statusCode === statusFilter);
            }

            // 4. 정렬 (신청일 최신순)
            combinedHistory.sort((a, b) => new Date(b.actionDate) - new Date(a.actionDate));

            if (combinedHistory.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="empty-row">조회된 신청 내역이 없습니다.</td></tr>';
                return;
            }

            // 5. 렌더링
            tbody.innerHTML = combinedHistory.map(item => {
                const statusClass = (item.statusCode || 'submitted').toLowerCase();
                // 도안 반영: 일수가 있으면 빨간색 강조
                const dayColorClass = item.days > 0 ? 'text-red' : '';

                return `
                    <tr class="${item.statusCode === 'APPROVED' ? '' : 'bg-light-yellow'}">
                        <td class="text-center">${item.title}</td>
                        <td class="text-center">${item.period}</td>
                        <td class="text-center ${dayColorClass}" style="font-weight: bold;">${item.days}일</td>
                        <td class="text-center">${item.actionDate}</td>
                        <td class="text-center">
                            <div class="status-wrapper">
                                <span class="status-dot ${statusClass}"></span>
                                <span class="status-text">${item.statusName}</span>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        } else {
            alert("데이터를 불러오는 데 실패했습니다.");
        }
    } catch (error) {
        console.error("시스템 오류:", error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center error">데이터 로드 실패</td></tr>';
    }
}