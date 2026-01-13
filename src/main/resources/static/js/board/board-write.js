// 게시글 작성/수정 페이지 JavaScript

const BOARD_API = '/api/board-posts';
const BOARD_CATEGORY_API = '/api/board-categories';

let categoryId = null;
let boardId = null;
let selectedFiles = [];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function () {
    // URL 파라미터에서 categoryId와 boardId 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    categoryId = urlParams.get('categoryId') || document.getElementById('categoryId')?.value;
    boardId = urlParams.get('boardId') || document.getElementById('boardId')?.value;

    if (categoryId) {
        categoryId = parseInt(categoryId);
    }
    if (boardId) {
        boardId = parseInt(boardId);
        // 수정 모드: 게시글 상세 정보 로드 (categoryId도 함께 설정됨 - 목록으로 돌아갈 때 사용)
        await loadBoardDetail();
    }

    // 폼 제출 이벤트
    const form = document.getElementById('boardWriteForm');
    if (form) {
        form.addEventListener('submit', handleSubmit);
    }

    // 게시판 목록 로드 (사이드바용)
    loadBoardCategories();

    // 게시판 이름 표시
    updateBoardCategoryName();

    // 글자수 카운터 초기화
    updateTitleCharCount();
    updateContentCharCount();

    // 엔터키 폼 제출 방지
    preventEnterSubmit();
});

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

        // 게시판 이름 업데이트
        updateBoardCategoryName();
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

// 엔터키 폼 제출 방지
function preventEnterSubmit() {
    const titleInput = document.getElementById('boardTitleInput');

    // 제목 입력 필드에서 엔터키 방지
    if (titleInput) {
        titleInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
            }
        });
    }
}

// 게시글 상세 조회 (수정 모드)
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
                await sweetError('수정 권한이 없습니다.');
                location.href = `/view/board/detail?boardId=${boardId}`;
                return;
            }
            if (response.status === 404) {
                await sweetError('게시글이 존재하지 않거나 삭제되었습니다.');
                location.href = '/view/board';
                return;
            }
            throw new Error('게시글을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const board = result.data;

        if (board) {
            const titleInput = document.getElementById('boardTitleInput');
            const contentInput = document.getElementById('boardContent');

            if (titleInput) {
                titleInput.value = board.boardTitle || board.title || '';
                updateTitleCharCount();
            }
            if (contentInput) {
                contentInput.value = board.boardContent || board.content || '';
                updateContentCharCount();
            }

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

            // categoryId 설정 후 사이드바 다시 렌더링하여 선택 상태 표시
            if (categoryId && (cachedAccessibleBoards.length > 0 || cachedOrganizationBoards.length > 0)) {
                // 캐시된 게시판 목록으로 사이드바 다시 렌더링
                renderSidebar(cachedAccessibleBoards, cachedOrganizationBoards);
            }

            // 게시판 이름 표시
            updateBoardCategoryName();
            const submitBtn = document.getElementById('submitBtn');
            if (submitBtn) {
                submitBtn.textContent = '수정';
            }

            // 파일 목록 표시
            if (board.files && board.files.length > 0) {
                board.files.forEach(file => {
                    addFileToList(file.originalFileName || file.originalFile, file.id, true);
                });
            }
        }
    } catch (error) {
        sweetError('게시글을 불러오는데 실패했습니다.');
    }
}

// 파일 선택 처리
function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    files.forEach(file => {
        if (!selectedFiles.find(f => f.name === file.name && f.size === file.size)) {
            // 파일 크기 체크 (50MB 제한)
            const maxSize = 50 * 1024 * 1024; // 50MB
            if (file.size > maxSize) {
                sweetWarning(`파일 "${file.name}"의 크기가 50MB를 초과하여 제외되었습니다.`);
                return;
            }
            selectedFiles.push(file);
            addFileToList(file.name, null, false);
        }
    });
    event.target.value = ''; // 같은 파일 다시 선택 가능하도록
}

// 파일 목록에 추가
function addFileToList(fileName, fileId, isExisting) {
    const fileList = document.getElementById('fileList');
    const fileItem = document.createElement('div');
    fileItem.className = 'file-item';
    fileItem.dataset.fileId = fileId || '';
    fileItem.dataset.isExisting = isExisting || false;
    fileItem.innerHTML = `
        <span class="file-name">${escapeHTML(fileName)}</span>
        <button type="button" class="btn-file-remove" onclick="removeFile(this)">
            <i class="fas fa-times"></i>
        </button>
    `;
    fileList.appendChild(fileItem);
}

// 게시판 이름 업데이트
function updateBoardCategoryName() {
    const categoryNameElement = document.getElementById('boardCategoryName');
    if (!categoryNameElement) return;

    if (categoryId) {
        // 캐시된 게시판 목록에서 찾기
        const allBoards = [...cachedAccessibleBoards, ...cachedOrganizationBoards];
        const board = allBoards.find(b => b.id === categoryId);

        if (board) {
            categoryNameElement.textContent = board.boardName || board.name || '게시판';
        } else {
            // 캐시에 없으면 API로 가져오기
            loadBoardCategoryName(categoryId);
        }
    } else {
        categoryNameElement.textContent = '게시판';
    }
}

// 게시판 이름 로드
async function loadBoardCategoryName(catId) {
    try {
        const response = await apiFetch(`${BOARD_CATEGORY_API}/${catId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            const board = result.data;
            const categoryNameElement = document.getElementById('boardCategoryName');
            if (categoryNameElement && board) {
                categoryNameElement.textContent = board.boardName || board.name || '게시판';
            }
        }
    } catch (error) {
    }
}

// 파일 제거
function removeFile(button) {
    const fileItem = button.closest('.file-item');
    const fileId = fileItem.dataset.fileId;
    const isExisting = fileItem.dataset.isExisting === 'true';

    if (isExisting) {
        // 기존 파일은 서버에서 삭제해야 함 (나중에 구현)
    } else {
        // 새로 추가한 파일은 배열에서 제거
        const fileName = fileItem.querySelector('.file-name').textContent;
        selectedFiles = selectedFiles.filter(f => f.name !== fileName);
    }

    fileItem.remove();
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

// 폼 제출
async function handleSubmit(e) {
    e.preventDefault();

    // 에러 메시지 초기화
    hideError('boardTitleError');
    hideError('boardContentError');

    // 작성 모드일 때만 categoryId 체크
    if (!boardId && !categoryId) {
        await sweetWarning('게시판을 선택해주세요.');
        return;
    }

    const title = document.getElementById('boardTitleInput').value.trim();
    const content = document.getElementById('boardContent').value.trim();

    if (!title) {
        showError('boardTitleError', '제목을 입력하세요');
        return;
    }

    if (title.length > 100) {
        showError('boardTitleError', '제목은 100자 이하여야 합니다.');
        return;
    }

    if (!content) {
        showError('boardContentError', '내용을 입력하세요');
        return;
    }

    if (content.length > 10000) {
        showError('boardContentError', '내용은 10000자 이하여야 합니다.');
        return;
    }

    try {
        setLoading(document.querySelector('.board-container'), true, '게시글 저장 중...');

        const formData = new FormData();

        if (boardId) {
            // 수정 모드: @ModelAttribute를 사용하므로 직접 필드 추가
            formData.append('boardTitle', title);
            formData.append('boardContent', content);

            // 유지할 기존 파일 ID 목록 추가
            const existingFileItems = document.querySelectorAll('#fileList .file-item[data-is-existing="true"]');
            existingFileItems.forEach(item => {
                const fid = item.dataset.fileId;
                if (fid) {
                    formData.append('keptFileIds', fid);
                }
            });
        } else {
            // 작성 모드: 직접 필드 추가
            formData.append('categoryId', categoryId);
            formData.append('boardTitle', title);
            formData.append('boardContent', content);
        }

        // 파일 추가
        selectedFiles.forEach(file => {
            formData.append('files', file);
        });

        const url = boardId ? `${BOARD_API}/${boardId}` : BOARD_API;
        const method = boardId ? 'PUT' : 'POST';

        // FormData를 사용할 때는 Content-Type 헤더를 설정하지 않음 (브라우저가 자동 설정)
        // apiFetch는 기본적으로 Authorization 헤더만 추가하므로 FormData와 함께 사용 가능
        const response = await apiFetch(url, {
            method: method,
            body: formData
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            if (response.status === 403) {
                throw new Error(boardId ? '게시글 수정 권한이 없습니다.' : '게시글 작성 권한이 없습니다.');
            }
            if (response.status === 404) {
                throw new Error('게시판 또는 게시글 정보를 찾을 수 없습니다.');
            }
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || '게시글 저장에 실패했습니다.');
        }

        const result = await response.json();
        const savedBoard = result.data;
        const savedBoardId = savedBoard?.id || savedBoard?.boardId || boardId;

        const successMessage = boardId ? '게시글이 수정되었습니다.' : '게시글이 등록되었습니다.';
        await sweetSuccess(successMessage);

        if (!savedBoardId) {
            // boardId가 없으면 목록으로 이동
            window.location.href = `/view/board?categoryId=${categoryId}`;
            return;
        }

        // 게시글 상세 페이지로 이동 (categoryId 포함)
        if (categoryId) {
            window.location.href = `/view/board/detail?boardId=${savedBoardId}&categoryId=${categoryId}`;
        } else {
            window.location.href = `/view/board/detail?boardId=${savedBoardId}`;
        }
    } catch (error) {
        sweetError(error.message || '게시글 저장에 실패했습니다.');
    } finally {
        setLoading(document.querySelector('.board-container'), false);
    }
}

// 취소
async function cancelWrite() {
    const result = await sweetConfirm(
        '작성 취소',
        '작성 중인 내용이 사라집니다. 정말 취소하시겠습니까?'
    );
    if (result.isConfirmed) {
        // 수정 모드일 때는 게시글 상세 페이지로 이동
        if (boardId) {
            if (categoryId) {
                window.location.href = `/view/board/detail?boardId=${boardId}&categoryId=${categoryId}`;
            } else {
                window.location.href = `/view/board/detail?boardId=${boardId}`;
            }
        } else {
            // 작성 모드일 때는 게시판 목록으로 이동
            if (categoryId) {
                window.location.href = `/view/board?categoryId=${categoryId}`;
            } else {
                window.location.href = '/view/board';
            }
        }
    }
}

// 제목 글자수 카운터 업데이트 (50자 이상부터 표시, 80자 이상 경고)
function updateTitleCharCount() {
    const input = document.getElementById('boardTitleInput');
    const countElement = document.getElementById('titleCharCount');
    if (input && countElement) {
        const currentLength = input.value.length;
        const maxLength = 100;

        // 50자 이상일 때만 표시
        if (currentLength >= 50) {
            countElement.style.display = 'block';
            countElement.textContent = `${currentLength} / ${maxLength}`;

            // 80자 이상이면 경고 색상
            if (currentLength >= 80) {
                countElement.style.color = '#EF4444';
            } else {
                countElement.style.color = '#6B7280';
            }
        } else {
            countElement.style.display = 'none';
        }
    }
}

// 내용 글자수 카운터 업데이트 (8000자 이상부터 표시, 9500자 이상 경고)
function updateContentCharCount() {
    const input = document.getElementById('boardContent');
    const countElement = document.getElementById('contentCharCount');
    if (input && countElement) {
        const currentLength = input.value.length;
        const maxLength = 10000;

        // 8000자 이상일 때만 표시
        if (currentLength >= 8000) {
            countElement.style.display = 'block';
            countElement.textContent = `${currentLength} / ${maxLength}`;

            // 9500자 이상이면 경고 색상
            if (currentLength >= 9500) {
                countElement.style.color = '#EF4444';
            } else {
                countElement.style.color = '#6B7280';
            }
        } else {
            countElement.style.display = 'none';
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

// 로딩 스피너 제어
function setLoading(targetEl, loading, message = '') {
    if (!targetEl) return;

    let overlay = targetEl.querySelector(':scope > .loading-overlay');

    if (loading) {
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'loading-overlay';
            overlay.innerHTML = `
        <div class="spinner"></div>
        ${message ? `<div class="loading-text">${message}</div>` : ''}
      `;
            targetEl.style.position = 'relative'; // 필요한 경우 주석 해제 (기본적으로 container가 relative여야 함)
            targetEl.appendChild(overlay);
        } else {
            const textEl = overlay.querySelector('.loading-text');
            if (textEl) textEl.textContent = message;
        }
    } else {
        overlay?.remove();
    }
}


