// 메시지 상세 페이지 JavaScript

const MESSAGE_API = '/api/messages';

let messageId = null;
let currentFolder = 'INBOX';

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
    // URL 파라미터에서 messageId와 folder 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    messageId = urlParams.get('messageId');
    currentFolder = urlParams.get('folder') || 'INBOX';
    
    if (messageId) {
        messageId = parseInt(messageId);
        loadMessageDetail();
    } else {
        showError('메시지 ID가 없습니다.');
    }
    
    // 사이드바 선택 효과
    updateSidebarSelection();
});

// 사이드바 선택 효과
function updateSidebarSelection() {
    const links = {
        'INBOX': document.getElementById('inboxLink'),
        'SENT': document.getElementById('sentLink'),
        'ARCHIVE': document.getElementById('archiveLink')
    };
    
    // 모든 선택 상태 초기화
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });
    
    // 현재 폴더 선택
    if (currentFolder && links[currentFolder]) {
        const menuItem = links[currentFolder].closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}

// 메시지 상세 조회
async function loadMessageDetail() {
    if (!messageId) return;

    try {
        const response = await apiFetch(`${MESSAGE_API}/${messageId}`, {
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
                showError('메시지를 찾을 수 없습니다.');
                return;
            }
            throw new Error('메시지를 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const message = result.data;

        if (message) {
            renderMessageDetail(message);
        } else {
            showError('메시지 데이터가 없습니다.');
        }
    } catch (error) {
        console.error('Error loading message detail:', error);
        showError('메시지를 불러오는데 실패했습니다.');
    }
}

// 메시지 상세 렌더링
function renderMessageDetail(message) {
    const content = document.getElementById('messageDetailContent');
    if (!content) return;

    const title = message.title || '';
    const messageContent = message.content || '';
    const senderName = message.senderName || '';
    const createdAt = formatDateTime(message.createdAt);
    
    // 폴더명 표시
    const folderNames = {
        'INBOX': '받은 메시지함',
        'SENT': '보낸 메시지함',
        'ARCHIVE': '보관함'
    };
    const folderNameElement = document.getElementById('messageFolderName');
    if (folderNameElement) {
        folderNameElement.textContent = folderNames[currentFolder] || '메시지';
    }

    // 폴더별 버튼 다르게 표시
    let actionButtons = '';
    if (currentFolder === 'INBOX') {
        actionButtons = `
            <button type="button" class="btn-archive" onclick="archiveMessage()">
                <i class="fas fa-archive"></i> 보관함 이동
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    } else if (currentFolder === 'SENT') {
        actionButtons = `
            <button type="button" class="btn-archive" onclick="archiveMessage()">
                <i class="fas fa-archive"></i> 보관함 이동
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    } else if (currentFolder === 'ARCHIVE') {
        actionButtons = `
            <button type="button" class="btn-unarchive" onclick="unarchiveMessage()">
                <i class="fas fa-inbox"></i> 보관 해제
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    }

    content.innerHTML = `
        <div class="board-detail-card">
            <!-- 메시지 헤더 -->
            <div class="board-detail-header-info">
                <h1 class="board-detail-title">${escapeHTML(title)}</h1>
                <div class="board-detail-meta">
                    ${currentFolder === 'INBOX' ? `
                        <span class="meta-item">
                            <i class="fas fa-user"></i> 발신자: ${escapeHTML(senderName)}
                        </span>
                    ` : currentFolder === 'SENT' ? `
                        <span class="meta-item">
                            <i class="fas fa-user"></i> 수신자: ${escapeHTML(senderName)}
                        </span>
                    ` : `
                        <span class="meta-item">
                            <i class="fas fa-user"></i> 발신자: ${escapeHTML(senderName)}
                        </span>
                    `}
                    <span class="meta-item">
                        <i class="fas fa-calendar"></i> ${createdAt}
                    </span>
                </div>
            </div>

            <!-- 메시지 내용 -->
            <div class="board-detail-body">
                <div class="board-detail-text">${escapeHTML(messageContent).replace(/\n/g, '<br>')}</div>
            </div>

            <!-- 버튼 영역 -->
            <div class="board-detail-actions">
                <button type="button" class="btn-back" onclick="goBack()">
                    <i class="fas fa-arrow-left"></i> 뒤로가기
                </button>
                <div class="action-buttons">
                    ${actionButtons}
                </div>
            </div>
        </div>
    `;
}

// 목록으로 돌아가기
function goBack() {
    const folderPaths = {
        'INBOX': '/view/message/inbox',
        'SENT': '/view/message/sent',
        'ARCHIVE': '/view/message/archive'
    };
    const path = folderPaths[currentFolder] || '/view/message/inbox';
    window.location.href = path;
}

// 보관함 이동
async function archiveMessage() {
    // TODO: 보관함 이동 API 호출
    alert('보관함 이동 기능은 추후 구현 예정입니다.');
}

// 보관 해제
async function unarchiveMessage() {
    // TODO: 보관 해제 API 호출
    alert('보관 해제 기능은 추후 구현 예정입니다.');
}

// 메시지 삭제
async function deleteMessage() {
    if (!confirm('메시지를 삭제하시겠습니까?')) {
        return;
    }
    
    // TODO: 삭제 API 호출
    alert('삭제 기능은 추후 구현 예정입니다.');
}

// 에러 표시
function showError(message) {
    const content = document.getElementById('messageDetailContent');
    if (!content) return;
    
    content.innerHTML = `
        <div class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            <p>${escapeHTML(message)}</p>
            <button type="button" class="btn-back" onclick="goBack()">
                <i class="fas fa-arrow-left"></i> 뒤로가기
            </button>
        </div>
    `;
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

