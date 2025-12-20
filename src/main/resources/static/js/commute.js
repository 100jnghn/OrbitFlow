const API_BASE_URL = '/api/attendance';
let currentAttendance = null;
let isAway = false;

document.addEventListener('DOMContentLoaded', function() {
    updateClock();
    setInterval(updateClock, 1000);

    loadActiveWorkHours(); // 사원별 맞춤 시간(기본/예외) 로드
    loadTodayAttendance(); // 오늘 현황 및 Enum 상태 로드
    setupEventListeners();
});

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

// 사원별 적용 시간 로드 (403 방지를 위해 사용자 API 호출)
async function loadActiveWorkHours() {
    try {
        // CommuteController의 /api/attendance/active-rule 호출
        const response = await fetch('/api/attendance/active-rule');
        if (response.ok) {
            const data = await response.json(); // { startTime, endTime }
            const start = data.startTime.substring(0, 5);
            const end = data.endTime.substring(0, 5);
            document.getElementById('workHours').textContent = `${start} ~ ${end}`;
        }
    } catch (error) {
        console.error('근무 규칙 로드 실패:', error);
        document.getElementById('workHours').textContent = "09:00 ~ 18:00";
    }
}

// 오늘 근태 및 Enum 상태(statusName) 로드
async function loadTodayAttendance() {
    try {
        const response = await fetch(`${API_BASE_URL}/today`);
        if (!response.ok) throw new Error('데이터 로드 실패');

        const data = await response.json();
        currentAttendance = data;
        isAway = data.isAway;

        document.getElementById('commuteTime').textContent = formatTime(data.commuteAt);
        document.getElementById('leaveTime').textContent = formatTime(data.leaveAt);

        updateStatusUI(data);
        updateButtonStates();
    } catch (error) {
        console.error('현황 로드 오류:', error);
    }
}

function updateStatusUI(data) {
    const workStatusBadge = document.getElementById('workStatus');
    const awayStatusBadge = document.getElementById('awayStatus');
    const workStatusText = document.getElementById('workStatusText');

    workStatusBadge.style.display = 'none';
    awayStatusBadge.style.display = 'none';
    workStatusText.style.display = 'none';

    if (!data.commuteAt) {
        workStatusText.textContent = data.statusName; // "근무예정"
        workStatusText.style.display = 'inline';
    } else if (!data.leaveAt) {
        if (isAway) {
            awayStatusBadge.style.display = 'inline-block';
        } else {
            workStatusBadge.style.display = 'inline-block';
            workStatusBadge.textContent = data.statusName; // "정상출근" 또는 "지각"
        }
    } else {
        workStatusText.textContent = `퇴근 완료 (${data.statusName})`; // "퇴근 완료 (정상출근/조퇴)"
        workStatusText.style.display = 'inline';
    }
}

// POST 요청 처리 (CSRF 토큰 필수 포함)
async function handleAction(endpoint, confirmMsg) {
    if (!confirm(confirmMsg)) return;

    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    try {
        const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '요청 처리 실패');
        }
        await loadTodayAttendance();
    } catch (error) {
        alert(error.message);
    }
}

// 시간 포맷팅 및 버튼 상태 제어 함수 생략 (기본 로직 유지)
function formatTime(isoString) {
    if (!isoString) return '미기록';
    return new Date(isoString).toLocaleTimeString('ko-KR', { hour12: false, hour: '2-digit', minute: '2-digit' });
}

function updateButtonStates() {
    const btnIn = document.getElementById('checkInBtn');
    const btnOut = document.getElementById('checkOutBtn');
    const btnAway = document.getElementById('awayBtn');
    const hasIn = !!(currentAttendance && currentAttendance.commuteAt);
    const hasOut = !!(currentAttendance && currentAttendance.leaveAt);
    btnIn.disabled = hasIn;
    btnOut.disabled = !hasIn || hasOut;
    btnAway.disabled = !hasIn || hasOut;
    if (isAway) { btnAway.classList.add('active'); btnAway.textContent = '자리비움 해제'; }
    else { btnAway.classList.remove('active'); btnAway.textContent = '자리비움'; }
}

function setupEventListeners() {
    document.getElementById('checkInBtn').onclick = () => handleAction('checkin', '출근하시겠습니까?');
    document.getElementById('checkOutBtn').onclick = () => handleAction('checkout', '퇴근하시겠습니까?');
    document.getElementById('awayBtn').onclick = () => {
        const action = isAway ? 'away/end' : 'away/start';
        handleAction(action, isAway ? '자리비움을 해제하시겠습니까?' : '자리비움을 시작하시겠습니까?');
    };
}