/**
 * 전역 상태 관리
 */
let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL',
    keyword: ''
};

document.addEventListener('DOMContentLoaded', function() {
    // 오늘 날짜 표시
    const today = new Date();
    const formattedToday = today.getFullYear() + '.' +
        String(today.getMonth() + 1).padStart(2, '0') + '.' +
        String(today.getDate()).padStart(2, '0');

    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) {
        todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;
    }

    // 날짜 입력 필드 실시간 검증 제거 (에러 메시지 표시하지 않음)

    // 정정 모달 정정 사유 실시간 검증 및 글자 수 카운터
    const modalReasonInput = document.getElementById('modalReason');
    const charCountElement = document.getElementById('charCount');
    if (modalReasonInput) {
        modalReasonInput.addEventListener('input', function() {
            clearFieldError('modalReasonError', 'modalReason');
            const reason = this.value;
            const currentLength = reason.length;
            const maxLength = 40;
            
            // 글자 수 업데이트
            if (charCountElement) {
                charCountElement.textContent = `${currentLength} / ${maxLength}`;
                // 40자에 가까워지면 색상 변경
                if (currentLength >= maxLength) {
                    charCountElement.style.color = 'var(--danger-color)';
                } else if (currentLength >= maxLength * 0.8) {
                    charCountElement.style.color = '#f59e0b';
                } else {
                    charCountElement.style.color = 'var(--neutral-500)';
                }
            }
            
            if (reason.trim() && reason.length > maxLength) {
                showError('modalReasonError', `정정 사유는 ${maxLength}자 이하여야 합니다.`);
            }
        });
    }

    // 모달 외부 클릭 시 닫기
    const modal = document.getElementById('correctionModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });
    }

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const modal = document.getElementById('correctionModal');
            if (modal && modal.style.display === 'flex') {
                closeModal();
            }
        }
    });

    // 초기 데이터 로드
    loadSummaryData();
    loadAttendanceList();
});

/**
 * 상단 통계 데이터 로드
 */
async function loadSummaryData() {
    try {
        const response = await fetch('/api/admin/attendance/summary', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        if (response.ok) {
            const data = await response.json();
            document.getElementById('totalEmployees').innerText = data.totalEmployees || 0;
            document.getElementById('onTimeCount').innerText = data.onTimeCount || 0;
            document.getElementById('lateCount').innerText = data.lateCount || 0;
            document.getElementById('notLeavingCount').innerText = data.notLeavingCount || 0;
        }
    } catch (error) {
        console.error("통계 데이터 로드 실패:", error);
    }
}
/**
 * 근태 목록 데이터 로드 (정렬 로직 제거 버전)
 */
async function loadAttendanceList() {
    const { page, size, startDate, endDate, status, keyword } = currentSearchParams;

    const params = new URLSearchParams({
        page: page,
        size: size,
        startDate: startDate,
        endDate: endDate,
        status: status,
        keyword: keyword
    });

    try {
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            // 서버에서 이미 정렬되어 오므로 content를 그대로 사용합니다.
            const list = data.content || (Array.isArray(data) ? data : []);


            renderAttendanceTable(list);
            renderPagination(data);
        } else {
            const tbody = document.querySelector('#attendanceTable tbody');
            tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="color:red;">데이터 로드 실패</td></tr>';
        }
    } catch (error) {
        console.error("목록 로드 실패:", error);
    }
}

/**
 * 테이블 렌더링 (보완된 근무 중 로직 포함)
 */
function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const commuteStyle = item.statusCode === 'LATE' ? 'color: #ff9800; font-weight: bold;' : '';

        // 출근 기록이 있을 때만 "근무 중" 여부 판단 로직
        let leaveTimeDisplay = item.leaveAt || '-';
        if (item.commuteAt && item.commuteAt !== '-' && (!item.leaveAt || item.leaveAt === '-')) {
            leaveTimeDisplay = '<span style="color: #2196F3; font-weight: bold;">근무 중</span>';
        } else if (!item.commuteAt || item.commuteAt === '-') {
            leaveTimeDisplay = '-';
        }

        return `
            <tr>
                <td><strong>${item.employeeName}</strong><br><small>${item.employeeNum}</small></td>
                <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
                <td>${leaveTimeDisplay}</td>
                <td>${item.workingTime || '-'}</td>
                <td><span class="status-badge ${item.statusCode}">${item.statusName}</span></td>
                <td>${item.workDate || '-'}</td>
                <td>
                    <button class="btn-table-action" onclick="openCorrectionModal(${item.attendanceId}, '${item.statusCode}')">
                        ${item.isCorrected ? '수정됨' : '정정'}
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}


/**
 * 에러 메시지 초기화 함수
 */
function clearAllErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
    
    const errorInputs = document.querySelectorAll('.error');
    errorInputs.forEach(el => {
        el.classList.remove('error');
    });
}

/**
 * 에러 메시지 표시 함수
 */
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    const inputElement = document.getElementById(elementId.replace('Error', ''));
    
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
    
    if (inputElement) {
        inputElement.classList.add('error');
        inputElement.focus();
    }
}

/**
 * 개별 필드 에러 초기화 함수
 */
function clearFieldError(errorElementId, inputElementId) {
    const errorElement = document.getElementById(errorElementId);
    const inputElement = document.getElementById(inputElementId);
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
    if (inputElement) {
        inputElement.classList.remove('error');
    }
}

/**
 * 검색 필터 유효성 검사 (에러 메시지 없이 검증만 수행)
 */
function validateSearchFilters() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const keyword = document.getElementById('searchKeyword').value.trim();

    // 시작일과 종료일 검증 (에러 메시지 표시하지 않음)
    if (startDate && endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        if (end < start) {
            return false; // 검색 차단만 함
        }
    }

    // 검색어 길이 검증 (이미 maxlength 있지만 추가 검증)
    if (keyword && keyword.length > 100) {
        return false; // 검색 차단만 함
    }

    return true;
}

/**
 * 검색 처리
 */
function handleSearch() {
    // 유효성 검사 (에러 메시지 표시하지 않고 검색만 차단)
    if (!validateSearchFilters()) {
        return; // 검색하지 않음
    }

    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.page = 0;

    const tbody = document.querySelector('#attendanceTable tbody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center">데이터를 검색 중입니다...</td></tr>';

    loadAttendanceList();
}

/**
 * 필터 초기화
 */
function resetFilters() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('statusFilter').value = 'ALL';
    document.getElementById('searchKeyword').value = '';
    
    currentSearchParams = {
        page: 0,
        size: 10,
        startDate: '',
        endDate: '',
        status: 'ALL',
        keyword: ''
    };
    
    loadAttendanceList();
}

/**
 * 모달 및 정정 제출
 */
function openCorrectionModal(id, status) {
    clearFieldError('modalReasonError', 'modalReason');
    document.getElementById('targetAttendanceId').value = id;
    document.getElementById('modalStatus').value = status;
    document.getElementById('modalReason').value = '';
    
    // 글자 수 카운터 초기화
    const charCountElement = document.getElementById('charCount');
    if (charCountElement) {
        charCountElement.textContent = '0 / 40';
        charCountElement.style.color = 'var(--neutral-500)';
    }
    
    const modal = document.getElementById('correctionModal');
    modal.style.display = 'flex';
    modal.style.alignItems = 'center';
    modal.style.justifyContent = 'center';
    // 모달이 열릴 때 body 스크롤 방지
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    const modal = document.getElementById('correctionModal');
    modal.style.display = 'none';
    document.getElementById('modalReason').value = '';
    clearFieldError('modalReasonError', 'modalReason');
    // 모달이 닫힐 때 body 스크롤 복원
    document.body.style.overflow = '';
}

/**
 * 정정 모달 유효성 검사
 */
function validateCorrectionForm() {
    clearFieldError('modalReasonError', 'modalReason');
    let isValid = true;

    const reason = document.getElementById('modalReason').value.trim();

    if (!reason) {
        showError('modalReasonError', '정정 사유를 입력해주세요.');
        isValid = false;
    } else if (reason.length > 40) {
        showError('modalReasonError', '정정 사유는 40자 이하여야 합니다.');
        isValid = false;
    }

    return isValid;
}

async function submitCorrection() {
    // 유효성 검사
    if (!validateCorrectionForm()) {
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;
    const reason = document.getElementById('modalReason').value.trim();

    try {
        const response = await fetch(`/api/admin/attendance/update/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            },
            body: JSON.stringify({ status: status, correctionReason: reason.trim() })
        });

        if (response.ok) {
            alert("성공적으로 정정되었습니다.");
            closeModal();
            loadAttendanceList();
            loadSummaryData();
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || "정정 처리에 실패했습니다.");
        }
    } catch (error) {
        console.error("정정 요청 중 오류:", error);
    }
}

/**
 * 페이징 렌더링 (게시판과 동일한 구조)
 */
function renderPagination(pageData) {
    const pagination = document.getElementById('boardPagination');
    if (!pagination || pageData.totalPages === undefined) return;

    pagination.innerHTML = '';

    const page = pageData.number;
    const totalPages = pageData.totalPages;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => {
        if (page > 0) {
            currentSearchParams.page = page - 1;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        }
    };
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisible = 5;
    let startPage = Math.max(0, page - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }

    if (startPage > 0) {
        const firstBtn = document.createElement('button');
        firstBtn.className = 'page-number';
        firstBtn.textContent = '1';
        firstBtn.onclick = () => {
            currentSearchParams.page = 0;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        };
        pagination.appendChild(firstBtn);

        if (startPage > 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = 'page-number';
        if (i === page) {
            pageBtn.classList.add('active');
        }
        pageBtn.textContent = i + 1;
        pageBtn.onclick = () => {
            currentSearchParams.page = i;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        };
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }

        const lastBtn = document.createElement('button');
        lastBtn.className = 'page-number';
        lastBtn.textContent = totalPages;
        lastBtn.onclick = () => {
            currentSearchParams.page = totalPages - 1;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        };
        pagination.appendChild(lastBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.className = 'page-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = page >= totalPages - 1;
    nextBtn.onclick = () => {
        if (page < totalPages - 1) {
            currentSearchParams.page = page + 1;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            loadAttendanceList();
        }
    };
    pagination.appendChild(nextBtn);
}