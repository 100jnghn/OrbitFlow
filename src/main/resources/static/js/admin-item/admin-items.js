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
   Data Load
========================== */
async function loadItems(categoryId = null) {
    try {
        let url = '/api/admin/items';
        if (categoryId) {
            url = `/api/admin/categories/${categoryId}/items`;
        }

        const res = await apiFetch(url, {method: 'GET'});

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data?.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7">
                        <div class="empty-state">
                            <i class="fas fa-box"></i>
                            <p>등록된 비품이 없습니다.</p>
                        </div>
                    </td>
                </tr>`;
            return;
        }

        data.forEach((item, i) => {
            console.log(item)
            const tr = document.createElement('tr');
            tr.append(
                createCell(i + 1),
                createCell(item.itemCategoryName),
                createCell(item.name),
                createCell(item.description, true),
                createCell(item.statusName),
                createActionCell(item.itemId)
            );
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error(e);
        alert('비품 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Category Load
========================== */
async function loadCategories() {
    try {
        const res = await apiFetch(
            '/api/item-categories',
            {method: 'GET'}
        );

        if (!res.ok) throw new Error();

        const {data} = await res.json();
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
    window.location.href = `/view/resource/admin/items/detail?id=${id}`;
}

async function deleteItem(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const res = await apiFetch(
            `/api/admin/items/${id}/delete`,
            {method: 'PATCH'}
        );

        if (!res.ok) throw new Error();

        alert('비품이 삭제되었습니다.');

        // 현재 선택된 카테고리로 다시 로드
        const categoryId = document.getElementById('item-category-filter').value;
        loadItems(categoryId || null);

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
            const categoryId = e.target.value;
            console.log("선택 카테고리 : " + categoryId)
            loadItems(categoryId || null);
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

