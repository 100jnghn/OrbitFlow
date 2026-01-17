import { showFullscreenSpinner, hideFullscreenSpinner } from "/js/ui/fullscreenSpinner.js";

/**
 * 관리자 - 비품 상세 조회 페이지
 */

// 현재 비품 ID
let currentItemId = null;

// 선택된 이미지 파일 (새로 업로드하는 경우)
let selectedImageFile = null;

// DOM 요소
let itemName, itemCategory, itemStatus, itemDescription;
let itemNameMsg, itemCategoryMsg, itemStatusMsg, itemDescriptionMsg;
let editBtn;

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

    editBtn = document.getElementById('btn-edit');

    currentItemId = getItemId();

    if (!currentItemId) {
        sweetError('비품 정보를 찾을 수 없습니다.');
        history.back();
        return;
    }

    loadItemDetail();
    initEventListeners();
});

/**
 * 쿼리스트링에서 비품 ID 추출
 * 예: /view/resource/admin/items/detail?id=3
 */
function getItemId() {
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
    if (editBtn) {
        editBtn.addEventListener('click', handleEdit);
    }

    const deleteBtn = document.getElementById('btn-delete');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', handleDelete);
    }

    // 실시간 검증
    itemName.addEventListener('input', () => {
        validateItemName();
        updateEditButtonState();
    });

    itemCategory.addEventListener('change', () => {
        validateItemCategory();
        updateEditButtonState();
    });

    itemStatus.addEventListener('change', () => {
        validateItemStatus();
        updateEditButtonState();
    });

    itemDescription.addEventListener('input', () => {
        validateItemDescription();
    });

    // 이미지 업로드 관련
    const imageInput = document.getElementById('item-image-input');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
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
    const itemImage = document.getElementById('item-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const itemImagePreview = document.getElementById('item-image-preview');

    if (editBtn) editBtn.disabled = true;
    if (removeBtn) removeBtn.style.display = 'none';
    if (removeImgBtn) removeImgBtn.style.display = 'none';

    // 기존 미리보기 숨김 & Placeholder 숨김
    if (itemImage) itemImage.style.display = 'none';
    if (placeholder) placeholder.style.display = 'none';

    // 스피너 추가
    let spinner = itemImagePreview.querySelector('.image-spinner');
    if (!spinner) {
        spinner = document.createElement('div');
        spinner.className = 'image-spinner';
        itemImagePreview.appendChild(spinner);
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
    const itemImage = document.getElementById('item-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const itemImagePreview = document.getElementById('item-image-preview');

    // 스피너 제거
    const spinner = itemImagePreview.querySelector('.image-spinner');
    if (spinner) spinner.remove();

    if (itemImage && placeholder) {
        itemImage.src = imageUrl;

        // 이미지 로드 완료 시 표시
        itemImage.onload = () => {
            itemImage.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';

            // 버튼 복구
            updateEditButtonState();
        };

        if (imageUrl.startsWith('data:')) {
            itemImage.style.display = 'block';
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

    const itemImage = document.getElementById('item-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const imageInput = document.getElementById('item-image-input');

    if (itemImage) {
        itemImage.src = '';
        itemImage.style.display = 'none';
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
            `/api/admin/items/${currentItemId}/file/delete`,
            { method: 'GET' }
        );

        if (!response.ok) {
            throw new Error('이미지 삭제 실패');
        }

        const result = await response.json();
        await sweetSuccess(result.message);

        // 이미지 뷰 초기화
        displayItemImage(null);

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
    if (!validateItemName() || !validateItemCategory() || !validateItemStatus()) {
        await sweetInfo('입력 항목을 확인해주세요.');
        return;
    }

    const formData = new FormData();
    formData.append('name', itemName.value.trim());
    formData.append('itemCategoryId', itemCategory.value);
    formData.append('description', itemDescription.value.trim());
    formData.append('statusId', itemStatus.value);

    // 이미지 파일이 새로 선택된 경우 추가
    if (selectedImageFile) {
        formData.append('imgFile', selectedImageFile);
    }

    try {
        // 스피너 표시 및 버튼 비활성화
        showFullscreenSpinner("비품을 수정 중입니다...");
        editBtn.disabled = true;

        const response = await apiFetch(
            `/api/admin/items/${currentItemId}`,
            {
                method: 'PUT',
                body: formData
            }
        );

        if (!response.ok) {
            throw new Error('비품 수정 실패');
        }

        // ✅ 관리자 비품 목록 화면으로 이동
        window.location.href = '/view/resource/admin/items';

    } catch (error) {
        console.error(error);
        await sweetError('비품 수정에 실패했습니다.');
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
        '자원을 삭제하시겠습니까?'
    );

    if (!result.isConfirmed) return;

    const deleteBtn = document.getElementById('btn-delete');

    try {
        // 스피너 표시 및 버튼 비활성화
        showFullscreenSpinner("비품을 삭제 중입니다...");
        if (deleteBtn) deleteBtn.disabled = true;

        const response = await apiFetch(
            `/api/admin/items/${currentItemId}/delete`,
            { method: 'DELETE' }
        );

        if (!response.ok) {
            throw new Error();
        }

        window.location.href = '/view/resource/admin/items';

    } catch (error) {
        console.error(error);
        await sweetError('비품 삭제에 실패했습니다.');
        // 실패 시 버튼 다시 활성화
        if (deleteBtn) deleteBtn.disabled = false;
    } finally {
        // 스피너 숨김
        hideFullscreenSpinner();
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

        const select = document.getElementById('item-status');
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

async function loadCategoryOptions(selectedCategoryId) {
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

            if (category.id === selectedCategoryId) {
                option.selected = true;
            }

            select.appendChild(option);
        });

    } catch (e) {
        console.error(e);
        await sweetError('카테고리 목록을 불러오지 못했습니다.');
    }
}

async function loadItemDetail() {
    console.log(currentItemId)

    try {
        const response = await apiFetch(
            `/api/items/${currentItemId}`,
            { method: 'GET' }
        );


        if (!response.ok) throw new Error();

        const result = await response.json();
        const data = result.data;

        itemName.value = data.name ?? '';
        itemDescription.value = data.description ?? '';

        // 등록자 정보 표시
        document.getElementById('uploader-name').textContent = data.uploaderName ?? '-';
        document.getElementById('created-at').textContent = formatDate(data.createdAt);

        // 비품 이미지 표시
        console.log("파일 아이디: " + data.fileId);
        displayItemImage(data.fileId);

        await loadStatusOptions(data.statusId);
        await loadCategoryOptions(data.itemCategoryId);

        // 초기 validation
        validateItemName();
        validateItemCategory();
        validateItemStatus();
        validateItemDescription();
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
 * 비품 이미지 표시 (기존 이미지 로드 시)
 */
async function displayItemImage(fileId) {
    const itemImage = document.getElementById('item-image');
    const placeholder = document.getElementById('upload-placeholder');
    const removeBtn = document.getElementById('btn-remove');
    const removeImgBtn = document.getElementById('btn-remove-img');
    const itemImagePreview = document.getElementById('item-image-preview');

    if (!fileId) {
        // 이미지 없는 경우
        itemImage.src = '';
        itemImage.style.display = 'none';

        if (placeholder) placeholder.style.display = 'flex';
        if (removeBtn) removeBtn.style.display = 'none';
        if (removeImgBtn) removeImgBtn.style.display = 'none';

        // 스피너 제거
        const spinner = itemImagePreview.querySelector('.image-spinner');
        if (spinner) spinner.remove();

        return;
    }

    // 스피너 추가 & 버튼 비활성화
    let spinner = itemImagePreview.querySelector('.image-spinner');
    if (!spinner) {
        spinner = document.createElement('div');
        spinner.className = 'image-spinner';
        itemImagePreview.appendChild(spinner);
    }
    if (placeholder) placeholder.style.display = 'none';
    if (itemImage) itemImage.style.display = 'none';
    if (editBtn) editBtn.disabled = true;

    try {
        // presigned URL 요청
        const res = await apiFetch(`/api/files/${fileId}/presigned`);
        if (!res.ok) throw new Error('presigned url 요청 실패');

        const result = await res.json();
        const imageUrl = result.data.url;

        itemImage.src = imageUrl;

        itemImage.onload = () => {
            if (spinner) spinner.remove();
            itemImage.style.display = 'block';
            updateEditButtonState();
        };

        itemImage.onerror = () => {
            if (spinner) spinner.remove();
            itemImage.style.display = 'none';
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
        itemImage.src = '';
        itemImage.style.display = 'none';

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
    document.getElementById('item-name').value = '';
    document.getElementById('item-description').value = '비품 정보를 불러올 수 없습니다.';
    document.getElementById('uploader-name').textContent = '-';
    document.getElementById('created-at').textContent = '-';

    // 이미지도 초기화
    displayItemImage(null);
}



