// 게시글 작성/수정 페이지 JavaScript

const BOARD_API = '/api/boards';
const BOARD_CATEGORY_API = '/api/board-categories';

let categoryId = null;
let boardId = null;
let selectedFiles = [];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
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
});

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
        li.className = 'sidebar-menu-item';
        if (board.id === categoryId) {
            li.classList.add('selected');
        }
        li.innerHTML = `
            <a href="/view/board?categoryId=${board.id}" class="board-link">
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
            li.className = 'sidebar-menu-item';
            if (board.id === categoryId) {
                li.classList.add('selected');
            }
            li.innerHTML = `
                <a href="/view/board?categoryId=${board.id}" class="board-link">
                    - ${escapeHTML(board.boardName || board.name || '게시판')}
                </a>
            `;
            sidebar.appendChild(li);
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
            throw new Error('게시글을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const board = result.data;

        if (board) {
            document.getElementById('boardTitleInput').value = board.boardTitle || board.title || '';
            document.getElementById('boardContent').value = board.boardContent || board.content || '';
            
            // categoryId 설정 (URL 파라미터가 없으면 게시글에서 가져옴)
            if (!categoryId) {
                categoryId = board.categoryId || board.category?.id;
                if (categoryId) {
                    categoryId = parseInt(categoryId);
                }
            }

            // 제목 업데이트
            const titleElement = document.getElementById('boardTitle');
            if (titleElement) {
                titleElement.textContent = '글수정';
            }
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
        console.error('Error loading board detail:', error);
        alert('게시글을 불러오는데 실패했습니다.');
    }
}

// 파일 선택 처리
function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    files.forEach(file => {
        if (!selectedFiles.find(f => f.name === file.name && f.size === file.size)) {
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

// 파일 제거
function removeFile(button) {
    const fileItem = button.closest('.file-item');
    const fileId = fileItem.dataset.fileId;
    const isExisting = fileItem.dataset.isExisting === 'true';

    if (isExisting) {
        // 기존 파일은 서버에서 삭제해야 함 (나중에 구현)
        console.log('Remove existing file:', fileId);
    } else {
        // 새로 추가한 파일은 배열에서 제거
        const fileName = fileItem.querySelector('.file-name').textContent;
        selectedFiles = selectedFiles.filter(f => f.name !== fileName);
    }

    fileItem.remove();
}

// 폼 제출
async function handleSubmit(e) {
    e.preventDefault();

    // 작성 모드일 때만 categoryId 체크
    if (!boardId && !categoryId) {
        alert('게시판을 선택해주세요.');
        return;
    }

    const title = document.getElementById('boardTitleInput').value.trim();
    const content = document.getElementById('boardContent').value.trim();

    if (!title) {
        alert('제목을 입력해주세요.');
        return;
    }

    if (!content) {
        alert('내용을 입력해주세요.');
        return;
    }

    try {
        const formData = new FormData();

        if (boardId) {
            // 수정 모드: request 파트에 JSON 객체로 전송
            const requestData = {
                boardTitle: title,
                boardContent: content
            };
            formData.append('request', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));
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
            // headers를 명시하지 않으면 apiFetch가 Authorization만 추가하고
            // 브라우저가 FormData에 대해 자동으로 Content-Type: multipart/form-data를 설정함
        });

        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 저장에 실패했습니다.');
        }

        const result = await response.json();
        alert(boardId ? '게시글이 수정되었습니다.' : '게시글이 등록되었습니다.');
        
        // 게시판 목록으로 이동
        window.location.href = `/view/board?categoryId=${categoryId}`;
    } catch (error) {
        console.error('Error submitting board:', error);
        alert(error.message || '게시글 저장에 실패했습니다.');
    }
}

// 취소
function cancelWrite() {
    if (confirm('작성 중인 내용이 사라집니다. 정말 취소하시겠습니까?')) {
        if (categoryId) {
            window.location.href = `/view/board?categoryId=${categoryId}`;
        } else {
            window.location.href = '/view/board';
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

