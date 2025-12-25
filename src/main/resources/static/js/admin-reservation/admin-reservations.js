/* ==========================
   Helper Functions
========================== */

// 날짜 포맷 (yyyy-MM-dd)
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// 시간 포맷 (int → HH:00)
function formatHour(hour) {
    if (hour === null || hour === undefined) return '-';
    return `${String(hour).padStart(2, '0')}:00`;
}

/* ==========================
   Table Cell Helpers
========================== */
function createCell(value = '-', fullText = null) {
    const td = document.createElement('td');
    td.textContent = value;
    
    // hover 시 전체 텍스트 보기 기능
    // fullText가 제공되면 사용하고, 없으면 value를 사용
    const tooltipText = fullText !== null ? fullText : value;
    if (tooltipText && tooltipText !== '-') {
        td.setAttribute('data-full-text', tooltipText);
    }
    
    return td;
}

function createCategoryCell(typeCode, typeName) {
    const td = document.createElement('td');
    const badge = document.createElement('span');

    badge.className = 'category-badge';

    // typeCode에 따른 색상 분기
    if (typeCode === 'MEETING') {
        badge.classList.add('badge-meeting');
    } else if (typeCode === 'CAR') {
        badge.classList.add('badge-car');
    } else {
        badge.classList.add('badge-etc');
    }

    badge.textContent = typeName;
    td.appendChild(badge);
    return td;
}

function createStatusCell(reservation) {
    const td = document.createElement('td');
    td.className = 'status-cell';
    
    const badge = document.createElement('span');
    badge.className = 'status-badge status-badge-clickable';
    badge.textContent = reservation.reservationStatusName;
    badge.dataset.reservationId = reservation.reservationId;
    badge.dataset.currentStatusId = reservation.reservationStatusId;
    
    badge.addEventListener('click', (e) => {
        e.stopPropagation();
        showStatusDropdown(badge, reservation);
    });
    
    td.appendChild(badge);
    return td;
}

function createActionCell(reservation) {
    const td = document.createElement('td');

    // 승인 대기(1)만 승인 가능
    if (reservation.reservationStatusId === 1) {
        const btn = document.createElement('button');
        btn.className = 'btn-approve';
        btn.textContent = '승인';
        btn.onclick = () => approveReservation(reservation.reservationId);
        td.appendChild(btn);
    } else {
        td.textContent = '-';
    }

    return td;
}

/* ==========================
   Pagination State & Filters
========================== */
let currentPage = 0;
let totalPages = 0;
let pageSize = 10;
let currentFilters = {
    showPast: false,
    statusId: null,
    typeCode: null
};
let statusList = []; // 상태 목록 저장
let activeDropdown = null; // 현재 활성화된 드롭다운

/* ==========================
   Data Load
========================== */
async function loadStatuses() {
    try {
        const res = await apiFetch(
            '/api/reservation/status',
            {method: 'GET'}
        );

        if(!res.ok) throw new Error();

        const {data} = await res.json();
        statusList = data; // 상태 목록 저장
        
        const select = document.getElementById("status-filter")

        select.innerHTML = '<option value="">전체</option>';

        data.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;
            option.textContent = status.statusName;
            select.appendChild(option);
        });
    } catch(e) {
        console.error(e);
        alert("상태 목록 조회 실패");
    }
}

async function loadReservations(page = 0) {
    try {
        const params = new URLSearchParams({
            page: page,
            size: pageSize
        });

        params.append('showPast', currentFilters.showPast);

        if (currentFilters.statusId !== null) {
            params.append('statusId', currentFilters.statusId);
        }

        if (currentFilters.typeCode !== null) {
            params.append('typeCode', currentFilters.typeCode);
        }

        // 관리자용 API 엔드포인트 (실제 엔드포인트에 맞게 수정 필요)
        const res = await apiFetch(`/api/admin/reservations?${params.toString()}`, {
            method: 'GET'
        });

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" class="empty-state">
                        예약 내역이 없습니다.
                    </td>
                </tr>
            `;
            document.getElementById('pagination-container').style.display = 'none';
            return;
        }

        document.getElementById('pagination-container').style.display = 'flex';

        const startNumber = data.number * pageSize;

        data.content.forEach((r, i) => {
            const tr = document.createElement('tr');
            
            // 신청자 이름 (employeeName 또는 applicantName 등 API 응답에 맞게 수정 필요)
            const applicantName = r.employeeName || r.applicantName || r.name || '-';
            
            // 자원 이름
            const resourceName = r.resourceName || '-';
            
            // 예약 사유
            const reservationReason = r.reservationReason || '-';
            
            tr.append(
                createCell(startNumber + i + 1),
                createCategoryCell(r.typeCode, r.typeName),
                createCell(applicantName, applicantName), // 신청자 이름
                createCell(resourceName, resourceName), // 자원 이름
                createCell(reservationReason, reservationReason), // 예약 사유 (hover 기능)
                createCell(formatDate(r.reservationDate)),
                createCell(r.typeCode === 'CAR' ? '-' : formatHour(r.startTime)),
                createCell(r.typeCode === 'CAR' ? '-' : formatHour(r.endTime)),
                createStatusCell(r),
                createActionCell(r)
            );
            tbody.appendChild(tr);
        });

        renderPagination(data);

    } catch (e) {
        console.error(e);
        alert('예약 목록을 불러오지 못했습니다.');
    }
}


/* ==========================
   Pagination Render
========================== */
function renderPagination(pageData) {
    const container = document.querySelector('.pagination');
    container.innerHTML = '';

    const {number, totalPages, first, last} = pageData;

    const prev = document.createElement('button');
    prev.textContent = '<';
    prev.disabled = first;
    prev.onclick = () => loadReservations(number - 1);
    container.appendChild(prev);

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        if (i === number) btn.className = 'active';
        btn.onclick = () => loadReservations(i);
        container.appendChild(btn);
    }

    const next = document.createElement('button');
    next.textContent = '>';
    next.disabled = last;
    next.onclick = () => loadReservations(number + 1);
    container.appendChild(next);
}

/* ==========================
   Status Dropdown
========================== */
function showStatusDropdown(badge, reservation) {
    // 기존 드롭다운이 있으면 제거
    if (activeDropdown) {
        activeDropdown.remove();
        activeDropdown = null;
    }

    // 드롭다운 생성
    const dropdown = document.createElement('div');
    dropdown.className = 'status-dropdown';
    dropdown.dataset.reservationId = reservation.reservationId;
    
    // 상태 목록 추가
    statusList.forEach(status => {
        const item = document.createElement('div');
        item.className = 'status-dropdown-item';
        if (status.id === reservation.reservationStatusId) {
            item.classList.add('active');
        }
        item.textContent = status.statusName;
        item.onclick = () => {
            updateReservationStatus(reservation.reservationId, status.id, status.statusName);
            dropdown.remove();
            activeDropdown = null;
        };
        dropdown.appendChild(item);
    });

    // 배지 위치 기준으로 드롭다운 위치 설정
    const rect = badge.getBoundingClientRect();
    dropdown.style.position = 'fixed';
    dropdown.style.top = `${rect.bottom + 4}px`;
    dropdown.style.left = `${rect.left}px`;
    dropdown.style.zIndex = '1000';

    document.body.appendChild(dropdown);
    activeDropdown = dropdown;

    // 외부 클릭 시 드롭다운 닫기
    setTimeout(() => {
        document.addEventListener('click', function closeDropdown(e) {
            if (!dropdown.contains(e.target) && e.target !== badge) {
                dropdown.remove();
                activeDropdown = null;
                document.removeEventListener('click', closeDropdown);
            }
        }, { once: true });
    }, 0);
}

/* ==========================
   Actions
========================== */
async function updateReservationStatus(reservationId, statusId, statusName) {
    try {
        const res = await apiFetch(`/api/admin/reservations/${reservationId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ statusId })
        });

        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '상태 변경에 실패했습니다.');
        }

        alert(`예약 상태가 "${statusName}"으로 변경되었습니다.`);
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert(e.message || '상태 변경에 실패했습니다.');
    }
}

async function approveReservation(id) {
    if (!confirm('예약을 승인하시겠습니까?')) return;

    try {
        const res = await apiFetch(`/api/admin/reservations/${id}/approve`, {
            method: 'PATCH'
        });

        if (!res.ok) throw new Error();

        alert('예약이 승인되었습니다.');
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert('예약 승인에 실패했습니다.');
    }
}

/* ==========================
   Filter Functions
========================== */
function applyFilters() {
    const typeSelect = document.getElementById('resource-category-filter');
    const statusSelect = document.getElementById('status-filter');
    const pastToggle = document.getElementById('past-reservations-toggle');

    currentFilters.typeCode = typeSelect?.value || null;

    currentFilters.statusId = statusSelect?.value
        ? Number(statusSelect.value)
        : null;

    currentFilters.showPast = pastToggle?.checked ?? false;

    // 필터 변경 시 항상 첫 페이지부터
    loadReservations(0);
}

/* ==========================
   Event Listeners
========================== */
function initFilters() {
    const typeSelect = document.getElementById('resource-category-filter');
    const statusSelect = document.getElementById('status-filter');
    const pastToggle = document.getElementById('past-reservations-toggle');

    // 자원 타입 변경 시 자동 검색
    if (typeSelect) {
        typeSelect.addEventListener('change', applyFilters);
    }

    // 상태 변경 시 자동 검색
    if (statusSelect) {
        statusSelect.addEventListener('change', applyFilters);
    }

    // 과거 예약 토글 변경 시 자동 검색
    if (pastToggle) {
        pastToggle.addEventListener('change', applyFilters);
    }
}

/* ==========================
   Init
========================== */
document.addEventListener('DOMContentLoaded', () => {
    initFilters();
    loadReservations();
    loadStatuses();
});

