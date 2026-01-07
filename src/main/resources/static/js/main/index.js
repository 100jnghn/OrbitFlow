// 대시보드 데이터 로드
document.addEventListener('DOMContentLoaded', async function () {
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

// 안전하게 텍스트를 설정하는 헬퍼 함수
function safeSetText(id, text) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = text;
    }
}

// 사용자 프로필 로드
async function loadUserProfile() {
    try {
        const meResponse = await apiFetch('/api/auth/me');
        if (!meResponse.ok) {
            if (meResponse.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('프로필 조회 실패');
        }

        const meResult = await meResponse.json();
        const user = meResult.data;

        safeSetText('profileName', user.name || '-');

        let organizationName = '-';
        let positionName = '';

        // 관리자 권한이 있는 경우에만 사원 검색 API 사용
        if (user.role === 'ADMIN' || user.role === 'COMPANY_ADMIN') {
            try {
                if (user.name && user.name.length >= 2) {
                    const searchResponse = await apiFetch(`/api/rules/employees/search?keyword=${encodeURIComponent(user.name)}`);
                    // 403 에러 발생 시를 대비해 응답 상태 확인
                    if (searchResponse.ok) {
                        const result = await searchResponse.json();
                        const employees = result.data || result;
                        if (Array.isArray(employees)) {
                            const currentEmployee = employees.find(emp => emp.id === user.employeeId);
                            if (currentEmployee) {
                                organizationName = currentEmployee.organizationName || '-';
                                positionName = currentEmployee.positionName || '';
                            }
                        }
                    }
                }
            } catch (searchError) {
                // API 권한 문제(403) 등은 로그에 남기지 않고 기본값 유지
            }
        }

        safeSetText('profileDept', organizationName);
        safeSetText('profileRank', positionName ? `(${positionName})` : '');

        const profileImage = document.getElementById('profileImage');
        if (profileImage) {
            profileImage.src = '/images/male.png';
        }
    } catch (error) {
        console.error('프로필 로드 실패:', error);
        safeSetText('profileName', '로드 실패');
        safeSetText('profileDept', '-');
        safeSetText('profileRank', '');
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
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항 게시판이 없습니다.</li>';
            return;
        }

        const boardResponse = await apiFetch(
            `/api/boards/categories/${noticeCategory.id}?page=0&size=5&sort=createdAt,desc`
        );

        if (!boardResponse.ok) throw new Error('공지사항 조회 실패');

        const boardResult = await boardResponse.json();
        const boards = boardResult.data?.content || [];

        if (boards.length === 0) {
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항이 없습니다.</li>';
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
                <li class="notice-item" onclick="location.href='/view/board/detail?boardId=${board.id}'">
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