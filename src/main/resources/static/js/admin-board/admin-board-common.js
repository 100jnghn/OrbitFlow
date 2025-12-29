// 공용 게시판 관리 페이지 JavaScript

const API_BASE_URL = '/api/admin/board-categories';
const EMPLOYEE_SEARCH_API = '/api/admin/rules/employees/search';
let currentBoardPage = 0;
let totalBoardPages = 1;
let isEditMode = false;
let editingBoardId = null;
let selectedEmployees = []; // 선택된 사원 목록
let deletingBoardId = null; // 삭제할 게시판 ID

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    try {
        loadBoardList();
        
        // 모달 폼 이벤트 리스너
        const boardForm = document.getElementById('boardForm');
        if (boardForm) {
            boardForm.addEventListener('submit', handleBoardSubmit);
        } else {
            console.warn('boardForm element not found');
        }
    } catch (error) {
        console.error('Error initializing board admin page:', error);
    }
});

// 게시판 목록 로드
async function loadBoardList(page = 0) {
    try {
        // Spring Data의 sort 파라미터 형식: sort=createdAt,desc (방향 포함)
        // 페이지 크기를 8로 설정하여 페이징 처리
        const response = await apiFetch(`${API_BASE_URL}?organizationOnly=false&page=${page}&size=8&sort=createdAt,desc`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                // 인증 실패 시 로그인 페이지로 리다이렉트
                location.href = '/login';
                return;
            }
            throw new Error('게시판 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        console.log('Board list response:', result); // 디버깅용
        console.log('Response structure:', JSON.stringify(result, null, 2));
        
        // ResponseDto 구조 확인
        if (!result) {
            console.error('No response received');
            alert('게시판 목록을 불러오는데 실패했습니다.');
            return;
        }
        
        // status가 200인지 확인 (ResponseDto 구조)
        if (result.status && result.status !== 200) {
            console.error('Invalid status:', result.status, result.message);
            alert(result.message || '게시판 목록을 불러오는데 실패했습니다.');
            return;
        }
        
        // data 추출 (ResponseDto.data 또는 직접 Page 객체)
        let data = result.data;
        
        // data가 없으면 result 자체가 Page 객체일 수 있음
        if (!data && result.content) {
            data = result;
        }
        
        if (!data) {
            console.error('No data in response:', result);
            alert('게시판 목록 데이터가 없습니다.');
            return;
        }
        
        console.log('Extracted data:', data);
        console.log('Data type:', typeof data);
        console.log('Data keys:', Object.keys(data));
        
        // Page 객체 구조 확인 (content, number, totalPages 등)
        const content = data.content || data.elements || (Array.isArray(data) ? data : []);
        const number = data.number !== undefined ? data.number : (data.pageNumber !== undefined ? data.pageNumber : 0);
        const totalPages = data.totalPages !== undefined ? data.totalPages : (data.totalPageCount !== undefined ? data.totalPageCount : 1);
        const totalElements = data.totalElements !== undefined ? data.totalElements : (data.total !== undefined ? data.total : content.length);
        
        console.log('Page info:', { 
            content: content.length, 
            number, 
            totalPages,
            totalElements: totalElements,
            size: data.size || 8,
            first: data.first !== undefined ? data.first : (number === 0),
            last: data.last !== undefined ? data.last : (number === totalPages - 1)
        });
        
        currentBoardPage = number;
        totalBoardPages = totalPages;
        
        console.log(`현재 페이지: ${currentBoardPage + 1}/${totalBoardPages}, 게시판 수: ${content.length}개, 전체: ${totalElements}개`);
        
        renderBoardTable(content);
        renderBoardPagination();
    } catch (error) {
        console.error('Error loading board list:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('게시판 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 게시판 테이블 렌더링
function renderBoardTable(boards) {
    const tbody = document.getElementById('boardTableBody');
    tbody.innerHTML = '';

    if (boards.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 40px; color: #9ca3af;">등록된 게시판이 없습니다.</td></tr>';
        return;
    }

    boards.forEach((board, index) => {
        const row = document.createElement('tr');
        const rowNumber = currentBoardPage * 8 + index + 1;
        
        const createdAt = new Date(board.createdAt).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });

        row.innerHTML = `
            <td>${rowNumber}</td>
            <td>${escapeHTML(board.boardName || board.name || '')}</td>
            <td>${createdAt}</td>
            <td>
                <button class="btn-action btn-modify" onclick="openEditBoardModal(${board.id})">수정</button>
                <button class="btn-action btn-delete" onclick="deleteBoard(${board.id})">삭제</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 게시판 페이지네이션 렌더링
function renderBoardPagination() {
    const pagination = document.getElementById('boardPagination');
    pagination.innerHTML = '';

    // 페이징 버튼 상시 출력
    // if (totalBoardPages <= 1) {
    //     return;
    // }

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '‹';
    prevBtn.disabled = currentBoardPage === 0;
    prevBtn.addEventListener('click', () => {
        if (currentBoardPage > 0) loadBoardList(currentBoardPage - 1);
    });
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisible = 5;
    let startPage = Math.max(0, currentBoardPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalBoardPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }

    if (startPage > 0) {
        const firstBtn = document.createElement('span');
        firstBtn.className = 'page-number';
        firstBtn.textContent = '1';
        firstBtn.addEventListener('click', () => loadBoardList(0));
        pagination.appendChild(firstBtn);

        if (startPage > 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('span');
        pageBtn.className = 'page-number' + (i === currentBoardPage ? ' active' : '');
        pageBtn.textContent = i + 1;
        pageBtn.addEventListener('click', () => loadBoardList(i));
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalBoardPages - 1) {
        if (endPage < totalBoardPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
        const lastBtn = document.createElement('span');
        lastBtn.className = 'page-number';
        lastBtn.textContent = totalBoardPages;
        lastBtn.addEventListener('click', () => loadBoardList(totalBoardPages - 1));
        pagination.appendChild(lastBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '›';
    nextBtn.disabled = currentBoardPage >= totalBoardPages - 1;
    nextBtn.addEventListener('click', () => {
        if (currentBoardPage < totalBoardPages - 1) loadBoardList(currentBoardPage + 1);
    });
    pagination.appendChild(nextBtn);
}

// 게시판 추가 모달 열기
function openAddBoardModal() {
    isEditMode = false;
    editingBoardId = null;
    selectedEmployees = [];
    document.getElementById('modalTitle').textContent = '게시판 추가';
    document.getElementById('submitBtn').textContent = '생성';
    document.getElementById('boardForm').reset();
    document.getElementById('boardId').value = '';
    document.getElementById('employeeSearchResults').innerHTML = '';
    document.getElementById('selectedEmployees').innerHTML = '';
    // 에러 메시지 초기화
    hideError('boardNameError');
    hideError('employeeSearchError');
    document.getElementById('boardModal').style.display = 'block';
}

// 게시판 수정 모달 열기
async function openEditBoardModal(boardId) {
    try {
        const [boardResponse, permissionResponse] = await Promise.all([
            apiFetch(`${API_BASE_URL}/${boardId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            }),
            apiFetch(`/api/admin/board-permissions/board-categories/${boardId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
        ]);

        if (!boardResponse.ok) {
            if (boardResponse.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('게시판 정보를 불러오는데 실패했습니다.');
        }

        const result = await boardResponse.json();
        const board = result.data;

        isEditMode = true;
        editingBoardId = boardId;
        selectedEmployees = [];
        
        // 권한 정보 로드
        if (permissionResponse && permissionResponse.ok) {
            try {
                const permissionResult = await permissionResponse.json();
                const permissions = permissionResult.data || [];
                console.log('Loaded permissions:', permissions);
                selectedEmployees = permissions.map(p => ({
                    id: p.employeeInfo.employeeId,
                    name: p.employeeInfo.name,
                    employeeNo: p.employeeInfo.employeeNumber,
                    rankName: p.employeeInfo.rankName,
                    departmentName: p.employeeInfo.departmentName,
                    permissionId: p.permissionId // 권한 제거를 위한 ID 저장
                }));
                console.log('Mapped selectedEmployees:', selectedEmployees);
            } catch (error) {
                console.error('Error parsing permission response:', error);
                selectedEmployees = [];
            }
        } else {
            console.warn('Permission response not OK:', permissionResponse?.status);
            selectedEmployees = [];
        }

        document.getElementById('modalTitle').textContent = '게시판 수정';
        document.getElementById('submitBtn').textContent = '수정';
        document.getElementById('boardId').value = boardId;
        document.getElementById('boardName').value = board.boardName || '';
        
        // 댓글 허용 설정
        const commentRadio = document.querySelector(`input[name="commentActivated"][value="${board.commentActivated ? 'true' : 'false'}"]`);
        if (commentRadio) {
            commentRadio.checked = true;
        }
        
        // 선택된 사원 표시
        renderSelectedEmployees();
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('boardModal').style.display = 'block';
    } catch (error) {
        console.error('Error loading board detail:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('게시판 정보를 불러오는데 실패했습니다.');
        }
    }
}

// 게시판 모달 닫기
function closeBoardModal() {
    document.getElementById('boardModal').style.display = 'none';
    document.getElementById('boardForm').reset();
    document.getElementById('employeeSearchResults').innerHTML = '';
    selectedEmployees = [];
    renderSelectedEmployees();
    // 에러 메시지 초기화
    hideError('boardNameError');
    hideError('employeeSearchError');
    isEditMode = false;
    editingBoardId = null;
}

// 에러 메시지 표시/숨김 함수
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

function hideError(elementId) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.style.display = 'none';
    }
}

// 게시판 폼 제출 처리
async function handleBoardSubmit(e) {
    e.preventDefault();

    const boardName = document.getElementById('boardName').value.trim();
    const commentActivated = document.querySelector('input[name="commentActivated"]:checked').value === 'true';

    // 에러 메시지 초기화
    hideError('boardNameError');

    if (!boardName) {
        showError('boardNameError', '게시판 이름을 입력해주세요.');
        return;
    }

    if (boardName.length > 100) {
        showError('boardNameError', '게시판 이름은 100자 이하여야 합니다.');
        return;
    }

    try {
        const formData = {
            boardName: boardName,
            boardType: 'FREE', // 기본값, 필요시 수정
            isActivated: true,
            commentActivated: commentActivated
        };

        const url = isEditMode 
            ? `${API_BASE_URL}/${editingBoardId}`
            : API_BASE_URL;
        const method = isEditMode ? 'PUT' : 'POST';

        const response = await apiFetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const error = await response.json();
            throw new Error(error.message || '게시판 저장에 실패했습니다.');
        }

        const result = await response.json();
        const savedBoardId = result.data || editingBoardId;

        // 권한 저장
        if (selectedEmployees.length > 0 && savedBoardId) {
            await saveBoardPermissions(savedBoardId);
        }

        alert(isEditMode ? '게시판이 수정되었습니다.' : '게시판이 추가되었습니다.');
        closeBoardModal();
        loadBoardList(currentBoardPage);
    } catch (error) {
        console.error('Error saving board:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert(error.message || '게시판 저장에 실패했습니다.');
        }
    }
}

// 사원 검색
async function searchEmployee() {
    const searchInput = document.getElementById('employeeSearchInput');
    const keyword = searchInput?.value.trim(); // trim은 하지만 공백 포함 검색 허용을 위해 원본 값 사용
    
    // 에러 메시지 초기화
    hideError('employeeSearchError');
    const resultsDiv = document.getElementById('employeeSearchResults');
    if (resultsDiv) {
        resultsDiv.innerHTML = '';
    }

    // Validation: 최소 2자, 최대 30자
    if (!keyword || keyword.length < 2) {
        showError('employeeSearchError', '검색어는 최소 2자 이상 입력해주세요.');
        return;
    }

    if (keyword.length > 30) {
        showError('employeeSearchError', '검색어는 30자 이하여야 합니다.');
        return;
    }

    try {
        const response = await apiFetch(`${EMPLOYEE_SEARCH_API}?keyword=${encodeURIComponent(keyword)}`, {
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
            throw new Error('사원 검색에 실패했습니다.');
        }

        const result = await response.json();
        // ResponseDto 구조인 경우 data 추출, 아니면 직접 사용
        const employees = result.data || result;
        
        // 검색 결과가 50건 이상이면 안내 메시지만 표시 (검색 결과는 표시하지 않음)
        if (Array.isArray(employees) && employees.length >= 50) {
            showError('employeeSearchError', '검색 결과가 너무 많습니다. 검색어를 더 입력해주세요.');
            if (resultsDiv) {
                resultsDiv.innerHTML = '';
            }
        } else {
            displayEmployeeSearchResults(employees);
            // 검색 성공 시 에러 메시지 숨김
            hideError('employeeSearchError');
        }
    } catch (error) {
        console.error('Error searching employees:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            showError('employeeSearchError', '사원 검색에 실패했습니다.');
        }
    }
}

// 사원 검색 결과 표시
function displayEmployeeSearchResults(employees) {
    const resultsDiv = document.getElementById('employeeSearchResults');
    resultsDiv.innerHTML = '';

    if (employees.length === 0) {
        resultsDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
        return;
    }

    const list = document.createElement('ul');
    list.className = 'employee-list';

    employees.forEach(emp => {
        // 이미 선택된 사원은 제외
        if (selectedEmployees.some(se => se.id === emp.id)) {
            return;
        }

        const item = document.createElement('li');
        item.className = 'employee-item';
        item.innerHTML = `
            <div class="employee-name">${escapeHTML(emp.name)}</div>
            <div class="employee-details">${escapeHTML(emp.employeeNo)} | ${escapeHTML(emp.organizationName || '')} | ${escapeHTML(emp.positionName || '')}</div>
        `;
        item.addEventListener('click', () => selectEmployee(emp));
        list.appendChild(item);
    });

    resultsDiv.appendChild(list);
}

// 사원 선택
async function selectEmployee(employee) {
    if (selectedEmployees.some(se => se.id === employee.id)) {
        return; // 이미 선택된 사원
    }

    // 수정 모드이고 게시판 ID가 있는 경우 즉시 권한 부여
    if (isEditMode && editingBoardId) {
        try {
            const response = await apiFetch('/api/admin/board-permissions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    boardCategoryId: editingBoardId,
                    employeeIds: [employee.id]
                })
            });

            if (!response.ok) {
                if (response.status === 401) {
                    location.href = '/login';
                    return;
                }
                const errorText = await response.text();
                console.error('Permission grant failed:', response.status, errorText);
                alert('권한 부여에 실패했습니다.');
                return;
            }

            const result = await response.json();
            const permissions = result.data || [];
            
            // 권한이 성공적으로 부여된 경우
            if (permissions.length > 0) {
                const permission = permissions[0];
                selectedEmployees.push({
                    id: employee.id,
                    name: employee.name,
                    employeeNo: employee.employeeNo,
                    rankName: employee.positionName || '',
                    departmentName: employee.organizationName || '',
                    permissionId: permission.permissionId // 권한 ID 저장
                });
                console.log('Permission granted successfully:', permission);
            } else {
                // 권한이 이미 있는 경우 (중복 방지로 인해 반환되지 않음)
                selectedEmployees.push({
                    id: employee.id,
                    name: employee.name,
                    employeeNo: employee.employeeNo,
                    rankName: employee.positionName || '',
                    departmentName: employee.organizationName || '',
                    permissionId: null
                });
            }
        } catch (error) {
            console.error('Error granting permission:', error);
            alert('권한 부여에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
            return;
        }
    } else {
        // 추가 모드이거나 수정 모드가 아닌 경우
        selectedEmployees.push({
            id: employee.id,
            name: employee.name,
            employeeNo: employee.employeeNo,
            rankName: employee.positionName || '',
            departmentName: employee.organizationName || '',
            permissionId: null
        });
    }

    renderSelectedEmployees();
    document.getElementById('employeeSearchInput').value = '';
    document.getElementById('employeeSearchResults').innerHTML = '';
}

// 선택된 사원 제거 (전역 함수로 선언)
window.removeEmployee = async function(employeeId, permissionId) {
    console.log('removeEmployee called:', { employeeId, permissionId, isEditMode });
    
    // permissionId가 문자열 'null'이거나 null/undefined인 경우 처리
    if (permissionId === 'null' || permissionId === null || permissionId === undefined) {
        permissionId = null;
    } else {
        permissionId = parseInt(permissionId);
        if (isNaN(permissionId)) {
            permissionId = null;
        }
    }
    
    // 수정 모드이고 권한이 이미 부여된 사원인 경우 (permissionId가 있음)
    if (isEditMode && permissionId) {
        if (!confirm('이 사원의 게시판 권한을 제거하시겠습니까?')) {
            return;
        }

        try {
            console.log('Deleting permission:', permissionId);
            const response = await apiFetch(`/api/admin/board-permissions/${permissionId}`, {
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
                const errorText = await response.text();
                console.error('Permission delete failed:', response.status, errorText);
                throw new Error('권한 제거에 실패했습니다.');
            }

            // 목록에서 제거
            selectedEmployees = selectedEmployees.filter(se => se.id !== employeeId);
            renderSelectedEmployees();
            console.log('Permission removed successfully');
        } catch (error) {
            console.error('Error removing permission:', error);
            if (error.message !== 'SESSION_EXPIRED') {
                alert('권한 제거에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
            }
        }
    } else {
        // 추가 모드이거나 아직 권한이 부여되지 않은 사원인 경우
        console.log('Removing employee from list (not a permission):', employeeId);
        selectedEmployees = selectedEmployees.filter(se => se.id !== employeeId);
        renderSelectedEmployees();
    }
}

// 선택된 사원 목록 렌더링
function renderSelectedEmployees() {
    const container = document.getElementById('selectedEmployees');
    container.innerHTML = '';

    if (selectedEmployees.length === 0) {
        container.innerHTML = '<div class="no-selected-employees">권한이 부여된 사원이 없습니다.</div>';
        return;
    }

    selectedEmployees.forEach(emp => {
        const div = document.createElement('div');
        div.className = 'selected-employee';
        const permissionIdValue = emp.permissionId ? emp.permissionId : 'null';
        console.log('Rendering employee:', emp.name, 'permissionId:', permissionIdValue, 'isEditMode:', isEditMode);
        
        // 사원 정보를 한 줄로 표시: 이름 | 사번 | 부서 | 직급
        const employeeInfoText = [
            escapeHTML(emp.name || ''),
            escapeHTML(emp.employeeNo || ''),
            escapeHTML(emp.departmentName || ''),
            escapeHTML(emp.rankName || '')
        ].filter(item => item).join(' | ');
        
        // 모든 사원에 대해 항상 X 버튼 표시
        div.innerHTML = `
            <span class="employee-info">${employeeInfoText}</span>
            <button type="button"
        class="remove-btn"
        onclick="removeEmployee(${emp.id}, ${permissionIdValue})"
        title="권한 제거">
</button>
        `;
        container.appendChild(div);
    });
}

// 게시판 권한 저장 (게시판 생성 시 사용)
async function saveBoardPermissions(boardCategoryId) {
    try {
        // permissionId가 없는 사원들만 권한 부여 (이미 권한이 있는 사원은 제외)
        const employeesToGrant = selectedEmployees.filter(se => !se.permissionId);
        
        if (employeesToGrant.length === 0) {
            console.log('No new permissions to grant');
            return;
        }
        
        const employeeIds = employeesToGrant.map(se => se.id);
        
        const response = await apiFetch('/api/admin/board-permissions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                boardCategoryId: boardCategoryId,
                employeeIds: employeeIds
            })
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const errorText = await response.text();
            console.warn('권한 저장에 실패했습니다:', response.status, errorText);
            throw new Error('권한 저장에 실패했습니다.');
        }
        
        console.log('Permissions saved successfully');
    } catch (error) {
        console.error('Error saving permissions:', error);
        throw error; // 상위로 에러 전달
    }
}

// 게시판 삭제
function deleteBoard(boardId) {
    deletingBoardId = boardId;
    document.getElementById('deleteModal').style.display = 'block';
}

// 삭제 확인 모달 닫기
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deletingBoardId = null;
}

// 삭제 확인
async function confirmDelete() {
    if (!deletingBoardId) return;

    try {
        const response = await apiFetch(`${API_BASE_URL}/${deletingBoardId}`, {
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
            throw new Error('게시판 삭제에 실패했습니다.');
        }

        alert('게시판이 삭제되었습니다.');
        closeDeleteModal();
        loadBoardList(currentBoardPage);
    } catch (error) {
        console.error('Error deleting board:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('게시판 삭제에 실패했습니다.');
        }
    }
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const boardModal = document.getElementById('boardModal');
    const deleteModal = document.getElementById('deleteModal');
    
    if (event.target === boardModal) {
        closeBoardModal();
    }
    if (event.target === deleteModal) {
        closeDeleteModal();
    }
}

// Enter 키로 사원 검색 및 실시간 validation
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('employeeSearchInput');
    if (searchInput) {
        // Enter 키로 검색
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchEmployee();
            }
        });
        
        // 입력 시 에러 메시지 초기화 (2자 이상 입력 시에만)
        searchInput.addEventListener('input', function() {
            const keyword = this.value.trim();
            if (keyword.length >= 2 && keyword.length <= 30) {
                hideError('employeeSearchError');
            }
        });
    }
});

