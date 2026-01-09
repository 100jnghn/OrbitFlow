/**
 * Orbit Flow 홈 대시보드 스크립트
 * 사용자 프로필, 공지사항, 실시간 근무 상태 배지 연동을 담당합니다.
 */

// 1. 🚀 [수정] 통일된 WorkStatus Enum 값에 맞춰 매핑 테이블 정리
const WORK_STATUS_MAP = {
    'WORKING': { text: '근무중', className: 'badge-working' },
    'VACATION': { text: '휴가중', className: 'badge-vacation' },
    'BUSINESS_TRIP': { text: '출장중', className: 'badge-business' },
    'OUTWORK': { text: '외근중', className: 'badge-outside' },
    'AWAY': { text: '자리비움', className: 'badge-outside' },
    'OFF_WORK': { text: '퇴근완료', className: 'badge-off-work' },
    'BEFORE_WORK': { text: '출근전', className: 'badge-before-work' },
    'DEFAULT': { text: '출근전', className: 'badge-before-work' }
};

// 🚀 특수 상태 정의 (차단 및 '자리 복귀' 전환용)
const SPECIAL_STATUSES = {
    'VACATION': '휴가중',
    'BUSINESS_TRIP': '출장중',
    'OUTWORK': '외근중'
};

const WORK_STATUS_STORAGE_KEY = 'optimisticWorkStatus';

document.addEventListener('DOMContentLoaded', async function () {
    initWorkStatusBadgeInteractions();
    applyOptimisticWorkStatusIfAny();
    await loadDashboardData();
});

async function loadDashboardData() {
    try {
        await Promise.all([
            loadUserProfile(),
            loadNotices(),
            loadAttendance(),
            loadScheduleSummary(),
            loadApprovalStats()
        ]);
    } catch (error) {
        console.error('대시보드 데이터 로드 실패:', error);
    }
}

/**
 * 1. 근무 상태 관리 유틸리티
 */
function getOptimisticWorkStatus() {
    try { return sessionStorage.getItem(WORK_STATUS_STORAGE_KEY); } catch (e) { return null; }
}

function setOptimisticWorkStatus(status) {
    try { sessionStorage.setItem(WORK_STATUS_STORAGE_KEY, status); } catch (e) { }
    updateNameAdjacentBadge(status);
}

function clearOptimisticWorkStatus() {
    try { sessionStorage.removeItem(WORK_STATUS_STORAGE_KEY); } catch (e) { }
}

function applyOptimisticWorkStatusIfAny() {
    const status = getOptimisticWorkStatus();
    if (status) updateNameAdjacentBadge(status);
}

/** 서버 상태와 화면 동기화 */
async function syncWorkStatusFromServer() {
    try {
        await loadUserProfile(); // /api/auth/me 재조회 → 배지 및 전역 변수 갱신
    } catch (e) {
        console.warn('workStatus 서버 동기화 실패:', e);
    }
}

/**
 * 2. 출퇴근 버튼 인터랙션 및 특수 상태 제어
 * 
 * 🔥 출퇴근/자리비움 버튼 클릭은 commute.js에서 처리합니다.
 * commute.js에서 API 호출 성공 후 workStatusChanged 이벤트를 dispatch하여
 * index.js의 이벤트 리스너가 상태를 업데이트합니다.
 */
function initWorkStatusBadgeInteractions() {
    // 외부 이벤트 수신 (commute.js 등)
    window.addEventListener('workStatusChanged', (e) => {
        const status = e?.detail?.status;
        if (!status) return;
        clearOptimisticWorkStatus();
        updateNameAdjacentBadge(status);
        // 서버 상태도 전역 변수에 저장
        if (window.currentServerStatus !== status) {
            window.currentServerStatus = status;
        }
        
        // commute.js의 버튼 상태도 업데이트되도록 함수 호출
        setTimeout(() => {
            if (typeof window.updateCommuteButtonStates === 'function') {
                window.updateCommuteButtonStates();
            }
        }, 100);
    });
}

/** 🚀 조기 복귀 처리 함수 (백엔드 LeaveService.processEarlyReturn 연동) */
async function processEarlyReturn() {
    try {
        const response = await apiFetch('/api/leave/return', { method: 'POST' });
        if (response.ok) {
            clearOptimisticWorkStatus();
            alert('업무로 복귀 처리되었습니다.');
            
            // 서버 상태 즉시 동기화
            await loadUserProfile();
            
            // workStatusChanged 이벤트를 dispatch하여 commute.js에도 알림
            const currentStatus = window.currentServerStatus;
            if (currentStatus) {
                window.dispatchEvent(new CustomEvent('workStatusChanged', {
                    detail: { status: currentStatus }
                }));
            }
            
            // commute.js의 버튼 상태도 업데이트되도록 이벤트 발생
            if (typeof window.updateCommuteButtons === 'function') {
                window.updateCommuteButtons();
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

/**
 * 3. 사용자 프로필 로드 (이름 옆 배지 포함)
 */
async function loadUserProfile() {
    try {
        const response = await apiFetch('/api/auth/me');
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('프로필 정보를 불러오지 못했습니다.');
        }

        const result = await response.json();
        const user = result.data;

        // 이름 및 배지 업데이트
        safeSetText('profileName', user.name || '-');

        // 🔥 서버에서 받아온 실제 상태를 전역 변수에 저장
        // workStatus가 null이거나 undefined일 수 있으므로 안전하게 처리
        const workStatus = (user && user.workStatus) ? user.workStatus : null;
        window.currentServerStatus = workStatus;
        
        // 디버깅 로그: workStatus 확인
        if (!workStatus) {
            console.warn('[loadUserProfile] workStatus가 없습니다. user:', user);
        } else {
            console.log('[loadUserProfile] workStatus:', workStatus);
        }

        // 이름 옆에 배지 생성/업데이트 함수 호출
        // workStatus가 없으면 기본값(DEFAULT)으로 표시
        updateNameAdjacentBadge(workStatus || 'DEFAULT');
        
        // commute.js의 버튼 상태도 업데이트
        setTimeout(() => {
            if (typeof window.updateCommuteButtonStates === 'function') {
                window.updateCommuteButtonStates();
            }
        }, 100);

        // 부서 및 직급 (직급 필드명은 백엔드 DTO에 따라 positionName 또는 positionRankName 확인 필요)
        safeSetText('profileDept', user.organizationName || '-');
        safeSetText('profileRank', user.positionName ? `(${user.positionName})` : '');

        // 세션 스토리지에 저장된 임시 상태(Optimistic UI)가 서버 상태와 일치하면 삭제
        const optimisticStatus = getOptimisticWorkStatus();
        if (user.workStatus && optimisticStatus === user.workStatus) {
            clearOptimisticWorkStatus();
        }

        // 프로필 이미지 (없을 경우 기본 이미지)
        const profileImgEl = document.getElementById('profileImage');
        if (profileImgEl && user.profileImageUrl) {
            profileImgEl.src = user.profileImageUrl;
        }

    } catch (error) {
        console.error('프로필 로드 중 오류 발생:', error);
    }
}

/**
 * 2. 이름 옆 근무 상태 배지 UI 업데이트
 */
function updateNameAdjacentBadge(status) {
    const nameEl = document.getElementById('profileName');
    if (!nameEl) return;
    
    // awayBtn은 commute.js에서도 사용하므로 여기서 찾음
    const awayBtn = document.getElementById('awayBtn');

    // 🔥 대소문자 정규화: status가 null이거나 undefined, 또는 빈 문자열이면 DEFAULT로 처리
    let normalizedStatus = 'DEFAULT';
    if (status && typeof status === 'string' && status.trim() !== '') {
        normalizedStatus = status.toUpperCase();
    }

    let badge = document.getElementById('workStatusBadge');
    if (!badge) {
        badge = document.createElement('span');
        badge.id = 'workStatusBadge';
        nameEl.parentNode.insertBefore(badge, nameEl.nextSibling);
    }

    const config = WORK_STATUS_MAP[normalizedStatus] || WORK_STATUS_MAP['DEFAULT'];

    badge.textContent = config.text;
    badge.className = `work-status-badge ${config.className}`;
    badge.style.display = 'inline-block';
    badge.style.marginLeft = '8px';
    
    // 🔥 디버깅 로그: workStatus 확인 (필요시 제거 가능)
    if (!status || normalizedStatus === 'DEFAULT') {
        console.warn('[updateNameAdjacentBadge] status가 없거나 DEFAULT입니다. status:', status);
    }

    // 🚀 특수 상태일 때 '자리비움' 버튼을 '자리 복귀'로 전환
    if (awayBtn) {
        const btnText = awayBtn.querySelector('span');
        if (SPECIAL_STATUSES[normalizedStatus]) {
            if (btnText) btnText.textContent = '자리 복귀';
            awayBtn.classList.add('btn-return');
        } else {
            if (btnText) btnText.textContent = '자리비움';
            awayBtn.classList.remove('btn-return');
        }
    }
}

/**
 * 5. 기타 대시보드 위젯 로드 로직
 */
async function loadNotices() {
    const noticeList = document.getElementById('noticeList');
    if (!noticeList) return;
    try {
        const categoryResponse = await apiFetch('/api/board-categories/accessible');
        if (!categoryResponse.ok) {
            if (categoryResponse.status === 401) { location.href = '/login'; return; }
            noticeList.innerHTML = '<li class="notice-item empty">공지사항 게시판을 찾을 수 없습니다.</li>';
            return;
        }
        const categoryResult = await categoryResponse.json();
        const categories = categoryResult.data || [];
        const noticeCategory = categories.find(cat => (cat.boardType === '공지사항' || cat.boardType === 'NOTICE') && cat.organizationId === null);

        if (!noticeCategory) {
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항 게시판이 없습니다.</li>';
            return;
        }

        const boardResponse = await apiFetch(`/api/boards/categories/${noticeCategory.id}?page=0&size=8&sort=createdAt,desc`);
        if (!boardResponse.ok) throw new Error('공지사항 조회 실패');

        const boardResult = await boardResponse.json();
        const boards = boardResult.data?.content || [];

        if (boards.length === 0) {
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항이 없습니다.</li>';
            return;
        }

        noticeList.innerHTML = boards.map(board => {
            let date = '-';
            if (board.createdAt) { try { date = formatDate(board.createdAt); } catch (e) { date = board.createdAt; } }
            const author = board.writer?.name || '작성자';
            const title = board.boardTitle || board.title || '제목 없음';
            return `<li class="notice-item" onclick="location.href='/view/board/detail?boardId=${board.id}'">
                <div class="notice-title">${escapeHTML(title)}</div>
                <div class="notice-meta"><span class="notice-author">${escapeHTML(author)}</span><span class="notice-date">${date}</span></div>
            </li>`;
        }).join('');
    } catch (error) {
        console.error('공지사항 로드 실패:', error);
        noticeList.innerHTML = '<li class="notice-item empty">공지사항을 불러올 수 없습니다.</li>';
    }
}

async function loadAttendance() {
    try {
        await Promise.all([loadTodayAttendance(), loadMonthlyWorkHours(), loadLeaveBalance(), loadLeaveInfo()]);
    } catch (error) {
        console.error('근태 정보 로드 실패:', error);
    }
}

async function loadTodayAttendance() {
    try {
        const response = await apiFetch('/api/attendance/today');
        if (!response.ok) return;
        const result = await response.json();
        const attendance = result.data;
        const now = new Date();
        const dateStr = now.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' });
        safeSetText('attendanceDate', dateStr);
        if (attendance && attendance.attendance) {
            const statusText = attendance.attendance.status === 'ON_TIME' ? '정상 출근' : '지각';
            safeSetText('attendanceStatus', statusText);
        } else {
            safeSetText('attendanceStatus', '근무 예정');
        }
    } catch (e) { }
}

async function loadMonthlyWorkHours() {
    try {
        const now = new Date();
        const response = await apiFetch(`/api/attendance/history/monthly?year=${now.getFullYear()}&month=${now.getMonth() + 1}`);
        if (response.ok) {
            const result = await response.json();
            safeSetText('totalWorkHours', result.data?.summary?.totalWorkTimeDisplay || '0h 0m');
        }
    } catch (e) { }
}

async function loadLeaveBalance() {
    try {
        const response = await apiFetch(`/api/leave/summary?year=${new Date().getFullYear()}`);
        if (response.ok) {
            const result = await response.json();
            const data = result.data;
            const remaining = (data && data.remainingDays !== undefined) ? parseFloat(data.remainingDays).toFixed(1) + '일' : '0일';
            safeSetText('remainingLeave', remaining);
        }
    } catch (e) { }
}

async function loadLeaveInfo() {
    try {
        const year = new Date().getFullYear();
        const summaryResponse = await apiFetch(`/api/leave/summary?year=${year}`);
        if (summaryResponse.ok) {
            const result = await summaryResponse.json();
            const data = result.data;
            if (data) safeSetText('leaveUsage', `${parseFloat(data.usedDays || 0).toFixed(1)}일 / ${parseFloat(data.totalGranted || 0).toFixed(1)}일`);
        }
    } catch (e) { }
}

async function loadScheduleSummary() {
    const dailyEl = document.getElementById('dailySummary');
    const weeklyEl = document.getElementById('weeklySummary');
    try {
        const response = await apiFetch('/api/schedule/summary');
        if (response.ok) {
            const result = await response.json();
            const { dailySummary, weeklySummary } = result.data;
            if (dailyEl) dailyEl.innerHTML = renderMarkdown(dailySummary || '오늘 일정이 없습니다.');
            if (weeklyEl) weeklyEl.innerHTML = renderMarkdown(weeklySummary || '이번 주 일정이 없습니다.');
        }
    } catch (e) { }
}

async function loadApprovalStats() {
    ['pendingApprovals', 'documentsCreated', 'approvedThisWeek', 'rejected'].forEach(id => { safeSetText(id, '-'); });
}

// --- 유틸리티 및 헬퍼 함수 ---
function safeSetText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function renderMarkdown(text) {
    if (!text) return '';
    return text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>');
}

function formatDate(dateString) {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';
    const diffMs = new Date() - date;
    const diffMins = Math.floor(diffMs / (1000 * 60));
    if (diffMins < 60) return `${diffMins}분 전`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}시간 전`;
    return date.toLocaleDateString();
}

function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}