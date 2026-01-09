/**
 * 관리자 - 회의실 상세 조회 페이지 (필수 기능만)
 */

// 현재 회의실 ID
let currentRoomId = null;

// DOM 요소
let roomName, roomPosition, roomStatus, roomDescription;
let roomNameMsg, roomPositionMsg, roomStatusMsg, roomDescriptionMsg;
let editBtn;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // DOM 요소 초기화
    roomName = document.getElementById('room-name');
    roomPosition = document.getElementById('room-position');
    roomStatus = document.getElementById('room-status');
    roomDescription = document.getElementById('room-description');

    roomNameMsg = document.getElementById('room-name-msg');
    roomPositionMsg = document.getElementById('room-position-msg');
    roomStatusMsg = document.getElementById('room-status-msg');
    roomDescriptionMsg = document.getElementById('room-description-msg');

    editBtn = document.getElementById('btn-edit');

    currentRoomId = getMeetingroomId();

    if (!currentRoomId) {
        sweetError('회의실 정보를 찾을 수 없습니다.');
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

/* ======================
   공통 메시지
====================== */
function showMsg(el, message, type) {
    el.textContent = message;
    el.className = 'hint ' + type;
}

/* ======================
   버튼 상태
====================== */
function updateEditButtonState() {
    editBtn.disabled = !(
        validateRoomName() &&
        validateRoomPosition() &&
        validateRoomStatus()
    );
}

/* ======================
   회의실명 검증 (최대 30자, not null)
====================== */
function validateRoomName() {
    const v = roomName.value.trim();
    if (!v) {
        showMsg(roomNameMsg, '회의실명을 입력해주세요. (0/30)', 'error');
        return false;
    }
    showMsg(roomNameMsg, `입력됨 (${v.length}/30)`, 'success');
    return true;
}

/* ======================
   위치 검증 (최대 50자, not null)
====================== */
function validateRoomPosition() {
    const v = roomPosition.value.trim();
    if (!v) {
        showMsg(roomPositionMsg, '위치를 입력해주세요. (0/50)', 'error');
        return false;
    }
    showMsg(roomPositionMsg, `입력됨 (${v.length}/50)`, 'success');
    return true;
}

/* ======================
   상태 검증 (not null)
====================== */
function validateRoomStatus() {
    const v = roomStatus.value;
    if (!v) {
        showMsg(roomStatusMsg, '상태를 선택해주세요.', 'error');
        return false;
    }
    showMsg(roomStatusMsg, '선택됨', 'success');
    return true;
}

/* ======================
   비고 검증 (최대 250자, nullable)
====================== */
function validateRoomDescription() {
    const v = roomDescription.value.trim();
    if (v) {
        showMsg(roomDescriptionMsg, `입력됨 (${v.length}/250)`, 'success');
    } else {
        roomDescriptionMsg.textContent = '';
    }
    return true; // nullable이므로 항상 true
}

/**
 * 이벤트 리스너 초기화
 */
function initEventListeners() {
    if (editBtn) {
        editBtn.addEventListener('click', handleEdit);
    }

    const deleteBtn = document.getElementById('btn-delete');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', handleDelete);
    }

    // 실시간 검증
    roomName.addEventListener('input', () => {
        validateRoomName();
        updateEditButtonState();
    });

    roomPosition.addEventListener('input', () => {
        validateRoomPosition();
        updateEditButtonState();
    });

    roomStatus.addEventListener('change', () => {
        validateRoomStatus();
        updateEditButtonState();
    });

    roomDescription.addEventListener('input', () => {
        validateRoomDescription();
    });
}

/**
 * 수정 버튼
 */
async function handleEdit() {
    // validation 검증
    if (!validateRoomName() || !validateRoomPosition() || !validateRoomStatus()) {
        await sweetInfo('입력 항목을 확인해주세요.');
        return;
    }

    const payload = {
        name: roomName.value.trim(),
        position: roomPosition.value.trim(),
        description: roomDescription.value.trim(),
        statusId: Number(roomStatus.value)   // 🔥 Long 매핑
    };

    try {
        const response = await apiFetch(
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
        await sweetError('회의실 수정에 실패했습니다.');
    }
}


/**
 * 삭제 버튼
 * ※ Controller 기준: PATCH /admin/meetingrooms/{id}/delete
 */
async function handleDelete() {

    const result = await sweetConfirm(
        '삭제 확인',
        '회의실을 삭제하시겠습니까?'
    );

    if (!result.isConfirmed) return;


    try {
        const response = await apiFetch(
            `/api/admin/meetingrooms/${currentRoomId}/delete`,
            { method: 'PATCH' }
        );

        if (!response.ok) {
            throw new Error();
        }

        window.location.href = '/view/resource/admin/meetingrooms';

    } catch (error) {
        console.error(error);
        await sweetError('회의실 삭제에 실패했습니다.');
    }
}

async function loadStatusOptions(selectedStatusId) {
    try {
        const response = await apiFetch('/api/admin/resource-status');

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
        await sweetError('상태 목록을 불러오지 못했습니다.');
    }
}

async function loadRoomDetail() {
    try {
        const response = await apiFetch(`/api/meetingrooms/${currentRoomId}`);
        if (!response.ok) throw new Error();

        const result = await response.json();
        const data = result.data;

        roomName.value = data.name ?? '';
        roomPosition.value = data.position ?? '';
        roomDescription.value = data.description ?? '';

        // 등록자 정보 표시
        document.getElementById('uploader-name').textContent = data.uploaderName ?? '-';
        document.getElementById('created-at').textContent = formatDate(data.createdAt);

        // 🔥 resourceStatusId 기준
        await loadStatusOptions(data.statusId);

        // 초기 validation
        validateRoomName();
        validateRoomPosition();
        validateRoomStatus();
        validateRoomDescription();
        updateEditButtonState();

    } catch (error) {
        console.error(error);
        showError();
    }
}

/**
 * 날짜 포맷팅
 */
function formatDate(localDateString) {
    if (!localDateString) return '-';

    // LocalDate는 이미 YYYY-MM-DD 형식
    return localDateString;
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
    document.getElementById('uploader-name').textContent = '-';
    document.getElementById('created-at').textContent = '-';
}
