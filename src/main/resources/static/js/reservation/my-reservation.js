/* ==========================
   Tooltip (singleton)
========================== */
let tooltipEl = null;

function ensureTooltip() {
    if (!tooltipEl) {
        tooltipEl = document.createElement('div');
        tooltipEl.className = 'tooltip';
        document.body.appendChild(tooltipEl);
    }
}

function showTooltip(e) {
    const text = e.currentTarget.dataset.fulltext;
    if (!text) return;

    ensureTooltip();
    tooltipEl.textContent = text;
    tooltipEl.style.display = 'block';
    moveTooltip(e);
}

function moveTooltip(e) {
    if (!tooltipEl) return;
    tooltipEl.style.left = e.pageX + 12 + 'px';
    tooltipEl.style.top = e.pageY + 12 + 'px';
}

function hideTooltip() {
    if (tooltipEl) {
        tooltipEl.style.display = 'none';
    }
}

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
function createCell(value = '-', tooltip = false) {
    const td = document.createElement('td');
    const text = (value ?? '').toString();

    td.textContent = text;

    if (tooltip && text.length > 0 && text !== '-') {
        td.dataset.fulltext = text;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }

    return td;
}

function createCategoryCell(typeCode, typeName) {
    const td = document.createElement('td');
    td.textContent = typeName || '-';

    // hover 시 전체 텍스트 보기 기능
    if (typeName && typeName !== '-') {
        td.dataset.fulltext = typeName;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }

    return td;
}

function createStatusCell(reservation) {
    const td = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = 'status-badge';
    badge.textContent = reservation.reservationStatusName;

    // 상태별 클래스 추가
    const statusName = reservation.reservationStatusName;
    if (statusName === '예약 확정') {
        badge.classList.add('status-confirmed');
    } else if (statusName === '승인 대기') {
        badge.classList.add('status-pending');
    } else if (statusName === '예약 반려') {
        badge.classList.add('status-rejected');
    } else if (statusName === '예약 취소') {
        badge.classList.add('status-cancelled');
    }

    td.appendChild(badge);

    // '예약 반려' 또는 '예약 취소'일 때 rejectReason tooltip 추가
    if ((reservation.reservationStatusName === '예약 반려' || reservation.reservationStatusName === '예약 취소')
        && reservation.rejectReason) {
        td.dataset.fulltext = reservation.rejectReason;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }

    return td;
}

function createActionCell(reservation) {
    const td = document.createElement('td');

    // 종료 시간 계산
    let endDateTime = new Date(reservation.endDate);

    // endTime이 있으면 해당 시간을 설정, 없으면(null) 해당 날짜의 23:59:59로 설정 (차량 등)
    if (reservation.endTime !== null && reservation.endTime !== undefined) {
        endDateTime.setHours(reservation.endTime, 0, 0, 0);
    } else {
        endDateTime.setHours(23, 59, 59, 999);
    }

    const now = new Date();
    const isBeforeEnd = now < endDateTime;

    // 승인 대기(1), 예약 확정(2) 이면서, 아직 종료 시간이 지나지 않은 경우에만 취소 가능
    if ((reservation.reservationStatusId === 1 || reservation.reservationStatusId === 2) && isBeforeEnd) {
        const btn = document.createElement('button');
        btn.className = 'btn-cancel';
        btn.textContent = '취소';
        btn.onclick = () => cancelReservation(reservation.reservationId);
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
let pageSize = 5;
let currentFilters = {
    showPast: false,
    statusId: null,
    typeCode: null
};

/* ==========================
   Data Load
========================== */
async function loadStatuses() {
    try {
        const res = await apiFetch(
            '/api/reservation/status',
            { method: 'GET' }
        );

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const select = document.getElementById("status-filter")

        select.innerHTML = '<option value="">전체</option>';

        data.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;
            option.textContent = status.statusName;
            select.appendChild(option);
        });
    } catch (e) {
        console.error(e);
        await sweetError("상태 목록 조회 실패");
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

        const res = await apiFetch(`/api/reservations/me?${params.toString()}`, {
            method: 'GET'
        });

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
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
            tr.append(
                createCell(startNumber + i + 1),
                createCategoryCell(r.typeCode, r.typeName), // 카테고리 (tooltip 적용)
                createCell(r.resourceName, true), // 이름 (tooltip 적용)
                createCell(r.reservationReason, true), // 예약 사유 (tooltip 적용)
                createCell(formatDate(r.reservationDate)),
                createCell(formatDate(r.endDate)),
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
        await sweetError('예약 목록을 불러오지 못했습니다.');
    }
}


/* ==========================
   Pagination Render
========================== */
function renderPagination(pageData) {
    const container = document.querySelector('.pagination');
    container.innerHTML = '';

    const { number, totalPages, first, last } = pageData;

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
   Actions
========================== */
async function cancelReservation(id) {

    const result = await sweetConfirm(
        '취소 확인',
        '예약을 취소하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    try {
        const res = await apiFetch(`/api/reservations/${id}/cancel`, {
            method: 'PATCH'
        });

        if (!res.ok) throw new Error();

        await sweetSuccess('예약이 취소되었습니다.');
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        await sweetError('예약 취소에 실패했습니다.');
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

function updateApprovalSidebarSelection() {
    // 모든 no-sub 메뉴 선택 해제
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });

    // 결재 대기함 선택
    const inboxLink = document.getElementById('myReservationLink');
    if (inboxLink) {
        const menuItem = inboxLink.closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}

/* ==========================
   Init
========================== */
document.addEventListener('DOMContentLoaded', () => {
    initFilters();
    loadReservations();
    loadStatuses();
    updateApprovalSidebarSelection();
});
