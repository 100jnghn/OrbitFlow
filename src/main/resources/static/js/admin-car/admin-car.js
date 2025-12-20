/**
 * 관리자 - 차량 상세 조회 페이지
 */

// 현재 차량 ID
let currentCarId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    currentCarId = getCarId();

    if (!currentCarId) {
        alert('차량 정보를 찾을 수 없습니다.');
        history.back();
        return;
    }

    loadCarDetail();
    initEventListeners();
});

/**
 * 쿼리스트링에서 차량 ID 추출
 * 예: /view/resource/admin/cars/detail?id=3
 */
function getCarId() {
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
 */
async function handleEdit() {

    const statusValue = document.getElementById('car-status').value;

    if (!statusValue) {
        alert('차량 상태를 선택해주세요.');
        return;
    }

    const payload = {
        number: document.getElementById('car-number').value,
        name: document.getElementById('car-model').value,
        description: document.getElementById('car-description').value,
        statusId: Number(statusValue)
    };

    try {
        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            throw new Error('차량 수정 실패');
        }

        // ✅ 관리자 차량 목록 화면으로 이동
        window.location.href = '/view/resource/admin/cars';

    } catch (error) {
        console.error(error);
        alert('차량 수정에 실패했습니다.');
    }
}


/**
 * 삭제 버튼
 */
async function handleDelete() {
    if (!confirm('정말로 이 차량을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}/delete`,
            { method: 'PATCH' }
        );

        if (!response.ok) {
            throw new Error();
        }

        window.location.href = '/view/resource/admin/cars';

    } catch (error) {
        console.error(error);
        alert('차량 삭제에 실패했습니다.');
    }
}

async function loadStatusOptions(selectedStatusId) {
    try {
        const response = await apiFetch(
            '/api/admin/resource-status',
        { method: 'GET' }
        );

        if (!response.ok) throw new Error();

        const result = await response.json();
        const statuses = result.data;

        const select = document.getElementById('car-status');
        select.innerHTML = '<option value="">상태 선택</option>';

        statuses.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;
            option.textContent = status.statusName;

            if (status.id === selectedStatusId) {
                option.selected = true;
            }

            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('상태 목록을 불러오지 못했습니다.');
    }
}

async function loadCarDetail() {
    try {
        const response = await apiFetch(
            '/api/cars/${currentCarId}',
            { method: 'GET' }
        );


        if (!response.ok) throw new Error();

        const result = await response.json();
        const data = result.data;

        document.getElementById('car-number').value = data.number ?? '';
        document.getElementById('car-model').value = data.name ?? '';
        document.getElementById('car-description').value = data.description ?? '';

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
    document.getElementById('car-number').value = '';
    document.getElementById('car-model').value = '';
    document.getElementById('car-description').value = '차량 정보를 불러올 수 없습니다.';
}


