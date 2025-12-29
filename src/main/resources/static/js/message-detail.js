// 메시지 상세 페이지 JavaScript

const MESSAGE_API = '/api/messages';

let messageId = null;
let currentFolder = 'INBOX';
let messageDetailData = null;  // 메시지 상세 데이터 저장용

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
        // recipientId 파라미터 추가 (보낸 메시지함에서 특정 수신자 선택 시)
        const urlParams = new URLSearchParams(window.location.search);
        const recipientId = urlParams.get('recipientId');
        
        let url = `${MESSAGE_API}/${messageId}`;
        if (recipientId) {
            url += `?recipientId=${recipientId}`;
        }
        
        const response = await apiFetch(url, {
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
            messageDetailData = message;  // 메시지 상세 데이터 저장
            window.messageDetailData = message;  // goBack 함수에서 사용하기 위해 전역 변수로도 저장
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
    const folderType = message.folderType || currentFolder;  // 백엔드에서 받은 folderType 사용
    const archived = message.archived !== undefined ? message.archived : false;
    const createdAt = formatDateTime(message.createdAt);
    
    // 실제 폴더 타입에 따라 표시할 폴더명 결정 (보관함인 경우 원래 폴더 타입으로)
    const displayFolderType = archived ? 'ARCHIVE' : folderType;
    
    // 폴더명 표시
    const folderNames = {
        'INBOX': '받은 메시지함',
        'SENT': '보낸 메시지함',
        'ARCHIVE': '보관함'
    };
    const folderNameElement = document.getElementById('messageFolderName');
    if (folderNameElement) {
        folderNameElement.textContent = folderNames[displayFolderType] || '메시지';
    }

    // 폴더별 버튼 다르게 표시 (보관함 여부에 따라)
    let actionButtons = '';
    if (archived) {
        // 보관함인 경우 보관 해제 버튼
        actionButtons = `
            <button type="button" class="btn-unarchive" onclick="unarchiveMessage()">
                <i class="fas fa-inbox"></i> 보관 해제
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    } else if (folderType === 'INBOX') {
        // 받은 메시지함인 경우 보관함 이동 버튼
        actionButtons = `
            <button type="button" class="btn-archive" onclick="archiveMessage()">
                <i class="fas fa-archive"></i> 보관함 이동
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    } else if (folderType === 'SENT') {
        // 보낸 메시지함인 경우 보관함 이동 버튼
        actionButtons = `
            <button type="button" class="btn-archive" onclick="archiveMessage()">
                <i class="fas fa-archive"></i> 보관함 이동
            </button>
            <button type="button" class="btn-delete" onclick="deleteMessage()">
                <i class="fas fa-trash"></i> 삭제
            </button>
        `;
    }

    // 발신자/수신자 표시 (folderType에 따라)
    let peerInfo = '';
    const recipientName = message.recipientName || '';
    
    if (folderType === 'INBOX') {
        peerInfo = `<span class="meta-item">
            <i class="fas fa-user"></i> 발신자: ${escapeHTML(senderName)}
        </span>`;
    } else if (folderType === 'SENT') {
        // 보낸 메시지함은 수신자 표시
        peerInfo = `<span class="meta-item">
            <i class="fas fa-user"></i> 수신자: ${escapeHTML(recipientName)}
        </span>`;
    } else {
        peerInfo = `<span class="meta-item">
            <i class="fas fa-user"></i> 발신자: ${escapeHTML(senderName)}
        </span>`;
    }

    content.innerHTML = `
        <div class="board-detail-card">
            <!-- 메시지 헤더 -->
            <div class="board-detail-header-info">
                <h1 class="board-detail-title">${escapeHTML(title)}</h1>
                <div class="board-detail-meta">
                    ${peerInfo}
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
    // message 객체에서 archived와 folderType을 사용하여 돌아갈 경로 결정
    // 일단 현재 폴더 기준으로 처리 (추후 message 객체를 저장하여 사용할 수 있음)
    const folderPaths = {
        'INBOX': '/view/message/inbox',
        'SENT': '/view/message/sent',
        'ARCHIVE': '/view/message/archive'
    };
    
    // currentFolder가 ARCHIVE로 설정되어 있으면 보관함으로, 아니면 원래 폴더로
    let targetFolder = currentFolder;
    if (currentFolder === 'ARCHIVE' || window.messageDetailData?.archived) {
        targetFolder = 'ARCHIVE';
    }
    
    const path = folderPaths[targetFolder] || '/view/message/inbox';
    window.location.href = path;
}

// 보관함 이동
async function archiveMessage() {
    if (!messageId) {
        alert('메시지 ID가 없습니다.');
        return;
    }
    
    if (!confirm('메시지를 보관함으로 이동하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await apiFetch(`${MESSAGE_API}/${messageId}/archive`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('보관함 이동에 실패했습니다.');
        }
        
        alert('보관함으로 이동되었습니다.');
        
        // 보관함으로 이동
        window.location.href = '/view/message/archive';
    } catch (error) {
        console.error('Error archiving message:', error);
        alert('보관함 이동에 실패했습니다.');
    }
}

// 보관 해제
async function unarchiveMessage() {
    if (!messageId) {
        alert('메시지 ID가 없습니다.');
        return;
    }
    
    if (!confirm('메시지를 보관함에서 해제하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await apiFetch(`${MESSAGE_API}/${messageId}/unarchive`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('보관 해제에 실패했습니다.');
        }
        
        alert('보관함에서 해제되었습니다.');
        
        // 원래 폴더로 이동 (folderType에 따라)
        const folderType = messageDetailData?.folderType || 'INBOX';
        if (folderType === 'INBOX') {
            window.location.href = '/view/message/inbox';
        } else {
            window.location.href = '/view/message/sent';
        }
    } catch (error) {
        console.error('Error unarchiving message:', error);
        alert('보관 해제에 실패했습니다.');
    }
}

// 메시지 삭제
async function deleteMessage() {
    if (!messageId) {
        alert('메시지 ID가 없습니다.');
        return;
    }
    
    if (!confirm('메시지를 삭제하시겠습니까?\n삭제된 메시지는 복구할 수 없습니다.')) {
        return;
    }
    
    try {
        const response = await apiFetch(`${MESSAGE_API}/${messageId}`, {
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
            throw new Error('메시지 삭제에 실패했습니다.');
        }
        
        alert('메시지가 삭제되었습니다.');
        
        // 현재 폴더에 따라 목록으로 이동
        const folderType = messageDetailData?.folderType || currentFolder;
        if (messageDetailData?.archived) {
            window.location.href = '/view/message/archive';
        } else if (folderType === 'INBOX') {
            window.location.href = '/view/message/inbox';
        } else {
            window.location.href = '/view/message/sent';
        }
    } catch (error) {
        console.error('Error deleting message:', error);
        alert('메시지 삭제에 실패했습니다.');
    }
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


