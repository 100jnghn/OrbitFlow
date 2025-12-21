// 게시글 상세 페이지 JavaScript

const BOARD_API = '/api/boards';
const BOARD_CATEGORY_API = '/api/board-categories';

let boardId = null;
let categoryId = null;
let currentUserId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
    // URL 파라미터에서 boardId 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    boardId = urlParams.get('boardId') || document.getElementById('boardId')?.value;

    if (boardId) {
        boardId = parseInt(boardId);
        // 현재 사용자 정보 로드
        await loadCurrentUser();
        loadBoardDetail();
    } else {
        showError('게시글 ID가 없습니다.');
    }

    // 게시판 목록 로드 (사이드바용)
    loadBoardCategories();
});

// 현재 사용자 정보 로드
async function loadCurrentUser() {
    try {
        const response = await apiFetch('/api/auth/me', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            currentUserId = result.data?.id || result.data?.employeeId;
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

// 게시판 카테고리 목록 로드 (사이드바용)
async function loadBoardCategories() {
    try {
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
            return;
        }

        const accessibleData = await accessibleResponse.json();
        const orgData = await orgResponse.json();

        const accessibleBoards = accessibleData.data || [];
        const organizationBoards = orgData.data || [];

        renderSidebar(accessibleBoards, organizationBoards);
    } catch (error) {
        console.error('Error loading board categories:', error);
    }
}

// 사이드바 렌더링
function renderSidebar(accessibleBoards, organizationBoards) {
    const sidebar = document.getElementById('boardSidebar');
    if (!sidebar) return;

    sidebar.innerHTML = '';

    accessibleBoards.forEach(board => {
        const li = document.createElement('li');
        const isSelected = board.id === categoryId;
        li.className = 'sidebar-menu-item' + (isSelected ? ' selected' : '');
        li.innerHTML = `
            <a href="/view/board?categoryId=${board.id}" class="board-link${isSelected ? ' active' : ''}">
                ${escapeHTML(board.boardName || board.name || '게시판')}
            </a>
        `;
        sidebar.appendChild(li);
    });

    if (organizationBoards.length > 0) {
        const orgHeader = document.createElement('li');
        orgHeader.className = 'sidebar-menu-item sidebar-menu-header';
        orgHeader.innerHTML = '<span class="sidebar-menu-header-text">부서게시판</span>';
        sidebar.appendChild(orgHeader);

        organizationBoards.forEach(board => {
            const li = document.createElement('li');
            const isSelected = board.id === categoryId;
            li.className = 'sidebar-menu-item' + (isSelected ? ' selected' : '');
            li.innerHTML = `
                <a href="/view/board?categoryId=${board.id}" class="board-link${isSelected ? ' active' : ''}">
                    - ${escapeHTML(board.boardName || board.name || '게시판')}
                </a>
            `;
            sidebar.appendChild(li);
        });
    }
}

// 게시글 상세 조회
async function loadBoardDetail() {
    if (!boardId) return;

    try {
        const response = await apiFetch(`${BOARD_API}/${boardId}`, {
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
            if (response.status === 404) {
                showError('게시글을 찾을 수 없습니다.');
                return;
            }
            throw new Error('게시글을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const board = result.data;

        if (board) {
            categoryId = board.categoryId || board.category?.id;
            renderBoardDetail(board);
        } else {
            showError('게시글 데이터가 없습니다.');
        }
    } catch (error) {
        console.error('Error loading board detail:', error);
        showError('게시글을 불러오는데 실패했습니다.');
    }
}

// 게시글 상세 렌더링
function renderBoardDetail(board) {
    const content = document.getElementById('boardDetailContent');
    if (!content) return;

    const title = board.boardTitle || board.title || '';
    const boardContent = board.boardContent || board.content || '';
    const categoryName = board.categoryName || board.category?.name || '';
    const writerName = board.writer?.name || board.authorName || '';
    const writerId = board.writer?.id || board.writerId;
    const writerNo = board.writer?.employeeNo || '';
    const viewCount = board.viewCount || board.view_count || 0;
    const createdAt = formatDateTime(board.createdAt || board.created_at);
    const updatedAt = formatDateTime(board.updatedAt || board.updated_at);
    const files = board.files || [];

    // 작성자 본인인지 확인
    const isAuthor = currentUserId && writerId && currentUserId === writerId;
    const showEditDelete = isAuthor ? '' : 'style="display: none;"';

    content.innerHTML = `
        <div class="board-detail-card">
            <!-- 게시글 헤더 -->
            <div class="board-detail-header-info">
                <h1 class="board-detail-title">${escapeHTML(title)}</h1>
                <div class="board-detail-meta">
                    <span class="meta-item">
                        <i class="fas fa-folder"></i> ${escapeHTML(categoryName)}
                    </span>
                    <span class="meta-item">
                        <i class="fas fa-user"></i> ${escapeHTML(writerName)}${writerNo ? ` (${escapeHTML(writerNo)})` : ''}
                    </span>
                    <span class="meta-item">
                        <i class="fas fa-calendar"></i> ${createdAt}
                    </span>
                    <span class="meta-item">
                        <i class="fas fa-eye"></i> ${viewCount}
                    </span>
                </div>
            </div>

            <!-- 게시글 내용 -->
            <div class="board-detail-body">
                <div class="board-detail-text">${escapeHTML(boardContent).replace(/\n/g, '<br>')}</div>
            </div>

            <!-- 첨부파일 -->
            ${files.length > 0 ? `
                <div class="board-detail-files">
                    <h3 class="files-title">
                        <i class="fas fa-paperclip"></i> 첨부파일 (${files.length})
                    </h3>
                    <ul class="files-list">
                        ${files.map(file => `
                            <li class="file-item">
                                <a href="#" class="file-link" onclick="downloadFile(${file.id}, '${escapeHTML(file.originalFileName || file.originalFile || '')}'); return false;">
                                    <i class="fas fa-file"></i> ${escapeHTML(file.originalFileName || file.originalFile || '파일')}
                                </a>
                            </li>
                        `).join('')}
                    </ul>
                </div>
            ` : ''}

            <!-- 버튼 영역 -->
            <div class="board-detail-actions">
                <button type="button" class="btn-back" onclick="goBack()">
                    <i class="fas fa-list"></i> 목록
                </button>
                <div class="action-buttons">
                    <button type="button" class="btn-edit" onclick="editBoard()" ${showEditDelete}>
                        <i class="fas fa-edit"></i> 수정
                    </button>
                    <button type="button" class="btn-delete" onclick="deleteBoard()" ${showEditDelete}>
                        <i class="fas fa-trash"></i> 삭제
                    </button>
                </div>
            </div>
        </div>
    `;
}

// 목록으로 돌아가기
function goBack() {
    if (categoryId) {
        window.location.href = `/view/board?categoryId=${categoryId}`;
    } else {
        window.location.href = '/view/board';
    }
}

// 게시글 수정
function editBoard() {
    if (!boardId) return;
    // categoryId도 함께 전달하여 수정 페이지에서 게시판 정보 유지
    if (categoryId) {
        window.location.href = `/view/board/edit?boardId=${boardId}&categoryId=${categoryId}`;
    } else {
        window.location.href = `/view/board/edit?boardId=${boardId}`;
    }
}

// 게시글 삭제
async function deleteBoard() {
    if (!boardId) return;

    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await apiFetch(`${BOARD_API}/${boardId}`, {
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
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 삭제에 실패했습니다.');
        }

        alert('게시글이 삭제되었습니다.');
        goBack();
    } catch (error) {
        console.error('Error deleting board:', error);
        alert(error.message || '게시글 삭제에 실패했습니다.');
    }
}

// 파일 다운로드
function downloadFile(fileId, fileName) {
    // TODO: 파일 다운로드 API 구현 시 연결
    console.log('Download file:', fileId, fileName);
    // window.location.href = `/api/files/${fileId}/download`;
}

// 에러 메시지 표시
function showError(message) {
    const content = document.getElementById('boardDetailContent');
    if (content) {
        content.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <p>${escapeHTML(message)}</p>
                <button type="button" class="btn-back" onclick="goBack()">
                    <i class="fas fa-arrow-left"></i> 목록으로 돌아가기
                </button>
            </div>
        `;
    }
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
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

