// 메시지 목록 페이지 JavaScript

const MESSAGE_API = '/api/messages';

let currentFolder = null;
let currentPage = 0;
let totalPages = 1;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // URL에서 folderType 확인
    const urlPath = window.location.pathname;
    if (urlPath.includes('/inbox')) {
        currentFolder = 'INBOX';
    } else if (urlPath.includes('/sent')) {
        currentFolder = 'SENT';
    } else if (urlPath.includes('/archive')) {
        currentFolder = 'ARCHIVE';
    } else if (urlPath.includes('/send')) {
        // 메시지 보내기 페이지는 목록 로드하지 않음
        return;
    }
    
    // 사이드바 선택 효과
    updateSidebarSelection();
    
    // 메시지 목록 로드
    if (currentFolder) {
        loadMessageList(0);
    }
    
    // 날짜 필터 이벤트 리스너
    setupDateFilter();
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

// 메시지 목록 로드
async function loadMessageList(page = 0) {
    if (!currentFolder) {
        return;
    }

    try {
        const params = new URLSearchParams({
            page: page,
            size: 10,
            sort: 'createdAt,desc'
        });
        
        // 보관함인 경우 archived=true, 그 외에는 archived=false
        if (currentFolder === 'ARCHIVE') {
            params.append('archived', 'true');
            // 보관함은 원래 폴더 타입을 지정하지 않거나, 둘 다 조회해야 할 수도 있음
            // 일단 폴더 파라미터 없이 archived만 true로 설정
        } else {
            params.append('archived', 'false');
            params.append('folder', currentFolder);
        }

        // 검색 조건 추가
        const dateFilter = document.getElementById('dateFilter')?.value;
        const startDate = document.getElementById('startDate')?.value;
        const endDate = document.getElementById('endDate')?.value;
        const searchType = document.getElementById('searchType')?.value;
        const keyword = document.getElementById('searchKeyword')?.value;

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

        const response = await apiFetch(`${MESSAGE_API}?${params.toString()}`, {
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
            throw new Error('메시지 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const messageData = result.data || result;
        
        const messageList = messageData?.content || messageData?.elements || (Array.isArray(messageData) ? messageData : []);
        totalPages = messageData?.totalPages || messageData?.totalPageCount || 1;
        currentPage = messageData?.number !== undefined ? messageData.number : page;

        renderMessageTable(messageList);
        renderPagination(currentPage);
    } catch (error) {
        console.error('Error loading message list:', error);
        if (error.message !== 'SESSION_EXPIRED') {
            alert('메시지 목록을 불러오는데 실패했습니다.');
        }
    }
}

// 메시지 테이블 렌더링
function renderMessageTable(messages) {
    const tbody = document.getElementById('messageTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';

    if (messages.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 40px; color: #9ca3af;">등록된 메시지가 없습니다.</td></tr>';
        return;
    }

    messages.forEach((message, index) => {
        const row = document.createElement('tr');
        const rowNumber = currentPage * 10 + index + 1;
        
        const title = message.title || '';
        const peerName = message.peerName || '';  // senderName → peerName
        const createdAt = formatDateTime(message.createdAt);
        const read = message.read !== undefined ? message.read : false;  // isRead → read
        const readStatus = read ? formatDateTime(message.readAt) : '-';
        
        // 보관함인 경우 원래 폴더 타입을 확인
        const folderType = message.folderType || currentFolder;
        
        // 폴더별로 다른 컬럼 렌더링
        if (currentFolder === 'INBOX' || (currentFolder === 'ARCHIVE' && folderType === 'INBOX')) {
            row.innerHTML = `
                <td>${rowNumber}</td>
                <td>
                    <a href="#" class="board-title-link" onclick="viewMessage(${message.messageId}, '${folderType}'); return false;">
                        ${escapeHTML(title)}
                    </a>
                </td>
                <td>${escapeHTML(peerName)}</td>
                <td>${createdAt}</td>
                <td>${readStatus}</td>
            `;
        } else if (currentFolder === 'SENT' || (currentFolder === 'ARCHIVE' && folderType === 'SENT')) {
            // 보낸 메시지함은 peerName에 수신자 정보가 있음
            row.innerHTML = `
                <td>${rowNumber}</td>
                <td>
                    <a href="#" class="board-title-link" onclick="viewMessage(${message.messageId}, '${folderType}'); return false;">
                        ${escapeHTML(title)}
                    </a>
                </td>
                <td>${escapeHTML(peerName)}</td>
                <td>${createdAt}</td>
                <td>${readStatus}</td>
            `;
        } else {
            // 기본값
            row.innerHTML = `
                <td>${rowNumber}</td>
                <td>
                    <a href="#" class="board-title-link" onclick="viewMessage(${message.messageId}, '${folderType}'); return false;">
                        ${escapeHTML(title)}
                    </a>
                </td>
                <td>${escapeHTML(peerName)}</td>
                <td>${createdAt}</td>
                <td>${readStatus}</td>
            `;
        }
        
        tbody.appendChild(row);
    });
}

// 페이지네이션 렌더링
function renderPagination(page) {
    const pagination = document.getElementById('messagePagination');
    if (!pagination) return;
    
    pagination.innerHTML = '';

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'page-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => {
        if (page > 0) {
            currentPage = page - 1;
            loadMessageList(currentPage);
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
            loadMessageList(currentPage);
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
            loadMessageList(currentPage);
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
            loadMessageList(currentPage);
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
            loadMessageList(currentPage);
        }
    };
    pagination.appendChild(nextBtn);
}

// 검색
function searchMessages() {
    currentPage = 0;
    loadMessageList(currentPage);
}

// 날짜 필터 설정 및 검색 입력 엔터 키 이벤트
function setupDateFilter() {
    const dateFilter = document.getElementById('dateFilter');
    if (!dateFilter) return;
    
    dateFilter.addEventListener('change', function() {
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        
        if (!startDateInput || !endDateInput) return;
        
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
    
    // 검색 입력 필드에 엔터 키 이벤트 추가
    const searchKeyword = document.getElementById('searchKeyword');
    if (searchKeyword) {
        searchKeyword.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchMessages();
            }
        });
    }
}

// 메시지 상세 보기
function viewMessage(messageId, folder) {
    if (!messageId) {
        alert('메시지 ID가 없습니다.');
        return;
    }
    window.location.href = `/view/message/detail?messageId=${messageId}&folder=${folder}`;
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


