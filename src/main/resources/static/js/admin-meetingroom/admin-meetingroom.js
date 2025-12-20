/**
 * 관리자 - 회의실 상세 조회 페이지 (필수 기능만)
 */

// 현재 회의실 ID
let currentRoomId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    currentRoomId = getMeetingroomId();

    if (!currentRoomId) {
        alert('회의실 정보를 찾을 수 없습니다.');
        history.back();
        return;
    }

    loadRoomDetail();
    initEventListeners();
});

/**
 * 쿼리스트링에서 회의실 ID 추출
 * 예: /view/resource/admin/meetingrooms/detail?id=3
 */
function getMeetingroomId() {
    return new URLSearchParams(window.location.search).get('id');
}

/**
 * 이벤트 리스너 초기화
 */
function initEventListeners() {
    const editBtn = document.getElementById('btn-edit');
    if (editBtn) {
        editBtn.addEventListener('click', handleEdit);
    }

    const deleteBtn = document.getElementById('btn-delete');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', handleDelete);
    }
}

/**
 * 수정 버튼
 * (아직 edit 화면이 없으므로 id 유지)
 */
async function handleEdit() {

    const statusValue = document.getElementById('room-status').value;

    if (!statusValue) {
        alert('회의실 상태를 선택해주세요.');
        return;
    }

    const payload = {
        name: document.getElementById('room-name').value,
        position: document.getElementById('room-position').value,
        description: document.getElementById('room-description').value,
        statusId: Number(statusValue)   // 🔥 Long 매핑
    };

    try {
        const response = await fetch(
            `/api/admin/meetingrooms/${currentRoomId}`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            throw new Error('회의실 수정 실패');
        }

        // ✅ 관리자 회의실 목록 화면으로 이동
        window.location.href = '/view/resource/admin/meetingrooms';

    } catch (error) {
        console.error(error);
        alert('회의실 수정에 실패했습니다.');
    }
}


/**
 * 삭제 버튼
 * ※ Controller 기준: PATCH /admin/meetingrooms/{id}/delete
 */
async function handleDelete() {
    if (!confirm('정말로 이 회의실을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(
            `/api/admin/meetingrooms/${currentRoomId}/delete`,
            { method: 'PATCH' }
        );

        if (!response.ok) {
            throw new Error();
        }

        window.location.href = '/view/resource/admin/meetingrooms';

    } catch (error) {
        console.error(error);
        alert('회의실 삭제에 실패했습니다.');
    }
}

async function loadStatusOptions(selectedStatusId) {
    try {
        const response = await fetch('/api/admin/resource-status');

        if (!response.ok) throw new Error();

        const result = await response.json();
        const statuses = result.data;

        const select = document.getElementById('room-status');
        select.innerHTML = '<option value="">상태 선택</option>';

        statuses.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;              // Long
            option.textContent = status.statusName;

            if (status.id === selectedStatusId) {
                option.selected = true;           // ✅ 매칭
            }

            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('상태 목록을 불러오지 못했습니다.');
    }
}

async function loadRoomDetail() {
    try {
        const response = await fetch(`/api/meetingrooms/${currentRoomId}`);
        if (!response.ok) throw new Error();

        const result = await response.json();
        const data = result.data;

        document.getElementById('room-name').value = data.name ?? '';
        document.getElementById('room-position').value = data.position ?? '';
        document.getElementById('room-description').value = data.description ?? '';

        // 🔥 resourceStatusId 기준
        await loadStatusOptions(data.statusId);

    } catch (error) {
        console.error(error);
        showError();
    }
}

/**
 * 에러 처리
 */
function showError() {
    document.getElementById('room-name').textContent = '정보 없음';
    document.getElementById('room-position').textContent = '정보 없음';
    document.getElementById('room-status').textContent = '정보 없음';
    document.getElementById('room-description').textContent =
        '회의실 정보를 불러올 수 없습니다.';
}
