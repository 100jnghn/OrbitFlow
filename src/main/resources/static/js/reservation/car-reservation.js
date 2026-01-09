/* ==========================
   Global State
========================== */
let cars = [];
let reservations = [];
let selectedCar = null;
let selectedStartDate = null;
let selectedEndDate = null;
let dates = []; // 오늘부터 14일 후까지의 날짜 배열
let currentUserName = null;


/* ==========================
   Generate Dates
========================== */
function generateDates() {
    dates = [];
    const today = new Date();

    for (let i = 0; i < 14; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);
        dates.push({
            dateString: formatDate(date),
            displayText: formatDateKorean(formatDate(date))
        });
    }
}

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
    return `${month}/${day} (${dayOfWeek})`;
}

// 시간 포맷팅 (HH:00 ~ HH:00)
function formatTimeRange(hour) {
    return `${String(hour).padStart(2, '0')}:00 ~ ${String(hour + 1).padStart(2, '0')}:00`;
}

/* ==========================
   Load Cars
========================== */
async function loadCars() {
    try {
        const res = await apiFetch('/api/cars', { method: 'GET' });

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        cars = data || [];

        renderDateHeaders();

    } catch (e) {
        console.error(e);
        alert('차량 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Load Reservations
========================== */
async function loadReservations() {
    try {
        const allReservations = [];

        for (const dateObj of dates) {
            const params = new URLSearchParams({
                date: dateObj.dateString,
                typeCode: 'CAR'
            });

            const res = await apiFetch(
                `/api/reservations/date?${params.toString()}`,
                { method: 'GET' }
            );

            if (res.ok) {
                const { data } = await res.json();
                if (data) {
                    allReservations.push(...data);
                }
            }
        }

        reservations = allReservations;
        renderGrid();

    } catch (e) {
        console.error(e);
        alert('예약 정보를 불러오지 못했습니다.');
        renderGrid();
    }
}


/* ==========================
   Render Date Headers
========================== */
function renderDateHeaders() {
    const container = document.getElementById('dates-header');
    container.innerHTML = '';

    dates.forEach(dateObj => {
        const header = document.createElement('div');
        header.className = 'date-header';
        header.textContent = dateObj.displayText;
        header.title = dateObj.dateString;
        container.appendChild(header);
    });
}

/* ==========================
   Render Grid
========================== */
function renderGrid() {
    const container = document.getElementById('grid-body');
    container.innerHTML = '';

    if (cars.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>차량 정보를 불러오는 중입니다...</p>
            </div>
        `;
        return;
    }

    cars.forEach(car => {
        const row = document.createElement('div');
        row.className = 'grid-row';

        // 차량 이름 셀
        const carNameCell = document.createElement('div');
        carNameCell.className = 'car-name-cell';
        carNameCell.textContent = car.name;
        carNameCell.title = car.name;
        carNameCell.addEventListener('click', () => openCarImageModal(car));
        row.appendChild(carNameCell);

        // 날짜 셀들
        const dateCells = document.createElement('div');
        dateCells.className = 'date-cells';

        dates.forEach(dateObj => {
            const cell = document.createElement('div');
            cell.className = 'date-cell';
            cell.dataset.carId = car.carId;
            cell.dataset.carName = car.name;
            cell.dataset.carNumber = car.number || '-';
            cell.dataset.carDriverAge = car.driverAge || '-';
            cell.dataset.carDescription = car.description || '-';
            cell.dataset.date = dateObj.dateString;

            // 예약 여부 확인
            const isReserved = checkReservation(car.carId, dateObj.dateString);

            if (isReserved) {
                if (isReserved.isMine) {
                    cell.className += ' my-reservation';
                    // cell.textContent = '예약됨';
                } else {
                    cell.className += ' unavailable';
                    // cell.textContent = '예약 불가';
                }
            } else {
                cell.className += ' available';
                cell.addEventListener('click', (e) => handleCellClick(e.currentTarget, car, dateObj.dateString));
            }

            dateCells.appendChild(cell);
        });

        row.appendChild(dateCells);
        container.appendChild(row);
    });
}

/* ==========================
   Check Reservation
========================== */
function checkReservation(carId, dateString) {
    const reservation = reservations.find(r => {
        if (r.resourceId !== carId) return false;
        if (r.reservationStatusId !== 1 && r.reservationStatusId !== 2) return false; // 대기 or 확정만

        // 예약 시작 날짜
        const startDate = r.reservationDate;
        // 예약 종료 날짜 (없으면 시작 날짜와 동일)
        const endDate = r.endDate || r.reservationDate;

        // 날짜 범위 내에 있는지 확인
        return dateString >= startDate && dateString <= endDate;
    });

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
async function handleCellClick(cell, car, dateString) {
    const carId = parseInt(cell.dataset.carId);

    // 다른 차량을 클릭하면 초기화
    if (selectedCar && selectedCar.carId !== carId) {
        clearSelection();
    }

    selectedCar = car;

    // 날짜 인덱스 찾기
    const clickedDateIndex = dates.findIndex(d => d.dateString === dateString);
    if (clickedDateIndex === -1) return;

    // 첫 번째 클릭 (시작 날짜)
    if (selectedStartDate === null) {
        selectedStartDate = dateString;
        selectedEndDate = dateString;
        updateSelection();
        await updateReservationForm();
        return;
    }

    // 두 번째 클릭 (종료 날짜)
    const startDateIndex = dates.findIndex(d => d.dateString === selectedStartDate);
    if (clickedDateIndex < startDateIndex) {
        // 시작 날짜보다 이전을 클릭하면 새로운 시작 날짜로
        selectedStartDate = dateString;
        selectedEndDate = dateString;
    } else {
        // 종료 날짜 설정
        selectedEndDate = dateString;
    }

    // 선택한 범위에 예약 불가능한 날짜가 있는지 확인
    if (!isRangeAvailable(carId, selectedStartDate, selectedEndDate)) {
        alert('선택한 날짜 범위에 예약 불가능한 날짜가 포함되어 있습니다.');
        clearSelection();
        selectedCar = car;
        selectedStartDate = dateString;
        selectedEndDate = dateString;
    }

    updateSelection();
    await updateReservationForm();
}

function isRangeAvailable(carId, startDate, endDate) {
    const startIndex = dates.findIndex(d => d.dateString === startDate);
    const endIndex = dates.findIndex(d => d.dateString === endDate);

    if (startIndex === -1 || endIndex === -1) return false;

    for (let i = startIndex; i <= endIndex; i++) {
        const dateObj = dates[i];
        const isReserved = checkReservation(carId, dateObj.dateString);
        if (isReserved) {
            return false;
        }
    }
    return true;
}

async function clearSelection() {
    selectedCar = null;
    selectedStartDate = null;
    selectedEndDate = null;

    // 모든 선택 해제
    document.querySelectorAll('.date-cell.selected').forEach(cell => {
        cell.classList.remove('selected');
    });

    await updateReservationForm();
}

function updateSelection() {
    // 모든 선택 해제
    document.querySelectorAll('.date-cell.selected').forEach(cell => {
        cell.classList.remove('selected');
    });

    // 해당 차량의 날짜 범위 선택
    if (selectedCar && selectedStartDate !== null && selectedEndDate !== null) {
        const startIndex = dates.findIndex(d => d.dateString === selectedStartDate);
        const endIndex = dates.findIndex(d => d.dateString === selectedEndDate);

        if (startIndex !== -1 && endIndex !== -1) {
            const minIndex = Math.min(startIndex, endIndex);
            const maxIndex = Math.max(startIndex, endIndex);

            document.querySelectorAll('.date-cell').forEach(cell => {
                const cellCarId = parseInt(cell.dataset.carId);
                const cellDate = cell.dataset.date;

                if (cellCarId === selectedCar.carId) {
                    const cellDateIndex = dates.findIndex(d => d.dateString === cellDate);
                    if (cellDateIndex >= minIndex && cellDateIndex <= maxIndex) {
                        cell.classList.add('selected');
                    }
                }
            });
        }
    }
}

async function updateReservationForm() {

    // 신청자 이름
    const applicantNameEl = document.getElementById('applicant-name');

    if (applicantNameEl) {
        const name = await loadCurrentUserName();
        applicantNameEl.textContent = name || '사용자';
    }
    // 차량 정보
    if (selectedCar) {
        document.getElementById('selected-car-name').textContent = selectedCar.name;
        document.getElementById('selected-car-number').textContent = selectedCar.number || '-';
        document.getElementById('selected-car-driver-age').textContent = selectedCar.driverAge ? `${selectedCar.driverAge}세 이상` : '-';
        document.getElementById('selected-car-description').textContent = selectedCar.description || '-';
    } else {
        document.getElementById('selected-car-name').textContent = '-';
        document.getElementById('selected-car-number').textContent = '-';
        document.getElementById('selected-car-driver-age').textContent = '-';
        document.getElementById('selected-car-description').textContent = '-';
    }


    // 시작 날짜
    document.getElementById('selected-start-date').textContent = selectedStartDate ? formatDateKorean(selectedStartDate) : '-';

    // 종료 날짜
    document.getElementById('selected-end-date').textContent = selectedEndDate ? formatDateKorean(selectedEndDate) : '-';

    // 신청 버튼 활성화/비활성화
    updateSubmitButtonState();
}

async function loadCurrentUserName() {
    if (currentUserName) return currentUserName;

    try {
        const response = await apiFetch('/api/employees/me', {
            method: 'GET'
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return null;
            }
            throw new Error('사용자 정보를 불러오지 못했습니다.');
        }

        const result = await response.json();
        currentUserName = result.data?.name || '사용자';

        return currentUserName;

    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        return '사용자';
    }
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
    const isValid = selectedCar &&
        selectedStartDate !== null &&
        selectedEndDate !== null &&
        validateReservationReason(false);
    submitBtn.disabled = !isValid;
}

/* ==========================
   Submit Reservation
========================== */
async function submitReservation() {
    if (!selectedCar || !selectedStartDate || !selectedEndDate) {
        alert('차량과 날짜 범위를 선택해주세요.');
        return;
    }

    const reason = document.getElementById('reservation-reason').value.trim();

    if (!validateReservationReason()) {
        return;
    }

    try {
        const payload = {
            typeCode: 'CAR',
            resourceId: selectedCar.carId,
            reservationDate: selectedStartDate,
            endDate: selectedEndDate,
            startTime: null,
            endTime: null,

            // startTime: 0, // 차량은 시간 X
            // endTime: 24,  // 차량은 시간 X
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
        loadReservations();

    } catch (e) {
        console.error(e);
        alert(e.message || '예약 중 오류가 발생했습니다.');
    }
}

/* ==========================
   Car Image Modal Functions
========================== */
async function openCarImageModal(car) {
    const modal = document.getElementById('car-image-modal');
    const modalCarName = document.getElementById('modal-car-name');
    const modalCarImage = document.getElementById('modal-car-image');

    // 차량 이름 설정
    modalCarName.textContent = car.name;

    // 차량 이미지 로드
    if (car.fileId) {
        try {
            // presigned URL 요청
            const res = await apiFetch(`/api/files/${car.fileId}/presigned`);
            if (!res.ok) throw new Error('presigned url 요청 실패');

            const result = await res.json();
            const imageUrl = result.data.url;

            modalCarImage.src = imageUrl;
            modalCarImage.style.display = 'block';
        } catch (e) {
            console.error('이미지 로드 실패', e);
            modalCarImage.src = '';
            modalCarImage.alt = '이미지를 불러올 수 없습니다.';
        }
    } else {
        modalCarImage.src = '';
        modalCarImage.alt = '등록된 이미지가 없습니다.';
    }

    // 모달 표시
    modal.classList.add('show');
    document.body.style.overflow = 'hidden'; // 배경 스크롤 방지
}

function closeCarImageModal() {
    const modal = document.getElementById('car-image-modal');
    modal.classList.remove('show');
    document.body.style.overflow = ''; // 배경 스크롤 복원
}

/* ==========================
   Event Listeners
========================== */
function initEventListeners() {
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

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeCarImageModal();
        }
    });
}

function updateApprovalSidebarSelection() {
    // 모든 no-sub 메뉴 선택 해제
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });

    // 결재 대기함 선택
    const inboxLink = document.getElementById('carLink');
    if (inboxLink) {
        const menuItem = inboxLink.closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', async () => {
    generateDates();
    initEventListeners();
    await updateReservationForm();
    updateApprovalSidebarSelection();

    await loadCars();
    await loadReservations();
});

