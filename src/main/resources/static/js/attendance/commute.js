(function() {
    // 1. 변수 선언을 즉시 실행 함수 내부로 옮겨 스코프 충돌 방지
    const ATTENDANCE_API_BASE_URL = '/api/attendance';
    let currentAttendance = null;
    let isAway = false;

    // DOM 로드 시 초기화
    document.addEventListener('DOMContentLoaded', function() {
        updateClock();
        setInterval(updateClock, 1000);

        // 초기 상태 벳지 표시
        updateWorkStatusBadge();

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
        if (!token) {
            console.warn('토큰이 없어 기준 근무 시간을 로드할 수 없습니다.');
            return;
        }

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
                
                // ResponseDto 구조 확인 (data 필드가 있으면 사용, 없으면 직접 사용)
                const ruleData = data.data || data;
                
                if (!ruleData || !ruleData.startTime || !ruleData.endTime) {
                    console.error('기준 근무 시간 데이터가 올바르지 않습니다:', ruleData);
                    const workHoursEl = document.getElementById('workHours');
                    if (workHoursEl) {
                        workHoursEl.textContent = '조회 실패';
                    }
                    return;
                }
                
                // LocalTime 형식 처리 (HH:mm:ss 또는 HH:mm)
                let startTimeStr = ruleData.startTime;
                let endTimeStr = ruleData.endTime;
                
                // 문자열이면 그대로 사용, 아니면 변환
                if (typeof startTimeStr !== 'string') {
                    console.error('startTime이 문자열이 아닙니다:', startTimeStr);
                    const workHoursEl = document.getElementById('workHours');
                    if (workHoursEl) {
                        workHoursEl.textContent = '조회 실패';
                    }
                    return;
                }
                
                const start = startTimeStr.substring(0, 5);
                const end = endTimeStr.substring(0, 5);
                
                const workHoursEl = document.getElementById('workHours');
                if (workHoursEl) {
                    workHoursEl.textContent = `${start} ~ ${end}`;
                }
                
                // 출근시간이 퇴근시간보다 늦은지 확인
                const startTime = new Date(`2000-01-01T${startTimeStr}`);
                const endTime = new Date(`2000-01-01T${endTimeStr}`);
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
            } else {
                console.error('기준 근무 시간 API 응답 오류:', response.status, response.statusText);
                const workHoursEl = document.getElementById('workHours');
                if (workHoursEl) {
                    workHoursEl.textContent = '조회 실패';
                }
            }
        } catch (error) {
            console.error('기준 시간 로드 실패:', error);
            const workHoursEl = document.getElementById('workHours');
            if (workHoursEl) {
                workHoursEl.textContent = '조회 실패';
            }
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
                const result = await response.json();
                // ResponseDto 구조 확인
                currentAttendance = result.data || result;
                isAway = currentAttendance ? (currentAttendance.isAway || false) : false;
                updateUI();
                
                // 🔥 workStatus도 함께 가져와서 버튼 상태 업데이트
                try {
                    const profileResponse = await fetch('/api/auth/me', {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });
                    
                    if (profileResponse.ok) {
                        const profileResult = await profileResponse.json();
                        const user = profileResult.data || profileResult;
                        // 🔥 workStatus가 없어도 null로 설정하여 명확히 처리
                        const workStatus = user ? (user.workStatus || null) : null;
                        window.currentServerStatus = workStatus;
                        updateButtonStates(); // 버튼 상태 업데이트
                        
                        // workStatusChanged 이벤트도 dispatch하여 index.js에 알림
                        if (workStatus) {
                            window.dispatchEvent(new CustomEvent('workStatusChanged', {
                                detail: { status: workStatus }
                            }));
                        }
                    }
                } catch (e) {
                    console.warn('workStatus 조회 실패:', e);
                }
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
        if (!badgeEl) return;

        // 🔥 window.currentServerStatus를 우선 확인 (서버 상태가 가장 정확)
        const currentWorkStatus = window.currentServerStatus || '';
        const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';
        
        // 특수 상태(휴가/출장/외근)는 서버 상태를 우선 표시
        if (normalizedStatus === 'VACATION' || normalizedStatus === 'BUSINESS_TRIP' || normalizedStatus === 'OUTWORK') {
            badgeEl.style.display = 'inline-block';
            badgeEl.className = 'work-status-badge';
            
            if (normalizedStatus === 'VACATION') {
                badgeEl.textContent = '휴가중';
                badgeEl.classList.add('badge-vacation');
            } else if (normalizedStatus === 'BUSINESS_TRIP') {
                badgeEl.textContent = '출장중';
                badgeEl.classList.add('badge-business');
            } else if (normalizedStatus === 'OUTWORK') {
                badgeEl.textContent = '외근중';
                badgeEl.classList.add('badge-outside');
            }
            return;
        }

        // currentAttendance가 없으면 기본 상태 표시
        if (!currentAttendance) {
            badgeEl.style.display = 'inline-block';
            badgeEl.className = 'work-status-badge badge-before-work';
            badgeEl.textContent = '근무예정';
            return;
        }

        badgeEl.style.display = 'inline-block';
        badgeEl.className = 'work-status-badge';

        const commuteAt = currentAttendance.commuteAt;
        const leaveAt = currentAttendance.leaveAt;

        if (!commuteAt) {
            // 출근 전
            badgeEl.textContent = '근무예정';
            badgeEl.classList.add('badge-before-work');
        } else if (!leaveAt) {
            // 출근 후, 퇴근 전
            // 🔥 AWAY 상태는 서버 상태를 우선 확인
            if (normalizedStatus === 'AWAY' || isAway) {
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

            const result = await response.json();
            const data = result.data || result;

            if (!response.ok) {
                const message = result.message || data.message || "요청을 처리할 수 없습니다.";
                alert(message);
                return;
            }

            // 즉시 UI 업데이트
            currentAttendance = data;
            isAway = data ? (data.isAway || false) : false;
            
            // 🔥 workStatus 동기화: 서버에서 최신 workStatus를 가져와서 이벤트로 전달
            // 자리비움 시작/종료 시 workStatus가 변경되므로 먼저 동기화
            try {
                const profileResponse = await fetch('/api/auth/me', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                
                if (profileResponse.ok) {
                    const profileResult = await profileResponse.json();
                    const user = profileResult.data || profileResult;
                    
                    if (user) {
                        // 🔥 workStatus가 없어도 null로 설정하여 명확히 처리
                        const workStatus = user.workStatus || null;
                        
                        // window.currentServerStatus 업데이트
                        window.currentServerStatus = workStatus;
                        
                        // workStatusChanged 이벤트를 dispatch하여 index.js에 알림
                        if (workStatus) {
                            window.dispatchEvent(new CustomEvent('workStatusChanged', {
                                detail: { status: workStatus }
                            }));
                        }
                    }
                }
            } catch (e) {
                console.warn('workStatus 동기화 실패:', e);
            }
            
            // UI 업데이트 (workStatus 동기화 후)
            updateUI();
            
            // 최신 데이터 다시 로드
            await loadTodayAttendance();
            
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

    // 버튼 활성/비활성 제어 (전역에서도 접근 가능하도록 window에 노출)
    function updateButtonStates() {
        const btnIn = document.getElementById('checkInBtn');
        const btnOut = document.getElementById('checkOutBtn');
        const btnAway = document.getElementById('awayBtn');

        if (!btnIn || !btnOut || !btnAway) return;

        // 🔥 특수 상태 확인 (휴가/출장/외근)
        const currentWorkStatus = window.currentServerStatus || '';
        const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';
        const isSpecialStatus = normalizedStatus === 'VACATION' || 
                                normalizedStatus === 'BUSINESS_TRIP' || 
                                normalizedStatus === 'OUTWORK';

        const hasIn = !!(currentAttendance && currentAttendance.commuteAt);
        const hasOut = !!(currentAttendance && currentAttendance.leaveAt);

        // 특수 상태일 때는 출퇴근/자리비움 버튼 비활성화
        if (isSpecialStatus) {
            btnIn.disabled = true;
            btnOut.disabled = true;
            btnAway.disabled = false; // 자리 복귀 버튼은 활성화
            
            // 자리 복귀 버튼으로 변경
            btnAway.classList.add('btn-return');
            const btnText = btnAway.querySelector('span') || btnAway;
            if (btnText.tagName === 'SPAN') {
                btnText.textContent = '자리 복귀';
            } else {
                btnAway.textContent = '자리 복귀';
            }
        } else {
            // 일반 상태일 때는 기존 로직
            btnIn.disabled = hasIn;
            btnOut.disabled = !hasIn || hasOut;
            btnAway.disabled = !hasIn || hasOut;
            btnAway.classList.remove('btn-return');

            if (isAway) {
                btnAway.classList.add('active');
                const btnText = btnAway.querySelector('span') || btnAway;
                if (btnText.tagName === 'SPAN') {
                    btnText.textContent = '자리비움 해제';
                } else {
                    btnAway.textContent = '자리비움 해제';
                }
            } else {
                btnAway.classList.remove('active');
                const btnText = btnAway.querySelector('span') || btnAway;
                if (btnText.tagName === 'SPAN') {
                    btnText.textContent = '자리비움';
                } else {
                    btnAway.textContent = '자리비움';
                }
            }
        }
    }
    
    // 전역에서 접근 가능하도록 window에 노출
    window.updateCommuteButtonStates = updateButtonStates;

    function setupEventListeners() {
        const checkInBtn = document.getElementById('checkInBtn');
        const checkOutBtn = document.getElementById('checkOutBtn');
        const awayBtn = document.getElementById('awayBtn');
        
        if (checkInBtn) {
            checkInBtn.onclick = () => {
                // 특수 상태 체크
                const currentWorkStatus = window.currentServerStatus || '';
                const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';
                if (normalizedStatus === 'VACATION' || normalizedStatus === 'BUSINESS_TRIP' || normalizedStatus === 'OUTWORK') {
                    alert('현재 휴가/출장/외근 상태이므로 출근할 수 없습니다.\n조기 복귀 시 "자리 복귀" 버튼을 눌러주세요.');
                    return;
                }
                handleAction('checkin', '출근하시겠습니까?');
            };
        }
        if (checkOutBtn) {
            checkOutBtn.onclick = () => {
                // 특수 상태 체크
                const currentWorkStatus = window.currentServerStatus || '';
                const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';
                if (normalizedStatus === 'VACATION' || normalizedStatus === 'BUSINESS_TRIP' || normalizedStatus === 'OUTWORK') {
                    alert('현재 휴가/출장/외근 상태이므로 퇴근할 수 없습니다.\n조기 복귀 시 "자리 복귀" 버튼을 눌러주세요.');
                    return;
                }
                handleAction('checkout', '퇴근하시겠습니까?');
            };
        }
        if (awayBtn) {
            awayBtn.onclick = async () => {
                // 특수 상태일 때는 자리 복귀 처리
                const currentWorkStatus = window.currentServerStatus || '';
                const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';
                
                if (normalizedStatus === 'VACATION' || normalizedStatus === 'BUSINESS_TRIP' || normalizedStatus === 'OUTWORK') {
                    const statusText = normalizedStatus === 'VACATION' ? '휴가중' : 
                                     normalizedStatus === 'BUSINESS_TRIP' ? '출장중' : '외근중';
                    if (confirm(`현재 ${statusText} 상태입니다. 업무로 복귀하시겠습니까?`)) {
                        try {
                            const token = getAuthToken();
                            if (!token) {
                                alert("로그인 정보가 없습니다.");
                                return;
                            }
                            
                            const response = await fetch('/api/leave/return', {
                                method: 'POST',
                                headers: {
                                    'Authorization': `Bearer ${token}`,
                                    'Content-Type': 'application/json'
                                }
                            });
                            
                            if (response.ok) {
                                alert('업무로 복귀 처리되었습니다.');
                                
                                // workStatus 동기화
                                const profileResponse = await fetch('/api/auth/me', {
                                    method: 'GET',
                                    headers: {
                                        'Authorization': `Bearer ${token}`,
                                        'Content-Type': 'application/json'
                                    }
                                });
                                
                                if (profileResponse.ok) {
                                    const profileResult = await profileResponse.json();
                                    const user = profileResult.data || profileResult;
                                    
                                    if (user && user.workStatus) {
                                        window.currentServerStatus = user.workStatus;
                                        window.dispatchEvent(new CustomEvent('workStatusChanged', {
                                            detail: { status: user.workStatus }
                                        }));
                                    }
                                }
                                
                                // 오늘 근태 정보 다시 로드
                                await loadTodayAttendance();
                                
                                // 홈화면인 경우 대시보드 데이터 새로고침
                                if (window.location.pathname === '/' || window.location.pathname === '/view/main') {
                                    if (typeof loadDashboardData === 'function') {
                                        loadDashboardData();
                                    }
                                }
                            } else {
                                const result = await response.json();
                                alert(result.message || '복귀 처리에 실패했습니다.');
                            }
                        } catch (error) {
                            console.error('복귀 API 호출 오류:', error);
                            alert('시스템 오류가 발생했습니다.');
                        }
                    }
                    return;
                }
                
                // 일반 상태일 때는 기존 자리비움 처리
                const action = isAway ? 'away/end' : 'away/start';
                handleAction(action, isAway ? '자리비움을 해제하시겠습니까?' : '자리비움을 시작하시겠습니까?');
            };
        }
    }
})(); // 즉시 실행 함수 닫기