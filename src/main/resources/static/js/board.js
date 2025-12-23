// 사용자 게시판 페이지 JavaScript

const BOARD_CATEGORY_API = '/api/board-categories';
const BOARD_API = '/api/boards';

let currentCategoryId = null;
let currentPage = 0;
let totalPages = 1;
let accessibleBoards = []; // 권한이 있는 일반 게시판
let organizationBoards = []; // 조직 게시판

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
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
            throw new Error('게시판 목록을 불러오는데 실패했습니다.');
        }

        const accessibleResult = await accessibleResponse.json();
        const orgResult = await orgResponse.json();

        console.log('[게시판 목록] 전체 응답 - Accessible:', accessibleResult);
        console.log('[게시판 목록] 전체 응답 - Organization:', orgResult);

        // ResponseDto 구조에서 data 추출
        // ResponseDto는 { status, message, data } 형태
        let accessibleData = accessibleResult.data;
        let orgData = orgResult.data;
        
        console.log('[게시판 목록] 추출된 data - Accessible:', accessibleData);
        console.log('[게시판 목록] 추출된 data - Organization:', orgData);
        
        // data가 없으면 result 자체가 배열일 수 있음
        if (!accessibleData && Array.isArray(accessibleResult)) {
            console.log('[게시판 목록] accessibleResult가 배열이므로 직접 사용');
            accessibleData = accessibleResult;
        }
        if (!orgData && Array.isArray(orgResult)) {
            console.log('[게시판 목록] orgResult가 배열이므로 직접 사용');
            orgData = orgResult;
        }
        
        // 최종적으로 배열인지 확인
        accessibleBoards = Array.isArray(accessibleData) ? accessibleData : [];
        organizationBoards = Array.isArray(orgData) ? orgData : [];

        console.log('[게시판 목록] 최종 파싱 결과 - Accessible boards:', accessibleBoards.length, '개', accessibleBoards);
        console.log('[게시판 목록] 최종 파싱 결과 - Organization boards:', organizationBoards.length, '개', organizationBoards);

        renderSidebar();
        
        // 첫 번째 게시판 자동 선택
        if (accessibleBoards.length > 0) {
            const firstBoard = accessibleBoards[0];
            selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
        } else if (organizationBoards.length > 0) {
            const firstBoard = organizationBoards[0];
            selectBoard(firstBoard.id, firstBoard.boardName || firstBoard.name || '게시판');
        }
    } catch (error) {
        console.error('Error loading board categories:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('게시판 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 사이드바 렌더링
function renderSidebar() {
    const sidebar = document.getElementById('boardSidebar');
    if (!sidebar) {
        console.error('Sidebar element not found');
        return;
    }

    sidebar.innerHTML = '';

    console.log('Rendering sidebar with', accessibleBoards.length, 'accessible boards and', organizationBoards.length, 'organization boards');

    // 일반 게시판 (권한이 있는 게시판)
    if (accessibleBoards && accessibleBoards.length > 0) {
        accessibleBoards.forEach((board, index) => {
        const boardName = board.boardName || board.name || '게시판';
        const boardId = board.id;
            
            if (!boardId) {
                console.warn('Board without ID found:', board);
                return;
            }
            
        const isSelected = boardId === currentCategoryId;
        
            console.log(`Adding accessible board ${index + 1}:`, boardName, boardId);
        
        const li = document.createElement('li');
        li.className = 'sidebar-menu-item' + (isSelected ? ' selected' : '');
        li.innerHTML = `
            <a href="#" class="board-link${isSelected ? ' active' : ''}" data-category-id="${boardId}" data-board-name="${escapeHTML(boardName)}">
                ${escapeHTML(boardName)}
            </a>
        `;
        sidebar.appendChild(li);
    });
    } else {
        console.warn('No accessible boards found. accessibleBoards:', accessibleBoards);
    }

    // 조직 게시판
    if (organizationBoards && organizationBoards.length > 0) {
        const orgHeader = document.createElement('li');
        orgHeader.className = 'sidebar-menu-item sidebar-menu-header';
        orgHeader.innerHTML = '<span class="sidebar-menu-header-text">부서게시판</span>';
        sidebar.appendChild(orgHeader);

        organizationBoards.forEach((board, index) => {
            const boardName = board.boardName || board.name || '게시판';
            const boardId = board.id;
            
            if (!boardId) {
                console.warn('Organization board without ID found:', board);
                return;
            }
            
            const isSelected = boardId === currentCategoryId;
            
            console.log(`Adding organization board ${index + 1}:`, boardName, boardId);
            
            const li = document.createElement('li');
            li.className = 'sidebar-menu-item' + (isSelected ? ' selected' : '');
            li.innerHTML = `
                <a href="#" class="board-link${isSelected ? ' active' : ''}" data-category-id="${boardId}" data-board-name="${escapeHTML(boardName)}">
                    - ${escapeHTML(boardName)}
                </a>
            `;
            sidebar.appendChild(li);
        });
    } else {
        console.warn('No organization boards found. organizationBoards:', organizationBoards);
    }

    // 이벤트 리스너 추가
    document.querySelectorAll('.board-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const categoryId = parseInt(this.getAttribute('data-category-id'));
            const boardName = this.getAttribute('data-board-name');
            console.log('Board selected:', categoryId, boardName);
            selectBoard(categoryId, boardName);
        });
    });
    
    console.log('Sidebar rendered with', document.querySelectorAll('.board-link').length, 'board links');
}

// 게시판 선택
function selectBoard(categoryId, boardName) {
    if (!categoryId) {
        console.error('Invalid categoryId:', categoryId);
        return;
    }
    
    currentCategoryId = categoryId;
    currentPage = 0;

    console.log('Selecting board:', categoryId, boardName);

    // 사이드바 활성화 상태 업데이트
    document.querySelectorAll('.board-link').forEach(link => {
        const linkCategoryId = parseInt(link.getAttribute('data-category-id'));
        const isSelected = linkCategoryId === categoryId;
        
        link.classList.toggle('active', isSelected);
        link.parentElement.classList.toggle('selected', isSelected);
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
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const searchType = document.getElementById('searchType').value;
        const keyword = document.getElementById('searchKeyword').value;

        if (dateFilter === 'custom' && startDate && endDate) {
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
            throw new Error('게시글 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        console.log('Board list response:', result);
        
        // ResponseDto 구조에서 data 추출
        let boardData = result.data;
        if (!boardData && result.content) {
            boardData = result;
        }
        
        // Page 객체에서 content 추출
        const boardList = boardData?.content || boardData?.elements || (Array.isArray(boardData) ? boardData : []);
        totalPages = boardData?.totalPages || boardData?.totalPageCount || 1;
        currentPage = boardData?.number !== undefined ? boardData.number : page;

        console.log('Loaded board list:', boardList.length, 'items');
        console.log('Total pages:', totalPages, 'Current page:', currentPage);

        renderBoardTable(boardList);
        renderPagination(currentPage);
    } catch (error) {
        console.error('Error loading board list:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('게시글 목록을 불러오는데 실패했습니다.');
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
        
        // 필드명 매핑: boardTitle -> title, writer.name -> authorName
        const title = board.boardTitle || board.title || '';
        const authorName = board.writer?.name || board.authorName || '';
        const createdAt = formatDateTime(board.createdAt || board.created_at);
        const viewCount = board.viewCount || board.view_count || 0;

        row.innerHTML = `
            <td>${rowNumber}</td>
            <td>
                <a href="#" class="board-title-link" onclick="viewBoard(${board.id}); return false;">
                    ${escapeHTML(title)}
                </a>
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

    if (totalPages <= 1) {
        return;
    }

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

// 날짜 필터 변경
document.addEventListener('DOMContentLoaded', function() {
    const dateFilter = document.getElementById('dateFilter');
    if (dateFilter) {
        dateFilter.addEventListener('change', function() {
            const startDateInput = document.getElementById('startDate');
            const endDateInput = document.getElementById('endDate');
            
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
                    startDateInput.value = startDate.toISOString().split('T')[0];
                    endDateInput.value = today.toISOString().split('T')[0];
                }
            }
        });
    }
});

// 게시글 상세 보기
function viewBoard(boardId) {
    if (!boardId) {
        alert('게시글 ID가 없습니다.');
        return;
    }
    // 게시글 상세 페이지로 이동
    window.location.href = `/view/board/detail?boardId=${boardId}`;
}

// 글쓰기
function writePost() {
    if (!currentCategoryId) {
        alert('게시판을 선택해주세요.');
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
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

