(function() {
    const ATTENDANCE_API_BASE_URL = '/api/attendance';
    let currentAttendance = null;
    let isAway = false;

    document.addEventListener('DOMContentLoaded', function() {
        updateClock();
        setInterval(updateClock, 1000);
        updateWorkStatusBadge();
        loadActiveWorkHours();
        loadTodayAttendance();
        setupEventListeners();
    });

    function getAuthToken() {
        return sessionStorage.getItem('accessToken');
    }

    function updateClock() {
        const clockEl = document.getElementById('currentTime');
        if (clockEl) {
            const now = new Date();
            clockEl.textContent = now.toLocaleTimeString('ko-KR', {
                hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit'
            });
        }
    }

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
                const ruleData = data.data || data;

                if (!ruleData || !ruleData.startTime || !ruleData.endTime) {
                    const workHoursEl = document.getElementById('workHours');
                    if (workHoursEl) workHoursEl.textContent = '조회 실패';
                    return;
                }

                const start = ruleData.startTime.substring(0, 5);
                const end = ruleData.endTime.substring(0, 5);
                const workHoursEl = document.getElementById('workHours');
                if (workHoursEl) workHoursEl.textContent = `${start} ~ ${end}`;

                const startTime = new Date(`2000-01-01T${ruleData.startTime}`);
                const endTime = new Date(`2000-01-01T${ruleData.endTime}`);
                const banner = document.getElementById('ruleErrorBanner');
                if (banner) banner.style.display = (startTime >= endTime) ? 'block' : 'none';
            }
        } catch (error) {
            console.error('기준 시간 로드 실패:', error);
        }
    }

    async function loadTodayAttendance() {
        const token = getAuthToken();
        if (!token) return;

        try {
            const response = await fetch(`${ATTENDANCE_API_BASE_URL}/today`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const result = await response.json();
                currentAttendance = result.data || result;
                isAway = currentAttendance ? (currentAttendance.isAway || false) : false;
                updateUI();

                // workStatus 동기화
                const profileResponse = await fetch('/api/auth/me', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (profileResponse.ok) {
                    const profileResult = await profileResponse.json();
                    const user = profileResult.data || profileResult;
                    const workStatus = user ? (user.workStatus || null) : null;

                    window.currentServerStatus = workStatus;
                    updateButtonStates();

                    if (workStatus) {
                        window.dispatchEvent(new CustomEvent('workStatusChanged', {
                            detail: { status: workStatus }
                        }));
                    }
                }
            }
        } catch (error) {
            console.error('현황 조회 실패:', error);
        }
    }

    function updateUI() {
        if (!currentAttendance) {
            updateWorkStatusBadge();
            updateButtonStates();
            return;
        }

        const commuteTimeEl = document.getElementById('commuteTime');
        const leaveTimeEl = document.getElementById('leaveTime');
        if (commuteTimeEl) commuteTimeEl.textContent = formatTime(currentAttendance.commuteAt);
        if (leaveTimeEl) leaveTimeEl.textContent = formatTime(currentAttendance.leaveAt);

        updateWorkStatusBadge();
        updateButtonStates();
    }

    function updateWorkStatusBadge() {
        const badgeEl = document.getElementById('workStatusBadge');
        if (!badgeEl) return;

        const currentWorkStatus = window.currentServerStatus || '';
        const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';

        // WorkStatus 기반 배지 (휴가/출장/외근)
        if (['VACATION', 'BUSINESS_TRIP', 'OUTWORK'].includes(normalizedStatus)) {
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

        // 일반 근무 배지
        if (!currentAttendance) {
            badgeEl.className = 'work-status-badge badge-before-work';
            badgeEl.textContent = '근무예정';
            return;
        }

        badgeEl.className = 'work-status-badge';
        if (!currentAttendance.commuteAt) {
            badgeEl.textContent = '근무예정';
            badgeEl.classList.add('badge-before-work');
        } else if (!currentAttendance.leaveAt) {
            if (normalizedStatus === 'AWAY' || isAway) {
                badgeEl.textContent = '자리비움';
                badgeEl.classList.add('badge-away');
            } else {
                badgeEl.textContent = '근무 중';
                badgeEl.classList.add('badge-working');
            }
        } else {
            badgeEl.textContent = '퇴근 완료';
            badgeEl.classList.add('badge-off-work');
        }
    }

    // SweetAlert 적용된 액션 처리 함수
    async function handleAction(endpoint, confirmTitle, confirmMsg) {
        const resultConfirm = await sweetConfirm(confirmTitle, confirmMsg);
        if (!resultConfirm.isConfirmed) return;

        const token = getAuthToken();
        if (!token) {
            sweetError("로그인 정보가 없습니다.");
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
                sweetWarning(message);
                return;
            }

            currentAttendance = data;
            isAway = data ? (data.isAway || false) : false;

            // workStatus 동기화
            const profileResponse = await fetch('/api/auth/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (profileResponse.ok) {
                const profileResult = await profileResponse.json();
                const user = profileResult.data || profileResult;
                if (user) {
                    const workStatus = user.workStatus || null;
                    window.currentServerStatus = workStatus;
                    if (workStatus) {
                        window.dispatchEvent(new CustomEvent('workStatusChanged', {
                            detail: { status: workStatus }
                        }));
                    }
                }
            }

            updateUI();
            await loadTodayAttendance();

            if (window.location.pathname === '/' || window.location.pathname === '/view/main') {
                if (typeof loadDashboardData === 'function') loadDashboardData();
            }

            sweetSuccess("처리가 완료되었습니다.");

        } catch (error) {
            console.error("액션 처리 중 오류:", error);
            sweetError("시스템 오류가 발생했습니다.");
        }
    }

    async function handleReturnFromOutsideOrTrip() {
        const token = getAuthToken();
        if (!token) {
            sweetError("로그인 정보가 없습니다.");
            return;
        }

        const resultConfirm = await sweetConfirm('복귀 안내', '업무로 복귀하시겠습니까?');
        if (!resultConfirm.isConfirmed) return;

        try {
            const response = await fetch('/api/attendance/return', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
            });

            // 응답 바디가 없을 수도 있으니 안전 처리
            let result = null;
            try { result = await response.json(); } catch (e) {}

            if (!response.ok) {
                const msg = (result && (result.message || (result.data && result.data.message))) || '복귀 처리에 실패했습니다.';
                sweetError(msg);
                return;
            }

            await sweetSuccess('자리복귀 및 정상출근 처리 완료');

            await loadTodayAttendance();
            if (window.location.pathname === '/' || window.location.pathname === '/view/main') {
                if (typeof loadDashboardData === 'function') loadDashboardData();
            }
        } catch (error) {
            console.error(error);
            sweetError('시스템 오류가 발생했습니다.');
        }
    }

    function formatTime(isoString) {
        if (!isoString) return '미기록';
        const date = new Date(isoString);
        return date.toLocaleTimeString('ko-KR', { hour12: false, hour: '2-digit', minute: '2-digit' });
    }

    function updateButtonStates() {
        const btnIn = document.getElementById('checkInBtn');
        const btnOut = document.getElementById('checkOutBtn');
        const btnAway = document.getElementById('awayBtn');
        if (!btnIn || !btnOut || !btnAway) return;

        const currentWorkStatus = window.currentServerStatus || '';
        const normalizedStatus = currentWorkStatus ? currentWorkStatus.toUpperCase() : '';

        const isVacation = normalizedStatus === 'VACATION';
        const isOutsideOrTrip = (normalizedStatus === 'BUSINESS_TRIP' || normalizedStatus === 'OUTWORK');

        const hasIn = !!(currentAttendance && currentAttendance.commuteAt);
        const hasOut = !!(currentAttendance && currentAttendance.leaveAt);

        const btnText = btnAway.querySelector('span') || btnAway;

        // ✅ 휴가중: 출퇴근/자리비움 모두 클릭 불가, 텍스트는 자리비움 유지
        if (isVacation) {
            btnIn.disabled = true;
            btnOut.disabled = true;
            btnAway.disabled = true;

            btnAway.classList.remove('btn-return');
            btnAway.classList.remove('active');
            btnText.textContent = '자리비움';
            return;
        }

        // ✅ 외근/출장중: 자리비움 버튼을 "자리 복귀"로 변경(활성), 출퇴근 불가
        if (isOutsideOrTrip) {
            btnIn.disabled = true;
            btnOut.disabled = true;

            btnAway.disabled = false;
            btnAway.classList.add('btn-return');
            btnAway.classList.remove('active');
            btnText.textContent = '자리 복귀';
            return;
        }

        // ✅ 일반 근무: 기존 로직
        btnIn.disabled = hasIn;
        btnOut.disabled = !hasIn || hasOut;
        btnAway.disabled = !hasIn || hasOut;

        btnAway.classList.remove('btn-return');

        if (isAway) {
            btnAway.classList.add('active');
            btnText.textContent = '자리비움 해제';
        } else {
            btnAway.classList.remove('active');
            btnText.textContent = '자리비움';
        }
    }

    window.updateCommuteButtonStates = updateButtonStates;

    function setupEventListeners() {
        const checkInBtn = document.getElementById('checkInBtn');
        const checkOutBtn = document.getElementById('checkOutBtn');
        const awayBtn = document.getElementById('awayBtn');

        if (checkInBtn) {
            checkInBtn.onclick = () => {
                const normalizedStatus = (window.currentServerStatus || '').toUpperCase();

                if (normalizedStatus === 'VACATION') {
                    sweetWarning('현재 휴가중이므로 출근할 수 없습니다.');
                    return;
                }
                if (['BUSINESS_TRIP', 'OUTWORK'].includes(normalizedStatus)) {
                    sweetWarning('현재 출장/외근 상태이므로 출근할 수 없습니다.\n업무 복귀 시 "자리 복귀" 버튼을 눌러주세요.');
                    return;
                }

                handleAction('checkin', '출근 안내', '출근하시겠습니까?');
            };
        }

        if (checkOutBtn) {
            checkOutBtn.onclick = () => {
                const normalizedStatus = (window.currentServerStatus || '').toUpperCase();

                if (normalizedStatus === 'VACATION') {
                    sweetWarning('현재 휴가중이므로 퇴근할 수 없습니다.');
                    return;
                }
                if (['BUSINESS_TRIP', 'OUTWORK'].includes(normalizedStatus)) {
                    sweetWarning('현재 출장/외근 상태이므로 퇴근할 수 없습니다.\n업무 복귀 시 "자리 복귀" 버튼을 눌러주세요.');
                    return;
                }

                handleAction('checkout', '퇴근 안내', '퇴근하시겠습니까?');
            };
        }

        if (awayBtn) {
            awayBtn.onclick = async () => {
                const normalizedStatus = (window.currentServerStatus || '').toUpperCase();

                // ✅ 휴가: 자리비움 자체 불가
                if (normalizedStatus === 'VACATION') {
                    sweetWarning('현재 휴가중이므로 자리비움 기능을 사용할 수 없습니다.');
                    return;
                }

                // ✅ 외근/출장: 자리복귀로 처리 (오늘 정상출근)
                if (['BUSINESS_TRIP', 'OUTWORK'].includes(normalizedStatus) || awayBtn.classList.contains('btn-return')) {
                    await handleReturnFromOutsideOrTrip();
                    return;
                }

                // ✅ 일반 근무: 기존 자리비움 start/end
                const action = isAway ? 'away/end' : 'away/start';
                handleAction(action, '근태 상태 변경', isAway ? '자리비움을 해제하시겠습니까?' : '자리비움을 시작하시겠습니까?');
            };
        }
    }
})();
