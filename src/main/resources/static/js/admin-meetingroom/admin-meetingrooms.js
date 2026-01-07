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
   Table Cell Helpers
========================== */
function createCell(value = '', tooltip = false) {
    const td = document.createElement('td');
    const text = (value ?? '').toString();

    td.textContent = text;

    if (tooltip && text.length > 0) {
        td.dataset.fulltext = text;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }

    return td;
}

function createStatusBadge(statusName) {
    const td = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = 'status-badge';
    badge.textContent = statusName;

    if (statusName === '사용가능' || statusName === '사용 가능') {
        badge.classList.add('status-available');
    } else if (statusName === '점검중') {
        badge.classList.add('status-maintenance');
    } else if (statusName === '사용불가' || statusName === '사용 불가') {
        badge.classList.add('status-unavailable');
    }

    td.appendChild(badge);
    return td;
}

function createActionCell(id) {
    const td = document.createElement('td');
    const box = document.createElement('div');
    box.className = 'action-btns';

    const edit = document.createElement('button');
    edit.className = 'btn-edit';
    edit.textContent = '수정';
    edit.onclick = () => editMeetingRoom(id);

    const del = document.createElement('button');
    del.className = 'btn-delete';
    del.textContent = '삭제';
    del.onclick = () => deleteMeetingRoom(id);

    box.append(edit, del);
    td.appendChild(box);
    return td;
}

/* ==========================
   Pagination State
========================== */
let currentPage = 0;
let totalPages = 0;
let pageSize = 8;

/* ==========================
   Data Load
========================== */
async function loadMeetingRooms(page = 0) {
    try {
        const res = await apiFetch(
            `/api/admin/meetingrooms?page=${page}&size=${pageSize}&sort=id,asc`,
            { method: 'GET' }
        );
        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        // Pagination 정보 업데이트
        currentPage = data.number;
        totalPages = data.totalPages;

        const content = data.content;

        if (!content?.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6">
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p>등록된 리소스가 없습니다.</p>
                        </div>
                    </td>
                </tr>`;
            // 페이지네이션 숨김
            document.getElementById('pagination-container').style.display = 'none';
            return;
        }

        // 페이지네이션 표시
        document.getElementById('pagination-container').style.display = 'flex';

        // 번호는 전체 목록 기준으로 계산
        const startNumber = currentPage * pageSize;

        content.forEach((room, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(startNumber + i + 1),
                createCell(room.name, true),
                createCell(room.position, true),
                createCell(room.description, true),
                createStatusBadge(room.statusName),
                createActionCell(room.meetingroomId)
            );
            tbody.appendChild(tr);
        });

        // 페이지네이션 렌더링
        renderPagination(data);

    } catch (e) {
        console.error(e);
        alert('회의실 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Pagination Render
========================== */
function renderPagination(pageData) {
    const container = document.querySelector('.pagination');
    container.innerHTML = '';

    const { number, totalPages, first, last } = pageData;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = first;
    prevBtn.onclick = () => loadMeetingRooms(number - 1);
    container.appendChild(prevBtn);

    // 페이지 번호 버튼
    const startPage = Math.floor(number / 5) * 5;
    const endPage = Math.min(startPage + 5, totalPages);

    for (let i = startPage; i < endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i + 1;
        pageBtn.className = i === number ? 'active' : '';
        pageBtn.onclick = () => loadMeetingRooms(i);
        container.appendChild(pageBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = last;
    nextBtn.onclick = () => loadMeetingRooms(number + 1);
    container.appendChild(nextBtn);
}

/* ==========================
   Actions
========================== */
// function editMeetingRoom(id) {
//     alert(`회의실 ID ${id} 수정 기능을 구현하세요.`);
// }

function editMeetingRoom(id) {
    // 상세 페이지로 이동 (id 전달)
    window.location.href = `/view/resource/admin/meetingrooms/detail?id=${id}`;
}

async function deleteMeetingRoom(id) {
    if (!confirm('정말로 이 회의실을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await apiFetch(
            `/api/admin/meetingrooms/${id}/delete`,
            { method: 'PATCH' }
        );

        if (!response.ok) {
            throw new Error();
        }

        alert('회의실이 삭제되었습니다.');
        // 현재 페이지 유지하며 다시 로드
        loadMeetingRooms(currentPage);

    } catch (error) {
        console.error(error);
        alert('회의실 삭제에 실패했습니다.');
    }
}

/* ==========================
   추가 버튼
========================== */
function initAddButton() {
    const addBtn = document.querySelector('.btn-add');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            // 회의실 추가 페이지로 이동
            window.location.href = '/view/resource/admin/meetingrooms/insert';
        });
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadMeetingRooms();
    initAddButton();
});
