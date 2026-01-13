// 게시글 상세 페이지 JavaScript

const BOARD_API = '/api/board-posts';
const BOARD_CATEGORY_API = '/api/board-categories';
const COMMENT_API = '/api/board-posts';

let boardId = null;
let categoryId = null;
let currentUserId = null;
let editingCommentId = null;
let currentCommentPage = 0;
let totalCommentPages = 1;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function () {
    // URL 파라미터에서 boardId와 categoryId 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    boardId = urlParams.get('boardId') || document.getElementById('boardId')?.value;
    const urlCategoryId = urlParams.get('categoryId');

    // URL에서 categoryId가 있으면 먼저 설정
    if (urlCategoryId) {
        categoryId = parseInt(urlCategoryId);
    }

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
    }
}

// 게시판 카테고리 목록 로드 (사이드바용)
let cachedAccessibleBoards = [];
let cachedOrganizationBoards = [];

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

        cachedAccessibleBoards = accessibleData.data || [];
        cachedOrganizationBoards = orgData.data || [];

        renderSidebar(cachedAccessibleBoards, cachedOrganizationBoards);

        // categoryId가 이미 설정되어 있으면 사이드바 선택 효과 적용
        if (categoryId) {
            // 사이드바 렌더링이 완료된 후 선택 효과 적용
            setTimeout(() => {
                updateSidebarSelection(categoryId);
            }, 100);
        }
    } catch (error) {
    }
}

// 사이드바 렌더링 (아코디언 구조)
function renderSidebar(accessibleBoards, organizationBoards) {
    const sidebar = document.getElementById('boardSidebar');
    if (!sidebar) return;

    sidebar.innerHTML = '';

    // 일반 게시판 (권한이 있는 게시판) - 단일 항목으로 표시
    if (accessibleBoards && accessibleBoards.length > 0) {
        accessibleBoards.forEach(board => {
            const isSelected = board.id === categoryId;
            const li = document.createElement('li');
            li.className = 'menu-item no-sub' + (isSelected ? ' selected' : '');
            li.innerHTML = `
                <a href="/view/board?categoryId=${board.id}" class="board-link">
                    <span>${escapeHTML(board.boardName || board.name || '게시판')}</span>
            </a>
        `;
            sidebar.appendChild(li);
        });
    }

    // 조직 게시판 - 아코디언 구조로 표시 (기본적으로 펼쳐진 상태)
    if (organizationBoards && organizationBoards.length > 0) {
        const hasSelectedOrgBoard = organizationBoards.some(board => board.id === categoryId);
        const orgMenuItem = document.createElement('li');
        orgMenuItem.className = 'menu-item active';
        orgMenuItem.innerHTML = `
            <div class="menu-title" role="button" onclick="toggleBoardMenu(this)" aria-expanded="true">
                <span>부서게시판</span>
                <i class="fas fa-chevron-down arrow"></i>
            </div>
            <ul class="sub-menu">
                ${organizationBoards.map(board => {
            const isSelected = board.id === categoryId;
            return `
                        <li class="${isSelected ? 'selected' : ''}">
                            <a href="/view/board?categoryId=${board.id}" class="board-link">
                                ${escapeHTML(board.boardName || board.name || '게시판')}
                            </a>
                        </li>
                    `;
        }).join('')}
            </ul>
        `;
        sidebar.appendChild(orgMenuItem);
    }
}

// 사이드바 선택 효과 업데이트
function updateSidebarSelection(selectedCategoryId) {
    if (!selectedCategoryId) return;


    // 모든 선택 상태 초기화
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('selected');
    });
    document.querySelectorAll('.sub-menu li').forEach(item => {
        item.classList.remove('selected');
    });

    // 선택된 항목 찾아서 표시
    document.querySelectorAll('.board-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href) {
            // URL 파라미터에서 categoryId 정확히 추출
            try {
                const url = new URL(href, window.location.origin);
                const linkCategoryId = url.searchParams.get('categoryId');

                // 정확한 숫자 비교
                if (linkCategoryId && parseInt(linkCategoryId) === selectedCategoryId) {
                    const menuItem = link.closest('.menu-item');
                    const subMenuItem = link.closest('.sub-menu li');

                    if (subMenuItem) {
                        // 부서게시판인 경우
                        subMenuItem.classList.add('selected');
                        const parentMenuItem = subMenuItem.closest('.menu-item');
                        if (parentMenuItem) {
                            parentMenuItem.classList.add('active'); // 아코디언 열기
                            const menuTitle = parentMenuItem.querySelector('.menu-title');
                            if (menuTitle) {
                                menuTitle.setAttribute('aria-expanded', 'true');
                            }
                        }
                    } else if (menuItem) {
                        // 일반 게시판인 경우
                        menuItem.classList.add('selected');
                    }
                }
            } catch (e) {
                // 상대 경로인 경우 수동 파싱
                const urlParams = new URLSearchParams(href.split('?')[1] || '');
                const linkCategoryId = urlParams.get('categoryId');

                if (linkCategoryId && parseInt(linkCategoryId) === selectedCategoryId) {
                    const menuItem = link.closest('.menu-item');
                    const subMenuItem = link.closest('.sub-menu li');

                    if (subMenuItem) {
                        // 부서게시판인 경우
                        subMenuItem.classList.add('selected');
                        const parentMenuItem = subMenuItem.closest('.menu-item');
                        if (parentMenuItem) {
                            parentMenuItem.classList.add('active'); // 아코디언 열기
                            const menuTitle = parentMenuItem.querySelector('.menu-title');
                            if (menuTitle) {
                                menuTitle.setAttribute('aria-expanded', 'true');
                            }
                        }
                    } else if (menuItem) {
                        // 일반 게시판인 경우
                        menuItem.classList.add('selected');
                    }
                }
            }
        }
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
            if (response.status === 403) {
                showError('접근 권한이 없습니다.');
                return;
            }
            if (response.status === 404) {
                showError('게시글이 존재하지 않거나 삭제되었습니다.');
                return;
            }
            throw new Error('게시글을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const board = result.data;

        if (board) {
            // 1. API 응답에서 categoryId 추출
            const boardCategoryId = board.categoryId || board.category_id || board.category?.id;

            if (boardCategoryId) {
                const parsedId = parseInt(boardCategoryId);
                if (!isNaN(parsedId)) {
                    // URL 파라미터가 없거나 API 결과가 다를 경우 업데이트
                    if (!categoryId || categoryId !== parsedId) {
                        categoryId = parsedId;
                        sessionStorage.setItem('lastBoardCategoryId', categoryId);
                    }
                }
            } else if (!categoryId) {
                // API 결과도 없고 URL도 없으면 세션 저장소 시도
                const savedId = sessionStorage.getItem('lastBoardCategoryId');
                if (savedId) categoryId = parseInt(savedId);
            }

            // categoryId 설정 후 사이드바 선택 효과 업데이트
            // 게시판 목록이 이미 로드되어 있으면 사이드바 다시 렌더링 후 선택 효과 적용
            if (categoryId) {
                if (cachedAccessibleBoards.length > 0 || cachedOrganizationBoards.length > 0) {
                    // 캐시된 게시판 목록으로 사이드바 다시 렌더링
                    renderSidebar(cachedAccessibleBoards, cachedOrganizationBoards);
                    // 렌더링 후 선택 효과 적용
                    setTimeout(() => {
                        updateSidebarSelection(categoryId);
                    }, 100);
                } else {
                    // 게시판 목록이 아직 로드되지 않았으면 로드 후 선택 효과 적용됨
                }
            }
            renderBoardDetail(board);
            // 댓글이 활성화된 경우에만 댓글 로드
            if (board.commentActivated !== false) {
                loadComments(currentCommentPage);
            }
        } else {
            showError('게시글 데이터가 없습니다.');
        }
    } catch (error) {
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
    const commentActivated = board.commentActivated !== false; // 기본값은 true (undefined인 경우도 허용)

    // 게시판 이름 표시
    const categoryNameElement = document.getElementById('boardCategoryName');
    if (categoryNameElement && categoryName) {
        categoryNameElement.textContent = categoryName;
    }

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
                        <i class="fas fa-paperclip"></i> 첨부파일
                    </h3>
                    <ul class="files-list">
                        ${files.map(file => `
                            <li class="file-item">
                                <a href="#" class="file-link" onclick="downloadFile(${file.id}, '${escapeHTML(file.originalFileName || file.originalFile || '')}'); return false;">
                                    <i class="fas fa-file"></i> ${escapeHTML(file.originalFileName || file.originalFile || '파일')} (${formatFileSize(file.fileSize)})
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

        <!-- 댓글 섹션 -->
        ${commentActivated ? `
        <div class="comment-section" id="commentSection">
            <h3 class="comment-section-title">댓글</h3>
            
            <!-- 댓글 작성 영역 -->
            <div class="comment-write-area">
                <textarea id="commentInput" class="comment-input" placeholder="댓글을 입력하세요." rows="3" onkeydown="handleCommentKeydown(event)" oninput="updateCommentCharCount()" maxlength="500"></textarea>
                <div id="commentCharCount" class="char-count">0/500</div>
                <div id="commentError" class="error-message" style="display: none;"></div>
                <div class="comment-write-actions">
                    <button type="button" class="btn-comment-submit" onclick="submitComment()">
                        등록
                    </button>
                </div>
            </div>

            <!-- 댓글 목록 -->
            <div class="comment-list" id="commentList">
                <div class="loading-message">댓글을 불러오는 중...</div>
            </div>
        </div>
        ` : ''}
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

    const result = await sweetConfirm('삭제 확인', '정말 삭제하시겠습니까?');
    if (!result.isConfirmed) {
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
            if (response.status === 403) {
                throw new Error('게시글 삭제 권한이 없습니다.');
            }
            if (response.status === 404) {
                throw new Error('이미 삭제되었거나 존재하지 않는 게시글입니다.');
            }
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || '게시글 삭제에 실패했습니다.');
        }

        await sweetSuccess('게시글이 삭제되었습니다.');
        // categoryId를 사용하여 해당 게시판 목록으로 이동
        if (categoryId) {
            window.location.href = `/view/board?categoryId=${categoryId}`;
        } else {
            window.location.href = '/view/board';
        }
    } catch (error) {
        sweetError(error.message || '게시글 삭제에 실패했습니다.');
    }
}

// 파일 다운로드
async function downloadFile(fileId, fileName) {
    if (!fileId) {
        sweetError('파일을 찾을 수 없습니다.');
        return;
    }


    try {
        // fetch API를 사용하여 Authorization 헤더 포함
        const response = await apiFetch(`/api/files/${fileId}/download`, {
            method: 'GET'
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            if (response.status === 403) {
                sweetError('파일 다운로드 권한이 없습니다.');
                return;
            }
            if (response.status === 404) {
                sweetError('파일을 찾을 수 없습니다.');
                return;
            }
            throw new Error('파일 다운로드에 실패했습니다.');
        }

        // Blob으로 변환
        const blob = await response.blob();

        // 다운로드 링크 생성
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName || '파일';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    } catch (error) {
        sweetError('파일 다운로드에 실패했습니다.');
    }
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

// 파일 크기 포맷
function formatFileSize(bytes) {
    if (bytes === 0) return '0 KB';
    if (!bytes) return '';
    const output = Math.log(bytes) / Math.log(1024);
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(output);
    return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i];
}

// 댓글 목록 로드
async function loadComments(page = 0) {
    if (!boardId) return;

    try {
        const response = await apiFetch(`${COMMENT_API}/${boardId}/comments?page=${page}&size=5&sort=createdAt,asc`, {
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
            return;
        }

        const result = await response.json();
        const commentData = result.data;
        const comments = commentData?.content || commentData || [];
        const totalPages = commentData?.totalPages || 1;
        const currentPage = commentData?.number !== undefined ? commentData.number : page;

        currentCommentPage = currentPage;
        totalCommentPages = totalPages;

        renderComments(comments);
        renderCommentPagination();
    } catch (error) {
        const commentList = document.getElementById('commentList');
        if (commentList) {
            commentList.innerHTML = '<div class="error-message">댓글을 불러오는데 실패했습니다.</div>';
        }
    }
}

// 댓글 목록 렌더링
function renderComments(comments) {
    const commentList = document.getElementById('commentList');
    if (!commentList) return;

    if (comments.length === 0) {
        commentList.innerHTML = '<div class="no-comments">댓글이 없습니다.</div>';
        return;
    }

    commentList.innerHTML = comments.map(comment => {
        const commentId = comment.commentId || comment.id;
        const writerId = comment.writerId || comment.writer?.id;
        const writerName = comment.writerName || comment.writer?.name || '';
        const content = comment.content || comment.commentContent || '';
        const createdAt = comment.createdAt || comment.created_at;
        const updatedAt = comment.updatedAt || comment.updated_at;

        // 수정 여부 확인 (updatedAt이 createdAt보다 나중이면 수정됨)
        let isModified = false;
        if (updatedAt && createdAt) {
            const created = new Date(createdAt).getTime();
            const updated = new Date(updatedAt).getTime();
            // 1초 이상 차이나면 수정된 것으로 간주 (DB 업데이트 시간 차이 고려)
            isModified = (updated - created) > 1000;
        }
        const displayDate = isModified ? formatDateTime(updatedAt) + ' (수정됨)' : formatDateTime(createdAt);

        // 작성자 본인인지 확인
        const isWriter = currentUserId && writerId && currentUserId === writerId;
        const showEditDelete = isWriter ? '' : 'style="display: none;"';

        return `
            <div class="comment-item" id="comment-${commentId}">
                <div class="comment-content-wrapper">
                    <div class="comment-content-display" id="comment-content-display-${commentId}">
                        <div class="comment-text">${escapeHTML(content).replace(/\n/g, '<br>')}</div>
                        <div class="comment-meta">
                            <span class="comment-writer">${escapeHTML(writerName)}</span>
                            <span class="comment-date">${displayDate}</span>
                        </div>
                    </div>
                    <div class="comment-content-edit" id="comment-content-edit-${commentId}" style="display: none;">
                        <textarea class="comment-edit-input" id="comment-edit-input-${commentId}" rows="3" maxlength="500" oninput="updateEditCommentCharCount(${commentId})">${escapeHTML(content)}</textarea>
                        <div id="comment-edit-char-count-${commentId}" class="char-count">${content.length}/500</div>
                        <div class="comment-edit-actions">
                            <button type="button" class="btn-comment-save" onclick="saveComment(${commentId})">저장</button>
                            <button type="button" class="btn-comment-cancel" onclick="cancelEditComment(${commentId})">취소</button>
                        </div>
                    </div>
                </div>
                <div class="comment-actions" ${showEditDelete}>
                    <button type="button" class="btn-comment-edit" onclick="editComment(${commentId})">수정</button>
                    <button type="button" class="btn-comment-delete" onclick="deleteComment(${commentId})">삭제</button>
                </div>
            </div>
        `;
    }).join('');
}

// 에러 메시지 표시/숨김 함수 (댓글 등 폼 요소용)
function showCommentError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

function hideCommentError(elementId) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.style.display = 'none';
    }
}

// 댓글 글자수 카운터 업데이트 (상시 표시)
function updateCommentCharCount() {
    const input = document.getElementById('commentInput');
    const countElement = document.getElementById('commentCharCount');
    if (input && countElement) {
        const currentLength = input.value.length;
        const maxLength = 500;

        // 항상 표시
        countElement.style.display = 'block';
        countElement.textContent = `${currentLength}/${maxLength}`;

        // 500자 이상이면 경고 색상
        if (currentLength >= 500) {
            countElement.style.color = '#EF4444';
        } else {
            countElement.style.color = '#6B7280';
        }
    }
}

// 댓글 수정 글자수 카운터 업데이트 (상시 표시)
function updateEditCommentCharCount(commentId) {
    const input = document.getElementById(`comment-edit-input-${commentId}`);
    const countElement = document.getElementById(`comment-edit-char-count-${commentId}`);
    if (input && countElement) {
        const currentLength = input.value.length;
        const maxLength = 500;

        // 항상 표시
        countElement.style.display = 'block';
        countElement.textContent = `${currentLength}/${maxLength}`;

        // 500자 이상이면 경고 색상
        if (currentLength >= 500) {
            countElement.style.color = '#EF4444';
        } else {
            countElement.style.color = '#6B7280';
        }
    }
}

// 댓글 작성
async function submitComment() {
    if (!boardId) return;

    const commentInput = document.getElementById('commentInput');
    const content = commentInput?.value.trim();

    // 에러 메시지 초기화
    hideCommentError('commentError');

    if (!content || content.length === 0) {
        showCommentError('commentError', '댓글 내용을 입력해주세요.');
        return;
    }

    // 공백만 입력된 경우 체크
    if (!content.replace(/\s/g, '').length) {
        showCommentError('commentError', '공백만 입력된 댓글은 등록할 수 없습니다.');
        return;
    }

    if (content.length > 500) {
        showCommentError('commentError', '댓글은 500자 이하여야 합니다.');
        return;
    }

    try {
        const response = await apiFetch(`${COMMENT_API}/${boardId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                commentContent: content
            })
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 작성에 실패했습니다.');
        }

        commentInput.value = '';
        hideCommentError('commentError');
        updateCommentCharCount(); // 카운터 초기화 (0/500 표시)
        loadComments(currentCommentPage);
    } catch (error) {
        showCommentError('commentError', error.message || '댓글 작성에 실패했습니다.');
    }
}

// 댓글 수정 모드로 전환
function editComment(commentId) {
    // 다른 댓글의 수정 모드 취소
    if (editingCommentId && editingCommentId !== commentId) {
        cancelEditComment(editingCommentId);
    }

    const displayDiv = document.getElementById(`comment-content-display-${commentId}`);
    const editDiv = document.getElementById(`comment-content-edit-${commentId}`);

    if (displayDiv && editDiv) {
        displayDiv.style.display = 'none';
        editDiv.style.display = 'block';
        editingCommentId = commentId;

        // 텍스트 영역에 포커스
        const textarea = document.getElementById(`comment-edit-input-${commentId}`);
        if (textarea) {
            textarea.focus();
            textarea.setSelectionRange(textarea.value.length, textarea.value.length);
            updateEditCommentCharCount(commentId); // 수정 모드 진입 시 카운터 업데이트
        }
    }
}

// 댓글 수정 취소
function cancelEditComment(commentId) {
    const displayDiv = document.getElementById(`comment-content-display-${commentId}`);
    const editDiv = document.getElementById(`comment-content-edit-${commentId}`);

    if (displayDiv && editDiv) {
        displayDiv.style.display = 'block';
        editDiv.style.display = 'none';
        editingCommentId = null;
    }
}

// 댓글 수정 저장
async function saveComment(commentId) {
    const textarea = document.getElementById(`comment-edit-input-${commentId}`);
    const content = textarea?.value.trim();

    if (!content || content.length === 0) {
        await sweetWarning('댓글 내용을 입력해주세요.');
        return;
    }

    // 공백만 입력된 경우 체크
    if (!content.replace(/\s/g, '').length) {
        await sweetWarning('공백만 입력된 댓글은 등록할 수 없습니다.');
        return;
    }

    if (content.length > 500) {
        await sweetWarning('댓글은 500자 이하여야 합니다.');
        return;
    }

    if (content.length > 1000) {
        await sweetWarning('댓글은 1000자 이하여야 합니다.');
        return;
    }

    try {
        const response = await apiFetch(`${COMMENT_API}/comments/${commentId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                commentContent: content
            })
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 수정에 실패했습니다.');
        }

        editingCommentId = null;
        loadComments(currentCommentPage);
    } catch (error) {
        sweetError(error.message || '댓글 수정에 실패했습니다.');
    }
}

// 댓글 삭제
async function deleteComment(commentId) {
    const result = await sweetConfirm('삭제 확인', '정말 삭제하시겠습니까?');
    if (!result.isConfirmed) {
        return;
    }

    try {
        const response = await apiFetch(`${COMMENT_API}/comments/${commentId}`, {
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
            throw new Error(errorData.message || '댓글 삭제에 실패했습니다.');
        }

        loadComments(currentCommentPage);
    } catch (error) {
        sweetError(error.message || '댓글 삭제에 실패했습니다.');
    }
}

// 댓글 입력 키보드 이벤트 처리 (Ctrl+Enter로 등록)
function handleCommentKeydown(event) {
    if (event.ctrlKey && event.key === 'Enter') {
        event.preventDefault();
        submitComment();
    }
}

// 댓글 페이지네이션 렌더링
function renderCommentPagination() {
    // 기존 페이지네이션 제거
    const existingPagination = document.getElementById('commentPagination');
    if (existingPagination) {
        existingPagination.remove();
    }

    // 댓글이 5개 이하면 페이지네이션 표시 안 함
    if (totalCommentPages <= 1) {
        return;
    }

    const commentSection = document.querySelector('.comment-section');
    if (!commentSection) return;

    const pagination = document.createElement('div');
    pagination.id = 'commentPagination';
    pagination.className = 'comment-pagination';

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '‹';
    prevBtn.className = 'page-btn';
    prevBtn.disabled = currentCommentPage === 0;
    prevBtn.addEventListener('click', () => {
        if (currentCommentPage > 0) loadComments(currentCommentPage - 1);
    });
    pagination.appendChild(prevBtn);

    // 페이지 번호
    const maxVisible = 5;
    let startPage = Math.max(0, currentCommentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalCommentPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }

    if (startPage > 0) {
        const firstBtn = document.createElement('span');
        firstBtn.className = 'page-number';
        firstBtn.textContent = '1';
        firstBtn.addEventListener('click', () => loadComments(0));
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
        pageBtn.className = 'page-number' + (i === currentCommentPage ? ' active' : '');
        pageBtn.textContent = i + 1;
        pageBtn.addEventListener('click', () => loadComments(i));
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalCommentPages - 1) {
        if (endPage < totalCommentPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'ellipsis';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
        const lastBtn = document.createElement('span');
        lastBtn.className = 'page-number';
        lastBtn.textContent = totalCommentPages;
        lastBtn.addEventListener('click', () => loadComments(totalCommentPages - 1));
        pagination.appendChild(lastBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '›';
    nextBtn.className = 'page-btn';
    nextBtn.disabled = currentCommentPage >= totalCommentPages - 1;
    nextBtn.addEventListener('click', () => {
        if (currentCommentPage < totalCommentPages - 1) loadComments(currentCommentPage + 1);
    });
    pagination.appendChild(nextBtn);

    commentSection.appendChild(pagination);
}

