const API_BASE_URL = '/api/attendance';
let currentAttendance = null;
let isAway = false;

document.addEventListener('DOMContentLoaded', function() {
    updateClock();
    setInterval(updateClock, 1000);

    loadActiveWorkHours(); // 사원별 맞춤 시간 로드
    loadTodayAttendance(); // 오늘 현황 로드
    setupEventListeners();
});

// 공통 토큰 가져오기 함수
function getAuthToken() {
    return sessionStorage.getItem('accessToken');
}

// 실시간 시계
function updateClock() {
    const clockEl = document.getElementById('currentTime');
    if (clockEl) {
        const now = new Date();
        clockEl.textContent = now.toLocaleTimeString('ko-KR', {
            hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    }
}

// 1. 기준 근무 시간 로드 (관리자 예외 규칙 반영)
async function loadActiveWorkHours() {
    const token = getAuthToken();
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/active-rule`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            const start = data.startTime.substring(0, 5);
            const end = data.endTime.substring(0, 5);
            document.getElementById('workHours').textContent = `${start} ~ ${end}`;
            console.log(`[규칙 적용] ${data.ruleType}: ${start}~${end}`);
        }
    } catch (error) {
        console.error('기준 시간 로드 실패:', error);
    }
}

// 2. 오늘 현황 조회 및 화면 업데이트
async function loadTodayAttendance() {
    const token = getAuthToken();
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/today`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            currentAttendance = await response.json(); // TodayAttResDto 수신
            isAway = currentAttendance.isAway;
            updateUI(); // 수신한 데이터로 UI 전체 갱신
        }
    } catch (error) {
        console.error('현황 조회 실패:', error);
    }
}

// 3. UI 갱신 로직 (출근 시간 및 근무 상태 바인딩)
// 3. UI 갱신 로직 (출근 시간 및 근무 상태 바인딩)
function updateUI() {
    if (!currentAttendance) return;

    // 출퇴근 시각 표시 (HH:mm)
    document.getElementById('commuteTime').textContent = formatTime(currentAttendance.commuteAt);
    document.getElementById('leaveTime').textContent = formatTime(currentAttendance.leaveAt);

    const workStatusBadge = document.getElementById('workStatus');   // "근무 중" 뱃지
    const awayStatusBadge = document.getElementById('awayStatus');   // "자리비움" 뱃지
    const workStatusText = document.getElementById('workStatusText'); // 텍스트 영역

    // 초기화
    workStatusBadge.style.display = 'none';
    awayStatusBadge.style.display = 'none';
    workStatusText.textContent = ''; // 기존 텍스트 비우기
    workStatusText.style.display = 'inline';

    // 최종 근태 상태 설명 (정상출근, 지각 등)
    const attendanceDesc = currentAttendance.statusName || "근무예정";

    // 1. 실시간 근무 상태(WorkStatus)에 따른 레이아웃 결정
    if (!currentAttendance.commuteAt) {
        // 출근 전: 초기값 OFF_WORK 상태
        workStatusText.textContent = `퇴근 (${attendanceDesc})`; // "퇴근 (근무예정)"
    } else if (!currentAttendance.leaveAt) {
        // 출근 후 + 퇴근 전: WORKING 또는 AWAY 상태
        if (isAway) {
            awayStatusBadge.style.display = 'inline-block';
            workStatusText.textContent = ` (${attendanceDesc})`; // "자리비움 (지각)" 등
        } else {
            workStatusBadge.style.display = 'inline-block';
            workStatusBadge.textContent = "근무 중";
            workStatusText.textContent = ` (${attendanceDesc})`; // "근무 중 (정상출근)" 등
        }
    } else {
        // 퇴근 완료: OFF_WORK 상태
        workStatusText.textContent = `퇴근 완료 (${attendanceDesc})`; // "퇴근 완료 (조퇴)" 등
    }

    updateButtonStates();
}

// 4. 출퇴근/자리비움 버튼 클릭 액션
async function handleAction(endpoint, confirmMsg) {
    if (!confirm(confirmMsg)) return;

    const token = getAuthToken();
    if (!token) {
        alert("로그인 정보가 없습니다.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '요청 실패');
        }

        // 서버에서 상태(WorkStatus) 업데이트가 완료된 후, 최신 현황을 다시 불러와 화면 갱신
        await loadTodayAttendance();
        alert("처리가 완료되었습니다.");

    } catch (error) {
        alert(error.message);
    }
}

// 시간 포맷팅 함수
function formatTime(isoString) {
    if (!isoString) return '미기록';
    // 서버에서 받은 LocalDateTime을 HH:mm 형식으로 변환
    const date = new Date(isoString);
    return date.toLocaleTimeString('ko-KR', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 버튼 활성/비활성 제어
function updateButtonStates() {
    const btnIn = document.getElementById('checkInBtn');
    const btnOut = document.getElementById('checkOutBtn');
    const btnAway = document.getElementById('awayBtn');

    const hasIn = !!(currentAttendance && currentAttendance.commuteAt);
    const hasOut = !!(currentAttendance && currentAttendance.leaveAt);

    btnIn.disabled = hasIn;
    btnOut.disabled = !hasIn || hasOut;
    btnAway.disabled = !hasIn || hasOut;

    if (isAway) {
        btnAway.classList.add('active');
        btnAway.textContent = '자리비움 해제';
    } else {
        btnAway.classList.remove('active');
        btnAway.textContent = '자리비움';
    }
}

function setupEventListeners() {
    document.getElementById('checkInBtn').onclick = () => handleAction('checkin', '출근하시겠습니까?');
    document.getElementById('checkOutBtn').onclick = () => handleAction('checkout', '퇴근하시겠습니까?');
    document.getElementById('awayBtn').onclick = () => {
        const action = isAway ? 'away/end' : 'away/start';
        handleAction(action, isAway ? '자리비움을 해제하시겠습니까?' : '자리비움을 시작하시겠습니까?');
    };
}