(() => {
    console.log('schedule.js loaded');

    // 전역 변수
    let currentDate = new Date();
    let schedules = [];
    let selectedOrgIds = [];
    let showPersonal = true; // 개인 일정 표시 여부
    let showCompany = true; // 전사 일정 표시 여부
    let orgList = [];
    let isSubmitting = false; // 제출 중 플래그 (중복 제출 방지)
    let selectedDate = null; // 선택된 날짜

    // 초기화
    document.addEventListener('DOMContentLoaded', function () {
        initializeTimeSelects();
        setupEventListeners();
        // 초기 토글 상태 설정
        document.getElementById('personalToggle').classList.toggle('active', showPersonal);
        document.getElementById('companyToggle').classList.toggle('active', showCompany);
        loadOrganizations();
        
        // 오늘 날짜를 선택된 날짜로 설정
        selectedDate = new Date();
        selectedDate.setHours(0, 0, 0, 0);
        
        // 초기 로드 시 list-title 업데이트
        updateScheduleListTitle();
        
        loadSchedules();
        renderCalendar();
        
        // 오늘 날짜의 일정 로드
        loadDateSchedules(selectedDate);
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

        // 일정 추가 버튼
        document.getElementById('addScheduleBtn').addEventListener('click', openAddScheduleModal);

        // Toggle 버튼
        document.getElementById('personalToggle').addEventListener('click', () => {
            togglePersonal();
        });

        document.getElementById('companyToggle').addEventListener('click', () => {
            toggleCompany();
        });

        // 조직 필터는 loadOrganizations에서 동적으로 추가됨

        // 폼 제출
        document.getElementById('scheduleForm').addEventListener('submit', handleScheduleSubmit);

        // 제목/설명 글자 수 카운트
        document.getElementById('scheduleTitle').addEventListener('input', updateTitleCharCount);
        document.getElementById('scheduleDescription').addEventListener('input', updateDescriptionCharCount);

        // 개인일정 체크박스 이벤트 리스너
        document.getElementById('isPersonalSchedule').addEventListener('change', (e) => {
            const orgSelect = document.getElementById('scheduleOrg');
            if (e.target.checked) {
                // 체크 시 조직 비활성화 및 초기화
                orgSelect.disabled = true;
                orgSelect.value = '';
            } else {
                // 체크 해제 시 조직 활성화 및 첫 번째 조직으로 설정
                orgSelect.disabled = false;
                const childOrgs = orgList.filter(org => org.parentOrgId !== null);
                if (childOrgs.length > 0) {
                    orgSelect.value = childOrgs[0].id;
                }
            }
        });
    }

    // 시간 선택 옵션 초기화
    function initializeTimeSelects() {
        const hours = Array.from({length: 24}, (_, i) => String(i).padStart(2, '0'));
        const minutes = ['00', '10', '20', '30', '40', '50'];

        const hourSelects = ['scheduleStartHour', 'scheduleEndHour'];
        const minuteSelects = ['scheduleStartMinute', 'scheduleEndMinute'];

        hourSelects.forEach(id => {
            const select = document.getElementById(id);
            select.innerHTML = hours.map(h => `<option value="${h}">${h}</option>`).join('');
        });

        minuteSelects.forEach(id => {
            const select = document.getElementById(id);
            select.innerHTML = minutes.map(m => `<option value="${m}">${m}</option>`).join('');
        });
    }

    // 조직 목록 로드
    async function loadOrganizations() {
        try {
            const response = await apiFetch('/api/organizations/include-orgs');
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
            orgList = result.data || [];

            const childOrgs = orgList.filter(org => org.parentOrgId !== null);

            // 조직 필터 체크박스 리스트
            const orgFilter = document.getElementById('orgFilter');
            orgFilter.innerHTML = childOrgs
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

            // 일정 등록용 조직 드롭다운
            const orgSelect = document.getElementById('scheduleOrg');
            const orgOptions = childOrgs
                .map(org => `<option value="${org.id}">${org.name}</option>`)
                .join('');
            orgSelect.innerHTML = orgOptions;
            
            // 기본값을 첫 번째 조직으로 설정
            if (childOrgs.length > 0) {
                orgSelect.value = childOrgs[0].id;
            }
        } catch (error) {
            console.error('Error loading organizations:', error);
        }
    }


    // 개인 일정 토글
    function togglePersonal() {
        showPersonal = !showPersonal;
        document.getElementById('personalToggle').classList.toggle('active', showPersonal);
        selectedDate = null; // 날짜 선택 초기화
        loadSchedules();
    }

    // 전사 일정 토글
    function toggleCompany() {
        showCompany = !showCompany;
        document.getElementById('companyToggle').classList.toggle('active', showCompany);
        selectedDate = null; // 날짜 선택 초기화
        loadSchedules();
    }

    // 조직 필터 변경 핸들러
    function handleOrgFilterChange() {
        const checkedBoxes = document.querySelectorAll('#orgFilter .org-filter-checkbox:checked');
        selectedOrgIds = Array.from(checkedBoxes).map(cb => cb.value);
        selectedDate = null; // 날짜 선택 초기화
        loadSchedules();
    }

    // 날짜별 일정 로드
    async function loadDateSchedules(date) {
        try {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const dateStr = `${year}-${month}-${day}`;

            const response = await apiFetch(`/api/schedules/schedule?date=${dateStr}`);
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

            // 개인 일정 로드
            if (showPersonal) {
                try {
                    const personalResponse = await apiFetch(`/api/schedules/personal?year=${year}&month=${month}`);
                    console.log("개인 일정 조회 요청")

                    if (personalResponse.ok) {
                        const personalResult = await personalResponse.json();
                        if (personalResult.data) {
                            allSchedules.push(...personalResult.data);
                        }
                    }
                } catch (error) {
                    console.error('Error loading personal schedules:', error);
                }
            }

            // 전사 일정 로드
            if (showCompany) {
                try {
                    const companyResponse = await apiFetch(`/api/schedules/company?year=${year}&month=${month}&status=RELEASE`);
                    if (companyResponse.ok) {
                        const companyResult = await companyResponse.json();
                        if (companyResult.data) {
                            allSchedules.push(...companyResult.data);
                        }
                    }
                } catch (error) {
                    console.error('Error loading company schedules:', error);
                }
            }

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
        let filtered = [...schedules];

        // 개인/전사 일정 필터 적용
        filtered = filtered.filter(s => {
            if (s.company) {
                return showCompany;
            } else if (s.orgId) {
                // 조직 일정은 항상 표시 (이미 selectedOrgIds로 필터링됨)
                return true;
            } else {
                return showPersonal;
            }
        });

        renderCalendar(filtered);
        
        // // 날짜가 선택되지 않은 경우에만 필터링된 일정 표시
        // if (!selectedDate) {
        //     renderScheduleList(filtered);
        // }
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
        const lastDay = new Date(year, month + 1, 0);
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
        item.className = 'schedule-item';
        
        // 일정 유형에 따라 클래스 추가
        if (schedule.company) {
            item.classList.add('company');
        } else if (schedule.orgId) {
            item.classList.add('organization');
        } else {
            item.classList.add('personal');
        }
        
        item.textContent = schedule.title;
        item.addEventListener('click', (e) => {
            e.stopPropagation();
            openScheduleDetailModal(schedule);
        });
        return item;
    }

    // 일정 목록 제목 업데이트
    function updateScheduleListTitle() {
        const listTitle = document.getElementById('scheduleListTitle');
        if (selectedDate) {
            const year = selectedDate.getFullYear();
            const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
            const day = String(selectedDate.getDate()).padStart(2, '0');
            listTitle.textContent = `${year}년 ${month}월 ${day}일 일정 목록`;
        } else {
            listTitle.textContent = '일정 목록';
        }
    }

    // 일정 목록 렌더링
    function renderScheduleList(filteredSchedules = schedules) {
        const listContainer = document.getElementById('scheduleList');
        listContainer.innerHTML = '';

        // 선택된 날짜에 따라 제목 업데이트
        updateScheduleListTitle();

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
        item.className = 'schedule-item-list';
        
        // 일정 유형에 따라 클래스 추가
        if (schedule.company) {
            item.classList.add('company');
        } else if (schedule.orgId) {
            item.classList.add('organization');
        } else {
            item.classList.add('personal');
        }

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
        if (schedule.company) {
            org.textContent = '전사 일정';
        } else if (schedule.orgId) {
            const orgData = orgList.find(o => o.id === schedule.orgId);
            org.textContent = orgData ? orgData.name : '조직 일정';
        } else {
            org.textContent = '개인 일정';
        }

        content.appendChild(title);
        content.appendChild(time);
        content.appendChild(org);

        item.appendChild(content);

        // 전사 일정이 아닌 경우에만 삭제 버튼 추가
        if (!schedule.company) {
            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'btn-delete-schedule';
            deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
            deleteBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                deleteSchedule(schedule.scheduleId);
            });
            item.appendChild(deleteBtn);
        }

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

// 일정 추가 모달 열기
    function openAddScheduleModal() {
        document.getElementById('modalTitle').textContent = '일정 등록';
        document.getElementById('submitBtn').textContent = '등록';

        // 폼 초기화
        document.getElementById('scheduleForm').reset();
        document.getElementById('scheduleId').value = '';

        // 기본값 설정
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');

        document.getElementById('scheduleStartDate').value = `${year}-${month}-${day}`;
        document.getElementById('scheduleEndDate').value = `${year}-${month}-${day}`;
        document.getElementById('scheduleStartHour').value = '09';
        document.getElementById('scheduleStartMinute').value = '00';
        document.getElementById('scheduleEndHour').value = '18';
        document.getElementById('scheduleEndMinute').value = '00';

        // 개인일정 체크박스 초기화
        document.getElementById('isPersonalSchedule').checked = false;
        const orgSelect = document.getElementById('scheduleOrg');
        orgSelect.disabled = false;
        // 기본값을 첫 번째 조직으로 설정
        const childOrgs = orgList.filter(org => org.parentOrgId !== null);
        if (childOrgs.length > 0) {
            orgSelect.value = childOrgs[0].id;
        }

        updateTitleCharCount();
        updateDescriptionCharCount();

        document.getElementById('scheduleModal').style.display = 'block';
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

        let typeText = '';
        console.log(schedule.company);
        if (schedule.company) {
            typeText = '전사 일정';
        } else if (schedule.orgId) {
            const orgData = orgList.find(o => o.id === schedule.orgId);
            typeText = orgData ? `${orgData.name} 일정` : '조직 일정';
        } else {
            typeText = '개인 일정';
        }
        document.getElementById('detailType').textContent = typeText;

        // 삭제 버튼 표시/숨김 처리
        const deleteBtn = document.getElementById('detailDeleteBtn');
        if (schedule.company) {
            // 전사 일정은 삭제 불가
            deleteBtn.style.display = 'none';
        } else {
            // 개인/조직 일정은 삭제 가능
            deleteBtn.style.display = 'inline-flex';
            deleteBtn.onclick = () => {
                closeScheduleDetailModal();
                deleteSchedule(schedule.scheduleId);
            };
        }

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

// 모달 닫기
    function closeScheduleModal() {
        document.getElementById('scheduleModal').style.display = 'none';
    }

    // 전역 스코프에 함수 노출 (HTML onclick에서 사용)
    window.closeScheduleDetailModal = closeScheduleDetailModal;
    window.closeScheduleModal = closeScheduleModal;

// 제목 글자 수 업데이트
    function updateTitleCharCount() {
        const input = document.getElementById('scheduleTitle');
        const count = input.value.length;
        const maxLength = 20;
        const charCount = document.getElementById('titleCharCount');
        const errorMsg = document.getElementById('titleError');

        charCount.textContent = `(${count}/${maxLength})`;

        if (count > maxLength) {
            charCount.classList.add('error');
            errorMsg.textContent = '제목은 최대 20자까지 입력 가능합니다.';
            errorMsg.classList.add('show');
        } else {
            charCount.classList.remove('error');
            errorMsg.classList.remove('show');
        }
    }

// 설명 글자 수 업데이트
    function updateDescriptionCharCount() {
        const input = document.getElementById('scheduleDescription');
        const count = input.value.length;
        const maxLength = 200;
        const charCount = document.getElementById('descriptionCharCount');
        const errorMsg = document.getElementById('descriptionError');

        charCount.textContent = `(${count}/${maxLength})`;

        if (count > maxLength) {
            charCount.classList.add('error');
            errorMsg.textContent = '설명은 최대 200자까지 입력 가능합니다.';
            errorMsg.classList.add('show');
        } else {
            charCount.classList.remove('error');
            errorMsg.classList.remove('show');
        }
    }

    // 날짜 변환 함수
    function toLocalDateTimeString(date) {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        const hh = String(date.getHours()).padStart(2, '0');
        const mi = String(date.getMinutes()).padStart(2, '0');
        const ss = String(date.getSeconds()).padStart(2, '0');

        return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`;
    }

    // 일정 제출 처리
    async function handleScheduleSubmit(e) {
        e.preventDefault();

        // 중복 제출 방지
        if (isSubmitting) {
            return;
        }
        isSubmitting = true;

        const title = document.getElementById('scheduleTitle').value.trim();
        const description = document.getElementById('scheduleDescription').value.trim();
        const startDate = document.getElementById('scheduleStartDate').value;
        const startHour = document.getElementById('scheduleStartHour').value;
        const startMinute = document.getElementById('scheduleStartMinute').value;
        const endDate = document.getElementById('scheduleEndDate').value;
        const endHour = document.getElementById('scheduleEndHour').value;
        const endMinute = document.getElementById('scheduleEndMinute').value;
        const isPersonal = document.getElementById('isPersonalSchedule').checked;

        const orgId = isPersonal ? '' : document.getElementById('scheduleOrg').value;
        let orgCategoryId = null;
        
        // 조직을 선택한 경우 해당 조직의 categoryId 가져오기
        if (orgId) {
            const selectedOrg = orgList.find(org => org.id == orgId);
            orgCategoryId = selectedOrg ? selectedOrg.categoryId : null;
        }

        // 상태는 항상 'RELEASE' (공개)로 고정
        const status = 'RELEASE';

        if (!title) {
            alert('제목을 입력해주세요.');
            return;
        }

        if (title.length > 20) {
            alert('제목은 최대 20자까지 입력 가능합니다.');
            document.getElementById('scheduleTitle').focus();
            return;
        }

        if (description.length > 200) {
            alert('설명은 최대 200자까지 입력 가능합니다.');
            document.getElementById('scheduleDescription').focus();
            return;
        }

        const startTime = `${startHour}:${startMinute}`;
        const endTime = `${endHour}:${endMinute}`;

        const startDateTime = new Date(`${startDate}T${startTime}`);
        const endDateTime = new Date(`${endDate}T${endTime}`);

        if (endDateTime <= startDateTime) {
            alert('종료 날짜/시간은 시작 날짜/시간보다 이후여야 합니다.');
            return;
        }

        const startAt = toLocalDateTimeString(startDateTime);
        const endAt = toLocalDateTimeString(endDateTime);

        const scheduleData = {
            isCompany: false,
            title: title,
            description: description || null,
            startAt: startAt,
            endAt: endAt,
            status: status,
            orgCategoryId: orgCategoryId ? parseInt(orgCategoryId) : null,
            orgId: orgId ? parseInt(orgId) : null
        };

        try {
            const response = await apiFetch('/api/schedules', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(scheduleData)
            });

            if (!response.ok) {
                if (response.status === 401) {
                    location.href = '/login';
                    return;
                }
                const error = await response.json();
                throw new Error(error.message || '일정 등록에 실패했습니다.');
            }

            alert('일정이 등록되었습니다.');
            closeScheduleModal();
            loadSchedules();
        } catch (error) {
            console.error('Error saving schedule:', error);
            if (error.message !== 'SESSION_EXPIRED') {
                alert(error.message || '일정 등록에 실패했습니다.');
            }
        } finally {
            isSubmitting = false;
        }
    }

// 일정 삭제
    async function deleteSchedule(scheduleId) {
        if (!confirm('정말 이 일정을 삭제하시겠습니까?')) {
            return;
        }

        try {
            const response = await apiFetch(`/api/schedules/${scheduleId}`, {
                method: 'DELETE'
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
            loadSchedules();
        } catch (error) {
            console.error('Error deleting schedule:', error);
            if (error.message !== 'SESSION_EXPIRED') {
                alert(error.message || '일정 삭제에 실패했습니다.');
            }
        }
    }

// 모달 외부 클릭 시 닫기
    window.onclick = function (event) {
        const scheduleModal = document.getElementById('scheduleModal');
        const detailModal = document.getElementById('scheduleDetailModal');
        if (event.target === scheduleModal) {
            closeScheduleModal();
        }
        if (event.target === detailModal) {
            closeScheduleDetailModal();
        }
    }
})();
