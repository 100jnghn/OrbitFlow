(function() {
    // 1. 변수 선언을 즉시 실행 함수 내부로 옮겨 스코프 충돌 방지
    const ATTENDANCE_API_BASE_URL = '/api/attendance';
    let currentAttendance = null;
    let isAway = false;

    // DOM 로드 시 초기화
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

    // 1. 기준 근무 시간 로드
    async function loadActiveWorkHours() {
        const token = getAuthToken();
        if (!token) return;

        try {
            const response = await fetch(`${ATTENDANCE_API_BASE_URL}/active-rule`, {
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
                
                // 출근시간이 퇴근시간보다 늦은지 확인
                const startTime = new Date(`2000-01-01T${data.startTime}`);
                const endTime = new Date(`2000-01-01T${data.endTime}`);
                if (startTime >= endTime) {
                    // 오류 배너 표시
                    const banner = document.getElementById('ruleErrorBanner');
                    if (banner) {
                        banner.style.display = 'block';
                    }
                } else {
                    // 정상이면 배너 숨기기
                    const banner = document.getElementById('ruleErrorBanner');
                    if (banner) {
                        banner.style.display = 'none';
                    }
                }
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
            const response = await fetch(`${ATTENDANCE_API_BASE_URL}/today`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                currentAttendance = await response.json();
                isAway = currentAttendance.isAway;
                updateUI();
            }
        } catch (error) {
            console.error('현황 조회 실패:', error);
        }
    }

    // 3. UI 갱신 로직
    function updateUI() {
        if (!currentAttendance) return;

        // 출퇴근 시간 표시 (홈화면과 출퇴근 기록 화면 모두 지원)
        const commuteTimeEl = document.getElementById('commuteTime');
        const leaveTimeEl = document.getElementById('leaveTime');
        if (commuteTimeEl) commuteTimeEl.textContent = formatTime(currentAttendance.commuteAt);
        if (leaveTimeEl) leaveTimeEl.textContent = formatTime(currentAttendance.leaveAt);

        // 근무상태 벳지 업데이트 (홈화면)
        updateWorkStatusBadge();

        // 출퇴근 기록 화면용 UI 업데이트 (기존 코드 유지)
        const workStatusBadge = document.getElementById('workStatus');
        const awayStatusBadge = document.getElementById('awayStatus');
        const workStatusText = document.getElementById('workStatusText');

        if (workStatusBadge) workStatusBadge.style.display = 'none';
        if (awayStatusBadge) awayStatusBadge.style.display = 'none';
        if (workStatusText) {
            workStatusText.textContent = '';
            workStatusText.style.display = 'inline';
        }

        const attendanceDesc = currentAttendance.statusName || "근무예정";

        if (workStatusText) {
            if (!currentAttendance.commuteAt) {
                workStatusText.textContent = `퇴근 (${attendanceDesc})`;
            } else if (!currentAttendance.leaveAt) {
                if (isAway) {
                    if (awayStatusBadge) awayStatusBadge.style.display = 'inline-block';
                    workStatusText.textContent = ` (${attendanceDesc})`;
                } else {
                    if (workStatusBadge) {
                        workStatusBadge.style.display = 'inline-block';
                        workStatusBadge.textContent = "근무 중";
                    }
                    workStatusText.textContent = ` (${attendanceDesc})`;
                }
            } else {
                workStatusText.textContent = `퇴근 완료 (${attendanceDesc})`;
            }
        }

        updateButtonStates();
    }

    // 근무상태 벳지 업데이트 (홈화면)
    function updateWorkStatusBadge() {
        const badgeEl = document.getElementById('workStatusBadge');
        if (!badgeEl || !currentAttendance) return;

        badgeEl.style.display = 'inline-block';
        badgeEl.className = 'work-status-badge';

        if (!currentAttendance.commuteAt) {
            // 출근 전
            badgeEl.textContent = '근무예정';
            badgeEl.classList.add('badge-before-work');
        } else if (!currentAttendance.leaveAt) {
            // 출근 후, 퇴근 전
            if (isAway) {
                badgeEl.textContent = '자리비움';
                badgeEl.classList.add('badge-away');
            } else {
                badgeEl.textContent = '근무 중';
                badgeEl.classList.add('badge-working');
            }
        } else {
            // 퇴근 완료
            badgeEl.textContent = '퇴근 완료';
            badgeEl.classList.add('badge-off-work');
        }
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
            const response = await fetch(`${ATTENDANCE_API_BASE_URL}/${endpoint}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (!response.ok) {
                alert(data.message || "요청을 처리할 수 없습니다.");
                return;
            }

            await loadTodayAttendance();
            alert("처리가 완료되었습니다.");
            
            // 홈화면인 경우 대시보드 데이터 새로고침
            if (window.location.pathname === '/' || window.location.pathname === '/view/main') {
                if (typeof loadDashboardData === 'function') {
                    loadDashboardData();
                }
            }

        } catch (error) {
            console.error("액션 처리 중 오류:", error);
            alert("시스템 오류가 발생했습니다.");
        }
    }

    // 시간 포맷팅 함수
    function formatTime(isoString) {
        if (!isoString) return '미기록';
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

        if (!btnIn || !btnOut || !btnAway) return;

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
        const checkInBtn = document.getElementById('checkInBtn');
        const checkOutBtn = document.getElementById('checkOutBtn');
        const awayBtn = document.getElementById('awayBtn');
        
        if (checkInBtn) {
            checkInBtn.onclick = () => handleAction('checkin', '출근하시겠습니까?');
        }
        if (checkOutBtn) {
            checkOutBtn.onclick = () => handleAction('checkout', '퇴근하시겠습니까?');
        }
        if (awayBtn) {
            awayBtn.onclick = () => {
                const action = isAway ? 'away/end' : 'away/start';
                handleAction(action, isAway ? '자리비움을 해제하시겠습니까?' : '자리비움을 시작하시겠습니까?');
            };
        }
    }
})(); // 즉시 실행 함수 닫기