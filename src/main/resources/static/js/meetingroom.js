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
   Data Load
========================== */
async function loadMeetingRooms() {
    try {
        const res = await apiFetch('/api/admin/meetingrooms');
        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data?.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6">
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p>등록된 리소스가 없습니다.</p>
                        </div>
                    </td>
                </tr>`;
            return;
        }

        data.forEach((room, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(i + 1),
                createCell(room.name),
                createCell(room.position, true),
                createCell(room.description, true),
                createCell(room.statusCode),
                createActionCell(room.meetingroomId)
            );
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error(e);
        alert('회의실 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Actions
========================== */
function editMeetingRoom(id) {
    alert(`회의실 ID ${id} 수정 기능을 구현하세요.`);
}

async function deleteMeetingRoom(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    await apiFetch(`/api/admin/meetingrooms/${id}`, { method: 'DELETE' });
    loadMeetingRooms();
}

document.addEventListener('DOMContentLoaded', loadMeetingRooms);
