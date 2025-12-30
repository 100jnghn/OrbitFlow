(() => {
    console.log('organization-schedule.js loaded');

    // 전역 변수
    let currentDate = new Date();
    let schedules = [];
    let selectedOrgIds = [];
    let orgList = [];
    let selectedDate = null; // 선택된 날짜
    let selectedCategoryId = null; // 선택된 조직 카테고리 ID

    // 초기화
    document.addEventListener('DOMContentLoaded', async function () {
        setupEventListeners();
        await loadOrgCategories();
        
        // 첫 번째 카테고리 자동 선택 및 조직 로드
        const categorySelect = document.getElementById('orgCategorySelect');
        if (categorySelect.options.length > 0) {
            const firstCategoryId = parseInt(categorySelect.options[0].value);
            categorySelect.value = firstCategoryId;
            selectedCategoryId = firstCategoryId;
            await loadOrganizationsByCategory(selectedCategoryId);
        }
        
        // 오늘 날짜를 선택된 날짜로 설정
        selectedDate = new Date();
        selectedDate.setHours(0, 0, 0, 0);
        
        loadSchedules();
        renderCalendar();
        
        // 오늘 날짜의 일정 로드
        if (selectedOrgIds.length > 0) {
            loadDateSchedules(selectedDate);
        }
    });

    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 이전/다음 달 버튼
        document.getElementById('prevMonthBtn').addEventListener('click', () => {
            currentDate.setMonth(currentDate.getMonth() - 1);
            selectedDate = null; // 날짜 선택 초기화
            renderCalendar();
            loadSchedules();
        });

        document.getElementById('nextMonthBtn').addEventListener('click', () => {
            currentDate.setMonth(currentDate.getMonth() + 1);
            selectedDate = null; // 날짜 선택 초기화
            renderCalendar();
            loadSchedules();
        });

        // 오늘 버튼
        document.getElementById('todayBtn').addEventListener('click', () => {
            currentDate = new Date();
            selectedDate = null; // 날짜 선택 초기화
            renderCalendar();
            loadSchedules();
        });

        // 조직 카테고리 선택 변경
        document.getElementById('orgCategorySelect').addEventListener('change', handleCategoryChange);
    }

    // 조직 카테고리 로드
    async function loadOrgCategories() {
        try {
            const response = await apiFetch('/api/org-categories');
            if (!response.ok) {
                if (response.status === 401) {
                    location.href = '/login';
                    return;
                }
                if (response.status === 403) {
                    throw new Error("권한 없음");
                }
                throw new Error('조직 카테고리를 불러오는데 실패했습니다.');
            }

            const result = await response.json();
            const categories = result.data || [];

            // 활성화된 카테고리만 필터링하고 '회사' 제외, orderIndex 순으로 정렬
            const activeCategories = categories
                .filter(cat => cat.isActive === true && cat.name !== '회사')
                .sort((a, b) => {
                    const orderA = a.orderIndex !== null ? a.orderIndex : 999;
                    const orderB = b.orderIndex !== null ? b.orderIndex : 999;
                    return orderA - orderB;
                });
            
            // 조직 카테고리 드롭다운 채우기 ('전체' 옵션 제거)
            const categorySelect = document.getElementById('orgCategorySelect');
            const categoryOptions = activeCategories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
            categorySelect.innerHTML = categoryOptions;

        } catch (error) {
            console.error('Error loading org categories:', error);
        }
    }

    // 조직 목록 로드 (카테고리별)
    async function loadOrganizationsByCategory(categoryId) {
        try {
            const response = await apiFetch(`/api/organizations/by-category/${categoryId}`);
            if (!response.ok) {
                if (response.status === 401) {
                    location.href = '/login';
                    return;
                }
                if (response.status === 403) {
                    throw new Error("권한 없음");
                }
                throw new Error('조직 목록을 불러오는데 실패했습니다.');
            }

            const result = await response.json();
            const organizations = result.data || [];

            // 활성화된 하위 조직만 필터링 (parentOrgId가 null이 아닌 조직)
            const activeChildOrgs = organizations
                .filter(org => org.isActive === true && org.parentOrgId !== null)
                .sort((a, b) => {
                    const orderA = a.orderIndex !== null ? a.orderIndex : 999;
                    const orderB = b.orderIndex !== null ? b.orderIndex : 999;
                    return orderA - orderB;
                });

            // 조직 필터 체크박스 업데이트
            const orgFilter = document.getElementById('orgFilter');
            orgFilter.innerHTML = activeChildOrgs
                .map(org => `
                    <label class="org-filter-checkbox-item">
                        <input type="checkbox" value="${org.id}" class="org-filter-checkbox">
                        <span>${org.name}</span>
                    </label>
                `)
                .join('');

            // 체크박스 이벤트 리스너 추가
            orgFilter.querySelectorAll('.org-filter-checkbox').forEach(checkbox => {
                checkbox.addEventListener('change', handleOrgFilterChange);
            });

            // 선택된 조직 ID 초기화
            selectedOrgIds = [];

            // 조직 목록 업데이트 (일정 표시용)
            orgList = organizations;

        } catch (error) {
            console.error('Error loading organizations by category:', error);
            // 에러 발생 시 빈 리스트 표시
            const orgFilter = document.getElementById('orgFilter');
            orgFilter.innerHTML = '';
            orgList = [];
        }
    }

    // 조직 카테고리 변경 핸들러
    async function handleCategoryChange() {
        const categorySelect = document.getElementById('orgCategorySelect');
        selectedCategoryId = categorySelect.value ? parseInt(categorySelect.value) : null;
        selectedDate = null; // 날짜 선택 초기화
        
        if (selectedCategoryId) {
            await loadOrganizationsByCategory(selectedCategoryId);
        }
        
        loadSchedules();
        
        // 날짜가 선택되지 않았으면 일정 목록 초기화
        renderScheduleList([]);
    }

    // 조직 필터 변경 핸들러
    function handleOrgFilterChange() {
        const checkedBoxes = document.querySelectorAll('#orgFilter .org-filter-checkbox:checked');
        selectedOrgIds = Array.from(checkedBoxes).map(cb => cb.value);
        
        loadSchedules();
        
        // 날짜가 선택되어 있으면 해당 날짜의 일정 다시 로드
        if (selectedDate) {
            loadDateSchedules(selectedDate);
        } else {
            // 날짜가 선택되지 않았으면 일정 목록 초기화
            renderScheduleList([]);
        }
    }

    // 날짜별 일정 로드
    async function loadDateSchedules(date) {
        try {
            // 선택된 조직이 없으면 일정 목록 초기화
            if (selectedOrgIds.length === 0) {
                renderScheduleList([]);
                return;
            }

            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const dateStr = `${year}-${month}-${day}`;

            // orgIds 파라미터 생성
            const orgIdsParam = selectedOrgIds.map(id => `orgIds=${id}`).join('&');
            
            const response = await apiFetch(`/api/schedules/organizations/schedule?${orgIdsParam}&date=${dateStr}`);
            if (!response.ok) {
                if (response.status === 401) {
                    location.href = '/login';
                    return;
                }
                throw new Error('일정을 불러오는데 실패했습니다.');
            }

            const result = await response.json();
            const dateSchedules = result.data || [];

            // 선택된 날짜의 일정만 목록에 표시
            renderScheduleList(dateSchedules);
        } catch (error) {
            console.error('Error loading date schedules:', error);
            alert('일정을 불러오는데 실패했습니다.');
        }
    }

    // 일정 로드
    async function loadSchedules() {
        try {
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth() + 1;

            const allSchedules = [];

            // 조직 일정 로드 (체크된 조직이 있을 때)
            if (selectedOrgIds.length > 0) {
                try {
                    const orgIdsParam = selectedOrgIds.map(id => `orgIds=${id}`).join('&');
                    const orgResponse = await apiFetch(`/api/schedules/organizations?${orgIdsParam}&year=${year}&month=${month}`);
                    if (orgResponse.ok) {
                        const orgResult = await orgResponse.json();
                        if (orgResult.data) {
                            allSchedules.push(...orgResult.data);
                        }
                    }
                } catch (error) {
                    console.error('Error loading organization schedules:', error);
                }
            }

            schedules = allSchedules;

            filterAndRenderSchedules();
        } catch (error) {
            console.error('Error loading schedules:', error);
            alert('일정을 불러오는데 실패했습니다.');
        }
    }

    // 필터링 및 렌더링
    function filterAndRenderSchedules() {
        // 조직 일정만 필터링
        const filtered = schedules.filter(s => s.orgId !== null && !s.company);

        renderCalendar(filtered);
        
        // 날짜가 선택되지 않은 경우 일정 목록 초기화
        if (!selectedDate) {
            renderScheduleList([]);
        }
    }

    // 캘린더 렌더링
    function renderCalendar(filteredSchedules = schedules) {
        console.log("캘린더 로드");

        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();

        // 월/년 표시 업데이트
        document.getElementById('calendarMonthYear').textContent = `${year}년 ${month + 1}월`;

        // 첫 번째 날짜 계산
        const firstDay = new Date(year, month, 1);
        const startDate = new Date(firstDay);
        startDate.setDate(startDate.getDate() - startDate.getDay());

        const calendarGrid = document.getElementById('calendarGrid');
        calendarGrid.innerHTML = '';

        // 42일 (6주) 렌더링
        for (let i = 0; i < 42; i++) {
            const date = new Date(startDate);
            date.setDate(startDate.getDate() + i);

            const dayElement = document.createElement('div');
            dayElement.className = 'calendar-day';

            if (date.getMonth() !== month) {
                dayElement.classList.add('other-month');
            }

            const today = new Date();
            if (date.toDateString() === today.toDateString()) {
                dayElement.classList.add('today');
            }

            // 선택된 날짜 표시
            if (selectedDate && date.toDateString() === selectedDate.toDateString()) {
                dayElement.classList.add('selected');
            }

            const dayNumber = document.createElement('div');
            dayNumber.className = 'day-number';
            dayNumber.textContent = date.getDate();
            dayElement.appendChild(dayNumber);

            // 해당 날짜의 일정 표시
            const daySchedules = filteredSchedules.filter(s => {
                const start = new Date(s.startAt);
                const end = new Date(s.endAt);
                const dayStart = new Date(date);
                dayStart.setHours(0, 0, 0, 0);
                const dayEnd = new Date(date);
                dayEnd.setHours(23, 59, 59, 999);
                return start <= dayEnd && end >= dayStart;
            });

            if (daySchedules.length > 0) {
                const scheduleItems = document.createElement('div');
                scheduleItems.className = 'schedule-items';

                daySchedules.slice(0, 3).forEach(schedule => {
                    const item = createScheduleItem(schedule);
                    scheduleItems.appendChild(item);
                });

                if (daySchedules.length > 3) {
                    const moreItem = document.createElement('div');
                    moreItem.className = 'schedule-item';
                    moreItem.textContent = `+${daySchedules.length - 3}`;
                    moreItem.style.background = 'var(--neutral-500)';
                    scheduleItems.appendChild(moreItem);
                }

                dayElement.appendChild(scheduleItems);
            }

            dayElement.addEventListener('click', (e) => {
                if (date.getMonth() === month) {
                    e.stopPropagation();
                    selectedDate = new Date(date);
                    selectedDate.setHours(0, 0, 0, 0);
                    loadDateSchedules(selectedDate);
                    // 캘린더 다시 렌더링하여 선택된 날짜 표시
                    filterAndRenderSchedules();
                }
            });

            calendarGrid.appendChild(dayElement);
        }
    }

    // 일정 아이템 생성 (캘린더용)
    function createScheduleItem(schedule) {
        const item = document.createElement('div');
        item.className = 'schedule-item organization';
        
        item.textContent = schedule.title;
        item.addEventListener('click', (e) => {
            e.stopPropagation();
            openScheduleDetailModal(schedule);
        });
        return item;
    }

    // 일정 목록 렌더링
    function renderScheduleList(filteredSchedules = schedules) {
        const listContainer = document.getElementById('scheduleList');
        listContainer.innerHTML = '';

        if (filteredSchedules.length === 0) {
            listContainer.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--neutral-500);">일정이 없습니다.</div>';
            return;
        }

        // 날짜순 정렬
        const sorted = [...filteredSchedules].sort((a, b) => new Date(a.startAt) - new Date(b.startAt));

        sorted.forEach(schedule => {
            const item = createScheduleListItem(schedule);
            listContainer.appendChild(item);
        });
    }

    // 일정 목록 아이템 생성
    function createScheduleListItem(schedule) {
        const item = document.createElement('div');
        item.className = 'schedule-item-list organization';

        const content = document.createElement('div');
        content.className = 'schedule-item-content';

        const title = document.createElement('div');
        title.className = 'schedule-item-title';
        title.textContent = schedule.title;

        const start = new Date(schedule.startAt);
        const end = new Date(schedule.endAt);
        const time = document.createElement('div');
        time.className = 'schedule-item-time';
        time.textContent = `${formatDateTime(start)} - ${formatDateTime(end)}`;

        const org = document.createElement('div');
        org.className = 'schedule-item-org';
        const orgData = orgList.find(o => o.id === schedule.orgId);
        org.textContent = orgData ? orgData.name : '조직 일정';

        content.appendChild(title);
        content.appendChild(time);
        content.appendChild(org);

        item.appendChild(content);

        item.addEventListener('click', () => {
            openScheduleDetailModal(schedule);
        });

        return item;
    }

    // 날짜/시간 포맷
    function formatDateTime(date) {
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${month}/${day} ${hours}:${minutes}`;
    }

    // 일정 세부 정보 모달 열기
    function openScheduleDetailModal(schedule) {
        // 세부 정보 표시
        document.getElementById('detailTitle').textContent = schedule.title || '-';
        document.getElementById('detailDescription').textContent = schedule.description || '-';

        const start = new Date(schedule.startAt);
        const end = new Date(schedule.endAt);
        document.getElementById('detailStartAt').textContent = formatDetailDateTime(start);
        document.getElementById('detailEndAt').textContent = formatDetailDateTime(end);

        const statusText = schedule.status === 'RELEASE' ? '공개' : '보류';
        document.getElementById('detailStatus').textContent = statusText;

        const orgData = orgList.find(o => o.id === schedule.orgId);
        const typeText = orgData ? `${orgData.name} 일정` : '조직 일정';
        document.getElementById('detailType').textContent = typeText;

        document.getElementById('scheduleDetailModal').style.display = 'block';
    }

    // 세부 정보용 날짜/시간 포맷
    function formatDetailDateTime(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}년 ${month}월 ${day}일 ${hours}:${minutes}`;
    }

    // 세부 정보 모달 닫기
    function closeScheduleDetailModal() {
        document.getElementById('scheduleDetailModal').style.display = 'none';
    }

    // 전역 스코프에 함수 노출 (HTML onclick에서 사용)
    window.closeScheduleDetailModal = closeScheduleDetailModal;

    // 모달 외부 클릭 시 닫기
    window.onclick = function (event) {
        const detailModal = document.getElementById('scheduleDetailModal');
        if (event.target === detailModal) {
            closeScheduleDetailModal();
        }
    }
})();

