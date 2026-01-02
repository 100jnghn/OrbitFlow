/**
 * 내 연차 현황 데이터 로드 및 렌더링
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
    loadLeaveBalance();
    loadLeaveTypes(); // 항목 목록 로드 (isCountable=true 인 것만)
    loadLeaveHistory(currentPage);
});

/**
 * 1. 상단 요약 정보 로드
 */
async function loadLeaveBalance() {
    const token = sessionStorage.getItem('accessToken');
    const currentYear = new Date().getFullYear();

    try {
        const response = await fetch(`/api/leave/my?year=${currentYear}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const balance = result.data;

            document.getElementById('totalGranted').innerText = (balance.totalGranted || 0).toFixed(1);
            document.getElementById('remainingDays').innerText = (balance.remainingDays || 0).toFixed(1);
            document.getElementById('usedDays').innerText = (balance.usedDays || 0).toFixed(1);
        }
    } catch (error) {
        console.error("연차 잔합 로드 중 시스템 오류:", error);
    }
}

/**
 * 항목 목록 로드 (필터용) - 차감되는 휴가 유형만
 */
async function loadLeaveTypes() {
    const select = document.getElementById('filterType');
    if (!select) {
        console.error('filterType select 요소를 찾을 수 없습니다.');
        return;
    }

    try {
        // 컨트롤러 경로: /api/leave/types/countable
        const response = await apiFetch('/api/leave/types/countable');
        
        console.log('API 호출 응답 상태:', response.status);

        if (response.ok) {
            const result = await response.json();
            console.log('API 응답 전체:', result);
            
            // ResponseDto 구조이므로 result.data 사용
            const types = result.data || [];
            console.log('휴가 유형 배열:', types);
            console.log('배열 길이:', types.length);

            select.innerHTML = '<option value="">전체</option>';

            if (Array.isArray(types) && types.length > 0) {
                types.forEach((type, index) => {
                    console.log(`타입 ${index + 1}:`, type);
                    const option = document.createElement('option');
                    // DB 컬럼 type_name 매핑
                    option.value = type.typeName || '';
                    option.textContent = type.typeName || '';
                    select.appendChild(option);
                    console.log(`옵션 추가: ${type.typeName}`);
                });
                console.log('최종 옵션 수:', select.options.length);
            } else {
                console.warn('휴가 유형 배열이 비어있거나 배열이 아닙니다.');
            }
        } else {
            const errorText = await response.text();
            console.error('API 호출 실패:', response.status, response.statusText);
            console.error('에러 응답:', errorText);
        }
    } catch (error) {
        console.error("차감 휴가 유형 로드 실패:", error);
        console.error('에러 상세:', error.stack);
    }
}

/**
 * 2. 상세 내역 로드
 */
async function loadLeaveHistory(page) {
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

        const response = await fetch(`/api/leave/history/annual?${params.toString()}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const pageData = result.data;

            if (!pageData || !pageData.content || pageData.content.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="padding: 50px; text-align: center;">데이터가 없습니다.</td></tr>';
                document.getElementById('leavePagination').innerHTML = '';
                return;
            }

            currentPage = pageData.number;
            totalPages = pageData.totalPages;

            tbody.innerHTML = pageData.content.map(item => {
                const isGrant = item.type === 'GRANT';
                const countClass = isGrant ? 'count-plus' : 'count-minus';
                const sign = isGrant ? '+' : '-';
                const statusCode = (item.statusCode || 'submitted').toUpperCase();
                const badgeClass = statusCode === 'APPROVED' ? 'badge-approved' : 
                                  statusCode === 'REJECTED' ? 'badge-rejected' : 
                                  'badge-submitted';

                return `
                    <tr class="${isGrant ? 'row-grant' : ''}">
                        <td>${item.title}</td>
                        <td>${item.actionDate}</td>
                        <td class="period-cell">${item.period || '-'}</td>
                        <td class="${countClass}">${sign}${parseFloat(item.days).toFixed(1)}</td>
                        <td>
                            <span class="status-badge ${badgeClass}">${item.statusName}</span>
                        </td>
                    </tr>
                `;
            }).join('');

            renderPagination(pageData, loadLeaveHistory);
        }
    } catch (error) {
        console.error("내역 로드 중 시스템 오류:", error);
    }
}

/** 페이지네이션 및 필터 로직 공통 (생략 가능하나 구조상 유지) **/
function applyFilters() {
    currentFilters.typeName = document.getElementById('filterType').value || null;
    currentFilters.status = document.getElementById('filterStatus').value || null;
    currentFilters.startDate = document.getElementById('startDate').value || null;
    currentFilters.endDate = document.getElementById('endDate').value || null;

    if (currentFilters.startDate && currentFilters.endDate && currentFilters.startDate > currentFilters.endDate) {
        alert('시작일은 종료일보다 이전이어야 합니다.');
        return;
    }
    currentPage = 0;
    loadLeaveHistory(currentPage);
}

function resetFilters() {
    document.getElementById('filterType').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    currentFilters = { typeName: null, status: null, startDate: null, endDate: null };
    currentPage = 0;
    loadLeaveHistory(currentPage);
}

function renderPagination(pageData, loadFunc) {
    const pagination = document.getElementById('leavePagination');
    if (!pagination) return;
    pagination.innerHTML = '';

    const page = pageData.number;
    const totalPages = pageData.totalPages;

    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => { if (page > 0) { window.scrollTo({ top: 0, behavior: 'smooth' }); loadFunc(page - 1); }};
    pagination.appendChild(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        if (i >= page - 2 && i <= page + 2) {
            const pageBtn = document.createElement('button');
            pageBtn.className = `page-number ${i === page ? 'active' : ''}`;
            pageBtn.textContent = i + 1;
            pageBtn.onclick = () => { window.scrollTo({ top: 0, behavior: 'smooth' }); loadFunc(i); };
            pagination.appendChild(pageBtn);
        }
    }

    const nextBtn = document.createElement('button');
    nextBtn.className = 'page-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = page >= totalPages - 1;
    nextBtn.onclick = () => { if (page < totalPages - 1) { window.scrollTo({ top: 0, behavior: 'smooth' }); loadFunc(page + 1); }};
    pagination.appendChild(nextBtn);
}