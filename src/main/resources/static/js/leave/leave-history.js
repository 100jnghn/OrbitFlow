/**
 * 모든 휴가 신청 현황 데이터 로드 및 렌더링
 */
let currentPage = 0;
let totalPages = 1;
let currentFilters = {
    typeName: null,
    status: null,
    startDate: null,
    endDate: null
};

document.addEventListener('DOMContentLoaded', function() {
    loadAllLeaveTypes(); // 필터용: 모든 휴가 유형 로드
    loadAllLeaveHistory(currentPage);
});

/**
 * 필터용 모든 휴가 유형 로드
 */
async function loadAllLeaveTypes() {
    const select = document.getElementById('filterType');
    if (!select) return;

    try {
        // 백엔드 경로: LeaveController.getLeaveTypes (@GetMapping("/leave/types"))
        const response = await fetch('/api/leave/types', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const result = await response.json();
            const types = result.data || [];

            select.innerHTML = '<option value="">전체</option>';
            types.forEach(type => {
                const option = document.createElement('option');
                option.value = type.typeName;
                option.textContent = type.typeName;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error("전체 휴가 유형 로드 실패:", error);
    }
}

/**
 * 모든 휴가 이력 로드 (대기, 승인, 반려 포함)
 */
async function loadAllLeaveHistory(page) {
    const token = sessionStorage.getItem('accessToken');
    const tbody = document.getElementById('leaveHistoryList');

    try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', '10');

        if (currentFilters.typeName) params.append('typeName', currentFilters.typeName);
        if (currentFilters.status) params.append('status', currentFilters.status);
        if (currentFilters.startDate) params.append('startDate', currentFilters.startDate);
        if (currentFilters.endDate) params.append('endDate', currentFilters.endDate);

        // 백엔드 경로: LeaveController.getMyHistory (@GetMapping("/leave/history"))
        const response = await fetch(`/api/leave/history?${params.toString()}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const pageData = result.data; // Page<LeaveHistoryResDto>

            if (!pageData || !pageData.content || pageData.content.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" style="padding: 50px; text-align: center;">신청 내역 데이터가 없습니다.</td></tr>';
                document.getElementById('leavePagination').innerHTML = '';
                return;
            }

            currentPage = pageData.number;
            totalPages = pageData.totalPages;

            tbody.innerHTML = pageData.content.map(item => {
                const statusCode = (item.statusCode || 'WAITING').toUpperCase();
                const badgeClass = statusCode === 'APPROVED' ? 'badge-approved' :
                    statusCode === 'REJECTED' ? 'badge-rejected' :
                        'badge-submitted';

                return `
                    <tr>
                        <td>${item.title || '-'}</td>
                        <td class="period-cell">${item.period || '-'}</td>
                        <td>${parseFloat(item.days || 0).toFixed(1)}일</td>
                        <td>
                            <span class="status-badge ${badgeClass}">${item.statusName || '대기'}</span>
                        </td>
                    </tr>
                `;
            }).join('');

            renderPagination(pageData, loadAllLeaveHistory);
        }
    } catch (error) {
        console.error("전체 이력 로드 중 시스템 오류:", error);
    }
}

/** 필터 적용 함수 **/
function applyFilters() {
    currentFilters.typeName = document.getElementById('filterType').value || null;
    currentFilters.status = document.getElementById('filterStatus').value || null;
    currentFilters.startDate = document.getElementById('startDate').value || null;
    currentFilters.endDate = document.getElementById('endDate').value || null;

    loadAllLeaveHistory(0);
}

function renderPagination(pageData, loadFunc) {
    const pagination = document.getElementById('leavePagination');
    if (!pagination) return;
    pagination.innerHTML = '';

    for (let i = 0; i < pageData.totalPages; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-number ${i === pageData.number ? 'active' : ''}`;
        pageBtn.textContent = i + 1;
        pageBtn.onclick = () => { loadFunc(i); };
        pagination.appendChild(pageBtn);
    }
}