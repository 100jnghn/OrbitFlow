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
   Data Load
========================== */
async function loadCars() {
    try {
        const res = await apiFetch(
            `/api/admin/cars`,
            { method: 'GET' }
        );

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data?.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6">
                        <div class="empty-state">
                            <i class="fas fa-car"></i>
                            <p>등록된 차량이 없습니다.</p>
                        </div>
                    </td>
                </tr>`;
            return;
        }

        data.forEach((car, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(i + 1),
                createCell(car.number),
                createCell(car.name),
                createCell(car.description, true),
                createCell(car.statusName),
                createActionCell(car.id)
            );
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error(e);
        alert('차량 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Actions
========================== */
function editCar(id) {
    // 차량 상세 조회 페이지로 이동
    window.location.href = `/view/resource/admin/cars/detail?id=${id}`;
}

async function deleteCar(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const res = await apiFetch(
            `/api/admin/cars/${id}/delete`,
            { method: 'PATCH' }
        );

        if (!res.ok) throw new Error();

        alert('차량이 삭제되었습니다.');
        loadCars();

    } catch (e) {
        console.error(e);
        alert('차량 삭제에 실패했습니다.');
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
            window.location.href = '/view/resource/admin/cars/create';
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

