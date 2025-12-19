// API 기본 URL
const API_BASE_URL = '/api/admin/rules';

// 전역 변수
let currentPage = 1;
let totalPages = 1;
let exceptionRules = [];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadDefaultRule();
    loadExceptionRules();
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // 기본 규칙 저장
    const saveBtn = document.getElementById('saveDefaultRuleBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveDefaultRule);
    }

    // 예외 규칙 추가 버튼
    const addBtn = document.getElementById('addExceptionRuleBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기
    const closeBtn = document.querySelector('.close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }
    
    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('exceptionRuleModal');
        if (modal && event.target === modal) {
            closeModal();
        }
    });

    // 예외 규칙 폼 제출
    const form = document.getElementById('exceptionRuleForm');
    if (form) {
        form.addEventListener('submit', handleExceptionRuleSubmit);
    }
}

// 기본 규칙 로드
async function loadDefaultRule() {
    try {
        const response = await fetch(`${API_BASE_URL}/default`);
        if (!response.ok) {
            // 404나 다른 에러일 경우 기본값 유지
            console.warn('기본 규칙을 불러올 수 없습니다. 기본값을 사용합니다.');
            return;
        }

        const data = await response.json();

        // 시간 형식 변환 (HH:mm:ss -> HH:mm)
        const startTimeInput = document.getElementById('defaultStartTime');
        const endTimeInput = document.getElementById('defaultEndTime');
        const breakMinutesInput = document.getElementById('defaultBreakMinutes');
        
        if (data.defaultStartTime && startTimeInput) {
            startTimeInput.value = data.defaultStartTime.substring(0, 5);
        }
        if (data.defaultEndTime && endTimeInput) {
            endTimeInput.value = data.defaultEndTime.substring(0, 5);
        }
        if (data.defaultBreakMinutes !== null && data.defaultBreakMinutes !== undefined && breakMinutesInput) {
            breakMinutesInput.value = data.defaultBreakMinutes;
        }
    } catch (error) {
        console.error('Error loading default rule:', error);
        // 조용히 실패 (기본값 사용)
    }
}

// 기본 규칙 저장
async function saveDefaultRule() {
    const startTimeInput = document.getElementById('defaultStartTime');
    const endTimeInput = document.getElementById('defaultEndTime');
    const breakMinutesInput = document.getElementById('defaultBreakMinutes');
    const tardinessInput = document.getElementById('tardinessMinutes');
    const earlyLeaveInput = document.getElementById('earlyLeaveMinutes');

    if (!startTimeInput || !endTimeInput || !breakMinutesInput) {
        alert('입력 필드를 찾을 수 없습니다.');
        return;
    }

    const startTime = startTimeInput.value;
    const endTime = endTimeInput.value;
    const breakMinutes = parseInt(breakMinutesInput.value) || 0;

    if (!startTime || !endTime) {
        alert('출근 시간과 퇴근 시간을 모두 입력해주세요.');
        return;
    }

    const requestData = {
        defaultStartTime: startTime + ':00',
        defaultEndTime: endTime + ':00',
        defaultBreakMinutes: breakMinutes
    };

    try {
        const response = await fetch(`${API_BASE_URL}/default`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || '저장에 실패했습니다.');
        }

        alert('기본 규칙이 저장되었습니다.');
    } catch (error) {
        console.error('Error saving default rule:', error);
        alert('저장에 실패했습니다: ' + error.message);
    }
}

// 예외 규칙 목록 로드
async function loadExceptionRules(page = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/exception`);
        if (!response.ok) {
            // 404나 빈 배열일 경우 빈 테이블 표시
            renderExceptionRulesTable([]);
            renderPagination();
            return;
        }

        const data = await response.json();
        exceptionRules = Array.isArray(data) ? data : [];

        // 페이지네이션 계산 (예시: 페이지당 10개)
        const itemsPerPage = 10;
        totalPages = Math.ceil(exceptionRules.length / itemsPerPage);
        currentPage = page;

        const startIndex = (page - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const pageData = exceptionRules.slice(startIndex, endIndex);

        renderExceptionRulesTable(pageData);
        renderPagination();

        // 사원 목록 로드 (모달용)
        await loadEmployees();
    } catch (error) {
        console.error('Error loading exception rules:', error);
        // 에러 발생 시 빈 테이블 표시
        renderExceptionRulesTable([]);
        renderPagination();
    }
}

// 예외 규칙 테이블 렌더링
function renderExceptionRulesTable(rules) {
    const tbody = document.getElementById('exceptionRulesTableBody');
    if (!tbody) {
        console.error('exceptionRulesTableBody element not found');
        return;
    }
    
    tbody.innerHTML = '';

    if (!rules || rules.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 40px; color: #999;">등록된 예외 규칙이 없습니다.</td></tr>';
        return;
    }

    rules.forEach(rule => {
        const row = document.createElement('tr');

        const timeRange = rule.startTime && rule.endTime
            ? `${rule.startTime.substring(0, 5)}~${rule.endTime.substring(0, 5)}`
            : '-';

        const validPeriod = rule.validFrom
            ? `${rule.validFrom}${rule.validTo ? '~' + rule.validTo : '~무기한'}`
            : '-';

        row.innerHTML = `
                <td>${rule.employeeName || '알 수 없음'}</td>
                <td>${timeRange}</td>
                <td>${rule.reason || '-'}</td>
                <td>${rule.breakMinutes !== null ? rule.breakMinutes + '분' : '-'}</td>
                <td>${validPeriod}</td>
                <td class="action-links">
                    <a href="#" class="edit-link" data-rule-id="${rule.overrideId}">수정</a>
                    <a href="#" class="delete-link" data-rule-id="${rule.overrideId}">삭제</a>
                </td>
            `;

        tbody.appendChild(row);
    });

    // 수정/삭제 이벤트 리스너
    tbody.querySelectorAll('.edit-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const ruleId = e.target.getAttribute('data-rule-id');
            openEditModal(ruleId);
        });
    });

    tbody.querySelectorAll('.delete-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const ruleId = e.target.getAttribute('data-rule-id');
            deleteExceptionRule(ruleId);
        });
    });
}

// 페이지네이션 렌더링
function renderPagination() {
    const pagination = document.getElementById('pagination');
    if (!pagination) {
        console.error('pagination element not found');
        return;
    }
    
    pagination.innerHTML = '';

    if (totalPages <= 1) return;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '<';
    prevBtn.disabled = currentPage === 1;
    prevBtn.addEventListener('click', () => {
        if (currentPage > 1) loadExceptionRules(currentPage - 1);
    });
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    if (startPage > 1) {
        const firstPage = document.createElement('span');
        firstPage.className = 'page-number';
        firstPage.textContent = '1';
        firstPage.addEventListener('click', () => loadExceptionRules(1));
        pagination.appendChild(firstPage);

        if (startPage > 2) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.style.cursor = 'default';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageSpan = document.createElement('span');
        pageSpan.className = 'page-number' + (i === currentPage ? ' active' : '');
        pageSpan.textContent = i;
        pageSpan.addEventListener('click', () => loadExceptionRules(i));
        pagination.appendChild(pageSpan);
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.style.cursor = 'default';
            pagination.appendChild(ellipsis);
        }

        const lastPage = document.createElement('span');
        lastPage.className = 'page-number';
        lastPage.textContent = totalPages;
        lastPage.addEventListener('click', () => loadExceptionRules(totalPages));
        pagination.appendChild(lastPage);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '>';
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.addEventListener('click', () => {
        if (currentPage < totalPages) loadExceptionRules(currentPage + 1);
    });
    pagination.appendChild(nextBtn);
}

// 사원 목록 로드 (모달용)
async function loadEmployees() {
    // 실제로는 사원 목록 API를 호출해야 하지만, 여기서는 예시로 처리
    // TODO: 실제 사원 목록 API 연동 필요
    const employeeSelect = document.getElementById('employeeSelect');
    if (employeeSelect.options.length > 1) return; // 이미 로드됨

    // 예시 데이터 (실제로는 API에서 가져와야 함)
    const employees = [
        { id: 1, name: '김민혁', department: '개발팀' },
        { id: 2, name: '이승아', department: '인사팀' },
        { id: 3, name: '박성구', department: '영업팀' }
    ];

    employees.forEach(emp => {
        const option = document.createElement('option');
        option.value = emp.id;
        option.textContent = `${emp.name} (${emp.department})`;
        employeeSelect.appendChild(option);
    });
}

// 모달 열기 (추가)
function openAddModal() {
    const modal = document.getElementById('exceptionRuleModal');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('exceptionRuleForm');
    const ruleId = document.getElementById('ruleId');
    const validTo = document.getElementById('validTo');
    
    if (!modal || !modalTitle || !form) {
        console.error('Modal elements not found');
        return;
    }
    
    modalTitle.textContent = '예외 규칙 추가';
    form.reset();
    if (ruleId) ruleId.value = '';
    if (validTo) validTo.value = '';
    modal.style.display = 'block';
}

// 모달 열기 (수정)
async function openEditModal(ruleId) {
    if (!ruleId) {
        console.error('Rule ID is required');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/exception/${ruleId}`);
        if (!response.ok) {
            throw new Error('규칙을 불러오는데 실패했습니다.');
        }

        const rule = await response.json();

        const modal = document.getElementById('exceptionRuleModal');
        const modalTitle = document.getElementById('modalTitle');
        const ruleIdInput = document.getElementById('ruleId');
        const employeeSelect = document.getElementById('employeeSelect');
        const startTimeInput = document.getElementById('exceptionStartTime');
        const endTimeInput = document.getElementById('exceptionEndTime');
        const reasonInput = document.getElementById('reason');
        const breakMinutesInput = document.getElementById('exceptionBreakMinutes');
        const validFromInput = document.getElementById('validFrom');
        const validToInput = document.getElementById('validTo');

        if (!modal || !modalTitle) {
            console.error('Modal elements not found');
            return;
        }

        modalTitle.textContent = '예외 규칙 수정';
        if (ruleIdInput) ruleIdInput.value = rule.overrideId || ruleId;
        if (employeeSelect) employeeSelect.value = rule.employeeId || '';
        if (startTimeInput) startTimeInput.value = rule.startTime ? rule.startTime.substring(0, 5) : '';
        if (endTimeInput) endTimeInput.value = rule.endTime ? rule.endTime.substring(0, 5) : '';
        if (reasonInput) reasonInput.value = rule.reason || '';
        if (breakMinutesInput) breakMinutesInput.value = rule.breakMinutes || '';
        if (validFromInput) validFromInput.value = rule.validFrom || '';
        if (validToInput) validToInput.value = rule.validTo || '';

        modal.style.display = 'block';
    } catch (error) {
        console.error('Error loading rule:', error);
        alert('규칙을 불러오는데 실패했습니다: ' + error.message);
    }
}

// 모달 닫기
function closeModal() {
    const modal = document.getElementById('exceptionRuleModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// 예외 규칙 폼 제출 처리
async function handleExceptionRuleSubmit(e) {
    e.preventDefault();

    const ruleIdInput = document.getElementById('ruleId');
    const employeeSelect = document.getElementById('employeeSelect');
    const startTimeInput = document.getElementById('exceptionStartTime');
    const endTimeInput = document.getElementById('exceptionEndTime');
    const breakMinutesInput = document.getElementById('exceptionBreakMinutes');
    const reasonInput = document.getElementById('reason');
    const validFromInput = document.getElementById('validFrom');
    const validToInput = document.getElementById('validTo');

    if (!employeeSelect || !startTimeInput || !endTimeInput || !reasonInput || !validFromInput) {
        alert('필수 입력 필드를 찾을 수 없습니다.');
        return;
    }

    const ruleId = ruleIdInput ? ruleIdInput.value : '';
    const employeeId = parseInt(employeeSelect.value);
    const startTime = startTimeInput.value;
    const endTime = endTimeInput.value;
    const reason = reasonInput.value;
    const validFrom = validFromInput.value;

    if (!employeeId || !startTime || !endTime || !reason || !validFrom) {
        alert('필수 항목을 모두 입력해주세요.');
        return;
    }

    const formData = {
        employeeId: employeeId,
        startTime: startTime + ':00',
        endTime: endTime + ':00',
        breakMinutes: breakMinutesInput ? (parseInt(breakMinutesInput.value) || null) : null,
        reason: reason,
        validFrom: validFrom,
        validTo: validToInput && validToInput.value ? validToInput.value : null
    };

    try {
        let response;
        if (ruleId) {
            // 수정
            response = await fetch(`${API_BASE_URL}/exception/${ruleId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        } else {
            // 추가
            response = await fetch(`${API_BASE_URL}/exception`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || '저장에 실패했습니다.');
        }

        alert(ruleId ? '규칙이 수정되었습니다.' : '규칙이 추가되었습니다.');
        closeModal();
        loadExceptionRules(currentPage);
    } catch (error) {
        console.error('Error saving exception rule:', error);
        alert('저장에 실패했습니다: ' + error.message);
    }
}

// 예외 규칙 삭제
async function deleteExceptionRule(ruleId) {
    if (!ruleId) {
        console.error('Rule ID is required');
        return;
    }

    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/exception/${ruleId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || '삭제에 실패했습니다.');
        }

        alert('규칙이 삭제되었습니다.');
        loadExceptionRules(currentPage);
    } catch (error) {
        console.error('Error deleting exception rule:', error);
        alert('삭제에 실패했습니다: ' + error.message);
    }
}

