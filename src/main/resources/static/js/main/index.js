// 대시보드 데이터 로드
document.addEventListener('DOMContentLoaded', async function() {
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
            loadSchedules(),
            loadApprovalStats()
        ]);
    } catch (error) {
        console.error('대시보드 데이터 로드 실패:', error);
    }
}

// 사용자 프로필 로드
async function loadUserProfile() {
    try {
        // 1. 로그인 정보 조회
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
        
        document.getElementById('profileName').textContent = user.name || '-';
        
        // 2. 부서명과 직책명 조회 (관리자 권한이 필요한 API이므로 제거)
        // 일반 사용자는 부서명과 직책명을 조회할 수 있는 별도 API가 필요합니다.
        // 현재는 기본값으로 표시합니다.
        let organizationName = '-';
        let positionName = '';
        
        // 관리자 권한이 있는 경우에만 사원 검색 API 사용
        if (user.role === 'ADMIN' || user.role === 'COMPANY_ADMIN') {
            try {
                if (user.name && user.name.length >= 2) {
                    const searchResponse = await apiFetch(`/api/admin/rules/employees/search?keyword=${encodeURIComponent(user.name)}`);
                    if (searchResponse.ok) {
                        const result = await searchResponse.json();
                        const employees = result.data || result;
                        if (Array.isArray(employees)) {
                            // 본인 employeeId와 일치하는 사원 찾기
                            const currentEmployee = employees.find(emp => emp.id === user.employeeId);
                            if (currentEmployee) {
                                organizationName = currentEmployee.organizationName || '-';
                                positionName = currentEmployee.positionName || '';
                            }
                        }
                    }
                }
            } catch (searchError) {
                // 403 에러 등은 조용히 처리 (콘솔에 표시하지 않음)
                // console.warn('사원 정보 검색 실패, 기본값 사용:', searchError);
            }
        }
        
        document.getElementById('profileDept').textContent = organizationName;
        document.getElementById('profileRank').textContent = positionName ? `(${positionName})` : '';
        
        // 프로필 이미지 설정
        const profileImage = document.getElementById('profileImage');
        if (profileImage) {
            profileImage.src = '/images/male.png';
        }
    } catch (error) {
        console.error('프로필 로드 실패:', error);
        document.getElementById('profileName').textContent = '로드 실패';
        document.getElementById('profileDept').textContent = '-';
        document.getElementById('profileRank').textContent = '';
    }
}

// 공지사항 로드 (전사 공지사항 게시글 목록)
async function loadNotices() {
    const noticeList = document.getElementById('noticeList');
    
    try {
        // 먼저 공지사항 게시판 찾기
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
        
        // 전사 공지사항 게시판 찾기 (boardType이 '공지사항' 또는 'NOTICE'이고 organizationId가 null인 게시판)
        const noticeCategory = categories.find(cat => 
            (cat.boardType === '공지사항' || cat.boardType === 'NOTICE') && 
            cat.organizationId === null
        );
        
        if (!noticeCategory) {
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항 게시판이 없습니다.</li>';
            return;
        }
        
        // 전사 공지사항 게시글 목록 조회 (최근 10개)
        const boardResponse = await apiFetch(
            `/api/boards/categories/${noticeCategory.id}?page=0&size=10&sort=createdAt,desc`
        );
        
        if (!boardResponse.ok) {
            throw new Error('공지사항 조회 실패');
        }
        
        const boardResult = await boardResponse.json();
        const boards = boardResult.data?.content || [];
        
        if (boards.length === 0) {
            noticeList.innerHTML = '<li class="notice-item empty">전사 공지사항이 없습니다.</li>';
            return;
        }
        
        noticeList.innerHTML = boards.map(board => {
            // createdAt이 없거나 유효하지 않은 경우 처리
            let date = '-';
            if (board.createdAt) {
                try {
                    date = formatDate(board.createdAt);
                } catch (error) {
                    console.error('날짜 포맷팅 오류:', error, board.createdAt);
                    // 날짜 파싱 실패 시 원본 값 표시
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
        const timeStr = now.toLocaleTimeString('ko-KR', { 
            hour: '2-digit', 
            minute: '2-digit', 
            second: '2-digit' 
        });
        
        const dateStr = now.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            weekday: 'short'
        });
        
        document.getElementById('attendanceTime').textContent = timeStr;
        document.getElementById('attendanceDate').textContent = dateStr;
        
        if (attendance.attendance) {
            const commuteAt = new Date(attendance.attendance.commuteAt);
            const workTimeMs = now - commuteAt;
            const workHours = Math.floor(workTimeMs / (1000 * 60 * 60));
            const workMinutes = Math.floor((workTimeMs % (1000 * 60 * 60)) / (1000 * 60));
            
            const statusText = attendance.attendance.status === 'ON_TIME' ? '정상 출근' : '지각';
            document.getElementById('attendanceStatus').textContent = statusText;
            
            // 근무 시간 표시 (예: 2h 50m)
            const workTimeText = `${workHours}h ${workMinutes}m`;
            document.getElementById('workTimeText').textContent = workTimeText;
            
            // 진행률 계산 (최소 40h 기준)
            const progress = Math.min((workHours / 40) * 100, 100);
            document.getElementById('progressFill').style.width = `${progress}%`;
        } else {
            document.getElementById('attendanceStatus').textContent = '근무 예정';
            document.getElementById('workTimeText').textContent = '0h';
            document.getElementById('progressFill').style.width = '0%';
        }
    } catch (error) {
        console.error('근태 정보 로드 실패:', error);
        document.getElementById('attendanceTime').textContent = '-';
        document.getElementById('attendanceDate').textContent = '-';
        document.getElementById('attendanceStatus').textContent = '조회 실패';
    }
}

// 금주 일정 로드
async function loadSchedules() {
    const scheduleList = document.getElementById('scheduleList');
    
    try {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
        
        // 금주 일정 조회 (isWeekly=true)
        const response = await apiFetch(
            `/api/schedules/personal?year=${year}&month=${month}&isWeekly=true`
        );
        
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('일정 조회 실패');
        }
        
        const result = await response.json();
        const schedules = result.data || [];
        
        // 최대 5개만 표시
        const displaySchedules = schedules.slice(0, 5);
        
        if (displaySchedules.length === 0) {
            scheduleList.innerHTML = '<li class="schedule-item empty">금주 일정이 없습니다.</li>';
            return;
        }
        
        scheduleList.innerHTML = displaySchedules.map(schedule => {
            const startDate = new Date(schedule.startAt);
            const dateStr = startDate.toLocaleDateString('ko-KR', {
                month: 'short',
                day: 'numeric'
            });
            const timeStr = startDate.toLocaleTimeString('ko-KR', {
                hour: '2-digit',
                minute: '2-digit'
            });
            
            return `
                <li class="schedule-item">
                    <div class="schedule-title">${escapeHTML(schedule.title)}</div>
                    <div class="schedule-date">${dateStr} ${timeStr}</div>
                </li>
            `;
        }).join('');
    } catch (error) {
        console.error('일정 로드 실패:', error);
        scheduleList.innerHTML = '<li class="schedule-item empty">일정을 불러올 수 없습니다.</li>';
    }
}

// 결재 통계 로드
async function loadApprovalStats() {
    try {
        // 결재 통계 API가 없으므로 임시로 기본값 표시
        // TODO: 결재 통계 API 구현 후 연동
        document.getElementById('pendingApprovals').textContent = '-';
        document.getElementById('documentsCreated').textContent = '-';
        document.getElementById('approvedThisWeek').textContent = '-';
        document.getElementById('rejected').textContent = '-';
        
        document.getElementById('pendingChange').textContent = '';
        document.getElementById('createdChange').textContent = '';
        document.getElementById('approvedChange').textContent = '';
        document.getElementById('rejectedChange').textContent = '';
    } catch (error) {
        console.error('결재 통계 로드 실패:', error);
    }
}


// 날짜 포맷팅 (시간 전 형식으로만 표시)
function formatDate(dateString) {
    if (!dateString) return '-';
    
    try {
        // Instant 형식 (ISO 8601) 또는 일반 날짜 문자열 처리
        const date = new Date(dateString);
        
        // 유효하지 않은 날짜인 경우
        if (isNaN(date.getTime())) {
            console.warn('유효하지 않은 날짜:', dateString);
            return '-';
        }
        
        const now = new Date();
        const diffMs = now - date;
        
        // 미래 날짜인 경우 "방금 전"으로 표시
        if (diffMs < 0) {
            return '방금 전';
        }
        
        const diffMins = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        const diffWeeks = Math.floor(diffDays / 7);
        const diffMonths = Math.floor(diffDays / 30);
        const diffYears = Math.floor(diffDays / 365);
        
        if (diffMins < 1) {
            return '방금 전';
        } else if (diffMins < 60) {
            return `${diffMins}분 전`;
        } else if (diffHours < 24) {
            return `${diffHours}시간 전`;
        } else if (diffDays < 7) {
            return `${diffDays}일 전`;
        } else if (diffWeeks < 4) {
            return `${diffWeeks}주 전`;
        } else if (diffMonths < 12) {
            return `${diffMonths}개월 전`;
        } else {
            return `${diffYears}년 전`;
        }
    } catch (error) {
        console.error('날짜 포맷팅 오류:', error, dateString);
        return '-';
    }
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

