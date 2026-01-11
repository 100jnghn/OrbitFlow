/**
 * 월별 근태 현황 관리 모듈
 * * 주요 기능:
 * 1. 기간 및 상태별 근태 내역 조회
 * 2. 통계 요약 정보(근무시간, 지각, 결근) 업데이트
 * 3. 페이징 처리 및 UI 배지 바인딩
 */
const MonthlyAttendance = {
    // 1. 상태 관리: 현재 조회 중인 파라미터 저장
    state: {
        page: 0,
        size: 31,
        status: 'ALL'
    },

    // 2. 엘리먼트 캐싱: DOM 접근 최소화로 성능 향상
    elements: null,

    init() {
        this.cacheDOM();
        this.bindEvents();
        this.resetInputs(); // 초기 입력 필드 세팅
        this.updateSidebar();
        this.executeSearch(); // 초기 데이터 로드
    },

    cacheDOM() {
        this.elements = {
            startDate: document.getElementById('startDate'),
            endDate: document.getElementById('endDate'),
            statusFilter: document.getElementById('statusFilter'),
            searchBtn: document.getElementById('searchBtn'),
            resetBtn: document.getElementById('resetBtn'),
            tableBody: document.querySelector('#attendanceTable tbody'),
            pagination: document.getElementById('boardPagination'),
            summary: {
                totalHours: document.getElementById('totalWorkHours'),
                lateCount: document.getElementById('lateCount'),
                absentCount: document.getElementById('absentCount')
            }
        };
    },

    bindEvents() {
        // 검색 버튼 클릭
        this.elements.searchBtn.addEventListener('click', () => {
            this.state.status = this.elements.statusFilter?.value || 'ALL';
            this.state.page = 0;
            this.executeSearch();
        });

        // 초기화 버튼 클릭
        this.elements.resetBtn.addEventListener('click', () => this.handleReset());
    },

    resetInputs() {
        this.elements.startDate.value = '';
        this.elements.endDate.value = '';
    },

    handleReset() {
        this.elements.statusFilter.value = 'ALL';
        this.resetInputs();
        this.state.status = 'ALL';
        this.state.page = 0;
        this.executeSearch(); // 초기 상태로 데이터 재로드
    },

    async executeSearch() {
        const start = this.elements.startDate.value || null;
        const end = this.elements.endDate.value || null;

        // 시작일이 종료일보다 늦은 경우 검증
        if (start && end && start > end) {
            alert('시작일이 종료일보다 늦을 수 없습니다.');
            return;
        }

        await this.loadData(start, end);
    },

    async loadData(start, end) {
        const { page, size, status } = this.state;
        let url = `/api/attendance/history/monthly?status=${status}&page=${page}&size=${size}`;

        if (start) url += `&startDate=${start}`;
        if (end) url += `&endDate=${end}`;

        try {
            const response = await fetch(url, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
            });
            const res = await response.json();

            if (!response.ok) {
                alert(res.message || '근태 내역을 조회하는 중 오류가 발생했습니다.');
                return;
            }

            this.updateUI(res.data);
        } catch (error) {
            console.error('API Error:', error);
            alert('서버와의 통신 중 오류가 발생했습니다.');
        }
    },

    // 3. UI 렌더링 로직
    updateUI(data) {
        if (!data) return;

        // 요약 정보 업데이트
        const { summary, pagedData } = data;
        this.elements.summary.totalHours.innerText = summary.totalWorkTimeDisplay || '0h 00m';
        this.elements.summary.lateCount.innerText = summary.lateCount || 0;
        this.elements.summary.absentCount.innerText = summary.leaveAbsentCount || 0;

        this.renderTable(pagedData.content);
        this.renderPagination(pagedData);
    },

    renderTable(records) {
        if (!records || records.length === 0) {
            this.elements.tableBody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 100px; color:#999;">조회된 기록이 없습니다.</td></tr>';
            return;
        }

        this.elements.tableBody.innerHTML = records.map(r => `
            <tr>
                <td style="font-weight:600;">${r.date}</td>
                <td>${r.commuteAt || '-'}</td>
                <td>${r.leaveAt || '-'}</td>
                <td>${r.workingTime || '0h 00m'}</td>
                <td><span class="status-badge ${this.getBadgeClass(r.statusCode)}">${r.statusName}</span></td>
            </tr>`).join('');
    },

    getBadgeClass(code) {
        const badgeMap = {
            'LATE': 'badge-late',
            'ABSENT': 'badge-absent',
            'ON_TIME': 'badge-normal',
            'VACATION': 'badge-vacation'
        };
        return badgeMap[code] || 'badge-normal';
    },

    renderPagination(pageInfo) {
        this.elements.pagination.innerHTML = '';
        const { number: page, totalPages } = pageInfo;

        const createButton = (content, isDisabled, onClick) => {
            const btn = document.createElement('button');
            btn.className = (typeof content === 'number') ? 'page-number' : 'page-btn';
            if (content === page + 1) btn.classList.add('active');
            btn.innerHTML = content;
            btn.disabled = isDisabled;
            btn.onclick = onClick;
            return btn;
        };

        // 이전 버튼
        this.elements.pagination.appendChild(createButton('<i class="fas fa-chevron-left"></i>', page === 0, () => {
            this.state.page = page - 1;
            this.executeSearch();
        }));

        // 페이지 번호 (최대 5개 노출 로직)
        const maxVisible = 5;
        let start = Math.max(0, page - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages - 1, start + maxVisible - 1);
        if (end - start < maxVisible - 1) start = Math.max(0, end - maxVisible + 1);

        for (let i = start; i <= end; i++) {
            this.elements.pagination.appendChild(createButton(i + 1, false, () => {
                this.state.page = i;
                this.executeSearch();
            }));
        }

        // 다음 버튼
        this.elements.pagination.appendChild(createButton('<i class="fas fa-chevron-right"></i>', page >= totalPages - 1, () => {
            this.state.page = page + 1;
            this.executeSearch();
        }));
    },

    updateSidebar() {
        document.querySelectorAll('.menu-item.no-sub').forEach(item => item.classList.remove('selected'));
        const menuItem = document.getElementById('monthlyLink')?.closest('.menu-item.no-sub');
        if (menuItem) menuItem.classList.add('selected');
    }
};

// 타임리프 DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', () => MonthlyAttendance.init());