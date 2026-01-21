// 사용자 게시판 페이지 JavaScript

const BOARD_CATEGORY_API = '/api/board-categories';
const BOARD_API = '/api/board-posts';

let currentCategoryId = null;
let currentPage = 0;
let totalPages = 1;
let accessibleBoards = []; // 권한이 있는 일반 게시판
let organizationBoards = []; // 조직 게시판

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    // URL 파라미터에서 categoryId 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const urlCategoryId = urlParams.get('categoryId');

    if (urlCategoryId) {
        const categoryId = parseInt(urlCategoryId);
        // categoryId가 있으면 해당 게시판을 선택하기 위해 저장
        window.selectedCategoryId = categoryId;
    }

    loadBoardCategories();
});

// 게시판 카테고리 목록 로드 (사이드바용)
async function loadBoardCategories() {
    try {
        // 권한이 있는 일반 게시판과 조직 게시판을 동시에 로드
        const [accessibleResponse, orgResponse] = await Promise.all([
            apiFetch(`${BOARD_CATEGORY_API}/accessible`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            }),
            apiFetch(`${BOARD_CATEGORY_API}/organization`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
        ]);

        if (!accessibleResponse.ok || !orgResponse.ok) {
            if (accessibleResponse.status === 401 || orgResponse.status === 401) {
                location.href = '/login';
                return;
            }
            if (accessibleResponse.status === 403 || orgResponse.status === 403) {
                throw new Error('접근 권한이 없는 게시판이 포함되어 있습니다.');
            }
            if (accessibleResponse.status === 404 || orgResponse.status === 404) {
                throw new Error('일부 게시판 정보를 찾을 수 없습니다.');
            }
            throw new Error('게시판 목록을 불러오는데 실패했습니다.');
        }

        const accessibleResult = await accessibleResponse.json();
        const orgResult = await orgResponse.json();


        // ResponseDto 구조에서 data 추출
        // ResponseDto는 { status, message, data } 형태
        let accessibleData = accessibleResult.data;
        let orgData = orgResult.data;


        // data가 없으면 result 자체가 배열일 수 있음
        if (!accessibleData && Array.isArray(accessibleResult)) {
            accessibleData = accessibleResult;
        }
        if (!orgData && Array.isArray(orgResult)) {
            orgData = orgResult;
        }

        // 최종적으로 배열인지 확인
        accessibleBoards = Array.isArray(accessibleData) ? accessibleData : [];
        organizationBoards = Array.isArray(orgData) ? orgData : [];


        renderSidebar();

        // URL에서 categoryId가 있으면 해당 게시판 선택, 없으면 첫 번째 게시판 선택
        if (window.selectedCategoryId) {
            // URL에서 받은 categoryId로 게시판 찾기
            let targetBoard = null;
            let targetBoardName = '';

            // 일반 게시판에서 찾기
            targetBoard = accessibleBoards.find(board => board.id === window.selectedCategoryId);
            if (targetBoard) {
                targetBoardName = targetBoard.boardName || targetBoard.name || '게시판';
            } else {
                // 조직 게시판에서 찾기
                targetBoard = organizationBoards.find(board => board.id === window.selectedCategoryId);
                if (targetBoard) {
                    targetBoardName = targetBoard.boardName || targetBoard.name || '게시판';
                }
            }

            if (targetBoard && targetBoardName) {
                selectBoard(window.selectedCategoryId, targetBoardName);
                window.selectedCategoryId = null; // 사용 후 초기화
            } else {
                // 게시판을 찾을 수 없으면 첫 번째 게시판 선택
                if (accessibleBoards.length > 0) {
                    const firstBoard = accessibleBoards[0];
                    selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
                } else if (organizationBoards.length > 0) {
                    const firstBoard = organizationBoards[0];
                    selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
                }
            }
        } else {
            // 첫 번째 게시판 자동 선택
            if (accessibleBoards.length > 0) {
                const firstBoard = accessibleBoards[0];
                selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
            } else if (organizationBoards.length > 0) {
                const firstBoard = organizationBoards[0];
                selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
            }
        }
    } catch (error) {
        if (error.message !== 'SESSION_EXPIRED') {
            sweetError('게시판 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 사이드바 렌더링 (아코디언 구조)
function renderSidebar() {
    const sidebar = document.getElementById('boardSidebar');
    if (!sidebar) {
        return;
    }

    sidebar.innerHTML = '';


    // 일반 게시판 (권한이 있는 게시판) - 단일 항목으로 표시
    if (accessibleBoards && accessibleBoards.length > 0) {
        accessibleBoards.forEach((board, index) => {
            const boardName = board.boardName || board.name || '게시판';
            const boardId = board.id;

            if (!boardId) {
                return;
            }

            const isSelected = boardId === currentCategoryId;


            const li = document.createElement('li');
            li.className = 'menu-item no-sub' + (isSelected ? ' selected' : '');
            li.innerHTML = `
                <a href="#" class="board-link" data-category-id="${boardId}" data-board-name="${escapeHTML(boardName)}">
                    <span>${escapeHTML(boardName)}</span>
            </a>
        `;
            sidebar.appendChild(li);
        });
    } else {
    }

    // 조직 게시판 - 아코디언 구조로 표시 (기본적으로 펼쳐진 상태)
    if (organizationBoards && organizationBoards.length > 0) {
        // 부서게시판이 현재 선택되어 있는지 확인
        const hasSelectedOrgBoard = organizationBoards.some(board => board.id === currentCategoryId);

        const orgMenuItem = document.createElement('li');
        // 기본적으로 active 클래스를 추가하여 펼쳐진 상태로 시작
        orgMenuItem.className = 'menu-item active';
        orgMenuItem.innerHTML = `
            <div class="menu-title" role="button" onclick="toggleBoardMenu(this)" aria-expanded="true">
                <span>부서게시판</span>
                <i class="fas fa-chevron-down arrow"></i>
            </div>
            <ul class="sub-menu">
                ${organizationBoards.map(board => {
            const boardName = board.boardName || board.name || '게시판';
            const boardId = board.id;
            if (!boardId) return '';

            const isSelected = boardId === currentCategoryId;
            return `
                        <li class="${isSelected ? 'selected' : ''}">
                            <a href="#" class="board-link" data-category-id="${boardId}" data-board-name="${escapeHTML(boardName)}">
                                ${escapeHTML(boardName)}
                </a>
                        </li>
                    `;
        }).join('')}
            </ul>
            `;
        sidebar.appendChild(orgMenuItem);
    } else {
    }

    // 이벤트 리스너 추가
    document.querySelectorAll('.board-link').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const categoryId = parseInt(this.getAttribute('data-category-id'));
            const boardName = this.getAttribute('data-board-name');
            selectBoard(categoryId, boardName);
        });
    });

}

// 부서게시판 아코디언 토글 함수
function toggleBoardMenu(element) {
    const menuItem = element.closest('.menu-item');
    if (!menuItem) return;

    const isOpen = menuItem.classList.contains('active');

    // 다른 메뉴 닫기
    document.querySelectorAll('.sidebar-menu .menu-item').forEach(item => {
        if (item !== menuItem) {
            item.classList.remove('active');
            const title = item.querySelector('.menu-title');
            if (title) title.setAttribute('aria-expanded', 'false');
        }
    });

    // 현재 메뉴 토글
    if (isOpen) {
        menuItem.classList.remove('active');
        element.setAttribute('aria-expanded', 'false');
    } else {
        menuItem.classList.add('active');
        element.setAttribute('aria-expanded', 'true');
    }
}

// 게시판 선택
function selectBoard(categoryId, boardName) {
    if (!categoryId) {
        return;
    }

    currentCategoryId = categoryId;
    currentPage = 0;


    // 사이드바 활성화 상태 업데이트 (아코디언 구조)
    // 모든 선택 상태 초기화
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('selected');
    });
    document.querySelectorAll('.sub-menu li').forEach(item => {
        item.classList.remove('selected');
    });

    // 선택된 항목 표시
    document.querySelectorAll('.board-link').forEach(link => {
        const linkCategoryId = parseInt(link.getAttribute('data-category-id'));
        if (linkCategoryId === categoryId) {
            const menuItem = link.closest('.menu-item');
            const subMenuItem = link.closest('.sub-menu li');

            if (subMenuItem) {
                // 부서게시판인 경우
                subMenuItem.classList.add('selected');
                if (menuItem) {
                    menuItem.classList.add('active'); // 아코디언 열기
                }
            } else if (menuItem) {
                // 일반 게시판인 경우
                menuItem.classList.add('selected');
            }
        }
    });

    // 게시판 제목 업데이트
    const titleElement = document.getElementById('boardTitle');
    if (titleElement) {
        titleElement.textContent = boardName || '게시판';
    }

    // 글쓰기 버튼 표시
    const writeBtn = document.getElementById('writeBtn');
    if (writeBtn) {
        writeBtn.style.display = 'block';
    }

    // 게시글 목록 로드
    loadBoardList(0);
}

// 게시글 목록 로드
async function loadBoardList(page = 0) {
    if (!currentCategoryId) {
        return;
    }

    try {
        const params = new URLSearchParams({
            page: page,
            size: 10,
            sort: 'createdAt,desc'
        });

        // 검색 조건 추가
        const dateFilter = document.getElementById('dateFilter').value;
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        const startDate = startDateInput?.value;
        const endDate = endDateInput?.value;
        const searchType = document.getElementById('searchType').value;
        const keyword = document.getElementById('searchKeyword').value;

        // 기간 조건: custom이거나 today/week/month일 때 날짜가 설정된 경우 전달
        if (startDate && endDate) {
            params.append('startDate', startDate);
            params.append('endDate', endDate);
        }

        if (searchType && searchType !== 'ALL') {
            params.append('searchType', searchType);
        }

        if (keyword) {
            params.append('keyword', keyword);
        }

        const response = await apiFetch(`${BOARD_API}/categories/${currentCategoryId}?${params.toString()}`, {
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
            if (response.status === 403) {
                throw new Error('접근 권한이 없습니다.');
            }
            if (response.status === 404) {
                throw new Error('존재하지 않거나 삭제된 게시판입니다.');
            }
            throw new Error('게시글 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();

        // ResponseDto 구조에서 data 추출
        let boardData = result.data;
        if (!boardData && result.content) {
            boardData = result;
        }

        // Page 객체에서 content 추출
        const boardList = boardData?.content || boardData?.elements || (Array.isArray(boardData) ? boardData : []);
        totalPages = boardData?.totalPages || boardData?.totalPageCount || 1;
        currentPage = boardData?.number !== undefined ? boardData.number : page;


        renderBoardTable(boardList);
        renderPagination(currentPage);
    } catch (error) {
        if (error.message !== 'SESSION_EXPIRED') {
            sweetError('게시글 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 게시글 테이블 렌더링
function renderBoardTable(boards) {
    const tbody = document.getElementById('boardTableBody');
    tbody.innerHTML = '';

    if (boards.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 40px; color: #9ca3af;">등록된 게시글이 없습니다.</td></tr>';
        return;
    }

    boards.forEach((board, index) => {
        const row = document.createElement('tr');
        const rowNumber = currentPage * 10 + index + 1;

        // 행 전체 클릭 이벤트 추가
        row.onclick = () => viewBoard(board.id);

        // 필드명 매핑: boardTitle -> title, writer.name -> authorName
        const title = board.boardTitle || board.title || '';
        const authorName = board.writer?.name || board.authorName || '';
        const createdAt = formatDateTime(board.createdAt || board.created_at);
        const viewCount = board.viewCount || board.view_count || 0;

        const commentCount = board.commentCount || 0;
        const commentHtml = commentCount > 0 ? ` <span class="comment-count">(${commentCount})</span>` : '';

        row.innerHTML = `
            <td>${rowNumber}</td>
            <td class="board-title-cell">
                <div class="title-container">
                    <a href="#" class="board-title-link" onclick="event.preventDefault();">
                        <span class="title-text">${escapeHTML(title)}</span>${commentHtml}
                    </a>
                    ${(board.hasFile === true || board.has_file === true) ? '<i class="fas fa-paperclip attachment-icon" title="첨부파일 있음"></i>' : ''}
                </div>
            </td>
            <td>${escapeHTML(authorName)}</td>
            <td>${createdAt}</td>
            <td>${viewCount}</td>
        `;
        tbody.appendChild(row);
    });
}

// 페이지네이션 렌더링
function renderPagination(page) {
    const pagination = document.getElementById('boardPagination');
    pagination.innerHTML = '';


    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => {
        if (page > 0) {
            currentPage = page - 1;
            loadBoardList(currentPage);
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
            currentPage = 0;
            loadBoardList(currentPage);
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
            currentPage = i;
            loadBoardList(currentPage);
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
            currentPage = totalPages - 1;
            loadBoardList(currentPage);
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
            currentPage = page + 1;
            loadBoardList(currentPage);
        }
    };
    pagination.appendChild(nextBtn);
}

// 검색
function searchBoards() {
    currentPage = 0;
    loadBoardList(currentPage);
}

// 날짜 필터 변경 및 검색 입력 엔터 키 이벤트
document.addEventListener('DOMContentLoaded', function () {
    const dateFilter = document.getElementById('dateFilter');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (dateFilter) {
        dateFilter.addEventListener('change', function () {
            if (this.value === 'custom') {
                startDateInput.style.display = 'inline-block';
                endDateInput.style.display = 'inline-block';
            } else {
                startDateInput.style.display = 'none';
                endDateInput.style.display = 'none';

                // 날짜 자동 설정
                const today = new Date();
                let startDate = new Date();

                if (this.value === 'today') {
                    startDate = today;
                } else if (this.value === 'week') {
                    startDate.setDate(today.getDate() - 7);
                } else if (this.value === 'month') {
                    startDate.setMonth(today.getMonth() - 1);
                }

                if (this.value !== '') {
                    const startDateStr = startDate.toISOString().split('T')[0];
                    const endDateStr = today.toISOString().split('T')[0];
                    startDateInput.value = startDateStr;
                    endDateInput.value = endDateStr;
                    // 시작일 설정 시 종료일의 최소값 설정
                    endDateInput.min = startDateStr;
                }
            }
        });
    }

    // 시작일 변경 시 종료일의 최소값 설정
    if (startDateInput) {
        startDateInput.addEventListener('change', function () {
            const startDate = this.value;
            if (startDate && endDateInput) {
                // 종료일의 최소값을 시작일로 설정
                endDateInput.min = startDate;
                // 종료일이 시작일보다 이전이면 시작일로 변경
                if (endDateInput.value && endDateInput.value < startDate) {
                    endDateInput.value = startDate;
                }
            }
        });
    }

    // 종료일 변경 시 시작일보다 이전인지 확인
    if (endDateInput && startDateInput) {
        endDateInput.addEventListener('change', async function () {
            const endDate = this.value;
            const startDate = startDateInput.value;
            if (startDate && endDate && endDate < startDate) {
                await sweetWarning('종료일은 시작일보다 이전일 수 없습니다.');
                this.value = startDate;
            }
        });
    }

    // 검색 입력 필드에 엔터 키 이벤트 추가
    const searchKeyword = document.getElementById('searchKeyword');
    if (searchKeyword) {
        searchKeyword.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchBoards();
            }
        });
    }
});

// 게시글 상세 보기
function viewBoard(boardId) {
    if (!boardId) {
        sweetWarning('게시글 ID가 없습니다.');
        return;
    }
    // 게시글 상세 페이지로 이동 (categoryId 포함)
    if (currentCategoryId) {
        window.location.href = `/view/board/detail?boardId=${boardId}&categoryId=${currentCategoryId}`;
    } else {
        window.location.href = `/view/board/detail?boardId=${boardId}`;
    }
}

// 글쓰기
function writePost() {
    if (!currentCategoryId) {
        sweetWarning('게시판을 선택해주세요.');
        return;
    }
    // 글쓰기 페이지로 이동
    window.location.href = `/view/board/write?categoryId=${currentCategoryId}`;
}

// 날짜 시간 포맷
function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} <span class="time-part">${hours}:${minutes}:${seconds}</span>`;
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

