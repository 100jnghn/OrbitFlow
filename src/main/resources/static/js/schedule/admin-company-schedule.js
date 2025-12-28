/**
 * 전사 일정 관리 페이지 JavaScript
 */

// API 엔드포인트
const API_BASE_URL = '/api/schedules/company';

// 현재 상태
let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth() + 1; // 1-12
let currentStatus = 'ALL';
let schedules = [];

// 상태별 색상 매핑
const statusColorMap = {
    'RELEASE': 'release',
    'HOLD': 'hold',
};

// 상태별 한글명 매핑
const statusNameMap = {
    'RELEASE': '공개',
    'HOLD': '보류',
};

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    loadSchedules();
});

/**
 * 페이지 초기화
 */
function initializePage() {
    const now = new Date();
    currentYear = now.getFullYear();
    currentMonth = now.getMonth() + 1;
    updateMonthDisplay();
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 상태 필터 변경
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            currentStatus = this.value;
            loadSchedules();
        });
    }

    // 이전 달 버튼
    const prevMonthBtn = document.getElementById('prevMonth');
    if (prevMonthBtn) {
        prevMonthBtn.addEventListener('click', function() {
            navigateMonth(-1);
        });
    }

    // 다음 달 버튼
    const nextMonthBtn = document.getElementById('nextMonth');
    if (nextMonthBtn) {
        nextMonthBtn.addEventListener('click', function() {
            navigateMonth(1);
        });
    }

    // 오늘 버튼
    const btnToday = document.getElementById('btnToday');
    if (btnToday) {
        btnToday.addEventListener('click', function() {
            goToToday();
        });
    }

    // 일정 등록 버튼
    const btnAddSchedule = document.getElementById('btnAddSchedule');
    if (btnAddSchedule) {
        btnAddSchedule.addEventListener('click', function() {
            openAddScheduleModal();
        });
    }
}

/**
 * 월 네비게이션
 */
function navigateMonth(direction) {
    currentMonth += direction;
    
    if (currentMonth > 12) {
        currentMonth = 1;
        currentYear++;
    } else if (currentMonth < 1) {
        currentMonth = 12;
        currentYear--;
    }
    
    updateMonthDisplay();
    loadSchedules();
}

/**
 * 오늘로 이동
 */
function goToToday() {
    const now = new Date();
    currentYear = now.getFullYear();
    currentMonth = now.getMonth() + 1;
    updateMonthDisplay();
    loadSchedules();
}

/**
 * 월 표시 업데이트
 */
function updateMonthDisplay() {
    const monthDisplay = document.getElementById('currentMonthDisplay');
    if (monthDisplay) {
        monthDisplay.textContent = `${currentYear}년 ${currentMonth}월`;
    }
}

/**
 * 일정 로드
 */
async function loadSchedules() {
    try {
        showLoading();
        
        const url = `${API_BASE_URL}?status=${currentStatus}&year=${currentYear}&month=${currentMonth}&isWeekly=false`;
        
        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('일정을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        
        // ResponseDto 구조에서 data 추출
        schedules = result.data || [];
        
        if (!Array.isArray(schedules)) {
            schedules = [];
        }

        renderCalendar();
        renderScheduleList();
        
    } catch (error) {
        console.error('Error loading schedules:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('일정을 불러오는데 실패했습니다.');
        }
        hideLoading();
    }
}

/**
 * 캘린더 렌더링
 */
function renderCalendar() {
    const calendarGrid = document.getElementById('calendarGrid');
    if (!calendarGrid) return;

    calendarGrid.innerHTML = '';

    // 해당 월의 첫 날과 마지막 날 계산
    const firstDay = new Date(currentYear, currentMonth - 1, 1);
    const lastDay = new Date(currentYear, currentMonth, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = firstDay.getDay(); // 0(일) ~ 6(토)

    // 이전 달의 마지막 날들
    const prevMonthLastDay = new Date(currentYear, currentMonth - 1, 0).getDate();
    
    // 오늘 날짜
    const today = new Date();
    const isCurrentMonth = today.getFullYear() === currentYear && today.getMonth() + 1 === currentMonth;

    // 캘린더 그리드 생성
    for (let i = 0; i < 42; i++) { // 6주 * 7일 = 42
        const dayCell = document.createElement('div');
        dayCell.className = 'calendar-day';

        let dayNumber;
        let date;
        let isOtherMonth = false;

        if (i < startDayOfWeek) {
            // 이전 달
            dayNumber = prevMonthLastDay - (startDayOfWeek - i - 1);
            date = new Date(currentYear, currentMonth - 2, dayNumber);
            isOtherMonth = true;
        } else if (i < startDayOfWeek + daysInMonth) {
            // 현재 달
            dayNumber = i - startDayOfWeek + 1;
            date = new Date(currentYear, currentMonth - 1, dayNumber);
        } else {
            // 다음 달
            dayNumber = i - startDayOfWeek - daysInMonth + 1;
            date = new Date(currentYear, currentMonth, dayNumber);
            isOtherMonth = true;
        }

        // 오늘 날짜 체크
        if (isCurrentMonth && dayNumber === today.getDate() && !isOtherMonth) {
            dayCell.classList.add('today');
        }

        // 주말 체크
        if (date.getDay() === 0 || date.getDay() === 6) {
            dayCell.classList.add('weekend');
        }

        if (isOtherMonth) {
            dayCell.classList.add('other-month');
        }

        // 날짜 번호
        const dayNumberEl = document.createElement('div');
        dayNumberEl.className = 'day-number';
        dayNumberEl.textContent = dayNumber;
        dayCell.appendChild(dayNumberEl);

        // 해당 날짜의 일정 표시
        const daySchedules = getSchedulesForDate(date);
        if (daySchedules.length > 0) {
            daySchedules.forEach(schedule => {
                const scheduleItem = createScheduleItem(schedule, date);
                dayCell.appendChild(scheduleItem);
            });
        }

        calendarGrid.appendChild(dayCell);
    }

    hideLoading();
}

/**
 * 특정 날짜의 일정 가져오기
 */
function getSchedulesForDate(date) {
    if (!schedules || schedules.length === 0) return [];

    const dateStr = formatDateForComparison(date);
    
    return schedules.filter(schedule => {
        const startAt = new Date(schedule.startAt);
        const endAt = new Date(schedule.endAt);
        
        // 시작일과 종료일을 날짜만 비교
        const scheduleStartDate = formatDateForComparison(startAt);
        const scheduleEndDate = formatDateForComparison(endAt);
        
        // 해당 날짜가 일정 기간 내에 있는지 확인
        return dateStr >= scheduleStartDate && dateStr <= scheduleEndDate;
    });
}

/**
 * 날짜를 YYYY-MM-DD 형식으로 변환 (비교용)
 */
function formatDateForComparison(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * 일정 아이템 생성
 */
function createScheduleItem(schedule, date) {
    const scheduleItem = document.createElement('div');
    scheduleItem.className = 'schedule-item';
    
    const status = schedule.status || 'ETC';
    const statusClass = statusColorMap[status] || 'etc';
    scheduleItem.classList.add(statusClass);

    // 다중 날짜 일정 체크
    const startAt = new Date(schedule.startAt);
    const endAt = new Date(schedule.endAt);
    const currentDate = formatDateForComparison(date);
    const scheduleStartDate = formatDateForComparison(startAt);
    const scheduleEndDate = formatDateForComparison(endAt);
    
    if (currentDate !== scheduleStartDate) {
        scheduleItem.classList.add('multi-day');
    }

    scheduleItem.textContent = schedule.title || '제목 없음';
    scheduleItem.title = `${schedule.title || '제목 없음'}\n${formatDateTime(schedule.startAt)} ~ ${formatDateTime(schedule.endAt)}`;

    // 클릭 이벤트 (상세 보기 등)
    scheduleItem.addEventListener('click', function() {
        showScheduleDetail(schedule);
    });

    return scheduleItem;
}

/**
 * 일정 목록 렌더링
 */
function renderScheduleList() {
    const scheduleList = document.getElementById('scheduleList');
    if (!scheduleList) return;

    scheduleList.innerHTML = '';

    if (!schedules || schedules.length === 0) {
        scheduleList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-calendar-times"></i>
                <p>등록된 일정이 없습니다.</p>
            </div>
        `;
        return;
    }

    // 일정을 시작일 기준으로 정렬
    const sortedSchedules = [...schedules].sort((a, b) => {
        return new Date(a.startAt) - new Date(b.startAt);
    });

    sortedSchedules.forEach(schedule => {
        const listItem = createScheduleListItem(schedule);
        scheduleList.appendChild(listItem);
    });
}

/**
 * 일정 목록 아이템 생성
 */
function createScheduleListItem(schedule) {
    const item = document.createElement('div');
    item.className = 'schedule-list-item';

    const status = schedule.status || 'ETC';
    const statusClass = statusColorMap[status] || 'etc';
    const statusName = statusNameMap[status] || '기타';

    item.innerHTML = `
        <div class="schedule-status-badge ${statusClass}">
            ${statusName}
        </div>
        <div class="schedule-info">
            <div class="schedule-title">${escapeHTML(schedule.title || '제목 없음')}</div>
            ${schedule.description ? `<div class="schedule-description">${escapeHTML(schedule.description)}</div>` : ''}
            <div class="schedule-time">
                <i class="fas fa-clock"></i>
                <span>${formatDateTime(schedule.startAt)} ~ ${formatDateTime(schedule.endAt)}</span>
            </div>
        </div>
    `;

    item.addEventListener('click', function() {
        showScheduleDetail(schedule);
    });

    return item;
}

/**
 * 일정 상세 보기 (추후 모달로 구현 가능)
 */
function showScheduleDetail(schedule) {
    const status = schedule.status || 'ETC';
    const statusName = statusNameMap[status] || '기타';
    
    const detail = `
제목: ${schedule.title || '제목 없음'}
설명: ${schedule.description || '설명 없음'}
상태: ${statusName}
시작: ${formatDateTime(schedule.startAt)}
종료: ${formatDateTime(schedule.endAt)}
    `.trim();

    alert(detail);
}

/**
 * 날짜/시간 포맷팅
 */
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    
    const date = new Date(dateTimeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

/**
 * HTML 이스케이프
 */
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

/**
 * 로딩 표시
 */
function showLoading() {
    const calendarGrid = document.getElementById('calendarGrid');
    if (calendarGrid) {
        calendarGrid.innerHTML = `
            <div class="loading" style="grid-column: 1 / -1;">
                <i class="fas fa-spinner"></i>
                <p>일정을 불러오는 중...</p>
            </div>
        `;
    }
}

/**
 * 로딩 숨기기
 */
function hideLoading() {
    // renderCalendar에서 처리됨
}

/**
 * 일정 등록 모달 열기
 */
function openAddScheduleModal() {
    // TODO: 일정 등록 모달 구현
    alert('일정 등록 기능은 추후 구현 예정입니다.');
}

