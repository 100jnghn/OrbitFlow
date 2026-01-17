import { showFullscreenSpinner, hideFullscreenSpinner } from "/js/ui/fullscreenSpinner.js";

/**
 * 관리자 - 차량 상세 조회 페이지
 */

// 현재 차량 ID
let currentCarId = null;

// 선택된 이미지 파일 (새로 업로드하는 경우)
let selectedImageFile = null;

// DOM 요소
let carModel, carAge, carStatus, carDescription;
let carModelMsg, carAgeMsg, carStatusMsg, carDescriptionMsg;
let editBtn;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // DOM 요소 초기화
    carModel = document.getElementById('car-model');
    carAge = document.getElementById('car-age');
    carStatus = document.getElementById('car-status');
    carDescription = document.getElementById('car-description');

    carModelMsg = document.getElementById('car-model-msg');
    carAgeMsg = document.getElementById('car-age-msg');
    carStatusMsg = document.getElementById('car-status-msg');
    carDescriptionMsg = document.getElementById('car-description-msg');

    editBtn = document.getElementById('btn-edit');

    currentCarId = getCarId();

    if (!currentCarId) {
        sweetError('차량 정보를 찾을 수 없습니다.');
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
        validateCarModel() &&
        validateCarAge() &&
        validateCarStatus()
    );
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
    if (editBtn) {
        editBtn.addEventListener('click', handleEdit);
    }

    const deleteBtn = document.getElementById('btn-delete');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', handleDelete);
    }

    // 실시간 검증
    carModel.addEventListener('input', () => {
        validateCarModel();
        updateEditButtonState();
    });

    carAge.addEventListener('input', () => {
        validateCarAge();
        updateEditButtonState();
    });

    carStatus.addEventListener('change', () => {
        validateCarStatus();
        updateEditButtonState();
    });

    carDescription.addEventListener('input', () => {
        validateCarDescription();
    });

    // 이미지 업로드 관련
    const imageInput = document.getElementById('car-image-input');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
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

    // 이미지 제거 버튼 (선택된 이미지)
    if (removeBtn) {
        removeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            handleImageRemove();
        });
    }

    // 기존 이미지 제거 버튼 (서버 이미지)
    if (removeImgBtn) {
        removeImgBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            handleDeleteImage();
        });
    }
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
/**
 * 이미지 선택 핸들러
 */
async function handleImageSelect(event) {
    const file = event.target.files[0];

    if (!file) return;

    // 업로드/전송 관련 버튼 비활성화, 스피너 표시
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const carImagePreview = document.getElementById('car-image-preview');

    if (editBtn) editBtn.disabled = true;
    if (removeBtn) removeBtn.style.display = 'none';
    if (removeImgBtn) removeImgBtn.style.display = 'none';

    // 기존 미리보기 숨김 & Placeholder 숨김
    if (carImage) carImage.style.display = 'none';
    if (placeholder) placeholder.style.display = 'none';

    // 스피너 추가
    let spinner = carImagePreview.querySelector('.image-spinner');
    if (!spinner) {
        spinner = document.createElement('div');
        spinner.className = 'image-spinner';
        carImagePreview.appendChild(spinner);
    }

    // 이미지 파일 검증
    const validation = await validateImageFile(file);

    if (!validation.valid) {
        spinner.remove();
        if (placeholder) placeholder.style.display = 'flex';
        // 버튼 상태 복구
        updateEditButtonState();

        await sweetWarning(validation.message);
        // 파일 입력 초기화
        event.target.value = '';
        return;
    }

    selectedImageFile = file;

    // 미리보기 표시
    const reader = new FileReader();
    reader.onload = (e) => {
        const imageUrl = e.target.result;
        displayImagePreview(imageUrl); // 내부에서 스피너 제거 처리
    };
    reader.readAsDataURL(file);
}

/**
 * 이미지 미리보기 표시
 */
/**
 * 이미지 미리보기 표시
 */
function displayImagePreview(imageUrl) {
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const carImagePreview = document.getElementById('car-image-preview');

    // 스피너 제거
    const spinner = carImagePreview.querySelector('.image-spinner');
    if (spinner) spinner.remove();

    if (carImage) {
        carImage.src = imageUrl;

        // 이미지 로드 완료 시 표시 (데이터 URL이라 거의 즉시겠지만)
        carImage.onload = () => {
            carImage.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';

            // 버튼 복구
            updateEditButtonState();
        };
        // 만약 onload가 안 탈 경우를 대비해 바로 표시 (DataURL 특성상 바로 가능)
        if (imageUrl.startsWith('data:')) {
            carImage.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';
            updateEditButtonState();
        }

        // 새로 선택한 이미지는 btn-remove만 표시
        if (removeBtn) {
            removeBtn.style.display = 'inline-flex';
        }
        if (removeImgBtn) {
            removeImgBtn.style.display = 'none';
        }
    }
}

/**
 * 이미지 제거 핸들러 (선택된 이미지만 제거)
 */
function handleImageRemove() {
    selectedImageFile = null;

    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
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

    if (removeImgBtn) {
        removeImgBtn.style.display = 'none';
    }

    if (imageInput) {
        imageInput.value = '';
    }
}

/**
 * 기존 이미지 삭제 핸들러 (서버에서 완전 삭제)
 */
async function handleDeleteImage() {
    const result = await sweetConfirm(
        '삭제 확인',
        '기존 이미지를 삭제하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    try {
        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}/file/delete`,
            { method: 'GET' }
        );

        if (!response.ok) {
            throw new Error('이미지 삭제 실패');
        }

        const result = await response.json();
        await sweetSuccess(result.message);

        // 이미지 뷰 초기화
        displayCarImage(null);

    } catch (error) {
        console.error(error);
        await sweetError('이미지 삭제에 실패했습니다.');
    }
}

/**
 * 수정 버튼
 */
async function handleEdit() {
    // validation 검증
    if (!validateCarModel() || !validateCarAge() || !validateCarStatus()) {
        await sweetInfo('입력 항목을 확인해주세요.');
        return;
    }

    const formData = new FormData();
    // 차량 번호는 이제 읽기 전용이므로 textContent로 가져옴
    formData.append('number', document.getElementById('car-number').textContent);
    formData.append('name', carModel.value.trim());
    formData.append('driverAge', carAge.value.trim());
    formData.append('description', carDescription.value.trim());
    formData.append('statusId', carStatus.value);

    // 이미지 파일이 새로 선택된 경우 추가
    if (selectedImageFile) {
        formData.append('imgFile', selectedImageFile);
    }

    try {
        // 스피너 표시 및 버튼 비활성화
        showFullscreenSpinner("차량을 수정 중입니다...");
        editBtn.disabled = true;

        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}`,
            {
                method: 'PUT',
                body: formData
            }
        );

        console.log(response);

        if (response.ok) {
            await sweetSuccess('차량이 수정되었습니다.');

            // 관리자 차량 목록 화면으로 이동
            window.location.href = '/view/resource/admin/cars';

        } else {
            const result = await response.json();
            await sweetError(result.message)
        }

    } catch (error) {
        console.error(error);
        await sweetWarning(error.message);
        // 실패 시 버튼 상태 복구
        updateEditButtonState();
    } finally {
        hideFullscreenSpinner();
    }
}


/**
 * 삭제 버튼
 */
async function handleDelete() {
    const result = await sweetConfirm(
        '삭제 확인',
        '차량을 삭제하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    try {
        const response = await apiFetch(
            `/api/admin/cars/${currentCarId}/delete`,
            { method: 'DELETE' }
        );

        if (!response.ok) {
            throw new Error();
        }

        window.location.href = '/view/resource/admin/cars';

    } catch (error) {
        console.error(error);
        await sweetError('차량 삭제에 실패했습니다.');
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
        await sweetError('상태 목록을 불러오지 못했습니다.');
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

        // 차량 번호는 이제 text로 표시
        document.getElementById('car-number').textContent = data.number ?? '-';
        carModel.value = data.name ?? '';
        carAge.value = data.driverAge ?? '';
        carDescription.value = data.description ?? '';

        // 등록자 정보 표시
        document.getElementById('uploader-name').textContent = data.uploaderName ?? '-';
        document.getElementById('created-at').textContent = formatDate(data.createdAt);

        // 차량 이미지 표시
        await displayCarImage(data.fileId);

        await loadStatusOptions(data.statusId);

        // 초기 validation
        validateCarModel();
        validateCarAge();
        validateCarStatus();
        validateCarDescription();
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
 * 차량 이미지 표시 (기존 이미지 로드 시)
 */
async function displayCarImage(fileId) {
    const carImage = document.getElementById('car-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const carImagePreview = document.getElementById('car-image-preview');

    if (!fileId) {
        // 이미지 없는 경우
        carImage.src = '';
        carImage.style.display = 'none';

        if (placeholder) placeholder.style.display = 'flex';
        if (removeBtn) removeBtn.style.display = 'none';
        if (removeImgBtn) removeImgBtn.style.display = 'none';

        // 스피너 제거 (혹시 있다면)
        const spinner = carImagePreview.querySelector('.image-spinner');
        if (spinner) spinner.remove();

        return;
    }

    // 스피너 추가 & 버튼 비활성화
    let spinner = carImagePreview.querySelector('.image-spinner');
    if (!spinner) {
        spinner = document.createElement('div');
        spinner.className = 'image-spinner';
        carImagePreview.appendChild(spinner);
    }
    if (placeholder) placeholder.style.display = 'none';
    if (carImage) carImage.style.display = 'none';
    if (editBtn) editBtn.disabled = true;

    try {
        // presigned URL 요청
        const res = await apiFetch(`/api/files/${fileId}/presigned`);
        if (!res.ok) throw new Error('presigned url 요청 실패');

        const result = await res.json();
        const imageUrl = result.data.url;

        carImage.src = imageUrl;

        carImage.onload = () => {
            // 로드 완료 시 스피너 제거 및 이미지 노출
            if (spinner) spinner.remove();
            carImage.style.display = 'block';
            updateEditButtonState();
        };

        carImage.onerror = () => {
            if (spinner) spinner.remove();
            carImage.style.display = 'none';
            if (placeholder) placeholder.style.display = 'flex';
            updateEditButtonState();
        };

        // 기존 서버 이미지는 btn-remove-img만 표시
        if (removeBtn) removeBtn.style.display = 'none';
        if (removeImgBtn) removeImgBtn.style.display = 'inline-flex';

    } catch (e) {
        console.error('이미지 로드 실패', e);

        // 실패 시 placeholder 표시
        if (spinner) spinner.remove();
        carImage.src = '';
        carImage.style.display = 'none';

        if (placeholder) placeholder.style.display = 'flex';
        if (removeBtn) removeBtn.style.display = 'none';
        if (removeImgBtn) removeImgBtn.style.display = 'none';
        updateEditButtonState();
    }
}

/**
 * 에러 처리
 */
function showError() {
    document.getElementById('car-number').textContent = '-';
    document.getElementById('car-model').value = '';
    document.getElementById('car-age').value = '';
    document.getElementById('car-description').value = '차량 정보를 불러올 수 없습니다.';
    document.getElementById('uploader-name').textContent = '-';
    document.getElementById('created-at').textContent = '-';

    // 이미지도 초기화
    displayCarImage(null);
}


