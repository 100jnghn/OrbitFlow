// API 기본 URL
const API_BASE_URL = '/api/attendance';
const RULES_API_URL = '/api/admin/rules';

// 전역 변수
let currentAttendance = null;
let workHours = { start: '09:00', end: '18:00' };
let isAway = false;
let awayStartTime = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    updateClock();
    setInterval(updateClock, 1000);
    loadWorkHours();
    loadTodayAttendance();
    setupEventListeners();
});

// 실시간 시계 업데이트
function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    document.getElementById('currentTime').textContent = `${hours}:${minutes}:${seconds}`;
}

// 기준 근무시간 로드
async function loadWorkHours() {
    try {
        const response = await fetch(`${RULES_API_URL}/default`);
        if (!response.ok) throw new Error('근무시간을 불러오는데 실패했습니다.');

        const data = await response.json();
        if (data.defaultStartTime && data.defaultEndTime) {
            workHours.start = data.defaultStartTime.substring(0, 5);
            workHours.end = data.defaultEndTime.substring(0, 5);
            document.getElementById('workHours').textContent = `${workHours.start} ~ ${workHours.end}`;
        }
    } catch (error) {
        console.error('Error loading work hours:', error);
    }
}

// 오늘의 출퇴근 기록 로드
async function loadTodayAttendance() {
    try {
        const response = await fetch(`${API_BASE_URL}/today`);
        if (!response.ok) throw new Error('출퇴근 기록을 불러오는데 실패했습니다.');

        const data = await response.json();
        currentAttendance = data;

        // 자리비움 상태 동기화
        isAway = data.isAway || false;
        if (isAway && !awayStartTime) {
            awayStartTime = new Date().toISOString();
        }

        // 출근 시각 업데이트
        const commuteTimeEl = document.getElementById('commuteTime');
        if (data.commuteAt) {
            const commuteTime = new Date(data.commuteAt);
            commuteTimeEl.textContent =
                `${String(commuteTime.getHours()).padStart(2, '0')}:${String(commuteTime.getMinutes()).padStart(2, '0')}`;
            commuteTimeEl.className = 'record-value';
        } else {
            commuteTimeEl.textContent = '미기록';
            commuteTimeEl.className = 'record-value empty';
        }

        // 퇴근 시각 업데이트
        const leaveTimeEl = document.getElementById('leaveTime');
        if (data.leaveAt) {
            const leaveTime = new Date(data.leaveAt);
            leaveTimeEl.textContent =
                `${String(leaveTime.getHours()).padStart(2, '0')}:${String(leaveTime.getMinutes()).padStart(2, '0')}`;
            leaveTimeEl.className = 'record-value';
        } else {
            leaveTimeEl.textContent = '미기록';
            leaveTimeEl.className = 'record-value empty';
        }

        // 근무 상태 업데이트
        const workStatusBadge = document.getElementById('workStatus');
        const awayStatusBadge = document.getElementById('awayStatus');
        const workStatusText = document.getElementById('workStatusText');
        const awayTimeItem = document.getElementById('awayTimeItem');
        const awayStartTimeEl = document.getElementById('awayStartTime');

        if (data.commuteAt && !data.leaveAt) {
            if (isAway) {
                workStatusBadge.style.display = 'none';
                awayStatusBadge.style.display = 'inline-block';
                workStatusText.style.display = 'none';
                awayTimeItem.style.display = 'flex';
                if (awayStartTime) {
                    const awayTime = new Date(awayStartTime);
                    awayStartTimeEl.textContent =
                        `${String(awayTime.getHours()).padStart(2, '0')}:${String(awayTime.getMinutes()).padStart(2, '0')}`;
                }
            } else {
                workStatusBadge.style.display = 'inline-block';
                awayStatusBadge.style.display = 'none';
                workStatusText.style.display = 'none';
                awayTimeItem.style.display = 'none';
            }
        } else if (data.commuteAt && data.leaveAt) {
            workStatusBadge.style.display = 'none';
            awayStatusBadge.style.display = 'none';
            workStatusText.textContent = '퇴근 완료';
            workStatusText.className = 'record-value';
            workStatusText.style.display = 'inline';
            awayTimeItem.style.display = 'none';
        } else {
            workStatusBadge.style.display = 'none';
            awayStatusBadge.style.display = 'none';
            workStatusText.textContent = '미출근';
            workStatusText.className = 'record-value empty';
            workStatusText.style.display = 'inline';
            awayTimeItem.style.display = 'none';
        }

        // 버튼 상태 업데이트
        updateButtonStates();
    } catch (error) {
        console.error('Error loading today attendance:', error);
        updateButtonStates();
    }
}

// 버튼 상태 업데이트
function updateButtonStates() {
    const checkInBtn = document.getElementById('checkInBtn');
    const checkOutBtn = document.getElementById('checkOutBtn');
    const awayBtn = document.getElementById('awayBtn');

    if (currentAttendance && currentAttendance.commuteAt) {
        checkInBtn.disabled = true;
        checkOutBtn.disabled = false;
        awayBtn.disabled = false;
    } else {
        checkInBtn.disabled = false;
        checkOutBtn.disabled = true;
        awayBtn.disabled = true;
    }

    if (currentAttendance && currentAttendance.leaveAt) {
        checkOutBtn.disabled = true;
        awayBtn.disabled = true;
    }

    if (isAway) {
        awayBtn.classList.add('active');
        awayBtn.textContent = '자리비움 해제';
    } else {
        awayBtn.classList.remove('active');
        awayBtn.textContent = '자리비움';
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    document.getElementById('checkInBtn').addEventListener('click', handleCheckIn);
    document.getElementById('checkOutBtn').addEventListener('click', handleCheckOut);
    document.getElementById('awayBtn').addEventListener('click', handleAway);
}

// 출근 처리
async function handleCheckIn() {
    if (!confirm('출근하시겠습니까?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/checkin`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '출근 처리에 실패했습니다.');
        }

        alert('출근이 완료되었습니다.');
        loadTodayAttendance();
    } catch (error) {
        console.error('Error checking in:', error);
        alert(error.message || '출근 처리에 실패했습니다.');
    }
}

// 퇴근 처리
async function handleCheckOut() {
    if (!confirm('퇴근하시겠습니까?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/checkout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '퇴근 처리에 실패했습니다.');
        }

        isAway = false;
        awayStartTime = null;
        alert('퇴근이 완료되었습니다.');
        loadTodayAttendance();
    } catch (error) {
        console.error('Error checking out:', error);
        alert(error.message || '퇴근 처리에 실패했습니다.');
    }
}

// 자리비움 처리
async function handleAway() {
    if (!currentAttendance || !currentAttendance.commuteAt || currentAttendance.leaveAt) {
        alert('출근 후에만 자리비움을 설정할 수 있습니다.');
        return;
    }

    if (isAway) {
        if (!confirm('자리비움을 해제하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/away/end`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) throw new Error('자리비움 해제에 실패했습니다.');

            isAway = false;
            awayStartTime = null;
            alert('자리비움이 해제되었습니다.');
            loadTodayAttendance();
        } catch (error) {
            console.error('Error ending away:', error);
            isAway = false;
            awayStartTime = null;
            loadTodayAttendance();
        }
    } else {
        if (!confirm('자리비움을 시작하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/away/start`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) throw new Error('자리비움 시작에 실패했습니다.');

            isAway = true;
            awayStartTime = new Date().toISOString();
            alert('자리비움이 시작되었습니다.');
            loadTodayAttendance();
        } catch (error) {
            console.error('Error starting away:', error);
            isAway = true;
            awayStartTime = new Date().toISOString();
            loadTodayAttendance();
        }
    }
}

