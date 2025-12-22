/**
 * 관리자 - 차량 추가 페이지
 */

// 선택된 이미지 파일
let selectedImageFile = null;

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

    // 이미지 업로드 관련
    const imageInput = document.getElementById('car-image-input');
    const uploadBtn = document.getElementById('btn-upload');
    const removeBtn = document.getElementById('btn-remove');
    const previewArea = document.getElementById('car-image-preview');

    // 업로드 버튼 클릭
    if (uploadBtn) {
        uploadBtn.addEventListener('click', () => {
            imageInput.click();
        });
    }

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
    const previewImage = document.getElementById('preview-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');

    if (previewImage && placeholder) {
        previewImage.src = imageUrl;
        previewImage.style.display = 'block';
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

    const previewImage = document.getElementById('preview-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const imageInput = document.getElementById('car-image-input');

    if (previewImage) {
        previewImage.src = '';
        previewImage.style.display = 'none';
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
 * 등록 버튼 핸들러
 */
async function handleSave() {
    // 입력값 검증
    const number = document.getElementById('car-number').value.trim();
    const model = document.getElementById('car-model').value.trim();
    const age = document.getElementById('car-age').value.trim();
    const statusValue = document.getElementById('car-status').value;
    const description = document.getElementById('car-description').value.trim();

    if (!number) {
        alert('차량 번호를 입력해주세요.');
        document.getElementById('car-number').focus();
        return;
    }

    if (!model) {
        alert('차종을 입력해주세요.');
        document.getElementById('car-model').focus();
        return;
    }

    if (!age || age < 18) {
        alert('운전 가능 나이를 올바르게 입력해주세요. (최소 18세)');
        document.getElementById('car-age').focus();
        return;
    }

    if (!statusValue) {
        alert('상태를 선택해주세요.');
        document.getElementById('car-status').focus();
        return;
    }

    // FormData 생성 (파일 업로드를 위해)
    const formData = new FormData();
    formData.append('number', number);
    formData.append('name', model);
    formData.append('driverAge', age);
    formData.append('description', description);
    formData.append('statusId', statusValue);

    // 이미지 파일이 있으면 추가 (선택사항)
    if (selectedImageFile) {
        formData.append('file', selectedImageFile);
    }

    try {
        const response = await apiFetch(
            '/api/admin/cars',
            {
                method: 'POST',
                body: formData
            }
        );

        console.log(response);

        if (response.ok) {
            alert('차량이 등록되었습니다.');

            // 관리자 차량 목록 화면으로 이동
            window.location.href = '/view/resource/admin/cars';

        } else {
            const result = await response.json();
            alert(result.message)
        }

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

/**
 * 취소 버튼 핸들러
 */
function handleCancel() {
    if (confirm('작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?')) {
        window.location.href = '/view/resource/admin/cars';
    }
}

/**
 * 상태 목록 로드 (초기값 없음)
 */
async function loadStatusOptions() {
    try {
        const response = await apiFetch(
            '/api/admin/resource-status',
            {method: 'GET'}
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
            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('상태 목록을 불러오지 못했습니다.');
    }
}

