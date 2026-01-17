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
    edit.onclick = () => editCar(id);

    const del = document.createElement('button');
    del.className = 'btn-delete';
    del.textContent = '삭제';
    del.onclick = () => deleteCar(id);

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
async function loadCars(page = 0) {
    try {
        const res = await apiFetch(
            `/api/admin/cars?page=${page}&size=${pageSize}&sort=id,asc`,
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
                            <i class="fas fa-car"></i>
                            <p>등록된 차량이 없습니다.</p>
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

        content.forEach((car, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(startNumber + i + 1),
                createCell(car.number, true),
                createCell(car.name, true),
                createCell(car.description, true),
                createStatusBadge(car.statusName),
                createActionCell(car.carId)
            );
            tbody.appendChild(tr);
        });

        // 페이지네이션 렌더링
        renderPagination(data);

    } catch (e) {
        console.error(e);
        await sweetError('차량 목록을 불러오지 못했습니다.');
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
    prevBtn.onclick = () => loadCars(number - 1);
    container.appendChild(prevBtn);

    // 페이지 번호 버튼
    const startPage = Math.floor(number / 5) * 5;
    const endPage = Math.min(startPage + 5, totalPages);

    for (let i = startPage; i < endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i + 1;
        pageBtn.className = i === number ? 'active' : '';
        pageBtn.onclick = () => loadCars(i);
        container.appendChild(pageBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = last;
    nextBtn.onclick = () => loadCars(number + 1);
    container.appendChild(nextBtn);
}

/* ==========================
   Actions
========================== */
function editCar(id) {
    // 차량 상세 조회 페이지로 이동
    window.location.href = `/view/resource/admin/cars/detail?id=${id}`;
}

async function deleteCar(id) {
    const result = await sweetConfirm(
        '삭제 확인',
        '차량을 삭제하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    try {
        const res = await apiFetch(
            `/api/admin/cars/${id}/delete`,
            { method: 'DELETE' }
        );

        if (!res.ok) throw new Error();

        await sweetSuccess('차량이 삭제되었습니다.');
        // 현재 페이지 유지하며 다시 로드
        loadCars(currentPage);

    } catch (e) {
        console.error(e);
        await sweetError('차량 삭제에 실패했습니다.');
    }
}

/* ==========================
   추가 버튼
========================== */
function initAddButton() {
    const addBtn = document.querySelector('.btn-add');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            // 차량 추가 페이지로 이동
            window.location.href = '/view/resource/admin/cars/insert';
        });
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadCars();
    initAddButton();
});
