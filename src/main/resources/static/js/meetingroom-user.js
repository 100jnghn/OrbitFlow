/* ==========================
   Tooltip (공통 재사용)
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
    if (tooltipEl) tooltipEl.style.display = 'none';
}

/* ==========================
   Cell Helper
========================== */
function createCell(value = '', tooltip = false) {
    const td = document.createElement('td');
    const text = (value ?? '').toString();
    td.textContent = text;

    if (tooltip && text) {
        td.dataset.fulltext = text;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }
    return td;
}

/* ==========================
   Load Data (USER)
========================== */
async function loadMeetingRooms() {
    try {
        const res = await apiFetch('/api/meetingrooms');
        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data?.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5">
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p>사용 가능한 회의실이 없습니다.</p>
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
                createCell(room.statusName)
            );
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error(e);
        alert('회의실 목록을 불러오지 못했습니다.');
    }
}

document.addEventListener('DOMContentLoaded', loadMeetingRooms);
