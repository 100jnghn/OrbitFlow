/**
 * 관리자 - 회의실 추가 페이지
 */

// DOM 요소
const roomName = document.getElementById('room-name');
const roomPosition = document.getElementById('room-position');
const roomStatus = document.getElementById('room-status');
const roomDescription = document.getElementById('room-description');

const roomNameMsg = document.getElementById('room-name-msg');
const roomPositionMsg = document.getElementById('room-position-msg');
const roomStatusMsg = document.getElementById('room-status-msg');
const roomDescriptionMsg = document.getElementById('room-description-msg');

const saveBtn = document.getElementById('btn-save');

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 상태 목록만 로드 (초기값 없음)
    loadStatusOptions();
    initEventListeners();
    updateSaveButtonState();
});

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
function updateSaveButtonState() {
    saveBtn.disabled = !(
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
    if (saveBtn) {
        saveBtn.addEventListener('click', handleSave);
    }

    const cancelBtn = document.getElementById('btn-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', handleCancel);
    }

    // 실시간 검증
    roomName.addEventListener('input', () => {
        validateRoomName();
        updateSaveButtonState();
    });

    roomPosition.addEventListener('input', () => {
        validateRoomPosition();
        updateSaveButtonState();
    });

    roomStatus.addEventListener('change', () => {
        validateRoomStatus();
        updateSaveButtonState();
    });

    roomDescription.addEventListener('input', () => {
        validateRoomDescription();
    });
}

/**
 * 등록 버튼 핸들러
 */
async function handleSave() {
    // 버튼이 비활성화되어 있으면 중단
    if (saveBtn.disabled) {
        return;
    }

    // 최종 검증
    if (!validateRoomName()) {
        alert('회의실명을 확인해주세요.');
        roomName.focus();
        return;
    }

    if (!validateRoomPosition()) {
        alert('위치를 확인해주세요.');
        roomPosition.focus();
        return;
    }

    if (!validateRoomStatus()) {
        alert('상태를 선택해주세요.');
        roomStatus.focus();
        return;
    }

    // 등록 데이터 준비
    const payload = {
        name: roomName.value.trim(),
        position: roomPosition.value.trim(),
        description: roomDescription.value.trim(),
        statusId: Number(roomStatus.value)
    };

    try {
        const response = await apiFetch(
            '/api/admin/meetingrooms',
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '회의실 등록 실패');
        }

        alert('회의실이 등록되었습니다.');
        // 관리자 회의실 목록 화면으로 이동
        window.location.href = '/view/resource/admin/meetingrooms';

    } catch (error) {
        console.error(error);
        alert(error.message || '회의실 등록에 실패했습니다.');
    }
}

/**
 * 취소 버튼 핸들러
 */
function handleCancel() {
    if (confirm('작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?')) {
        window.location.href = '/view/resource/admin/meetingrooms';
    }
}

/**
 * 상태 목록 로드 (초기값 없음)
 */
async function loadStatusOptions() {
    try {
        const response = await fetch('/api/admin/resource-status');
        if (!response.ok) throw new Error();

        const result = await response.json();
        const statuses = result.data;

        const select = document.getElementById('room-status');
        select.innerHTML = '<option value="">상태 선택</option>';

        statuses.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;
            option.textContent = status.statusName;
            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('상태 목록을 불러오지 못했습니다.');
    }
}

