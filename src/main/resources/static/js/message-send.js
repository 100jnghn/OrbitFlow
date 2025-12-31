// 메시지 보내기 페이지 JavaScript

const MESSAGE_API = '/api/messages';
const EMPLOYEE_SEARCH_API = '/api/admin/rules/employees/search';

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
        
        if (keyword.length < 1) {
            resultsDiv.innerHTML = '';
            resultsDiv.style.display = 'none';
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
        
        const employees = await response.json();
        displaySearchResults(employees);
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
            <div class="employee-details">${escapeHTML(emp.employeeNo || '')} | ${escapeHTML(emp.organizationName || '')} | ${escapeHTML(emp.positionName || '')}</div>
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

// 폼 제출 처리
async function handleSubmit(event) {
    event.preventDefault();
    
    // 유효성 검사
    const title = document.getElementById('messageTitle').value.trim();
    const content = document.getElementById('messageContent').value.trim();
    
    if (!title) {
        alert('제목을 입력해주세요.');
        return;
    }
    
    if (!content) {
        alert('내용을 입력해주세요.');
        return;
    }
    
    if (selectedRecipients.length === 0) {
        alert('수신자를 최소 1명 이상 선택해주세요.');
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

