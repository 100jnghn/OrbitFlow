/**
 * 관리자 - 차량 추가 페이지
 */

// 선택된 이미지 파일
let selectedImageFile = null;

// DOM 요소
let carNumber, carModel, carAge, carStatus, carDescription;
let carNumberMsg, carModelMsg, carAgeMsg, carStatusMsg, carDescriptionMsg;
let saveBtn;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // DOM 요소 초기화
    carNumber = document.getElementById('car-number');
    carModel = document.getElementById('car-model');
    carAge = document.getElementById('car-age');
    carStatus = document.getElementById('car-status');
    carDescription = document.getElementById('car-description');

    carNumberMsg = document.getElementById('car-number-msg');
    carModelMsg = document.getElementById('car-model-msg');
    carAgeMsg = document.getElementById('car-age-msg');
    carStatusMsg = document.getElementById('car-status-msg');
    carDescriptionMsg = document.getElementById('car-description-msg');

    saveBtn = document.getElementById('btn-save');

    // 상태 목록만 로드 (초기값 없음)
    loadStatusOptions();
    initEventListeners();
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
        validateCarNumber() &&
        validateCarModel() &&
        validateCarAge() &&
        validateCarStatus()
    );
}

/* ======================
   차량 번호 검증 (최대 15자, not null)
====================== */
function validateCarNumber() {
    const v = carNumber.value.trim();
    if (!v) {
        showMsg(carNumberMsg, '차량 번호를 입력해주세요. (0/15)', 'error');
        return false;
    }
    showMsg(carNumberMsg, `입력됨 (${v.length}/15)`, 'success');
    return true;
}

/* ======================
   차종 검증 (최대 50자, not null)
====================== */
function validateCarModel() {
    const v = carModel.value.trim();
    if (!v) {
        showMsg(carModelMsg, '차종을 입력해주세요. (0/50)', 'error');
        return false;
    }
    showMsg(carModelMsg, `입력됨 (${v.length}/50)`, 'success');
    return true;
}

/* ======================
   운전 가능 나이 검증 (0 이상, not null)
====================== */
function validateCarAge() {
    const v = carAge.value.trim();
    if (!v) {
        showMsg(carAgeMsg, '운전 가능 나이를 입력해주세요.', 'error');
        return false;
    }
    const age = parseInt(v);
    if (isNaN(age) || age < 0) {
        showMsg(carAgeMsg, '0 이상의 나이를 입력해주세요.', 'error');
        return false;
    }
    showMsg(carAgeMsg, '입력됨', 'success');
    return true;
}

/* ======================
   상태 검증 (not null)
====================== */
function validateCarStatus() {
    const v = carStatus.value;
    if (!v) {
        showMsg(carStatusMsg, '상태를 선택해주세요.', 'error');
        return false;
    }
    showMsg(carStatusMsg, '선택됨', 'success');
    return true;
}

/* ======================
   비고 검증 (최대 255자, nullable)
====================== */
function validateCarDescription() {
    const v = carDescription.value.trim();
    if (v) {
        showMsg(carDescriptionMsg, `입력됨 (${v.length}/255)`, 'success');
    } else {
        carDescriptionMsg.textContent = '';
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
    carNumber.addEventListener('input', () => {
        validateCarNumber();
        updateSaveButtonState();
    });

    carModel.addEventListener('input', () => {
        validateCarModel();
        updateSaveButtonState();
    });

    carAge.addEventListener('input', () => {
        validateCarAge();
        updateSaveButtonState();
    });

    carStatus.addEventListener('change', () => {
        validateCarStatus();
        updateSaveButtonState();
    });

    carDescription.addEventListener('input', () => {
        validateCarDescription();
    });

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

    // 초기 버튼 상태 업데이트
    updateSaveButtonState();
}

/**
 * 이미지 파일 검증
 * - 파일 크기: 50MB 이하
 * - 이미지 비율: 가로가 세로보다 길어야 함 (1:1 제외)
 * - 최대 비율: 16:9
 */
async function validateImageFile(file) {
    // 파일 타입 검증
    if (!file.type.startsWith('image/')) {
        return { valid: false, message: '이미지 파일만 업로드 가능합니다.' };
    }

    // 파일 크기 검증 (50MB)
    const maxSize = 50 * 1024 * 1024; // 50MB in bytes
    if (file.size > maxSize) {
        return { valid: false, message: '이미지 크기는 50MB를 초과할 수 없습니다.' };
    }

    // 이미지 비율 검증을 위해 이미지 로드
    return new Promise((resolve) => {
        const img = new Image();
        const url = URL.createObjectURL(file);

        img.onload = () => {
            URL.revokeObjectURL(url); // 메모리 해제

            const width = img.width;
            const height = img.height;
            const ratio = width / height;

            // 1:1 이하 (세로가 가로보다 길거나 같은 경우) 제외
            if (ratio <= 1) {
                resolve({
                    valid: false,
                    message: '가로가 세로보다 긴 이미지만 업로드 가능합니다.'
                });
                return;
            }

            // 16:9 초과 체크
            const maxRatio = 1.7; // 약 1.778
            if (width * 9 > height * 16) {
                resolve({
                    valid: false,
                    message: '이미지 비율은 최대 16:9까지 가능합니다.'
                });
                return;
            }

            resolve({ valid: true });
        };

        img.onerror = () => {
            URL.revokeObjectURL(url);
            resolve({ valid: false, message: '이미지 파일을 읽을 수 없습니다.' });
        };

        img.src = url;
    });
}

/**
 * 이미지 선택 핸들러
 */
async function handleImageSelect(event) {
    const file = event.target.files[0];

    if (!file) return;

    // 이미지 파일 검증
    const validation = await validateImageFile(file);

    if (!validation.valid) {
        await sweetWarning(validation.message);
        // 파일 입력 초기화
        event.target.value = '';
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
    // validation 검증
    if (!validateCarNumber() || !validateCarModel() || !validateCarAge() || !validateCarStatus()) {
        await sweetInfo('입력 항목을 확인해주세요.');
        return;
    }

    const number = carNumber.value.trim();
    const model = carModel.value.trim();
    const age = carAge.value.trim();
    const statusValue = carStatus.value;
    const description = carDescription.value.trim();

    // FormData 생성 (파일 업로드를 위해)
    const formData = new FormData();
    formData.append('number', number);
    formData.append('name', model);
    formData.append('driverAge', age);
    formData.append('description', description);
    formData.append('statusId', statusValue);

    // 이미지 파일이 있으면 추가
    if (selectedImageFile) {
        formData.append('imgFile', selectedImageFile);
    } else {
        formData.append('imgFile', new Blob([]), '');
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
            await sweetSuccess('차량이 등록되었습니다.');

            // 관리자 차량 목록 화면으로 이동
            window.location.href = '/view/resource/admin/cars';

        } else {
            const result = await response.json();
            await sweetError(result.message)
        }

    } catch (error) {
        console.error(error);
        await sweetError(error.message);
    }
}

/**
 * 취소 버튼 핸들러
 */
function handleCancel() {
    const result = sweetConfirm(
        '취소 확인',
        '작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    window.location.href = '/view/resource/admin/cars';
}

/**
 * 상태 목록 로드 (초기값 없음)
 */
async function loadStatusOptions() {
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
            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        await sweetError('상태 목록을 불러오지 못했습니다.');
    }
}

