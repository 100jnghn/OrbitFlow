/**
 * 챗봇 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // DOM 요소
    const floatIcon = document.getElementById('chatbotFloatIcon');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const closeBtn = document.getElementById('chatbotCloseBtn');
    const closeBottomBtn = document.getElementById('chatbotCloseBottomBtn');
    const categoryView = document.getElementById('chatbotCategoryView');
    const chatView = document.getElementById('chatbotChatView');
    const categoryButtons = document.getElementById('chatbotCategoryButtons');
    const backBtn = document.getElementById('chatbotBackBtn');
    const currentCategorySpan = document.getElementById('chatbotCurrentCategory');
    const messagesContainer = document.getElementById('chatbotMessages');
    const inputField = document.getElementById('chatbotInput');
    const sendBtn = document.getElementById('chatbotSendBtn');

    // 상태
    let selectedCategoryId = null;
    let selectedCategoryName = null;
    let categories = [];

    // 초기화
    init();

    function init() {
        loadCategories();
        setupEventListeners();
    }

    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 플로팅 아이콘 클릭
        floatIcon.addEventListener('click', () => {
            openChatbot();
        });

        // 닫기 버튼들
        closeBtn.addEventListener('click', closeChatbot);
        closeBottomBtn.addEventListener('click', closeChatbot);

        // 뒤로가기 버튼
        backBtn.addEventListener('click', () => {
            showCategoryView();
        });

        // 전송 버튼
        sendBtn.addEventListener('click', sendMessage);

        // 엔터 키로 전송
        inputField.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    // 챗봇 열기
    function openChatbot() {
        chatbotWindow.style.display = 'flex';
        if (!selectedCategoryId) {
            showCategoryView();
        } else {
            showChatView();
        }
    }

    // 챗봇 닫기
    function closeChatbot() {
        chatbotWindow.style.display = 'none';
    }

    // 카테고리 목록 로드
    async function loadCategories() {
        try {
            const response = await apiFetch('/api/manual/categories');
            if (!response.ok) {
                console.error('카테고리 목록 로드 실패');
                return;
            }

            const result = await response.json();
            categories = result.data || [];
            renderCategoryButtons();
        } catch (error) {
            console.error('카테고리 로드 오류:', error);
        }
    }

    // 카테고리 버튼 렌더링
    function renderCategoryButtons() {
        categoryButtons.innerHTML = '';

        // 카테고리별 예시 텍스트 매핑 (실제로는 카테고리 description을 사용하거나 DB에서 가져올 수 있음)
        const categoryExamples = {
            '결재 문서 정책': '출퇴근, 지각 등',
            '근태 및 휴가 정책': '연차, 경조사 등',
            '자원 예약 및 일정': '취업규칙, 복지 등',
            '기타 질문': '자유 검색'
        };

        categories.forEach(category => {
            const button = document.createElement('button');
            button.className = 'chatbot-category-btn';
            button.innerHTML = `
                <span class="category-name">${category.categoryName}</span>
                ${category.description ? `<span class="category-examples">${category.description}</span>` : ''}
            `;

            button.addEventListener('click', () => {
                selectCategory(category.id, category.categoryName);
            });

            categoryButtons.appendChild(button);
        });
    }

    // 카테고리 선택
    function selectCategory(categoryId, categoryName) {
        selectedCategoryId = categoryId;
        selectedCategoryName = categoryName;
        currentCategorySpan.textContent = categoryName;
        showChatView();

        // 환영 메시지 추가
        addBotMessage(`${categoryName} 관련 질문을 입력해주세요.`);
    }

    // 카테고리 화면 보이기
    function showCategoryView() {
        categoryView.style.display = 'block';
        chatView.style.display = 'none';
        selectedCategoryId = null;
        selectedCategoryName = null;
        messagesContainer.innerHTML = '';
    }

    // 채팅 화면 보이기
    function showChatView() {
        categoryView.style.display = 'none';
        chatView.style.display = 'flex';
        inputField.focus();
    }

    // 메시지 전송
    async function sendMessage() {
        const question = inputField.value.trim();
        if (!question) return;

        if (!selectedCategoryId) {
            alert('카테고리를 먼저 선택해주세요.');
            return;
        }

        // 사용자 메시지 추가
        addUserMessage(question);
        inputField.value = '';
        sendBtn.disabled = true;

        // 로딩 메시지 추가
        const loadingId = addBotMessage('답변을 생성하고 있습니다...', true);

        try {
            const response = await apiFetch('/api/chatbot/ask', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    question: question,
                    categoryId: selectedCategoryId
                })
            });

            if (!response.ok) {
                throw new Error('답변 생성 실패');
            }

            const result = await response.json();
            const answer = result.data || '답변을 생성할 수 없습니다.';

            // 로딩 메시지 제거하고 실제 답변 추가
            removeMessage(loadingId);
            addBotMessage(answer);

        } catch (error) {
            console.error('메시지 전송 오류:', error);
            removeMessage(loadingId);
            addBotMessage('오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            sendBtn.disabled = false;
        }
    }

    // 사용자 메시지 추가
    function addUserMessage(text) {
        const messageId = 'msg-' + Date.now();
        const messageElement = createMessageElement('user', text, messageId);
        messagesContainer.appendChild(messageElement);
        scrollToBottom();
    }

    // 봇 메시지 추가
    function addBotMessage(text, isLoading = false) {
        const messageId = 'msg-' + Date.now();
        const messageElement = createMessageElement('bot', text, messageId, isLoading);
        messagesContainer.appendChild(messageElement);
        scrollToBottom();
        return messageId;
    }

    // 메시지 요소 생성
    function createMessageElement(type, text, id, isLoading = false) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `chatbot-message ${type}`;
        messageDiv.id = id;

        const bubble = document.createElement('div');
        bubble.className = 'chatbot-message-bubble';
        bubble.textContent = text;

        const time = document.createElement('div');
        time.className = 'chatbot-message-time';
        const now = new Date();
        time.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

        messageDiv.appendChild(bubble);
        messageDiv.appendChild(time);

        if (isLoading) {
            bubble.style.opacity = '0.6';
            bubble.style.fontStyle = 'italic';
        }

        return messageDiv;
    }

    // 메시지 제거
    function removeMessage(messageId) {
        const messageElement = document.getElementById(messageId);
        if (messageElement) {
            messageElement.remove();
        }
    }

    // 스크롤을 맨 아래로
    function scrollToBottom() {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
});

