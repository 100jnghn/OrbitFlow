// 부서 게시판 관리 페이지 JavaScript

const API_BASE_URL = '/api/admin/board-categories';
let currentOrgPage = 0;
let totalOrgPages = 1;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    try {
        loadOrganizationList();
    } catch (error) {
        console.error('Error initializing dept board page:', error);
    }
});

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

        const orgResult = await orgResponse.json();
        // /api/admin/organizations는 List를 직접 반환하므로 data가 아닐 수 있음
        const organizations = Array.isArray(orgResult) ? orgResult : (orgResult.data || []);

        // 조직 게시판 목록에서 각 부서별 활성화 상태 및 게시판 ID 확인
        let orgBoardsMap = {};
        if (boardResponse.ok) {
            const boardResult = await boardResponse.json();


            // ResponseDto 구조에서 data 추출
            let boardData = boardResult.data;
            if (!boardData && boardResult.content) {
                boardData = boardResult;
            }

            // Page 객체에서 content 추출
            const orgBoards = boardData?.content || boardData?.elements || (Array.isArray(boardData) ? boardData : []);

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

                        // 기본값으로 true 설정 (게시판이 존재하면 기본적으로 활성화)
                        isActivated = true;
                    }



                    orgBoardsMap[board.organizationId] = {
                        id: board.id,
                        isActivated: isActivated
                    };
                }
            });

        } else {

        }

        // 부서 목록에 활성화 상태 및 게시판 ID 추가
        const orgsWithActivation = organizations
            .filter(org => org.parentOrgId !== null) // 최상위 조직(회사) 제외
            .map(org => {
                const boardInfo = orgBoardsMap[org.id];
                const activated = boardInfo !== undefined ? boardInfo.isActivated : true;
                const boardCategoryId = boardInfo?.id || null;

                return {
                    ...org,
                    activated: activated,
                    boardCategoryId: boardCategoryId
                };
            });



        // 페이지네이션을 위한 처리
        const pageSize = 10;
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
            sweetError('부서 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 부서 테이블 렌더링
function renderOrganizationTable(organizations) {


    const tbody = document.getElementById('organizationTableBody');
    tbody.innerHTML = '';

    if (organizations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 40px; color: #9ca3af;">등록된 부서가 없습니다.</td></tr>';
        return;
    }

    organizations.forEach((org, index) => {

        const row = document.createElement('tr');
        const rowNumber = currentOrgPage * 10 + index + 1;

        const boardCategoryId = org.boardCategoryId;
        const hasBoard = boardCategoryId && boardCategoryId !== null && boardCategoryId !== 0;
        // org.activated가 명시적으로 false일 수 있으므로, boolean 값으로 직접 사용
        const isActivated = hasBoard ? (org.activated === true) : false; // 게시판이 없으면 비활성화 상태



        row.innerHTML = `
            <td class="col-number">${rowNumber}</td>
            <td class="col-dept">${escapeHTML(org.name)}</td>
            <td class="col-toggle">
                ${hasBoard ? `
                <div class="toggle-label">
        <span class="status-inactive mobile-hide">비활성화</span>
        <label class="toggle-switch">
            <input type="checkbox" ${isActivated ? 'checked' : ''} 
                   data-org-id="${org.id}"
                   data-board-id="${boardCategoryId}">
            <span class="toggle-slider"></span>
        </label>
        <span class="status-active mobile-hide">활성화</span>
    </div>
                ` : `
                <div class="toggle-label" style="opacity: 0.5;">
                    <span class="mobile-hide" style="color: #9ca3af;">게시판 없음</span>
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
                checkbox.addEventListener('change', function (e) {
                    e.stopPropagation();

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

// 토글 변경 핸들러 (전역 함수로 선언)
window.handleToggleChange = function (checkbox) {
    try {


        const organizationId = parseInt(checkbox.getAttribute('data-org-id'));
        const boardCategoryIdStr = checkbox.getAttribute('data-board-id');
        let boardCategoryId = null;



        if (boardCategoryIdStr && boardCategoryIdStr !== '' && boardCategoryIdStr !== 'null') {
            boardCategoryId = parseInt(boardCategoryIdStr);
            if (isNaN(boardCategoryId)) {
                boardCategoryId = null;
            }
        }

        const isActivated = checkbox.checked;



        if (!organizationId || isNaN(organizationId)) {
            console.error('Invalid organizationId:', organizationId);
            sweetError('부서 정보를 찾을 수 없습니다.');
            checkbox.checked = !checkbox.checked;
            return;
        }

        toggleActivation(organizationId, boardCategoryId, isActivated, checkbox);
    } catch (error) {
        console.error('Error in handleToggleChange:', error);
        console.error('Error stack:', error.stack);
        sweetError('토글 변경 중 오류가 발생했습니다: ' + error.message);
        checkbox.checked = !checkbox.checked; // 원복
    }
};

// 부서 게시판 활성화 토글
async function toggleActivation(organizationId, boardCategoryId, isActivated, checkboxElement) {
    try {


        // 게시판이 없는 경우
        if (!boardCategoryId || boardCategoryId === 0 || isNaN(boardCategoryId)) {

            await sweetWarning('해당 부서의 게시판이 없습니다. 먼저 게시판을 생성해주세요.');
            // 토글 상태 원복
            if (checkboxElement) {
                checkboxElement.checked = !isActivated;
            }
            return;
        }

        const apiUrl = `${API_BASE_URL}/organization/${boardCategoryId}/activation`;
        const requestBody = { isActivated: isActivated };



        // 인증 토큰 확인
        const accessToken = sessionStorage.getItem('accessToken');

        if (!accessToken) {
            console.error('No access token found in sessionStorage');
            await sweetWarning('인증 정보가 없습니다. 다시 로그인해주세요.');
            location.href = '/login';
            return;
        }

        // 게시판 활성화 상태 변경

        const response = await apiFetch(apiUrl, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });



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

            result = text ? JSON.parse(text) : null;
        } catch (e) {

            result = null;
        }



        // 성공 메시지는 표시하지 않고 바로 목록 새로고침


        // 약간의 지연을 두어 서버에서 데이터가 완전히 업데이트되도록 함
        await new Promise(resolve => setTimeout(resolve, 100));

        await loadOrganizationList(currentOrgPage);

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
            sweetError(error.message || '활성화 상태 변경에 실패했습니다.');
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

