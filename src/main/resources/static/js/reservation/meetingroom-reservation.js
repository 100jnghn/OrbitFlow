/* ==========================
   Global State
========================== */
let meetingRooms = [];
let selectedDate = null;
let reservations = [];
let selectedRoom = null;
let selectedStartHour = null;
let selectedEndHour = null;
let isSelectingRange = false;

const HOURS = Array.from({ length: 24 }, (_, i) => i); // 0~23

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
   Load Meeting Rooms
========================== */
async function loadMeetingRooms() {
    try {
        const res = await apiFetch('/api/meetingrooms', { method: 'GET' });
        
        if (!res.ok) throw new Error();
        
        const { data } = await res.json();
        meetingRooms = data || [];
        
        renderTimeHeaders();
        
    } catch (e) {
        console.error(e);
        alert('회의실 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Load Reservations
========================== */
async function loadReservations(date) {
    try {
        const params = new URLSearchParams({
            date: date,
            typeCode: 'MEETING'
        });
        
        const res = await apiFetch(`/api/reservations/date?${params.toString()}`, {
            method: 'GET'
        });
        
        if (!res.ok) throw new Error();
        
        const { data } = await res.json();
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
    
    if (meetingRooms.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>회의실 정보가 없습니다</p>
            </div>
        `;
        return;
    }
    
    meetingRooms.forEach(room => {
        const row = document.createElement('div');
        row.className = 'grid-row';
        
        // 회의실 이름 셀
        const roomNameCell = document.createElement('div');
        roomNameCell.className = 'room-name-cell';
        roomNameCell.textContent = room.name;
        roomNameCell.title = room.name;
        row.appendChild(roomNameCell);
        
        // 시간대 셀들
        const timeCells = document.createElement('div');
        timeCells.className = 'time-cells';
        
        HOURS.forEach(hour => {
            const cell = document.createElement('div');
            cell.className = 'time-cell';
            cell.dataset.roomId = room.meetingroomId;
            cell.dataset.roomName = room.name;
            cell.dataset.roomPosition = room.position || '-';
            cell.dataset.roomDescription = room.description || '-';
            cell.dataset.hour = hour;

            console.log("회의실 번호 : " + room.meetingroomId)

            const reservationInfo = checkReservation(room.meetingroomId, hour);

            if (reservationInfo) {
                const { statusId } = reservationInfo;
                console.log("상태값: " + statusId)

                // 승인 대기 → 빨간색
                if (statusId === 1) {
                    cell.classList.add('pending');
                    // cell.textContent = '승인 대기';
                }
                // 예약 확정
                else if (statusId === 2) {
                    cell.classList.add('unavailable');
                    // cell.textContent = '예약됨';
                }

                cell.style.cursor = 'not-allowed';
            }
            else {
                cell.classList.add('available');
                // cell.textContent = '예약 가능';
                cell.addEventListener('click', () =>
                    handleCellClick(cell, room, hour)
                );
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
function checkReservation(roomId, hour) {
    const reservation = reservations.find(r =>
        r.resourceId === roomId &&
        r.startTime <= hour &&
        r.endTime > hour &&
        r.typeCode === 'MEETING'
    );

    if (!reservation) return null;

    return {
        statusId: reservation.reservationStatusId,
        reservation
    };
}

/* ==========================
   Cell Selection Functions
========================== */
function handleCellClick(cell, room, hour) {
    const roomId = parseInt(cell.dataset.roomId);

    console.log("클릭 roomId:", roomId);
    console.log("현재 selectedRoom:", selectedRoom);

    // 다른 회의실을 클릭하면 초기화
    if (selectedRoom && selectedRoom.meetingroomId !== roomId) {
        clearSelection();
    }
    
    selectedRoom = room;
    
    // 첫 번째 클릭 (시작 시간)
    if (selectedStartHour === null) {
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
    if (!isRangeAvailable(roomId, selectedStartHour, selectedEndHour)) {
        alert('선택한 시간 범위에 예약 불가능한 시간이 포함되어 있습니다.');
        clearSelection();
        selectedRoom = room;
        selectedStartHour = hour;
        selectedEndHour = hour + 1;
    }
    
    updateSelection();
    updateReservationForm();
}

function isRangeAvailable(roomId, startHour, endHour) {
    for (let hour = startHour; hour < endHour; hour++) {
        const isReserved = checkReservation(roomId, hour);
        if (isReserved) {
            return false;
        }
    }
    return true;
}

function clearSelection() {
    selectedRoom = null;
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
    
    // 해당 회의실의 시간 범위 선택
    if (selectedRoom && selectedStartHour !== null && selectedEndHour !== null) {
        document.querySelectorAll('.time-cell').forEach(cell => {
            const cellRoomId = parseInt(cell.dataset.roomId);
            const cellHour = parseInt(cell.dataset.hour);
            
            if (cellRoomId === selectedRoom.meetingroomId &&
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
    
    // 회의실 정보
    if (selectedRoom) {
        document.getElementById('selected-room-name').textContent = selectedRoom.name;
        document.getElementById('selected-room-location').textContent = selectedRoom.position || '-';
        document.getElementById('selected-room-description').textContent = selectedRoom.description || '-';
    } else {
        document.getElementById('selected-room-name').textContent = '-';
        document.getElementById('selected-room-location').textContent = '-';
        document.getElementById('selected-room-description').textContent = '-';
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
    const isValid = selectedRoom &&
                    selectedStartHour !== null &&
                    selectedEndHour !== null &&
                    validateReservationReason(false);
    submitBtn.disabled = !isValid;
}

/* ==========================
   Submit Reservation
========================== */
async function submitReservation() {
    if (!selectedRoom || selectedStartHour === null || selectedEndHour === null) {
        alert('회의실과 시간을 선택해주세요.');
        return;
    }
    
    const reason = document.getElementById('reservation-reason').value.trim();
    
    if (!validateReservationReason()) {
        return;
    }
    
    try {
        const payload = {
            typeCode: 'MEETING',
            resourceId: selectedRoom.meetingroomId,
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
    
    await loadMeetingRooms();
    await loadReservations(selectedDate);
});

