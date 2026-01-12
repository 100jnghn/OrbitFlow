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

// 대시보드 데이터 로드
async function loadDashboardData() {
    try {
        // 병렬로 모든 데이터 로드
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
 * 3. 사용자 프로필 로드 (이름, 부서, 직급, 배지 포함)
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

        // 1. 사용자 이름 표시 (id="profileName")
        const nameEl = document.getElementById('profileName');
        if (nameEl) {
            // 기존 텍스트('-')를 사용자 이름으로 교체
            nameEl.textContent = user.name || '사용자';
        }

        // 2. 부서 및 직급 표시
        // 직급이 있을 경우에만 괄호를 붙이거나 스타일을 맞춤
        safeSetText('profileRank', user.positionName || '');

        // 3. 근무 상태 배지 업데이트
        // 이름 옆에 배지를 생성하거나 업데이트하는 로직 호출
        const workStatus = user.workStatus || 'DEFAULT';
        window.currentServerStatus = workStatus;

        if (typeof updateNameAdjacentBadge === 'function') {
            updateNameAdjacentBadge(workStatus);
        }

        // 4. 프로필 이미지 처리
        const profileImgEl = document.getElementById('profileImage');
        const profileIconEl = document.getElementById('profileIcon');

        if (user.profileImageUrl) {
            if (profileImgEl) {
                profileImgEl.src = user.profileImageUrl;
                profileImgEl.style.display = 'block';
            }
            if (profileIconEl) profileIconEl.style.display = 'none';
        } else {
            // 이미지가 없을 때 성별에 따른 기본 이미지 표시
            if (profileImgEl) {
                const gender = user.gender || 'MALE'; // 기본값 남성
                const defaultImg = gender === 'FEMALE' ? '/images/female.png' : '/images/male.png';

                profileImgEl.src = defaultImg;
                profileImgEl.style.display = 'block';
            }
            if (profileIconEl) profileIconEl.style.display = 'none';
        }

        // 5. 버튼 상태 동기화 (Commute.js 등 연동)
        setTimeout(() => {
            if (typeof window.updateCommuteButtonStates === 'function') {
                window.updateCommuteButtonStates();
            }
        }, 100);

    } catch (error) {
        console.error('프로필 로드 중 오류 발생:', error);
        // 에러 발생 시 기본값 표시
        safeSetText('profileName', '사용자');
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


// 공지사항 로드
async function loadNotices() {
    const noticeList = document.getElementById('noticeList');
    if (!noticeList) return;

    try {
        const categoryResponse = await apiFetch('/api/board-categories/accessible');
        if (!categoryResponse.ok) {
            if (categoryResponse.status === 401) {
                location.href = '/login';
                return;
            }
            noticeList.innerHTML = '<li class="notice-item empty">공지사항 게시판을 찾을 수 없습니다.</li>';
            return;
        }

        const categoryResult = await categoryResponse.json();
        const categories = categoryResult.data || [];

        const noticeCategory = categories.find(cat =>
            (cat.boardType === '공지사항' || cat.boardType === 'NOTICE') &&
            cat.organizationId === null
        );

        if (!noticeCategory) {
            noticeList.innerHTML = '<li class="notice-item empty">공지사항 게시글이 없습니다.</li>';
            return;
        }

        const boardResponse = await apiFetch(
            `/api/board-posts/categories/${noticeCategory.id}?page=0&size=5&sort=createdAt,desc`
        );

        if (!boardResponse.ok) throw new Error('공지사항 조회 실패');

        const boardResult = await boardResponse.json();
        const boards = boardResult.data?.content || [];

        if (boards.length === 0) {
            noticeList.innerHTML = '<li class="notice-item empty">공지사항 게시글이 없습니다.</li>';
            return;
        }

        noticeList.innerHTML = boards.map(board => {
            let date = '-';
            if (board.createdAt) {
                try {
                    date = formatDate(board.createdAt);
                } catch (e) {
                    date = board.createdAt;
                }
            }
            const author = board.writer?.name || '작성자';
            const title = board.boardTitle || board.title || '제목 없음';
            return `
                <li class="notice-item" onclick="location.href='/view/board/detail?boardId=${board.id}&categoryId=${noticeCategory.id}'">
                    <div class="notice-title">${escapeHTML(title)}</div>
                    <div class="notice-meta">
                        <span class="notice-author">${escapeHTML(author)}</span>
                        <span class="notice-date">${date}</span>
                    </div>
                </li>
            `;
        }).join('');
    } catch (error) {
        console.error('공지사항 로드 실패:', error);
        noticeList.innerHTML = '<li class="notice-item empty">공지사항을 불러올 수 없습니다.</li>';
    }
}
// 근태 정보 로드
async function loadAttendance() {
    try {
        await Promise.all([
            loadTodayAttendance(),
            loadMonthlyWorkHours(),
            loadLeaveBalance(),
            loadLeaveInfo()
        ]);
    } catch (error) {
        console.error('근태 정보 로드 실패:', error);
        safeSetText('attendanceDate', '-');
        safeSetText('attendanceStatus', '조회 실패');
    }
}

// 오늘의 근태 정보 로드
async function loadTodayAttendance() {
    try {
        const response = await apiFetch('/api/attendance/today');
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('근태 정보 조회 실패');
        }

        const result = await response.json();
        const attendance = result.data;

        const now = new Date();
        const dateStr = now.toLocaleDateString('ko-KR', {
            year: 'numeric', month: 'long', day: 'numeric', weekday: 'short'
        });

        safeSetText('attendanceDate', dateStr);

        // attendance.attendance가 존재하는지 안전하게 확인
        if (attendance && attendance.attendance) {
            const statusText = attendance.attendance.status === 'ON_TIME' ? '정상 출근' : '지각';
            safeSetText('attendanceStatus', statusText);
        } else {
            safeSetText('attendanceStatus', '근무 예정');
        }
    } catch (error) {
        console.error('오늘 근태 정보 로드 실패:', error);
    }
}

// 월별 총 근무시간 로드
async function loadMonthlyWorkHours() {
    try {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;

        const response = await apiFetch(`/api/attendance/history/monthly?year=${year}&month=${month}`);
        if (response.ok) {
            const result = await response.json();
            const data = result.data;
            const display = data?.summary?.totalWorkTimeDisplay || '0h 0m';
            safeSetText('totalWorkHours', display);
        }
    } catch (error) {
        console.error('월별 근무시간 로드 실패:', error);
        safeSetText('totalWorkHours', '-');
    }
}

// 연차 현황 로드
async function loadLeaveBalance() {
    try {
        const now = new Date();
        const year = now.getFullYear();
        const response = await apiFetch(`/api/leave/summary?year=${year}`);
        if (response.ok) {
            const result = await response.json();
            const data = result.data;
            const remaining = (data && data.remainingDays !== undefined) ? parseFloat(data.remainingDays).toFixed(1) + '일' : '0일';
            safeSetText('remainingLeave', remaining);
        }
    } catch (error) {
        console.error('연차 현황 로드 실패:', error);
        safeSetText('remainingLeave', '-');
    }
}




// 휴가 관련 정보 로드
async function loadLeaveInfo() {
    try {
        const now = new Date();
        const year = now.getFullYear();

        const summaryResponse = await apiFetch(`/api/leave/summary?year=${year}`);
        if (summaryResponse.ok) {
            const summaryResult = await summaryResponse.json();
            const summaryData = summaryResult.data;
            if (summaryData) {
                const total = parseFloat(summaryData.totalGranted || 0).toFixed(1);
                const used = parseFloat(summaryData.usedDays || 0).toFixed(1);
                safeSetText('leaveUsage', `${used}일 / ${total}일`);
            }
        }

        const historyResponse = await apiFetch(`/api/leave/history?page=0&size=50`);
        if (historyResponse.ok) {
            const historyResult = await historyResponse.json();
            const content = historyResult.data?.content || [];
            const pendingCount = content.filter(l => l.statusCode === 'SUBMITTED' || l.statusCode === 'IN_PROGRESS').length;
            safeSetText('pendingLeaveCount', pendingCount > 0 ? `${pendingCount}건` : '없음');
        }

        const approvedResponse = await apiFetch(`/api/leave/history?status=APPROVED&page=0&size=50`);
        if (approvedResponse.ok) {
            const approvedResult = await approvedResponse.json();
            const content = approvedResult.data?.content || [];

            let nextLeaveDate = null;
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            content.forEach(leave => {
                const dateStr = leave.period ? leave.period.split(' ~ ')[0] : leave.actionDate;
                if (dateStr) {
                    const startDate = new Date(dateStr);
                    startDate.setHours(0, 0, 0, 0);
                    if (startDate >= today && (!nextLeaveDate || startDate < nextLeaveDate)) {
                        nextLeaveDate = startDate;
                    }
                }
            });

            const nextStr = nextLeaveDate ? nextLeaveDate.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }) : '없음';
            safeSetText('nextLeaveDate', nextStr);
        }
    } catch (error) {
        console.error('휴가 정보 로드 실패:', error);
    }
}


// 일정 요약 로드
async function loadScheduleSummary() {
    const dailySummaryEl = document.getElementById('dailySummary');
    const weeklySummaryEl = document.getElementById('weeklySummary');

    if (dailySummaryEl) dailySummaryEl.textContent = 'AI가 오늘의 일정을 요약 중입니다...';
    if (weeklySummaryEl) weeklySummaryEl.textContent = 'AI가 주간 일정을 요약 중입니다...';

    try {
        const response = await apiFetch('/api/schedule/summary');
        if (response.ok) {
            const result = await response.json();
            const { dailySummary, weeklySummary } = result.data;
            if (dailySummaryEl) dailySummaryEl.innerHTML = renderMarkdown(dailySummary || '오늘 일정이 없습니다.');
            if (weeklySummaryEl) weeklySummaryEl.innerHTML = renderMarkdown(weeklySummary || '이번 주 일정이 없습니다.');
        }
    } catch (error) {
        console.error('일정 요약 로드 실패:', error);
    }
}

function renderMarkdown(text) {
    if (!text) return '';
    return text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>');
}

// 결재 통계 로드
async function loadApprovalStats() {
    // 요소가 있는 경우에만 기본값 설정
    ['pendingApprovals', 'documentsCreated', 'approvedThisWeek', 'rejected'].forEach(id => {
        safeSetText(id, '-');
    });
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';

    const diffMs = new Date() - date;
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;
    return date.toLocaleDateString();
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

/**
 * 요소 안전 텍스트 설정 유틸리티
 */
function safeSetText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}