// 게시글 상세 페이지 JavaScript

const BOARD_API = '/api/boards';
const BOARD_CATEGORY_API = '/api/board-categories';
const COMMENT_API = '/api';

let boardId = null;
let categoryId = null;
let currentUserId = null;
let editingCommentId = null;
let currentCommentPage = 0;
let totalCommentPages = 1;

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
            loadComments(currentCommentPage);
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

        <!-- 댓글 섹션 -->
        <div class="comment-section">
            <h3 class="comment-section-title">댓글</h3>
            
            <!-- 댓글 작성 영역 -->
            <div class="comment-write-area">
                <textarea id="commentInput" class="comment-input" placeholder="댓글을 입력하세요..." rows="3" onkeydown="handleCommentKeydown(event)"></textarea>
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

// 댓글 목록 로드
async function loadComments(page = 0) {
    if (!boardId) return;

    try {
        const response = await apiFetch(`${COMMENT_API}/boards/${boardId}/comments?page=${page}&size=5&sort=createdAt,asc`, {
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
            console.error('댓글 목록을 불러오는데 실패했습니다.');
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
        console.error('Error loading comments:', error);
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
                        <textarea class="comment-edit-input" id="comment-edit-input-${commentId}" rows="3">${escapeHTML(content)}</textarea>
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

// 댓글 작성
async function submitComment() {
    if (!boardId) return;

    const commentInput = document.getElementById('commentInput');
    const content = commentInput?.value.trim();

    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    try {
        const response = await apiFetch(`${COMMENT_API}/boards/${boardId}/comments`, {
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
        loadComments(currentCommentPage);
    } catch (error) {
        console.error('Error submitting comment:', error);
        alert(error.message || '댓글 작성에 실패했습니다.');
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

    if (!content) {
        alert('댓글 내용을 입력해주세요.');
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
        console.error('Error saving comment:', error);
        alert(error.message || '댓글 수정에 실패했습니다.');
    }
}

// 댓글 삭제
async function deleteComment(commentId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
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
        console.error('Error deleting comment:', error);
        alert(error.message || '댓글 삭제에 실패했습니다.');
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

