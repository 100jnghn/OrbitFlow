// 게시판 관리자 페이지 JavaScript

const API_BASE_URL = '/api/admin/board-categories';
const EMPLOYEE_SEARCH_API = '/api/admin/rules/employees/search';
let currentBoardPage = 0;
let currentOrgPage = 0;
let totalBoardPages = 1;
let totalOrgPages = 1;
let isEditMode = false;
let editingBoardId = null;
let selectedEmployees = []; // 선택된 사원 목록
let deletingBoardId = null; // 삭제할 게시판 ID

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadBoardList();
    loadOrganizationList();
    
    // 모달 폼 이벤트 리스너
    document.getElementById('boardForm').addEventListener('submit', handleBoardSubmit);
});

// 게시판 목록 로드
async function loadBoardList(page = 0) {
    try {
        const response = await apiFetch(`${API_BASE_URL}?organizationOnly=false&page=${page}&size=5&sort=createdAt,desc`, {
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
        
        if (!result.data) {
            console.error('No data in response:', result);
            alert('게시판 목록 데이터가 없습니다.');
            return;
        }
        
        const data = result.data;
        
        currentBoardPage = data.number;
        totalBoardPages = data.totalPages;
        
        renderBoardTable(data.content || []);
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
        const rowNumber = currentBoardPage * 5 + index + 1;
        
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

    if (totalBoardPages <= 1) return;

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

// 부서 목록 로드
async function loadOrganizationList(page = 0) {
    try {
        // 부서 목록과 조직 게시판 목록을 함께 가져옴
        const [orgResponse, boardResponse] = await Promise.all([
            apiFetch('/api/admin/organizations', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            }),
            apiFetch(`${API_BASE_URL}?organizationOnly=true&page=0&size=100`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
        ]);

        if (!orgResponse.ok) {
            if (orgResponse.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('부서 목록을 불러오는데 실패했습니다.');
        }

        const organizations = await orgResponse.json();
        
        // 조직 게시판 목록에서 각 부서별 활성화 상태 및 게시판 ID 확인
        let orgBoardsMap = {};
        if (boardResponse.ok) {
            const boardResult = await boardResponse.json();
            const orgBoards = boardResult.data?.content || [];
            console.log('=== Loaded organization boards ===');
            console.log('Raw board data:', JSON.stringify(orgBoards, null, 2));
            orgBoards.forEach(board => {
                if (board.organizationId) {
                    // Jackson이 boolean 필드를 직렬화할 때 필드명이 다를 수 있음
                    // isActivated, activated, is_activated 등 여러 가능성 확인
                    let isActivated = board.isActivated;
                    
                    if (isActivated === undefined) {
                        // 다른 가능한 필드명들 확인
                        isActivated = board.activated;
                    }
                    if (isActivated === undefined) {
                        isActivated = board.is_activated;
                    }
                    if (isActivated === undefined) {
                        // 모든 필드 확인
                        console.warn(`Board ${board.id}: isActivated field not found. Available fields:`, Object.keys(board));
                        // 기본값으로 true 설정 (게시판이 존재하면 기본적으로 활성화)
                        isActivated = true;
                    }
                    
                    console.log(`Board ${board.id} for org ${board.organizationId}:`, {
                        id: board.id,
                        organizationId: board.organizationId,
                        'board.isActivated': board.isActivated,
                        'board.activated': board.activated,
                        'board.is_activated': board.is_activated,
                        allFields: Object.keys(board),
                        finalIsActivated: isActivated
                    });
                    
                    orgBoardsMap[board.organizationId] = {
                        id: board.id,
                        isActivated: isActivated
                    };
                }
            });
            console.log('Organization boards map:', orgBoardsMap);
        } else {
            console.warn('Failed to load organization boards:', boardResponse.status, boardResponse.statusText);
        }
        
        // 부서 목록에 활성화 상태 및 게시판 ID 추가
        const orgsWithActivation = organizations.map(org => {
            const boardInfo = orgBoardsMap[org.id];
            const activated = boardInfo !== undefined ? boardInfo.isActivated : true;
            const boardCategoryId = boardInfo?.id || null;
            
            console.log(`Processing org ${org.id} (${org.name}):`, {
                hasBoardInfo: boardInfo !== undefined,
                boardInfo: boardInfo,
                activated: activated,
                boardCategoryId: boardCategoryId
            });
            
            return {
                ...org,
                activated: activated,
                boardCategoryId: boardCategoryId
            };
        });
        
        console.log('=== Final organizations with activation status ===');
        console.log(orgsWithActivation);
        
        // 페이지네이션을 위한 처리
        const pageSize = 5;
        const startIndex = page * pageSize;
        const endIndex = startIndex + pageSize;
        const paginatedOrgs = orgsWithActivation.slice(startIndex, endIndex);
        
        currentOrgPage = page;
        totalOrgPages = Math.ceil(orgsWithActivation.length / pageSize);
        
        renderOrganizationTable(paginatedOrgs);
        renderOrganizationPagination();
    } catch (error) {
        console.error('Error loading organization list:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('부서 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 부서 테이블 렌더링
function renderOrganizationTable(organizations) {
    console.log('=== renderOrganizationTable called ===');
    console.log('Organizations to render:', organizations);
    
    const tbody = document.getElementById('organizationTableBody');
    tbody.innerHTML = '';

    if (organizations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 40px; color: #9ca3af;">등록된 부서가 없습니다.</td></tr>';
        return;
    }

    organizations.forEach((org, index) => {
        console.log(`Rendering org ${index}:`, org);
        const row = document.createElement('tr');
        const rowNumber = currentOrgPage * 5 + index + 1;

        const boardCategoryId = org.boardCategoryId;
        const hasBoard = boardCategoryId && boardCategoryId !== null && boardCategoryId !== 0;
        // org.activated가 명시적으로 false일 수 있으므로, boolean 값으로 직접 사용
        const isActivated = hasBoard ? (org.activated === true) : false; // 게시판이 없으면 비활성화 상태
        
        console.log(`Org ${org.id} (${org.name}):`, {
            boardCategoryId,
            hasBoard,
            activated: org.activated,
            isActivated
        });
        
        row.innerHTML = `
            <td>${rowNumber}</td>
            <td>${escapeHTML(org.name)}</td>
            <td>
                ${hasBoard ? `
                <div class="toggle-label">
                    <span class="${isActivated ? 'status-inactive' : 'status-active'}">비활성화</span>
                    <label class="toggle-switch">
                        <input type="checkbox" ${isActivated ? 'checked' : ''} 
                               data-org-id="${org.id}"
                               data-board-id="${boardCategoryId}">
                        <span class="toggle-slider"></span>
                    </label>
                    <span class="${isActivated ? 'status-active' : 'status-inactive'}">활성화</span>
                </div>
                ` : `
                <div class="toggle-label" style="opacity: 0.5;">
                    <span style="color: #9ca3af;">게시판 없음</span>
                    <label class="toggle-switch" style="pointer-events: none;">
                        <input type="checkbox" disabled>
                        <span class="toggle-slider"></span>
                    </label>
                </div>
                `}
            </td>
        `;
        tbody.appendChild(row);
        
        // 이벤트 리스너 직접 추가 (게시판이 있는 경우만)
        if (hasBoard) {
            const checkbox = row.querySelector('input[type="checkbox"]');
            if (checkbox) {
                checkbox.addEventListener('change', function(e) {
                    e.stopPropagation();
                    console.log('Checkbox change event fired:', {
                        checked: this.checked,
                        orgId: this.getAttribute('data-org-id'),
                        boardId: this.getAttribute('data-board-id')
                    });
                    handleToggleChange(this);
                });
            }
        }
    });
}

// 부서 페이지네이션 렌더링
function renderOrganizationPagination() {
    const pagination = document.getElementById('organizationPagination');
    pagination.innerHTML = '';

    if (totalOrgPages <= 1) return;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '‹';
    prevBtn.disabled = currentOrgPage === 0;
    prevBtn.addEventListener('click', () => {
        if (currentOrgPage > 0) loadOrganizationList(currentOrgPage - 1);
    });
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisible = 5;
    let startPage = Math.max(0, currentOrgPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalOrgPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }

    if (startPage > 0) {
        const firstBtn = document.createElement('span');
        firstBtn.className = 'page-number';
        firstBtn.textContent = '1';
        firstBtn.addEventListener('click', () => loadOrganizationList(0));
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
        pageBtn.className = 'page-number' + (i === currentOrgPage ? ' active' : '');
        pageBtn.textContent = i + 1;
        pageBtn.addEventListener('click', () => loadOrganizationList(i));
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalOrgPages - 1) {
        if (endPage < totalOrgPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
        const lastBtn = document.createElement('span');
        lastBtn.className = 'page-number';
        lastBtn.textContent = totalOrgPages;
        lastBtn.addEventListener('click', () => loadOrganizationList(totalOrgPages - 1));
        pagination.appendChild(lastBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '›';
    nextBtn.disabled = currentOrgPage >= totalOrgPages - 1;
    nextBtn.addEventListener('click', () => {
        if (currentOrgPage < totalOrgPages - 1) loadOrganizationList(currentOrgPage + 1);
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
            Promise.resolve(null) // 권한 API는 아직 구현되지 않음
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
        
        // 권한 정보 로드 (API가 구현되면 활성화)
        // if (permissionResponse && permissionResponse.ok) {
        //     const permissionResult = await permissionResponse.json();
        //     const permissions = permissionResult.data || [];
        //     selectedEmployees = permissions.map(p => ({
        //         id: p.employeeInfo.employeeId,
        //         name: p.employeeInfo.name,
        //         rankName: p.employeeInfo.rankName
        //     }));
        // }

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
    isEditMode = false;
    editingBoardId = null;
}

// 게시판 폼 제출 처리
async function handleBoardSubmit(e) {
    e.preventDefault();

    const boardName = document.getElementById('boardName').value.trim();
    const commentActivated = document.querySelector('input[name="commentActivated"]:checked').value === 'true';

    if (!boardName) {
        alert('게시판 이름을 입력해주세요.');
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
    const keyword = document.getElementById('employeeSearchInput').value.trim();
    if (!keyword) {
        alert('사원 이름 또는 사번을 입력해주세요.');
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

        const employees = await response.json();
        displayEmployeeSearchResults(employees);
    } catch (error) {
        console.error('Error searching employees:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('사원 검색에 실패했습니다.');
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
function selectEmployee(employee) {
    if (selectedEmployees.some(se => se.id === employee.id)) {
        return; // 이미 선택된 사원
    }

    selectedEmployees.push({
        id: employee.id,
        name: employee.name,
        rankName: employee.positionName || ''
    });

    renderSelectedEmployees();
    document.getElementById('employeeSearchInput').value = '';
    document.getElementById('employeeSearchResults').innerHTML = '';
}

// 선택된 사원 제거
function removeEmployee(employeeId) {
    selectedEmployees = selectedEmployees.filter(se => se.id !== employeeId);
    renderSelectedEmployees();
}

// 선택된 사원 목록 렌더링
function renderSelectedEmployees() {
    const container = document.getElementById('selectedEmployees');
    container.innerHTML = '';

    selectedEmployees.forEach(emp => {
        const div = document.createElement('div');
        div.className = 'selected-employee';
        div.innerHTML = `
            <i class="fas fa-user employee-icon"></i>
            <span class="employee-name">${escapeHTML(emp.name)} ${escapeHTML(emp.rankName || '')}</span>
            <button type="button" class="remove-btn" onclick="removeEmployee(${emp.id})" title="제거">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(div);
    });
}

// 게시판 권한 저장 (API가 구현되면 활성화)
async function saveBoardPermissions(boardCategoryId) {
    // TODO: 게시판 권한 API 구현 필요
    // try {
    //     const employeeIds = selectedEmployees.map(se => se.id);
    //     
    //     const response = await apiFetch('/api/admin/board-permissions', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json'
    //         },
    //         body: JSON.stringify({
    //             boardCategoryId: boardCategoryId,
    //             employeeIds: employeeIds
    //         })
    //     });
    //
    //     if (!response.ok) {
    //         console.warn('권한 저장에 실패했습니다:', await response.text());
    //     }
    // } catch (error) {
    //     console.error('Error saving permissions:', error);
    // }
    console.log('권한 저장 기능은 아직 구현되지 않았습니다.');
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

// 토글 변경 핸들러 (전역 함수로 선언)
window.handleToggleChange = function(checkbox) {
    try {
        console.log('=== handleToggleChange called ===');
        console.log('Checkbox element:', checkbox);
        console.log('Checkbox checked:', checkbox.checked);
        
        const organizationId = parseInt(checkbox.getAttribute('data-org-id'));
        const boardCategoryIdStr = checkbox.getAttribute('data-board-id');
        let boardCategoryId = null;
        
        console.log('Raw attributes:', {
            'data-org-id': checkbox.getAttribute('data-org-id'),
            'data-board-id': checkbox.getAttribute('data-board-id')
        });
        
        if (boardCategoryIdStr && boardCategoryIdStr !== '' && boardCategoryIdStr !== 'null') {
            boardCategoryId = parseInt(boardCategoryIdStr);
            if (isNaN(boardCategoryId)) {
                boardCategoryId = null;
            }
        }
        
        const isActivated = checkbox.checked;
        
        console.log('Parsed values:', { 
            organizationId, 
            boardCategoryId, 
            isActivated, 
            boardCategoryIdStr
        });
        
        if (!organizationId || isNaN(organizationId)) {
            console.error('Invalid organizationId:', organizationId);
            alert('부서 정보를 찾을 수 없습니다.');
            checkbox.checked = !checkbox.checked;
            return;
        }
        
        toggleActivation(organizationId, boardCategoryId, isActivated, checkbox);
    } catch (error) {
        console.error('Error in handleToggleChange:', error);
        console.error('Error stack:', error.stack);
        alert('토글 변경 중 오류가 발생했습니다: ' + error.message);
        checkbox.checked = !checkbox.checked; // 원복
    }
};

// 부서 게시판 활성화 토글
async function toggleActivation(organizationId, boardCategoryId, isActivated, checkboxElement) {
    try {
        console.log('=== toggleActivation called ===');
        console.log('Parameters:', { organizationId, boardCategoryId, isActivated });
        console.log('API_BASE_URL:', API_BASE_URL);
        
        // 게시판이 없는 경우
        if (!boardCategoryId || boardCategoryId === 0 || isNaN(boardCategoryId)) {
            console.warn('Board category ID is invalid:', boardCategoryId);
            alert('해당 부서의 게시판이 없습니다. 먼저 게시판을 생성해주세요.');
            // 토글 상태 원복
            if (checkboxElement) {
                checkboxElement.checked = !isActivated;
            }
            return;
        }
        
        const apiUrl = `${API_BASE_URL}/organization/${boardCategoryId}/activation`;
        const requestBody = { isActivated: isActivated };
        
        console.log('=== API Call Details ===');
        console.log('URL:', apiUrl);
        console.log('Method: PATCH');
        console.log('Body:', requestBody);
        console.log('Full URL will be:', window.location.origin + apiUrl);
        
        // 인증 토큰 확인
        const accessToken = sessionStorage.getItem('accessToken');
        console.log('Access Token exists:', !!accessToken);
        if (!accessToken) {
            console.error('No access token found in sessionStorage');
            alert('인증 정보가 없습니다. 다시 로그인해주세요.');
            location.href = '/login';
            return;
        }
        
        // 게시판 활성화 상태 변경
        console.log('Calling apiFetch...');
        const response = await apiFetch(apiUrl, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });
        
        console.log('=== API Response ===');
        console.log('Status:', response.status, response.statusText);
        console.log('Headers:', Object.fromEntries(response.headers.entries()));
        
        if (!response.ok) {
            if (response.status === 401) {
                console.error('401 Unauthorized - redirecting to login');
                location.href = '/login';
                return;
            }
            
            let errorData;
            try {
                const text = await response.text();
                console.error('Error response text:', text);
                errorData = JSON.parse(text);
            } catch (e) {
                errorData = { message: `HTTP ${response.status}: ${response.statusText}` };
            }
            
            console.error('API Error Details:', errorData);
            throw new Error(errorData.message || `활성화 상태 변경에 실패했습니다. (${response.status})`);
        }
        
        let result;
        try {
            const text = await response.text();
            console.log('Response text:', text);
            result = text ? JSON.parse(text) : null;
        } catch (e) {
            console.warn('Failed to parse response as JSON:', e);
            result = null;
        }
        
        console.log('API Success:', result);
        console.log('=== End API Call ===');
        
        // 성공 메시지는 표시하지 않고 바로 목록 새로고침
        console.log('Reloading organization list...');
        console.log('Current page:', currentOrgPage);
        
        // 약간의 지연을 두어 서버에서 데이터가 완전히 업데이트되도록 함
        await new Promise(resolve => setTimeout(resolve, 100));
        
        await loadOrganizationList(currentOrgPage);
        console.log('Organization list reloaded successfully');
    } catch (error) {
        console.error('=== Error in toggleActivation ===');
        console.error('Error:', error);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        
        // 토글 상태 원복
        if (checkboxElement) {
            checkboxElement.checked = !isActivated;
        }
        
        if (error.message !== 'SESSION_EXPIRED') {
            alert(error.message || '활성화 상태 변경에 실패했습니다.');
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

// Enter 키로 사원 검색
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('employeeSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchEmployee();
            }
        });
    }
});

