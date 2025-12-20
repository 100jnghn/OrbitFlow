/**
 * 관리자 - 차량 상세 조회 페이지
 */

// 현재 차량 ID
let currentCarId = null;

// 선택된 이미지 파일 (새로 업로드하는 경우)
let selectedImageFile = null;

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

    // 이미지 업로드 관련
    const imageInput = document.getElementById('car-image-input');
    const removeBtn = document.getElementById('btn-remove');
    const previewArea = document.getElementById('car-image-preview');

    // 미리보기 영역 클릭
    if (previewArea) {
        previewArea.addEventListener('click', () => {
            imageInput.click();
        });
    }

    // 파일 선택 시
    if (imageInput) {
        imageInput.addEventListener('change', handleImageSelect);
    }

    // 이미지 제거 버튼
    if (removeBtn) {
        removeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            handleImageRemove();
        });
    }
}

/**
 * 이미지 선택 핸들러
 */
function handleImageSelect(event) {
    const file = event.target.files[0];
    
    if (!file) return;

    // 파일 타입 검증
    if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 업로드 가능합니다.');
        return;
    }

    // 파일 크기 검증 (5MB)
    if (file.size > 5 * 1024 * 1024) {
        alert('이미지 크기는 5MB를 초과할 수 없습니다.');
        return;
    }

    selectedImageFile = file;

    // 미리보기 표시
    const reader = new FileReader();
    reader.onload = (e) => {
        displayImagePreview(e.target.result);
    };
    reader.readAsDataURL(file);
}

/**
 * 이미지 미리보기 표시
 */
function displayImagePreview(imageUrl) {
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');

    if (carImage && placeholder) {
        carImage.src = imageUrl;
        carImage.style.display = 'block';
        placeholder.style.display = 'none';
        
        if (removeBtn) {
            removeBtn.style.display = 'inline-flex';
        }
    }
}

/**
 * 이미지 제거 핸들러
 */
function handleImageRemove() {
    selectedImageFile = null;
    
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const imageInput = document.getElementById('car-image-input');

    if (carImage) {
        carImage.src = '';
        carImage.style.display = 'none';
    }
    
    if (placeholder) {
        placeholder.style.display = 'flex';
    }
    
    if (removeBtn) {
        removeBtn.style.display = 'none';
    }
    
    if (imageInput) {
        imageInput.value = '';
    }
}

/**
 * 수정 버튼
 */
async function handleEdit() {

    const statusValue = document.getElementById('car-status').value;

    console.log("수정 " + currentCarId)
    console.log("상태값 " + statusValue)

    if (!statusValue) {
        alert('차량 상태를 선택해주세요.');
        return;
    }

    const formData = new FormData();
    formData.append('number', document.getElementById('car-number').value);
    formData.append('name', document.getElementById('car-model').value);
    formData.append('driverAge', document.getElementById('car-age').value);
    formData.append('description', document.getElementById('car-description').value);
    formData.append('statusId', statusValue);

    // 이미지 파일이 새로 선택된 경우 추가
    if (selectedImageFile) {
        formData.append('file', selectedImageFile);
    }

    try {
        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}`,
            {
                method: 'PUT',
                body: formData
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
    console.log(currentCarId)

    try {
        const response = await apiFetch(
            `/api/cars/${currentCarId}`,
            { method: 'GET' }
        );


        if (!response.ok) throw new Error();

        const result = await response.json();
        const data = result.data;

        document.getElementById('car-number').value = data.number ?? '';
        document.getElementById('car-model').value = data.name ?? '';
        document.getElementById('car-age').value = data.driverAge ?? '';
        document.getElementById('car-description').value = data.description ?? '';

        // 차량 이미지 표시
        displayCarImage(data.fileUrl);

        await loadStatusOptions(data.statusId);

    } catch (error) {
        console.error(error);
        showError();
    }
}

/**
 * 차량 이미지 표시 (기존 이미지 로드 시)
 */
function displayCarImage(imageUrl) {
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    
    if (imageUrl) {
        // 이미지가 있는 경우
        carImage.src = imageUrl;
        carImage.style.display = 'block';
        if (placeholder) {
            placeholder.style.display = 'none';
        }
        if (removeBtn) {
            removeBtn.style.display = 'inline-flex';
        }
    } else {
        // 이미지가 없는 경우
        carImage.style.display = 'none';
        if (placeholder) {
            placeholder.style.display = 'flex';
        }
        if (removeBtn) {
            removeBtn.style.display = 'none';
        }
    }
}

/**
 * 에러 처리
 */
function showError() {
    document.getElementById('car-number').value = '';
    document.getElementById('car-model').value = '';
    document.getElementById('car-age').value = '';
    document.getElementById('car-description').value = '차량 정보를 불러올 수 없습니다.';
    
    // 이미지도 초기화
    displayCarImage(null);
}


