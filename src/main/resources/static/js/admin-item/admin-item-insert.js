/**
 * 관리자 - 비품 추가 페이지
 */

// 선택된 이미지 파일
let selectedImageFile = null;

// DOM 요소
let itemName, itemCategory, itemStatus, itemDescription;
let itemNameMsg, itemCategoryMsg, itemStatusMsg, itemDescriptionMsg;
let saveBtn;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // DOM 요소 초기화
    itemName = document.getElementById('item-name');
    itemCategory = document.getElementById('item-category');
    itemStatus = document.getElementById('item-status');
    itemDescription = document.getElementById('item-description');

    itemNameMsg = document.getElementById('item-name-msg');
    itemCategoryMsg = document.getElementById('item-category-msg');
    itemStatusMsg = document.getElementById('item-status-msg');
    itemDescriptionMsg = document.getElementById('item-description-msg');

    saveBtn = document.getElementById('btn-save');

    // 상태 목록과 카테고리 목록 로드
    loadStatusOptions();
    loadCategoryOptions();
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
        validateItemName() &&
        validateItemCategory() &&
        validateItemStatus()
    );
}

/* ======================
   비품명 검증 (최대 50자, not null)
====================== */
function validateItemName() {
    const v = itemName.value.trim();
    if (!v) {
        showMsg(itemNameMsg, '비품명을 입력해주세요. (0/50)', 'error');
        return false;
    }
    showMsg(itemNameMsg, `입력됨 (${v.length}/50)`, 'success');
    return true;
}

/* ======================
   카테고리 검증 (필수 선택)
====================== */
function validateItemCategory() {
    const v = itemCategory.value;
    if (!v) {
        showMsg(itemCategoryMsg, '카테고리를 선택해주세요.', 'error');
        return false;
    }
    showMsg(itemCategoryMsg, '선택됨', 'success');
    return true;
}

/* ======================
   상태 검증 (필수 선택)
====================== */
function validateItemStatus() {
    const v = itemStatus.value;
    if (!v) {
        showMsg(itemStatusMsg, '상태를 선택해주세요.', 'error');
        return false;
    }
    showMsg(itemStatusMsg, '선택됨', 'success');
    return true;
}

/* ======================
   비고 검증 (최대 50자, nullable)
====================== */
function validateItemDescription() {
    const v = itemDescription.value.trim();
    if (v) {
        showMsg(itemDescriptionMsg, `입력됨 (${v.length}/50)`, 'success');
    } else {
        itemDescriptionMsg.textContent = '';
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
    itemName.addEventListener('input', () => {
        validateItemName();
        updateSaveButtonState();
    });

    itemCategory.addEventListener('change', () => {
        validateItemCategory();
        updateSaveButtonState();
    });

    itemStatus.addEventListener('change', () => {
        validateItemStatus();
        updateSaveButtonState();
    });

    itemDescription.addEventListener('input', () => {
        validateItemDescription();
    });

    // 이미지 업로드 관련
    const imageInput = document.getElementById('item-image-input');
    const removeBtn = document.getElementById('btn-remove');
    const previewArea = document.getElementById('item-image-preview');

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
                    message: '가로가 세로보다 긴 이미지만 업로드 가능합니다.' + '\n' + '(현재 비율: ' +
                        width + 'x' + height + ')'
                });
                return;
            }

            // 16:9 초과 체크
            const maxRatio = 1.7; // 약 1.778
            if (width * 9 > height * 16) {
                resolve({
                    valid: false,
                    message: '이미지 비율은 최대 16:9까지 가능합니다. (현재 비율: ' +
                        width + 'x' + height + ')'
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
        alert(validation.message);
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
    const imageInput = document.getElementById('item-image-input');

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
    if (!validateItemName() || !validateItemCategory() || !validateItemStatus()) {
        alert('입력 항목을 확인해주세요.');
        return;
    }

    const name = itemName.value.trim();
    const categoryValue = itemCategory.value;
    const statusValue = itemStatus.value;
    const description = itemDescription.value.trim();

    // FormData 생성 (파일 업로드를 위해)
    const formData = new FormData();

    formData.append('name', name);
    formData.append('itemCategoryId', categoryValue);
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
            '/api/admin/items',
            {
                method: 'POST',
                body: formData
            }
        );

        if (!response.ok) {
            throw new Error('비품 등록 실패');
        }

        alert('비품이 등록되었습니다.');
        // 관리자 비품 목록 화면으로 이동
        window.location.href = '/view/resource/admin/items';

    } catch (error) {
        console.error(error);
        alert('비품 등록에 실패했습니다.');
    }
}

/**
 * 취소 버튼 핸들러
 */
function handleCancel() {
    if (confirm('작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?')) {
        window.location.href = '/view/resource/admin/items';
    }
}

/**
 * 상태 목록 로드
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

        const select = document.getElementById('item-status');
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

/**
 * 카테고리 목록 로드
 */
async function loadCategoryOptions() {
    try {
        const response = await apiFetch(
            '/api/item-categories',
            { method: 'GET' }
        );

        if (!response.ok) throw new Error();

        const result = await response.json();
        const categories = result.data;

        const select = document.getElementById('item-category');
        select.innerHTML = '<option value="">카테고리 선택</option>';

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        alert('카테고리 목록을 불러오지 못했습니다.');
    }
}

