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
    edit.onclick = () => editItem(id);

    const del = document.createElement('button');
    del.className = 'btn-delete';
    del.textContent = '삭제';
    del.onclick = () => deleteItem(id);

    box.append(edit, del);
    td.appendChild(box);
    return td;
}

/* ==========================
   Pagination State
========================== */
let currentPage = 0;
let totalPages = 0;
let pageSize = 5;
let currentCategoryId = null;

/* ==========================
   Data Load
========================== */
async function loadItems(categoryId = null, page = 0) {
    try {
        // 현재 카테고리 ID 저장
        currentCategoryId = categoryId;

        let url = '/api/admin/items';
        if (categoryId) {
            url = `/api/admin/categories/${categoryId}/items`;
        }

        // Pagination 파라미터 추가
        url += `?page=${page}&size=${pageSize}&sort=id,asc`;

        const res = await apiFetch(url, { method: 'GET' });

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
                            <i class="fas fa-box"></i>
                            <p>등록된 비품이 없습니다.</p>
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

        content.forEach((item, i) => {
            console.log(item)
            const tr = document.createElement('tr');
            tr.append(
                createCell(startNumber + i + 1),
                createCell(item.itemCategoryName),
                createCell(item.name, true),
                createCell(item.description, true),
                createStatusBadge(item.statusName),
                createActionCell(item.itemId)
            );
            tbody.appendChild(tr);
        });

        // 페이지네이션 렌더링
        renderPagination(data);

    } catch (e) {
        console.error(e);
        alert('비품 목록을 불러오지 못했습니다.');
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
    prevBtn.onclick = () => loadItems(currentCategoryId, number - 1);
    container.appendChild(prevBtn);

    // 페이지 번호 버튼
    const startPage = Math.floor(number / 5) * 5;
    const endPage = Math.min(startPage + 5, totalPages);

    for (let i = startPage; i < endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i + 1;
        pageBtn.className = i === number ? 'active' : '';
        pageBtn.onclick = () => loadItems(currentCategoryId, i);
        container.appendChild(pageBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = last;
    nextBtn.onclick = () => loadItems(currentCategoryId, number + 1);
    container.appendChild(nextBtn);
}

/* ==========================
   Category Load
========================== */
async function loadCategories() {
    try {
        const res = await apiFetch(
            '/api/item-categories',
            { method: 'GET' }
        );

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const select = document.getElementById('item-category-filter');

        // 기존 "전체" 옵션 유지
        select.innerHTML = '<option value="">전체</option>';

        data.forEach(category => {
            console.log("카테고리 : " + category.name)

            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('카테고리 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Actions
   수정 버튼 (상세 조회)
========================== */
function editItem(id) {
    // 비품 상세 조회 페이지로 이동
    console.log(id)
    window.location.href = `/view/resource/admin/items/detail?id=${id}`;
}

async function deleteItem(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const res = await apiFetch(
            `/api/admin/items/${id}/delete`,
            { method: 'PATCH' }
        );

        if (!res.ok) throw new Error();

        alert('비품이 삭제되었습니다.');

        // 현재 페이지 유지하며 다시 로드
        loadItems(currentCategoryId, currentPage);

    } catch (e) {
        console.error(e);
        alert('비품 삭제에 실패했습니다.');
    }
}

/* ==========================
   추가 버튼
========================== */
function initAddButton() {
    const addBtn = document.querySelector('.btn-add');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            // 비품 추가 페이지로 이동
            window.location.href = '/view/resource/admin/items/insert';
        });
    }
}

/* ==========================
   카테고리 관리 버튼
========================== */
function initCategoryManageButton() {
    const manageBtn = document.getElementById('btn-category-manage');
    if (manageBtn) {
        manageBtn.addEventListener('click', () => {
            // 카테고리 관리 페이지로 이동
            window.location.href = '/view/resource/admin/item-categories';
        });
    }
}

/* ==========================
   카테고리 필터
========================== */
function initCategoryFilter() {
    const categoryFilter = document.getElementById('item-category-filter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', (e) => {
            const categoryId = e.target.value
            currentCategoryId = categoryId
            console.log("선택 카테고리 : " + categoryId)
            console.log("현재 카테고리 : " + currentCategoryId)
            // 카테고리 변경 시 첫 페이지부터 시작
            loadItems(categoryId, 0);
        });
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadItems();
    initAddButton();
    initCategoryManageButton();
    initCategoryFilter();
});
