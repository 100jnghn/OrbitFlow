/* ==========================
   Global State
========================== */
let items = [];
let categories = [];
let selectedCategoryId = null;
let selectedDate = null;
let reservations = [];
let selectedItem = null;
let selectedStartHour = null;
let selectedEndHour = null;
let isSelectingRange = false;

const HOURS = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20];

/* ==========================
   Helper Functions
========================== */

// 날짜 포맷팅 (yyyy-MM-dd)
function formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

// 날짜 포맷팅 (한글: MM월 DD일 (요일))
function formatDateKorean(dateString) {
    const date = new Date(dateString);
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const dayOfWeek = days[date.getDay()];
    return `${month}월 ${day}일 (${dayOfWeek})`;
}

// 시간 포맷팅 (HH:00 ~ HH:00)
function formatTimeRange(hour) {
    return `${String(hour).padStart(2, '0')}:00 ~ ${String(hour + 1).padStart(2, '0')}:00`;
}

/* ==========================
   Date Selector
========================== */
function initDateSelector() {
    const select = document.getElementById('reservation-date-select');
    const today = new Date();

    for (let i = 0; i < 14; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);

        const option = document.createElement('option');
        option.value = formatDate(date);
        option.textContent = formatDateKorean(formatDate(date));

        if (i === 0) {
            option.selected = true;
            selectedDate = option.value;
        }

        select.appendChild(option);
    }
}

/* ==========================
   Load Categories
========================== */
async function loadCategories() {
    try {
        const res = await apiFetch('/api/item-categories', {method: 'GET'});

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        categories = data || [];

        renderCategorySelector();

    } catch (e) {
        console.error(e);
        alert('카테고리 목록을 불러오지 못했습니다.');
    }
}

function renderCategorySelector() {
    const select = document.getElementById('category-select');

    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;

        option.textContent = category.name;
        select.appendChild(option);
    });
}

/* ==========================
   Load Items
========================== */
async function loadItems(categoryId = null) {
    try {
        let url = '/api/items';
        if (categoryId) {
            url = `/api/categories/${categoryId}/items`;
        }

        const res = await apiFetch(url, {method: 'GET'});

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        items = data || [];

        renderGrid();

    } catch (e) {
        console.error(e);
        alert('자원 목록을 불러오지 못했습니다.');
        renderGrid();
    }
}

/* ==========================
   Load Reservations
========================== */
async function loadReservations(date) {
    try {
        const params = new URLSearchParams({
            date: date,
            typeCode: 'ITEM'
        });

        const res = await apiFetch(`/api/reservations/date?${params.toString()}`, {
            method: 'GET'
        });

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        reservations = data || [];

        renderGrid();

    } catch (e) {
        console.error(e);
        alert('예약 정보를 불러오지 못했습니다.');
        renderGrid();
    }
}

/* ==========================
   Render Time Headers
========================== */
function renderTimeHeaders() {
    const container = document.getElementById('times-header');
    container.innerHTML = '';

    HOURS.forEach(hour => {
        const header = document.createElement('div');
        header.className = 'time-header';
        header.textContent = `${String(hour).padStart(2, '0')}:00`;
        container.appendChild(header);
    });
}

/* ==========================
   Render Grid
========================== */
function renderGrid() {
    const container = document.getElementById('grid-body');
    container.innerHTML = '';

    if (items.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>카테고리를 선택하세요</p>
            </div>
        `;
        return;
    }

    items.forEach(item => {
        const row = document.createElement('div');
        row.className = 'grid-row';

        // 자원 이름 셀
        const itemNameCell = document.createElement('div');
        itemNameCell.className = 'item-name-cell';
        itemNameCell.textContent = item.name;
        itemNameCell.title = item.name;
        row.appendChild(itemNameCell);

        // 시간대 셀들
        const timeCells = document.createElement('div');
        timeCells.className = 'time-cells';

        HOURS.forEach(hour => {
            const cell = document.createElement('div');
            cell.className = 'time-cell';
            cell.dataset.itemId = item.itemId;
            cell.dataset.itemName = item.name;
            cell.dataset.itemCategoryId = item.itemCategoryId || '-';
            cell.dataset.itemCategoryName = item.itemCategoryName || '-';
            cell.dataset.itemDescription = item.description || '-';
            cell.dataset.hour = hour;

            // 예약 여부 확인
            const isReserved = checkReservation(item.itemId, hour);

            if (isReserved) {
                if (isReserved.isMine) {
                    cell.className += ' my-reservation';
                    cell.textContent = '예약됨';
                } else {
                    cell.className += ' unavailable';
                    cell.textContent = '예약 불가';
                }
            } else {
                cell.className += ' available';
                cell.addEventListener('click', (e) => handleCellClick(e.currentTarget, item, hour));
            }

            timeCells.appendChild(cell);
        });

        row.appendChild(timeCells);
        container.appendChild(row);
    });
}

/* ==========================
   Check Reservation
========================== */
function checkReservation(itemId, hour) {
    const reservation = reservations.find(r =>
        r.resourceId === itemId &&
        r.startTime <= hour &&
        r.endTime > hour &&
        (r.reservationStatusId === 1 || r.reservationStatusId === 2) // 대기 or 확정
    );

    if (reservation) {
        return {
            isMine: reservation.isMine || false,
            reservation: reservation
        };
    }

    return null;
}

/* ==========================
   Cell Selection Functions
========================== */
function handleCellClick(cell, item, hour) {
    const itemId = parseInt(cell.dataset.itemId);

    // 다른 자원을 클릭하면 초기화
    if (selectedItem && selectedItem.itemId !== itemId) {
        clearSelection();
    }

    selectedItem = item;

    // 첫 번째 클릭 (시작 시간)
    if (!selectedStartHour) {
        selectedStartHour = hour;
        selectedEndHour = hour + 1;
        updateSelection();
        updateReservationForm();
        return;
    }

    // 두 번째 클릭 (종료 시간)
    if (hour < selectedStartHour) {
        // 시작 시간보다 이전을 클릭하면 새로운 시작 시간으로
        selectedStartHour = hour;
        selectedEndHour = hour + 1;
    } else {
        // 종료 시간 설정 (클릭한 시간 +1)
        selectedEndHour = hour + 1;
    }

    // 선택한 범위에 예약 불가능한 시간이 있는지 확인
    if (!isRangeAvailable(itemId, selectedStartHour, selectedEndHour)) {
        alert('선택한 시간 범위에 예약 불가능한 시간이 포함되어 있습니다.');
        clearSelection();
        selectedItem = item;
        selectedStartHour = hour;
        selectedEndHour = hour + 1;
    }

    updateSelection();
    updateReservationForm();
}

function isRangeAvailable(itemId, startHour, endHour) {
    for (let hour = startHour; hour < endHour; hour++) {
        const isReserved = checkReservation(itemId, hour);
        if (isReserved) {
            return false;
        }
    }
    return true;
}

function clearSelection() {
    selectedItem = null;
    selectedStartHour = null;
    selectedEndHour = null;

    // 모든 선택 해제
    document.querySelectorAll('.time-cell.selected').forEach(cell => {
        cell.classList.remove('selected');
    });

    updateReservationForm();
}

function updateSelection() {
    // 모든 선택 해제
    document.querySelectorAll('.time-cell.selected').forEach(cell => {
        cell.classList.remove('selected');
    });

    // 해당 자원의 시간 범위 선택
    if (selectedItem && selectedStartHour !== null && selectedEndHour !== null) {
        document.querySelectorAll('.time-cell').forEach(cell => {
            const cellItemId = parseInt(cell.dataset.itemId);
            const cellHour = parseInt(cell.dataset.hour);

            if (cellItemId === selectedItem.itemId &&
                cellHour >= selectedStartHour &&
                cellHour < selectedEndHour) {
                cell.classList.add('selected');
            }
        });
    }
}

function updateReservationForm() {
    // 신청자 (현재 사용자 정보 - 추후 API에서 가져오기)
    document.getElementById('applicant-name').textContent = '사용자'; // TODO: 실제 사용자 정보

    // 자원 정보
    const itemImage = document.getElementById('selected-item-image');
    if (selectedItem) {
        document.getElementById('selected-item-name').textContent = selectedItem.name;
        document.getElementById('selected-item-category').textContent = selectedItem.itemCategoryName || '-';
        document.getElementById('selected-item-description').textContent = selectedItem.description || '-';

        // 자원 이미지 업데이트
        if (selectedItem.objectKey) {
            itemImage.src = `/api/files/${selectedItem.objectKey}`;
            itemImage.style.display = 'block';
        } else {
            itemImage.src = '';
            itemImage.style.display = 'none';
        }
    } else {
        document.getElementById('selected-item-name').textContent = '-';
        document.getElementById('selected-item-category').textContent = '-';
        document.getElementById('selected-item-description').textContent = '-';

        // 자원 이미지 숨기기
        itemImage.src = '';
        itemImage.style.display = 'none';
    }

    // 날짜
    document.getElementById('selected-date').textContent = selectedDate ? formatDateKorean(selectedDate) : '-';

    // 시작/종료 시간
    if (selectedStartHour !== null) {
        document.getElementById('selected-start-time').textContent =
            `${String(selectedStartHour).padStart(2, '0')}:00`;
    } else {
        document.getElementById('selected-start-time').textContent = '-';
    }

    if (selectedEndHour !== null) {
        document.getElementById('selected-end-time').textContent =
            `${String(selectedEndHour).padStart(2, '0')}:00`;
    } else {
        document.getElementById('selected-end-time').textContent = '-';
    }

    // 신청 버튼 활성화/비활성화
    updateSubmitButtonState();
}

/* ==========================
   Validation Functions
========================== */
function showMsg(el, message, type) {
    el.textContent = message;
    el.className = 'hint ' + type;
}

function validateReservationReason(showEmptyMessage = true) {
    const reasonInput = document.getElementById('reservation-reason');
    const reasonMsg = document.getElementById('reservation-reason-msg');
    const v = reasonInput.value.trim();

    if (!v) {
        if (showEmptyMessage) {
            showMsg(reasonMsg, '신청 사유를 입력해주세요.', 'error');
        } else {
            reasonMsg.textContent = '';
            reasonMsg.className = 'hint';
        }
        return false;
    }

    if (v.length > 200) {
        showMsg(reasonMsg, `최대 200자까지 입력 가능합니다. (${v.length}/200)`, 'error');
        return false;
    }

    showMsg(reasonMsg, `${v.length}/200`, 'success');
    return true;
}

function updateSubmitButtonState() {
    const submitBtn = document.getElementById('btn-submit');
    const isValid = selectedItem &&
        selectedStartHour !== null &&
        selectedEndHour !== null &&
        validateReservationReason(false);
    submitBtn.disabled = !isValid;
}

/* ==========================
   Submit Reservation
========================== */
async function submitReservation() {
    if (!selectedItem || selectedStartHour === null || selectedEndHour === null) {
        alert('자원과 시간을 선택해주세요.');
        return;
    }

    const reason = document.getElementById('reservation-reason').value.trim();

    if (!validateReservationReason()) {
        return;
    }

    try {
        const payload = {
            typeCode: 'ITEM',
            resourceId: selectedItem.itemId,
            itemCategoryId: selectedItem.itemCategoryId,
            reservationDate: selectedDate,
            startTime: selectedStartHour,
            endTime: selectedEndHour,
            reservationReason: reason
        };

        const res = await apiFetch('/api/reservations/me', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '예약에 실패했습니다.');
        }

        alert('예약이 완료되었습니다.');

        // 입력 초기화
        document.getElementById('reservation-reason').value = '';
        const reasonMsg = document.getElementById('reservation-reason-msg');
        if (reasonMsg) {
            reasonMsg.textContent = '';
            reasonMsg.className = 'hint';
        }
        clearSelection();
        loadReservations(selectedDate);

    } catch (e) {
        console.error(e);
        alert(e.message || '예약 중 오류가 발생했습니다.');
    }
}

/* ==========================
   Event Listeners
========================== */
function initEventListeners() {
    // 카테고리 선택 시 자원 목록 필터링
    const categorySelect = document.getElementById('category-select');

    if (categorySelect) {
        categorySelect.addEventListener('change', (e) => {
            selectedCategoryId = e.target.value ? parseInt(e.target.value) : null;
            clearSelection();

            if (selectedCategoryId !== null) {
                loadItems(selectedCategoryId);
            } else {
                clearSelection()
                const container = document.getElementById('grid-body');

                container.innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-inbox"></i>
                        <p>카테고리를 선택하세요</p>
                    </div>
                    `;
            }
        });
    }

    // 날짜 선택 시 자동 조회
    const dateSelect = document.getElementById('reservation-date-select');
    if (dateSelect) {
        dateSelect.addEventListener('change', (e) => {
            selectedDate = e.target.value;
            clearSelection();
            loadReservations(selectedDate);
        });
    }

    // 신청 버튼
    const btnSubmit = document.getElementById('btn-submit');
    if (btnSubmit) {
        btnSubmit.addEventListener('click', submitReservation);
        btnSubmit.disabled = true;
    }

    // 신청 사유 입력 필드
    const reasonInput = document.getElementById('reservation-reason');
    if (reasonInput) {
        reasonInput.addEventListener('input', () => {
            validateReservationReason();
            updateSubmitButtonState();
        });

        reasonInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !btnSubmit.disabled) {
                submitReservation();
            }
        });
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', async () => {
    initDateSelector();
    initEventListeners();
    updateReservationForm();

    // 시간 헤더 먼저 표시
    renderTimeHeaders();

    await loadCategories();
    await loadReservations(selectedDate);
});

