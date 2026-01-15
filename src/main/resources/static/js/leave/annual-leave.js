let currentPage = 0;

document.addEventListener('DOMContentLoaded', async function () {
    await initYearFilter();
    refreshLeaveData();
    loadLeaveTypes();

    // 날짜 입력 필드에 변경 이벤트 리스너 추가 (실시간 필터링 + 유효성)
    const startDateEl = document.getElementById('startDate');
    const endDateEl = document.getElementById('endDate');

    if (startDateEl && endDateEl) {
        startDateEl.addEventListener('change', function () {
            endDateEl.min = this.value;
            if (endDateEl.value && endDateEl.value < this.value) {
                endDateEl.value = '';
            }
            applyFilters(0);
        });
        endDateEl.addEventListener('change', () => applyFilters(0));
    }
});

async function initYearFilter() {
    const yearSelect = document.getElementById('filterYear');
    if (!yearSelect) return;

    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch('/api/leave/summary', {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const result = await response.json();
            const hireDate = result.data.hireDate;
            const currentYear = new Date().getFullYear();
            const startYear = hireDate ? new Date(hireDate).getFullYear() : currentYear;

            yearSelect.innerHTML = '';
            for (let year = currentYear; year >= startYear; year--) {
                const option = document.createElement('option');
                option.value = year;
                option.textContent = `${year}년`;
                yearSelect.appendChild(option);
            }
            yearSelect.value = currentYear;
        }
    } catch (error) {
        console.error("연도 필터 초기화 실패:", error);
    }
}

async function refreshLeaveData() {
    const yearEl = document.getElementById('filterYear');
    const selectedYear = yearEl ? yearEl.value : new Date().getFullYear();
    if (!selectedYear) return;

    currentPage = 0;
    await loadLeaveBalance(selectedYear);
    await applyFilters(0);
}

async function loadLeaveBalance(year) {
    const token = sessionStorage.getItem('accessToken');
    try {
        const response = await fetch(`/api/leave/summary?year=${year}`, {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const result = await response.json();
            const balance = result.data;
            document.getElementById('totalGranted').innerText = (balance.totalGranted || 0).toFixed(1);
            document.getElementById('usedDays').innerText = (balance.usedDays || 0).toFixed(1);
            document.getElementById('remainingDays').innerText = (balance.remainingDays || 0).toFixed(1);
            const displayYearText = document.getElementById('displayYearText');
            if (displayYearText) displayYearText.innerText = year;
        }
    } catch (error) {
        console.error("연차 요약 로드 실패:", error);
    }
}

async function applyFilters(page = 0) {
    const token = sessionStorage.getItem('accessToken');
    const yearEl = document.getElementById('filterYear');
    const year = yearEl ? yearEl.value : new Date().getFullYear();

    const type = document.getElementById('filterType').value;
    const status = document.getElementById('filterStatus').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // 날짜 유효성 검사
    if (startDate && endDate) {
        if (new Date(startDate) > new Date(endDate)) {
            alert('시작일은 종료일보다 이전이어야 합니다.');
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';
            return;
        }
    }

    currentPage = page;
    const params = new URLSearchParams({ page: page, size: 10, year: year });
    if (type) params.append('typeName', type);
    if (status) params.append('status', status);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    try {
        const response = await fetch(`/api/leave/usage?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const result = await response.json();
            renderUsageTable(result.data, year);
        }
    } catch (error) {
        console.error("내역 로드 실패:", error);
    }
}

function renderUsageTable(pageData, year) {
    const tbody = document.getElementById('leaveHistoryList');
    if (!tbody) return;

    if (!pageData || !pageData.content || pageData.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="padding: 50px; text-align: center;">내역이 없습니다.</td></tr>`;
        document.getElementById('leavePagination').innerHTML = '';
        return;
    }

    tbody.innerHTML = pageData.content.map(item => `
        <tr>
            <td>${item.title}</td>
            <td>${item.actionDate}</td>
            <td class="period-cell">${item.period || '-'}</td>
            <td class="count-minus">-${parseFloat(item.days).toFixed(1)}</td>
            <td>
                <span class="status-badge badge-${item.statusName === '승인' ? 'approved' : item.statusName === '반려' ? 'rejected' : 'submitted'}">
                    ${item.statusName}
                </span>
            </td>
        </tr>
    `).join('');

    renderPagination(pageData, (targetPage) => applyFilters(targetPage));
}

function resetFilters() {
    document.getElementById('filterType').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    refreshLeaveData();
}

async function loadLeaveTypes() {
    const select = document.getElementById('filterType');
    if (!select) return;

    try {
        const response = await fetch('/api/leave/types?isCountable=true', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const result = await response.json();
            const types = result.data || [];
            select.innerHTML = '<option value="">전체 유형</option>';
            types.forEach(type => {
                const option = document.createElement('option');
                option.value = type.typeName;
                option.textContent = type.typeName;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error("휴가 유형 로드 실패:", error);
    }
}

function renderPagination(pageData, loadFunc) {
    const pagination = document.getElementById('leavePagination');
    if (!pagination) return;
    pagination.innerHTML = '';
    for (let i = 0; i < pageData.totalPages; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-number ${i === pageData.number ? 'active' : ''}`;
        pageBtn.textContent = i + 1;
        pageBtn.onclick = () => { window.scrollTo({ top: 0, behavior: 'smooth' }); loadFunc(i); };
        pagination.appendChild(pageBtn);
    }
}