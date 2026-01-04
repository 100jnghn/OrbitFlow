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

    // 이미지 파일이 있으면 추가 (선택사항)
    if (selectedImageFile) {
        formData.append('imgFile', selectedImageFile);
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

