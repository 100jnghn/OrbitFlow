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
function createCell(value = '-') {
    const td = document.createElement('td');
    td.textContent = value;
    return td;
}

function createCategoryCell(typeName) {
    const td = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = 'category-badge';
    badge.textContent = typeName;
    td.appendChild(badge);
    return td;
}

function createStatusCell(statusName) {
    const td = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = 'status-badge';
    badge.textContent = statusName;
    td.appendChild(badge);
    return td;
}

function createActionCell(reservation) {
    const td = document.createElement('td');

    // 승인 대기(1), 예약 확정(2)만 취소 가능
    if (reservation.reservationStatusId === 1 || reservation.reservationStatusId === 2) {
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
   Pagination State
========================== */
let currentPage = 0;
let totalPages = 0;
let pageSize = 10;

/* ==========================
   Data Load
========================== */
async function loadReservations(page = 0) {
    try {
        const params = new URLSearchParams({
            page,
            size: pageSize
        });

        const res = await apiFetch(`/api/reservations/me?${params}`, {
            method: 'GET'
        });

        if (!res.ok) throw new Error();

        const json = await res.json();
        const data = json.data;

        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        currentPage = data.number;
        totalPages = data.totalPages;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
                        예약 내역이 없습니다.
                    </td>
                </tr>
            `;
            return;
        }

        const startNumber = currentPage * pageSize;

        data.content.forEach((r, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(startNumber + i + 1),
                createCategoryCell(r.typeName),
                createCell(r.resourceName),
                createCell(r.reservationReason),
                createCell(formatDate(r.reservationDate)),
                createCell(formatHour(r.startTime)),
                createCell(formatHour(r.endTime)),
                createStatusCell(r.reservationStatusName),
                createActionCell(r)
            )
            ;
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
   Actions
========================== */
async function cancelReservation(id) {
    if (!confirm('예약을 취소하시겠습니까?')) return;

    try {
        const res = await apiFetch(`/api/reservations/${id}/cancel`, {
            method: 'PATCH'
        });

        if (!res.ok) throw new Error();

        alert('예약이 취소되었습니다.');
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert('예약 취소에 실패했습니다.');
    }
}

/* ==========================
   Init
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadReservations();
});
