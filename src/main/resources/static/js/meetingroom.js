/* ==========================
   회의실 목록 조회
========================== */
async function loadMeetingRooms() {
    try {
        const res = await apiFetch('/api/admin/meetingrooms');

        if (!res.ok) {
            throw new Error('NETWORK_ERROR');
        }

        const result = await res.json();
        const list = result.data; // ResponseDto 기준

        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (list && list.length > 0) {
            list.forEach((room, index) => {
                const row = `
                    <tr>
                        <td>${index + 1}</td>
                        <td>${room.name ?? ''}</td>
                        <td>${room.position ?? ''}</td>
                        <td>${room.description ?? ''}</td>
                        <td>
                            <div class="action-btns">
                                <button type="button"
                                        class="btn-edit"
                                        onclick="editMeetingRoom(${room.meetingroomId})">
                                    수정
                                </button>
                                <button type="button"
                                        class="btn-delete"
                                        onclick="deleteMeetingRoom(${room.meetingroomId})">
                                    삭제
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
                tbody.insertAdjacentHTML('beforeend', row);
            });
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5">
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p>등록된 리소스가 없습니다.</p>
                        </div>
                    </td>
                </tr>
            `;
        }

    } catch (error) {
        console.error('회의실 목록 로드 실패:', error);
        alert('회의실 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   회의실 수정 (placeholder)
========================== */
function editMeetingRoom(meetingroomId) {
    console.log('수정할 회의실 ID:', meetingroomId);
    alert(`회의실 ID ${meetingroomId} 수정 기능을 구현하세요.`);
}

/* ==========================
   회의실 삭제
========================== */
async function deleteMeetingRoom(meetingroomId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    try {
        const res = await apiFetch(`/api/admin/meetingrooms/${meetingroomId}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            throw new Error('DELETE_FAILED');
        }

        alert('삭제되었습니다.');
        loadMeetingRooms(); // 목록 갱신

    } catch (error) {
        console.error('회의실 삭제 실패:', error);
        alert('회의실 삭제에 실패했습니다.');
    }
}

/* ==========================
   페이지 진입 시 실행
========================== */
document.addEventListener('DOMContentLoaded', loadMeetingRooms);
