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
let isEditMode = false;
let editingScheduleId = null;
let holidayMap = new Map(); // key: yyyy-MM-dd, value: CalendarDayResDto

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
document.addEventListener('DOMContentLoaded', function () {
    initializePage();
    setupEventListeners();
    loadHolidays(currentYear);
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
        statusFilter.addEventListener('change', function () {
            currentStatus = this.value;
            loadSchedules();
        });
    }

    // 이전 달 버튼
    const prevMonthBtn = document.getElementById('prevMonth');
    if (prevMonthBtn) {
        prevMonthBtn.addEventListener('click', function () {
            navigateMonth(-1);
        });
    }

    // 다음 달 버튼
    const nextMonthBtn = document.getElementById('nextMonth');
    if (nextMonthBtn) {
        nextMonthBtn.addEventListener('click', function () {
            navigateMonth(1);
        });
    }

    // 오늘 버튼
    const btnToday = document.getElementById('btnToday');
    if (btnToday) {
        btnToday.addEventListener('click', function () {
            goToToday();
        });
    }

    // 일정 등록 버튼
    const btnAddSchedule = document.getElementById('btnAddSchedule');
    if (btnAddSchedule) {
        btnAddSchedule.addEventListener('click', function () {
            openAddScheduleModal();
        });
    }

    // 시작 날짜 변경 시 종료 날짜 min 설정 (모달이 열렸을 때 동적으로 추가)
    // 모달이 열릴 때 이벤트 리스너를 추가하도록 openAddScheduleModal과 openEditScheduleModal에서 처리
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
 * 휴일 + 주말 정보 로드 (연 단위 1회)
 */
async function loadHolidays(year) {
    try {
        const response = await apiFetch(`/api/calendar/holidays?year=${year}`);

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('휴일 정보를 불러오지 못했습니다.');
        }

        const result = await response.json();
        const list = result.data || [];

        holidayMap.clear();
        list.forEach(day => {
            holidayMap.set(day.date, day);
        });

        console.log(`휴일 ${list.length}건 로드 완료`);
    } catch (error) {
        console.error('휴일 로드 실패:', error);
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

        // 휴일 정보 가져오기
        const yyyyMMdd = formatDateForComparison(date);
        const holiday = holidayMap.get(yyyyMMdd);

        if (holiday) {
            console.log('휴일 매칭:', yyyyMMdd, holiday.dayType, holiday.holidayName);
        }

        // JS 기준 요일
        const jsDay = date.getDay(); // 0=일, 6=토

        // 일요일
        if (jsDay === 0) {
            dayCell.classList.add('sunday');
        }

        // 토요일
        if (jsDay === 6) {
            dayCell.classList.add('saturday');
        }

        // 공휴일 (주말보다 우선)
        if (holiday && holiday.dayType !== 'WORKDAY') {
            dayCell.classList.add('holiday');
        }

        // 오늘 날짜 체크
        if (isCurrentMonth && dayNumber === today.getDate() && !isOtherMonth) {
            dayCell.classList.add('today');
        }

        // 주말 체크 (기존 코드 유지)
        if (date.getDay() === 0 || date.getDay() === 6) {
            dayCell.classList.add('weekend');
        }

        if (isOtherMonth) {
            dayCell.classList.add('other-month');
        }

        /* =========================
           날짜 상단: 날짜 + 공휴일명
        ========================== */
        const dayHeaderRow = document.createElement('div');
        dayHeaderRow.className = 'day-header-row';

        const dayNumberEl = document.createElement('div');
        dayNumberEl.className = 'day-number';
        dayNumberEl.textContent = dayNumber;
        dayHeaderRow.appendChild(dayNumberEl);

        // 공휴일이면 holidayName 오른쪽에 표시
        if (holiday && holiday.holidayName) {
            const holidayNameEl = document.createElement('div');
            holidayNameEl.className = 'holiday-name';
            holidayNameEl.textContent = holiday.holidayName;
            holidayNameEl.title = holiday.holidayName;
            dayHeaderRow.appendChild(holidayNameEl);
        }

        dayCell.appendChild(dayHeaderRow);

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

    // 클릭 이벤트 (수정 모달 열기)
    scheduleItem.addEventListener('click', function (e) {
        e.stopPropagation();
        openEditScheduleModal(schedule);
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
        <button type="button" class="btn-delete-schedule" onclick="deleteSchedule(${schedule.scheduleId}, event)">
            <i class="fas fa-trash"></i>
        </button>
    `;

    item.addEventListener('click', function (e) {
        // 삭제 버튼 클릭이 아닐 때만 수정 모달 열기
        if (!e.target.closest('.btn-delete-schedule')) {
            openEditScheduleModal(schedule);
        }
    });

    return item;
}

/**
 * 일정 수정 모달 열기
 */
async function openEditScheduleModal(schedule) {
    if (!schedule || !schedule.scheduleId) {
        console.error('Invalid schedule data');
        return;
    }

    const modal = document.getElementById('scheduleModal');
    if (!modal) return;

    // 수정 모드 설정
    isEditMode = true;
    editingScheduleId = schedule.scheduleId;

    // 모달 제목 및 버튼 변경
    document.getElementById('modalTitle').textContent = '일정 수정';
    document.getElementById('submitBtn').textContent = '수정';

    // 시간/분 select 옵션 생성
    initializeTimeSelects();

    // 일정 정보를 폼에 채우기
    document.getElementById('scheduleTitle').value = schedule.title || '';
    document.getElementById('scheduleDescription').value = schedule.description || '';
    document.getElementById('scheduleStatus').value = schedule.status || 'RELEASE';

    // 날짜/시간 파싱 및 설정
    const startDate = new Date(schedule.startAt);
    const endDate = new Date(schedule.endAt);

    const startDateInput = document.getElementById('scheduleStartDate');
    const endDateInput = document.getElementById('scheduleEndDate');
    const startDateStr = formatDateForInput(startDate);
    const endDateStr = formatDateForInput(endDate);

    startDateInput.value = startDateStr;
    endDateInput.value = endDateStr;
    // 종료 날짜의 min을 시작 날짜로 설정
    endDateInput.min = startDateStr;

    // 시작 날짜 변경 시 종료 날짜 min 설정
    startDateInput.removeEventListener('change', handleStartDateChange);
    startDateInput.addEventListener('change', handleStartDateChange);

    // 시간/분 추출 (10분 단위로 반올림)
    const startHour = String(startDate.getHours()).padStart(2, '0');
    const startMinute = roundToNearestTen(startDate.getMinutes());
    const endHour = String(endDate.getHours()).padStart(2, '0');
    const endMinute = roundToNearestTen(endDate.getMinutes());

    document.getElementById('scheduleStartHour').value = startHour;
    document.getElementById('scheduleStartMinute').value = startMinute;
    document.getElementById('scheduleEndHour').value = endHour;
    document.getElementById('scheduleEndMinute').value = endMinute;

    // 조직 카테고리를 '회사'로 고정
    await setCompanyOrgCategory();

    // 설명 필드 글자 수 업데이트
    updateDescriptionCharCount();

    // 제목 필드 글자 수 업데이트
    updateTitleCharCount();

    modal.style.display = 'block';

    // 폼 제출 이벤트 리스너
    const form = document.getElementById('scheduleForm');
    form.onsubmit = handleScheduleSubmit;

    // 설명 필드 실시간 글자 수 업데이트
    const descriptionField = document.getElementById('scheduleDescription');
    descriptionField.addEventListener('input', updateDescriptionCharCount);

    // 제목 필드 실시간 글자 수 업데이트
    const titleField = document.getElementById('scheduleTitle');
    titleField.addEventListener('input', updateTitleCharCount);
}

/**
 * 분을 10분 단위로 반올림
 */
function roundToNearestTen(minute) {
    return String(Math.round(minute / 10) * 10).padStart(2, '0');
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
 * 시작 날짜 변경 핸들러
 */
function handleStartDateChange() {
    const startDateInput = document.getElementById('scheduleStartDate');
    const endDateInput = document.getElementById('scheduleEndDate');

    if (startDateInput && endDateInput && startDateInput.value) {
        endDateInput.min = startDateInput.value;
        // 종료 날짜가 시작 날짜보다 이전이면 시작 날짜로 설정
        if (endDateInput.value && endDateInput.value < startDateInput.value) {
            endDateInput.value = startDateInput.value;
        }
    }
}

/**
 * 일정 등록 모달 열기
 */
async function openAddScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    if (!modal) return;

    // 등록 모드 설정
    isEditMode = false;
    editingScheduleId = null;

    // 모달 제목 및 버튼 변경
    document.getElementById('modalTitle').textContent = '일정 등록';
    document.getElementById('submitBtn').textContent = '등록';

    // 폼 초기화
    document.getElementById('scheduleForm').reset();

    // 시간/분 select 옵션 생성
    initializeTimeSelects();

    // 오늘 날짜로 기본값 설정
    const today = new Date();
    const todayStr = formatDateForInput(today);
    const startDateInput = document.getElementById('scheduleStartDate');
    const endDateInput = document.getElementById('scheduleEndDate');
    startDateInput.value = todayStr;
    endDateInput.value = todayStr;
    // 종료 날짜의 min을 시작 날짜로 설정
    endDateInput.min = todayStr;

    // 시작 날짜 변경 시 종료 날짜 min 설정
    startDateInput.removeEventListener('change', handleStartDateChange);
    startDateInput.addEventListener('change', handleStartDateChange);

    // 기본 시간 설정 (09:00, 18:00)
    document.getElementById('scheduleStartHour').value = '09';
    document.getElementById('scheduleStartMinute').value = '00';
    document.getElementById('scheduleEndHour').value = '18';
    document.getElementById('scheduleEndMinute').value = '00';

    // 조직 카테고리를 '회사'로 고정
    await setCompanyOrgCategory();

    // 설명 필드 글자 수 초기화
    updateDescriptionCharCount();

    // 제목 필드 글자 수 초기화
    updateTitleCharCount();

    modal.style.display = 'block';

    // 폼 제출 이벤트 리스너
    const form = document.getElementById('scheduleForm');
    form.onsubmit = handleScheduleSubmit;

    // 설명 필드 실시간 글자 수 업데이트
    const descriptionField = document.getElementById('scheduleDescription');
    descriptionField.addEventListener('input', updateDescriptionCharCount);

    // 제목 필드 실시간 글자 수 업데이트
    const titleField = document.getElementById('scheduleTitle');
    titleField.addEventListener('input', updateTitleCharCount);
}

/**
 * 모달 닫기
 */
function closeScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    if (modal) {
        modal.style.display = 'none';
    }

    // 모드 초기화
    isEditMode = false;
    editingScheduleId = null;
}

/**
 * 시간/분 select 옵션 초기화 (시간만 커스텀 드롭다운)
 */
let timeSelectsInitialized = false;
function initializeTimeSelects() {
    const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
    const minutes = ['00', '10', '20', '30', '40', '50'];

    // 시간 선택만 커스텀 드롭다운으로
    const hourSelects = [
        { id: 'scheduleStartHour', options: hours },
        { id: 'scheduleEndHour', options: hours }
    ];

    hourSelects.forEach(({ id, options }) => {
        const select = document.getElementById(id);
        if (!select) return;

        const wrapper = select.parentElement;

        // 이미 커스텀 드롭다운이 생성되어 있으면 스킵
        if (wrapper.querySelector('.custom-select')) {
            return;
        }

        // 원래 select 숨기기
        select.style.display = 'none';

        // 커스텀 드롭다운 생성
        const customSelect = document.createElement('div');
        customSelect.className = 'custom-select';
        customSelect.dataset.selectId = id;

        const selected = document.createElement('div');
        selected.className = 'custom-select-selected';
        selected.textContent = options[0];
        select.value = options[0];

        const optionsContainer = document.createElement('div');
        optionsContainer.className = 'custom-select-options';
        optionsContainer.style.display = 'none';

        options.forEach(opt => {
            const optionDiv = document.createElement('div');
            optionDiv.className = 'custom-select-option';
            optionDiv.textContent = opt;
            optionDiv.dataset.value = opt;

            optionDiv.addEventListener('click', () => {
                selected.textContent = opt;
                select.value = opt;
                optionsContainer.style.display = 'none';
                customSelect.classList.remove('active');

                // 모든 옵션의 selected 클래스 제거
                optionsContainer.querySelectorAll('.custom-select-option').forEach(o => {
                    o.classList.remove('selected');
                });
                optionDiv.classList.add('selected');
            });

            optionsContainer.appendChild(optionDiv);
        });

        // 첫 번째 옵션을 selected로 표시
        optionsContainer.querySelector('.custom-select-option').classList.add('selected');

        selected.addEventListener('click', (e) => {
            e.stopPropagation();

            // 다른 모든 드롭다운 닫기
            document.querySelectorAll('.custom-select').forEach(cs => {
                if (cs !== customSelect) {
                    cs.querySelector('.custom-select-options').style.display = 'none';
                    cs.classList.remove('active');
                }
            });

            // 현재 드롭다운 토글
            const isVisible = optionsContainer.style.display === 'block';
            optionsContainer.style.display = isVisible ? 'none' : 'block';
            customSelect.classList.toggle('active', !isVisible);
        });

        customSelect.appendChild(selected);
        customSelect.appendChild(optionsContainer);
        wrapper.insertBefore(customSelect, select);
    });

    // 분 선택은 기본 select로
    const minuteSelects = [
        document.getElementById('scheduleStartMinute'),
        document.getElementById('scheduleEndMinute')
    ];

    minuteSelects.forEach(select => {
        if (!select) return;
        select.innerHTML = '';
        minutes.forEach(minute => {
            const option = document.createElement('option');
            option.value = minute;
            option.textContent = `${minute}분`;
            select.appendChild(option);
        });
    });

    // 외부 클릭 시 모든 드롭다운 닫기 (한 번만 등록)
    if (!timeSelectsInitialized) {
        document.addEventListener('click', () => {
            document.querySelectorAll('.custom-select-options').forEach(opt => {
                opt.style.display = 'none';
            });
            document.querySelectorAll('.custom-select').forEach(cs => {
                cs.classList.remove('active');
            });
        });
        timeSelectsInitialized = true;
    }
}

/**
 * 조직 카테고리를 '회사'로 설정
 */
async function setCompanyOrgCategory() {
    try {
        const response = await apiFetch('/api/organizations/include-orgs');
        if (!response.ok) throw new Error();

        const result = await response.json();
        const categories = result.data || [];

        const rootCategories = categories.filter(cat => cat.parentOrgId === null);

        const select = document.getElementById('scheduleOrgCategory');
        select.innerHTML = ''; //

        rootCategories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            select.appendChild(option);
        });

        // 기본값: 회사
        const companyCategory = rootCategories.find(cat => cat.name === '회사');
        if (companyCategory) {
            select.value = companyCategory.id;
        }

        select.disabled = true;
    } catch (e) {
        console.error(e);
    }
}

/**
 * 제목 필드 글자 수 업데이트
 */
function updateTitleCharCount() {
    const titleField = document.getElementById('scheduleTitle');
    const charCountEl = document.getElementById('titleCharCount');
    const errorEl = document.getElementById('titleError');

    if (!titleField || !charCountEl) return;

    const currentLength = titleField.value.length;
    const maxLength = 20;

    charCountEl.textContent = `(${currentLength}/${maxLength})`;

    // 글자 수에 따라 스타일 변경
    charCountEl.classList.remove('warning', 'error');
    errorEl.classList.remove('show');

    if (currentLength > maxLength) {
        charCountEl.classList.add('error');
        errorEl.textContent = `제목은 최대 ${maxLength}자까지 입력 가능합니다.`;
        errorEl.classList.add('show');
    } else if (currentLength > maxLength * 0.9) {
        charCountEl.classList.add('warning');
    }
}

/**
 * 설명 필드 글자 수 업데이트
 */
function updateDescriptionCharCount() {
    const descriptionField = document.getElementById('scheduleDescription');
    const charCountEl = document.getElementById('descriptionCharCount');
    const errorEl = document.getElementById('descriptionError');

    if (!descriptionField || !charCountEl) return;

    const currentLength = descriptionField.value.length;
    const maxLength = 200;

    charCountEl.textContent = `(${currentLength}/${maxLength})`;

    // 글자 수에 따라 스타일 변경
    charCountEl.classList.remove('warning', 'error');
    errorEl.classList.remove('show');

    if (currentLength > maxLength) {
        charCountEl.classList.add('error');
        errorEl.textContent = `설명은 최대 ${maxLength}자까지 입력 가능합니다.`;
        errorEl.classList.add('show');
    } else if (currentLength > maxLength * 0.9) {
        charCountEl.classList.add('warning');
    }
}

/**
 * 날짜 변환 함수 (LocalDateTime 형식)
 */
function toLocalDateTimeString(date) {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mi = String(date.getMinutes()).padStart(2, '0');
    const ss = String(date.getSeconds()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`;
}

/**
 * 일정 등록 폼 제출
 */
async function handleScheduleSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('scheduleTitle').value.trim();
    const description = document.getElementById('scheduleDescription').value.trim();
    const startDate = document.getElementById('scheduleStartDate').value;
    const startHour = document.getElementById('scheduleStartHour').value || '00';
    const startMinute = document.getElementById('scheduleStartMinute').value || '00';
    const endDate = document.getElementById('scheduleEndDate').value;
    const endHour = document.getElementById('scheduleEndHour').value || '00';
    const endMinute = document.getElementById('scheduleEndMinute').value || '00';
    const status = document.getElementById('scheduleStatus').value;
    const orgCategoryId = document.getElementById('scheduleOrgCategory').value;

    if (!title) {
        alert('제목을 입력해주세요.');
        return;
    }

    // 제목 글자 수 검증
    if (title.length > 20) {
        alert('제목은 최대 20자까지 입력 가능합니다.');
        document.getElementById('scheduleTitle').focus();
        return;
    }

    // 설명 글자 수 검증
    if (description.length > 200) {
        alert('설명은 최대 200자까지 입력 가능합니다.');
        document.getElementById('scheduleDescription').focus();
        return;
    }

    // 시간 문자열 조합 (HH:MM 형식)
    const startTime = `${startHour}:${startMinute}`;
    const endTime = `${endHour}:${endMinute}`;

    // 날짜/시간 검증
    const startDateTime = new Date(`${startDate}T${startTime}`);
    const endDateTime = new Date(`${endDate}T${endTime}`);

    if (endDateTime < startDateTime) {
        alert('종료 날짜/시간은 시작 날짜/시간보다 이전일 수 없습니다.');
        return;
    }

    // LocalDateTime 형식으로 변환
    const startAt = toLocalDateTimeString(startDateTime);
    const endAt = toLocalDateTimeString(endDateTime);

    const scheduleData = {
        isCompany: true,  // 전사 일정
        isPersonal: false,
        title: title,
        description: description || null,
        startAt: startAt,
        endAt: endAt,
        status: status,
        orgCategoryId: orgCategoryId ? parseInt(orgCategoryId) : null,
        orgId: null  // 조직은 null로 고정
    };

    try {
        let response;

        if (isEditMode && editingScheduleId) {
            // 수정 모드
            response = await apiFetch(`/api/admin/schedules/${editingScheduleId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(scheduleData)
            });
        } else {
            // 등록 모드
            response = await apiFetch('/api/schedules', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(scheduleData)
            });
        }

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const error = await response.json();
            throw new Error(error.message || (isEditMode ? '일정 수정에 실패했습니다.' : '일정 등록에 실패했습니다.'));
        }

        alert(isEditMode ? '일정이 수정되었습니다.' : '일정이 등록되었습니다.');
        closeScheduleModal();
        loadSchedules();  // 일정 목록 새로고침
    } catch (error) {
        console.error(`Error ${isEditMode ? 'updating' : 'creating'} schedule:`, error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert(error.message || (isEditMode ? '일정 수정에 실패했습니다.' : '일정 등록에 실패했습니다.'));
        }
    }
}

/**
 * 날짜를 input[type="date"] 형식으로 변환 (YYYY-MM-DD)
 */
function formatDateForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 모달 외부 클릭 시 닫기
window.onclick = function (event) {
    const scheduleModal = document.getElementById('scheduleModal');
    if (event.target === scheduleModal) {
        closeScheduleModal();
    }
}

/**
 * 일정 삭제
 */
async function deleteSchedule(scheduleId, event) {
    if (event) {
        event.stopPropagation();
    }

    if (!confirm('정말로 이 일정을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await apiFetch(`/api/schedules/${scheduleId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const error = await response.json();
            throw new Error(error.message || '일정 삭제에 실패했습니다.');
        }

        alert('일정이 삭제되었습니다.');
        loadSchedules();  // 일정 목록 새로고침
    } catch (error) {
        console.error('Error deleting schedule:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert(error.message || '일정 삭제에 실패했습니다.');
        }
    }
}

