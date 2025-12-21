/**
 * 관리자 - 회의실 추가 페이지
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 상태 목록만 로드 (초기값 없음)
    loadStatusOptions();
    initEventListeners();
});

/**
 * 이벤트 리스너 초기화
 */
function initEventListeners() {
    const saveBtn = document.getElementById('btn-save');
    if (saveBtn) {
        saveBtn.addEventListener('click', handleSave);
    }

    const cancelBtn = document.getElementById('btn-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', handleCancel);
    }
}

/**
 * 등록 버튼 핸들러
 */
async function handleSave() {
    // 입력값 검증
    const name = document.getElementById('room-name').value.trim();
    const position = document.getElementById('room-position').value.trim();
    const statusValue = document.getElementById('room-status').value;
    const description = document.getElementById('room-description').value.trim();

    if (!name) {
        alert('회의실명을 입력해주세요.');
        document.getElementById('room-name').focus();
        return;
    }

    if (!position) {
        alert('위치를 입력해주세요.');
        document.getElementById('room-position').focus();
        return;
    }

    if (!statusValue) {
        alert('상태를 선택해주세요.');
        document.getElementById('room-status').focus();
        return;
    }

    // 등록 데이터 준비
    const payload = {
        name: name,
        position: position,
        description: description,
        statusId: Number(statusValue)
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
            throw new Error('회의실 등록 실패');
        }

        alert('회의실이 등록되었습니다.');
        // 관리자 회의실 목록 화면으로 이동
        window.location.href = '/view/resource/admin/meetingrooms';

    } catch (error) {
        console.error(error);
        alert('회의실 등록에 실패했습니다.');
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

