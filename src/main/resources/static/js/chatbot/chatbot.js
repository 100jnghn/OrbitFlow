/**
 * Orbitflow Chatbot JS - 새로고침 복구 및 카테고리 부재 시 안내 기능 통합본
 */
document.addEventListener('DOMContentLoaded', function () {
    // === DOM 요소 ===
    const floatIcon = document.getElementById('chatbotFloatIcon');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const closeBtn = document.getElementById('chatbotCloseBtn');
    const messagesContainer = document.getElementById('chatbotMessages');
    const inputField = document.getElementById('chatbotInput');
    const sendBtn = document.getElementById('chatbotSendBtn');

    // === 상태 관리 ===
    let currentConversationId = sessionStorage.getItem('activeChatId'); // 새로고침 시 세션에서 복구
    let selectedCategoryName = sessionStorage.getItem('activeCategoryName');
    let categories = [];

    // 드래그 관련 변수
    let isDragging = false;
    let dragStartX, dragStartY;
    const DRAG_THRESHOLD = 5;

    init();

    function init() {
        setupEventListeners();
    }

    function setupEventListeners() {
        floatIcon.addEventListener('mousedown', startDragging);
        closeBtn.addEventListener('click', () => {
            chatbotWindow.style.display = 'none';
        });
        sendBtn.addEventListener('click', sendMessage);
        inputField.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    // 아이콘 클릭/드래그 핸들러
    function startDragging(e) {
        isDragging = false;
        dragStartX = e.clientX;
        dragStartY = e.clientY;
        const rect = floatIcon.getBoundingClientRect();

        const moveHandler = (moveEvent) => {
            const dx = moveEvent.clientX - dragStartX;
            const dy = moveEvent.clientY - dragStartY;
            if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) isDragging = true;

            if (isDragging) {
                floatIcon.style.left = (rect.left + dx) + 'px';
                floatIcon.style.top = (rect.top + dy) + 'px';
                floatIcon.style.right = 'auto';
                floatIcon.style.bottom = 'auto';
            }
        };

        const stopHandler = () => {
            document.removeEventListener('mousemove', moveHandler);
            document.removeEventListener('mouseup', stopHandler);
            if (!isDragging) openChatbot();
        };

        document.addEventListener('mousemove', moveHandler);
        document.addEventListener('mouseup', stopHandler);
    }

    /**
     * 🚀 챗봇 열기 - 상태에 따른 분기 처리
     */
    function openChatbot() {
        chatbotWindow.style.display = 'flex';

        if (currentConversationId) {
            // 1. 진행 중인 세션이 있으면 복원
            restoreConversation(currentConversationId);
        } else {
            // 2. 새로운 시작
            startWelcomeFlow();
        }
    }

    /**
     * 🚀 대화 복원 로직
     */
    async function restoreConversation(conversationId) {
        messagesContainer.innerHTML = '';
        addBotMessage(`${selectedCategoryName}에 대해 대화 중이었습니다. 이전 기록을 불러옵니다...`);

        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${conversationId}/messages`);
            if (response.ok) {
                const result = await response.json();
                const messages = result.data || [];

                messages.forEach(msg => {
                    if (msg.role === 'USER') addUserMessage(msg.content, msg.createdAt);
                    else if (msg.role === 'ASSISTANT') {
                        const msgEl = createMessageElement('bot', msg.content, null, false, msg.createdAt);
                        messagesContainer.appendChild(msgEl);
                    }
                });
                showRechoicePrompt();
            } else {
                clearChatSession();
                startWelcomeFlow();
            }
        } catch (error) {
            startWelcomeFlow();
        }
        scrollToBottom();
    }

    function startWelcomeFlow() {
        messagesContainer.innerHTML = '';
        addBotMessage("안녕하세요! Orbitflow AI 도우미입니다. ✨\n궁금하신 분야를 선택하시면 상세히 안내해 드리겠습니다.");
        loadAndShowCategories();
    }

    /**
     * 🚀 카테고리 로드 및 공백 상태(Empty State) 처리
     */
    async function loadAndShowCategories() {
        try {
            const response = await apiFetch(`/api/manual/categories?t=${Date.now()}`);
            if (!response.ok) return addBotMessage("카테고리를 불러올 수 없습니다.");

            const result = await response.json();
            const list = result?.data ?? [];

            // ✅ 관리자가 등록한 카테고리가 없는 경우 안내 문구 표시
            if (list.length === 0) {
                renderEmptyCategoryState();
                return;
            }

            renderCategoryChoices(list);
        } catch (error) {
            addBotMessage("데이터 로드 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🚀 카테고리 부재 시 안내 UI 생성
     */
    function renderEmptyCategoryState() {
        const emptyMsg = document.createElement('div');
        emptyMsg.style.cssText = "background:#fff; border:1px dashed #ddd; border-radius:12px; padding:20px; text-align:center; color:#666; margin:10px 0;";
        emptyMsg.innerHTML = `
            <div style="font-size:24px; margin-bottom:10px;">🚧</div>
            <strong>현재 등록된 매뉴얼 카테고리가 없습니다.</strong><br>
            <small>보다 정확한 정보를 제공해드리기 위해<br>관리자가 매뉴얼을 준비하고 있습니다. 😊</small>
        `;
        messagesContainer.appendChild(emptyMsg);
        scrollToBottom();
    }

    function renderCategoryChoices(list) {
        const group = document.createElement('div');
        group.className = 'chatbot-choice-group';
        list.forEach(cat => {
            const btn = document.createElement('button');
            btn.className = 'chatbot-choice-btn';
            btn.textContent = cat.categoryName || cat.name;
            btn.onclick = () => handleCategorySelect(cat.id || cat.categoryId, btn.textContent, group);
            group.appendChild(btn);
        });
        messagesContainer.appendChild(group);
        scrollToBottom();
    }

    function handleCategorySelect(id, name, group) {
        if (group) group.remove();
        addUserMessage(name);
        createNewConversation(id, name);
    }

    async function createNewConversation(categoryId, categoryName) {
        try {
            const response = await apiFetch('/api/auth/chatbot/conversations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ manualCategoryId: categoryId })
            });

            if (response.ok) {
                const result = await response.json();
                currentConversationId = String(result.data.conversationId);
                selectedCategoryName = categoryName;

                sessionStorage.setItem('activeChatId', currentConversationId);
                sessionStorage.setItem('activeCategoryName', selectedCategoryName);

                addBotMessage(`${categoryName} 주제로 대화를 시작합니다. 질문을 입력해주세요.`);
            }
        } catch (error) {
            addBotMessage("대화방 생성 실패");
        }
    }

    async function sendMessage() {
        const content = inputField.value.trim();
        if (!content || !currentConversationId) return;

        addUserMessage(content);
        inputField.value = '';
        const loadingId = addBotMessage('답변을 작성 중입니다...', true);

        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${currentConversationId}/messages`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: content })
            });

            removeMessage(loadingId);
            if (response.ok) {
                const result = await response.json();
                const answer = result.data.assistant.content;
                const createdAt = result.data.assistant.createdAt;
                const msgEl = createMessageElement('bot', '', null, false, createdAt);
                messagesContainer.appendChild(msgEl);
                typeWriter(msgEl.querySelector('.chatbot-message-bubble'), answer);
            }
        } catch (error) {
            removeMessage(loadingId);
            addBotMessage('전송 오류가 발생했습니다.');
        }
    }

    function typeWriter(element, text) {
        let i = 0;
        function type() {
            if (i < text.length) {
                element.textContent += text.charAt(i++);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
                setTimeout(type, 20);
            } else {
                setTimeout(showRechoicePrompt, 500);
            }
        }
        type();
    }

    function showRechoicePrompt() {
        const oldGroup = document.querySelector('.rechoice-group');
        if (oldGroup) oldGroup.remove();

        const group = document.createElement('div');
        group.className = 'chatbot-choice-group rechoice-group';
        const rechoiceBtn = document.createElement('button');
        rechoiceBtn.className = 'chatbot-choice-btn highlight';
        rechoiceBtn.innerHTML = '<i class=\"fas fa-redo-alt\"></i> 다른 카테고리 선택하기';

        rechoiceBtn.onclick = () => {
            clearChatSession();
            startWelcomeFlow();
        };

        group.appendChild(rechoiceBtn);
        messagesContainer.appendChild(group);
        scrollToBottom();
    }

    function clearChatSession() {
        sessionStorage.removeItem('activeChatId');
        sessionStorage.removeItem('activeCategoryName');
        currentConversationId = null;
        selectedCategoryName = null;
    }

    function addUserMessage(text, timestamp = null) {
        const ts = timestamp || new Date().toISOString();
        messagesContainer.appendChild(createMessageElement('user', text, null, false, ts));
        scrollToBottom();
    }

    function addBotMessage(text, isLoading = false) {
        const id = 'bot-' + Date.now();
        messagesContainer.appendChild(createMessageElement('bot', text, id, isLoading));
        scrollToBottom();
        return id;
    }

    function createMessageElement(type, text, id, isLoading = false, timestamp = null) {
        const div = document.createElement('div');
        div.className = `chatbot-message ${type}`;
        if (id) div.id = id;
        const bubble = document.createElement('div');
        bubble.className = 'chatbot-message-bubble';
        bubble.textContent = text;
        if (isLoading) bubble.style.fontStyle = 'italic';
        div.appendChild(bubble);

        if (timestamp && !isLoading) {
            const timeSpan = document.createElement('span');
            timeSpan.className = 'chatbot-message-time';
            timeSpan.textContent = formatDate(timestamp);
            div.appendChild(timeSpan);
        }

        return div;
    }

    function formatDate(isoString) {
        if (!isoString) return '';
        try {
            const date = new Date(isoString);
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${hours}:${minutes}`;
        } catch (e) {
            return '';
        }
    }

    function removeMessage(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    async function apiFetch(url, options = {}) {
        const token = sessionStorage.getItem('accessToken');
        const headers = { ...(options.headers || {}) };
        if (token) headers['Authorization'] = `Bearer ${token}`;
        return fetch(url, { ...options, headers });
    }
});