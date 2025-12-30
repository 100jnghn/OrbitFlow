// 메시지 보내기 페이지 JavaScript

const MESSAGE_API = '/api/messages';
const EMPLOYEE_SEARCH_API = '/api/employees/search';

let selectedRecipients = []; // {id, name, employeeNo, organizationName, positionName}
let selectedFileId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 사이드바 선택 효과
    updateSidebarSelection();
    
    // 수신자 검색 이벤트 리스너
    setupRecipientSearch();
    
    // 폼 제출 이벤트 리스너
    document.getElementById('messageSendForm').addEventListener('submit', handleSubmit);
    
    // 답장 모드 확인 및 초기화
    initializeReplyMode();
    
    // 글자수 카운터 설정
    setupCharCounters();
});

// 사이드바 선택 효과
function updateSidebarSelection() {
    // 모든 선택 상태 초기화
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });
    
    // 메시지 보내기 선택
    const sendLink = document.getElementById('sendLink');
    if (sendLink) {
        const menuItem = sendLink.closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}

// 수신자 검색 설정
function setupRecipientSearch() {
    const searchInput = document.getElementById('recipientSearch');
    const resultsDiv = document.getElementById('recipientSearchResults');
    
    let searchTimeout;
    
    searchInput.addEventListener('input', function() {
        const keyword = this.value.trim();
        
        clearTimeout(searchTimeout);
        
        // 최소 2자 이상 입력 시에만 검색
        if (keyword.length < 2) {
            resultsDiv.innerHTML = '';
            resultsDiv.style.display = 'none';
            return;
        }
        
        // 최대 30자 제한 (maxlength로 이미 제한되지만 추가 확인)
        if (keyword.length > 30) {
            return;
        }
        
        searchTimeout = setTimeout(() => {
            searchEmployees(keyword);
        }, 300);
    });
    
    // 외부 클릭 시 검색 결과 닫기
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !resultsDiv.contains(e.target)) {
            resultsDiv.style.display = 'none';
        }
    });
}

// 사원 검색
async function searchEmployees(keyword) {
    if (!keyword || keyword.length < 1) {
        return;
    }
    
    try {
        const response = await apiFetch(`${EMPLOYEE_SEARCH_API}?keyword=${encodeURIComponent(keyword)}`, {
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
            throw new Error('사원 검색에 실패했습니다.');
        }
        
        const result = await response.json();
        const employees = result.data || result;
        displaySearchResults(Array.isArray(employees) ? employees : []);
    } catch (error) {
        console.error('Error searching employees:', error);
        const resultsDiv = document.getElementById('recipientSearchResults');
        resultsDiv.innerHTML = '<div class="search-result-item error">검색 중 오류가 발생했습니다.</div>';
        resultsDiv.style.display = 'block';
    }
}

// 검색 결과 표시
function displaySearchResults(employees) {
    const resultsDiv = document.getElementById('recipientSearchResults');
    resultsDiv.innerHTML = '';
    
    // 검색 결과가 50건 이상이면 안내 메시지 표시
    if (Array.isArray(employees) && employees.length >= 50) {
        resultsDiv.innerHTML = '<div class="search-result-item no-results">검색 결과가 너무 많습니다. 검색어를 더 입력해주세요.</div>';
        resultsDiv.style.display = 'block';
        return;
    }
    
    if (employees.length === 0) {
        resultsDiv.innerHTML = '<div class="search-result-item no-results">검색 결과가 없습니다.</div>';
        resultsDiv.style.display = 'block';
        return;
    }
    
    employees.forEach(emp => {
        // 이미 선택된 수신자는 제외
        if (selectedRecipients.some(r => r.id === emp.id)) {
            return;
        }
        
        const item = document.createElement('div');
        item.className = 'search-result-item';
        item.innerHTML = `
            <div class="employee-name">${escapeHTML(emp.name)}</div>
            <div class="employee-details">${escapeHTML(emp.employeeNo || '')} | ${escapeHTML(emp.email || '')} | ${escapeHTML(emp.organizationName || '')} | ${escapeHTML(emp.positionName || '')}</div>
        `;
        item.addEventListener('click', () => selectRecipient(emp));
        resultsDiv.appendChild(item);
    });
    
    resultsDiv.style.display = 'block';
}

// 수신자 선택
function selectRecipient(employee) {
    // 중복 체크
    if (selectedRecipients.some(r => r.id === employee.id)) {
        return;
    }
    
    selectedRecipients.push({
        id: employee.id,
        name: employee.name,
        employeeNo: employee.employeeNo,
        organizationName: employee.organizationName,
        positionName: employee.positionName
    });
    
    renderSelectedRecipients();
    
    // 검색 입력 초기화
    document.getElementById('recipientSearch').value = '';
    document.getElementById('recipientSearchResults').style.display = 'none';
}

// 선택된 수신자 렌더링
function renderSelectedRecipients() {
    const container = document.getElementById('selectedRecipients');
    container.innerHTML = '';
    
    if (selectedRecipients.length === 0) {
        return;
    }
    
    selectedRecipients.forEach((recipient, index) => {
        const chip = document.createElement('div');
        chip.className = 'recipient-chip';
        chip.innerHTML = `
            <i class="fas fa-user"></i>
            <span>${escapeHTML(recipient.name)}</span>
            <button type="button" class="btn-remove-recipient" onclick="removeRecipient(${index})">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(chip);
    });
}

// 수신자 제거
function removeRecipient(index) {
    selectedRecipients.splice(index, 1);
    renderSelectedRecipients();
}

// 파일 선택 처리
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) {
        return;
    }
    
    // 파일 업로드 API 호출 (추후 구현)
    // 일단 파일명만 표시
    document.getElementById('fileName').textContent = file.name;
    document.getElementById('selectedFile').style.display = 'flex';
    
    // TODO: 파일 업로드 API 호출하여 fileId 받기
    // selectedFileId = fileId;
    alert('파일 업로드 기능은 추후 구현 예정입니다.');
}

// 파일 제거
function removeFile() {
    document.getElementById('fileInput').value = '';
    document.getElementById('selectedFile').style.display = 'none';
    selectedFileId = null;
}

// 글자수 카운터 설정
function setupCharCounters() {
    const titleInput = document.getElementById('messageTitle');
    const contentTextarea = document.getElementById('messageContent');
    const titleCharCount = document.getElementById('titleCharCount');
    const contentCharCount = document.getElementById('contentCharCount');
    
    // 제목 글자수 카운터
    if (titleInput && titleCharCount) {
        titleInput.addEventListener('input', function() {
            updateTitleCharCount();
        });
    }
    
    // 내용 글자수 카운터
    if (contentTextarea && contentCharCount) {
        contentTextarea.addEventListener('input', function() {
            updateContentCharCount();
        });
    }
}

// 제목 글자수 카운터 업데이트
function updateTitleCharCount() {
    const titleInput = document.getElementById('messageTitle');
    const titleCharCount = document.getElementById('titleCharCount');
    
    if (!titleInput || !titleCharCount) return;
    
    const currentLength = titleInput.value.length;
    const maxLength = 100;
    
    // 80자 이상부터 표시
    if (currentLength >= 80) {
        titleCharCount.style.display = 'block';
        titleCharCount.textContent = `${currentLength} / ${maxLength}`;
        
        // 90자 이상부터 경고 색상
        if (currentLength >= 90) {
            titleCharCount.style.color = '#EF4444'; // 경고 색상
        } else {
            titleCharCount.style.color = '#6B7280'; // 기본 색상
        }
    } else {
        titleCharCount.style.display = 'none';
    }
    
    // 100자 초과 시 입력 차단
    if (currentLength > maxLength) {
        titleInput.value = titleInput.value.substring(0, maxLength);
        updateTitleCharCount(); // 다시 업데이트
    }
}

// 내용 글자수 카운터 업데이트
function updateContentCharCount() {
    const contentTextarea = document.getElementById('messageContent');
    const contentCharCount = document.getElementById('contentCharCount');
    
    if (!contentTextarea || !contentCharCount) return;
    
    const currentLength = contentTextarea.value.length;
    const maxLength = 3000;
    
    // 2,500자 이상부터 표시
    if (currentLength >= 2500) {
        contentCharCount.style.display = 'block';
        contentCharCount.textContent = `${currentLength} / ${maxLength}`;
        
        // 2,800자 이상부터 경고 색상
        if (currentLength >= 2800) {
            contentCharCount.style.color = '#EF4444'; // 경고 색상
        } else {
            contentCharCount.style.color = '#6B7280'; // 기본 색상
        }
    } else {
        contentCharCount.style.display = 'none';
    }
    
    // 3,000자 초과 시 입력 차단
    if (currentLength > maxLength) {
        contentTextarea.value = contentTextarea.value.substring(0, maxLength);
        updateContentCharCount(); // 다시 업데이트
    }
}

// 폼 제출 처리
async function handleSubmit(event) {
    event.preventDefault();
    
    // 유효성 검사
    const title = document.getElementById('messageTitle').value.trim();
    const content = document.getElementById('messageContent').value.trim();
    
    if (!title) {
        alert('제목을 입력해주세요.');
        document.getElementById('messageTitle').focus();
        return;
    }
    
    // 제목 글자수 검증 (100자 초과)
    if (title.length > 100) {
        alert('제목은 100자 이하여야 합니다.');
        document.getElementById('messageTitle').focus();
        return;
    }
    
    if (!content) {
        alert('내용을 입력해주세요.');
        document.getElementById('messageContent').focus();
        return;
    }
    
    // 공백만 입력된 내용 검증
    if (!content.replace(/\s/g, '').length) {
        alert('공백만 입력된 내용은 전송할 수 없습니다.');
        document.getElementById('messageContent').focus();
        return;
    }
    
    // 내용 글자수 검증 (3,000자 초과)
    if (content.length > 3000) {
        alert('내용은 3,000자 이하여야 합니다.');
        document.getElementById('messageContent').focus();
        return;
    }
    
    if (selectedRecipients.length === 0) {
        alert('수신자를 최소 1명 이상 선택해주세요.');
        document.getElementById('recipientSearch').focus();
        return;
    }
    
    // 전송 데이터 구성
    const requestData = {
        messageTitle: title,
        messageContent: content,
        recipientEmployeeIds: selectedRecipients.map(r => r.id),
        fileId: selectedFileId || null
    };
    
    try {
        const response = await apiFetch(MESSAGE_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                location.href = '/login';
                return;
            }
            
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || '메시지 전송에 실패했습니다.');
        }
        
        const result = await response.json();
        alert('메시지가 전송되었습니다.');
        
        // 보낸 메시지함으로 이동
        window.location.href = '/view/message/sent';
    } catch (error) {
        console.error('Error sending message:', error);
        alert(error.message || '메시지 전송에 실패했습니다.');
    }
}

// 답장 모드 초기화
async function initializeReplyMode() {
    const urlParams = new URLSearchParams(window.location.search);
    const replyTo = urlParams.get('replyTo');
    
    if (!replyTo) {
        return; // 답장 모드가 아님
    }
    
    const senderId = urlParams.get('senderId');
    const originalTitle = urlParams.get('originalTitle') || '';
    const originalContent = urlParams.get('originalContent') || '';
    const originalSenderName = urlParams.get('originalSenderName') || '';
    const originalCreatedAt = urlParams.get('originalCreatedAt') || '';
    
    if (!senderId) {
        console.warn('답장 모드이지만 발신자 ID가 없습니다.');
        return;
    }
    
    // 발신자 정보를 사원 검색 API로 찾기
    if (originalSenderName) {
        try {
            // 발신자 이름으로 검색
            const response = await apiFetch(`${EMPLOYEE_SEARCH_API}?keyword=${encodeURIComponent(originalSenderName)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                const employees = result.data || result;
                
                if (Array.isArray(employees)) {
                    // senderId와 일치하는 사원 찾기
                    const sender = employees.find(emp => emp.id && emp.id.toString() === senderId.toString());
                    
                    if (sender) {
                        // 수신자로 자동 추가
                        selectRecipient(sender);
                    }
                }
            }
        } catch (error) {
            console.error('Error loading sender info for reply:', error);
            // 에러가 발생해도 계속 진행 (수동 입력 가능)
        }
    }
    
    // 제목 자동 설정: "RE: {원문 제목}" (중복 RE: 방지)
    let replyTitle = originalTitle;
    if (replyTitle && !replyTitle.startsWith('RE: ')) {
        replyTitle = 'RE: ' + replyTitle;
    }
    document.getElementById('messageTitle').value = replyTitle;
    // 답장 모드에서 제목 설정 후 글자수 카운터 업데이트
    updateTitleCharCount();
    
    // 본문에 원문 인용 텍스트 추가
    if (originalContent) {
        // HTML 태그 제거 (이스케이프 전에 처리)
        let cleanContent = originalContent.replace(/<[^>]*>/g, ''); // HTML 태그 제거
        cleanContent = cleanContent.replace(/&nbsp;/g, ' '); // &nbsp;를 공백으로
        cleanContent = cleanContent.replace(/<br\s*\/?>/gi, '\n'); // <br>을 줄바꿈으로
        
        let quotedContent = '';
        if (originalSenderName) {
            quotedContent += `From: ${escapeHTML(originalSenderName)}\n`;
        }
        if (originalCreatedAt) {
            const formattedDate = formatDateTime(originalCreatedAt);
            quotedContent += `Date: ${formattedDate}\n`;
        }
        if (originalTitle) {
            quotedContent += `Title: ${escapeHTML(originalTitle)}\n`;
        }
        quotedContent += '\n' + escapeHTML(cleanContent);
        
        // 원문 앞에 "-----Original Message-----" 구분선 추가 (위에 두 줄 공백)
        document.getElementById('messageContent').value = `\n\n-----Original Message-----\n${quotedContent}\n\n`;
        // 답장 모드에서 내용 설정 후 글자수 카운터 업데이트
        updateContentCharCount();
    }
}

// 날짜 시간 포맷 (답장 모드용)
function formatDateTime(dateString) {
    if (!dateString) return '';
    try {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (e) {
        return dateString;
    }
}

// 뒤로가기
function goBack() {
    if (confirm('작성 중인 내용이 저장되지 않습니다. 정말 취소하시겠습니까?')) {
        window.location.href = '/view/message/inbox';
    }
}

// HTML 이스케이프
function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

