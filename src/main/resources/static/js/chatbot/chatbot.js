/**
 * Orbitflow Chatbot JS (RAG & 대화 저장 지원 버전 - FIX)
 * ✅ 수정 사항
 * 1) 카테고리 API 경로 수정: /api/auth/manual/categories  ->  /api/manual/categories
 * 2) result.status === 'OK' 같은 비교 제거(환경마다 status 포맷이 달라 실패 가능)
 *    -> response.ok 기반 처리 + result.data 사용
 * 3) 챗봇 열 때마다 카테고리 재조회(관리자 변경 반영)
 */
document.addEventListener('DOMContentLoaded', function () {
    // === DOM 요소 ===
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

    // === 상태 관리 ===
    let currentConversationId = sessionStorage.getItem('activeChatId'); // 대화방 ID 세션 복원
    let selectedCategoryName = sessionStorage.getItem('activeCategoryName');
    let categories = [];

    init();

    function init() {
        setupEventListeners();

        // 카테고리는 미리 로드
        loadCategories();

        // 기존에 진행 중인 대화가 있다면 복원
        if (currentConversationId) {
            restoreConversation(currentConversationId);
        }
    }

    // === 이벤트 리스너 ===
    function setupEventListeners() {
        floatIcon.addEventListener('click', () => {
            chatbotWindow.style.display = 'flex';

            // ✅ 열 때마다 최신 카테고리 다시 로드(관리자 변경 반영)
            loadCategories();

            if (!currentConversationId) showCategoryView(false);
            else showChatView();
        });

        closeBtn.addEventListener('click', () => chatbotWindow.style.display = 'none');
        closeBottomBtn.addEventListener('click', () => chatbotWindow.style.display = 'none');

        backBtn.addEventListener('click', () => {
            if (confirm('현재 대화를 종료하고 카테고리 목록으로 돌아가시겠습니까?')) {
                clearChatSession();
                showCategoryView(true);
            }
        });

        sendBtn.addEventListener('click', sendMessage);

        inputField.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    // === 1. 카테고리 로드 (사용자용) ===
    async function loadCategories() {
        try {
            categoryButtons.innerHTML = `<div style="padding:10px;font-size:13px;">카테고리를 불러오는 중...</div>`;

            // ✅ FIX: 올바른 카테고리 API 경로
            const response = await apiFetch('/api/manual/categories', { method: 'GET' });

            if (response.status === 401 || response.status === 403) {
                categoryButtons.innerHTML = `
                    <div style="padding:10px;font-size:13px;line-height:1.5;">
                        카테고리를 불러올 수 없습니다.<br/>
                        로그인/권한을 확인해주세요. (HTTP ${response.status})
                    </div>`;
                console.error('[loadCategories] auth error:', response.status);
                return;
            }

            if (!response.ok) {
                categoryButtons.innerHTML = `
                    <div style="padding:10px;font-size:13px;line-height:1.5;">
                        카테고리 로드 실패 (HTTP ${response.status})
                    </div>`;
                console.error('[loadCategories] http error:', response.status);
                return;
            }

            const result = await response.json();
            console.log('[loadCategories] result:', result);

            const data = result?.data ?? [];
            categories = Array.isArray(data) ? data : [];

            renderCategoryButtons();
        } catch (error) {
            console.error('[loadCategories] Error:', error);
            categoryButtons.innerHTML = `
                <div style="padding:10px;font-size:13px;line-height:1.5;">
                    카테고리 로드 중 오류가 발생했습니다.<br/>콘솔을 확인해주세요.
                </div>`;
        }
    }

    function renderCategoryButtons() {
        categoryButtons.innerHTML = '';

        if (!categories || categories.length === 0) {
            categoryButtons.innerHTML = `<div style="padding:10px;font-size:13px;">표시할 카테고리가 없습니다.</div>`;
            return;
        }

        categories.forEach(category => {
            // snake_case / camelCase 방어
            const id = category.id ?? category.categoryId ?? category.category_id;
            const name = category.categoryName ?? category.category_name ?? category.name;
            const desc = category.description ?? category.desc ?? category.categoryDescription;

            if (id == null || !name) {
                console.warn('[renderCategoryButtons] invalid category:', category);
                return;
            }

            const button = document.createElement('button');
            button.className = 'chatbot-category-btn';
            button.innerHTML = `
                <span class="category-name">${escapeHtml(String(name))}</span>
                ${desc ? `<span class="category-examples">${escapeHtml(String(desc))}</span>` : ''}
            `;

            button.addEventListener('click', () => createNewConversation(Number(id), String(name)));
            categoryButtons.appendChild(button);
        });
    }

    // === 2. 대화방 생성 (카테고리 선택 시점) ===
    async function createNewConversation(categoryId, categoryName) {
        try {
            const response = await apiFetch('/api/auth/chatbot/conversations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ manualCategoryId: categoryId })
            });

            if (response.status === 401 || response.status === 403) {
                alert(`권한이 없거나 로그인 정보가 만료되었습니다. (HTTP ${response.status})`);
                return;
            }

            if (!response.ok) {
                throw new Error(`대화방 생성 실패 (HTTP ${response.status})`);
            }

            const result = await response.json();
            console.log('[createNewConversation] result:', result);

            const convId = result?.data?.conversationId;
            if (!convId) {
                throw new Error('conversationId가 응답에 없습니다.');
            }

            currentConversationId = String(convId);
            selectedCategoryName = categoryName;

            sessionStorage.setItem('activeChatId', currentConversationId);
            sessionStorage.setItem('activeCategoryName', selectedCategoryName);

            currentCategorySpan.textContent = selectedCategoryName;
            showChatView();
            messagesContainer.innerHTML = '';
            addBotMessage(`${selectedCategoryName} 관련 질문을 입력해주세요.`);
        } catch (error) {
            console.error('[createNewConversation] Error:', error);
            alert('대화 시작 중 오류가 발생했습니다.');
        }
    }

    // === 3. 대화 내역 복원 (기존 ID가 있을 때) ===
    async function restoreConversation(convId) {
        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${convId}/messages`, { method: 'GET' });

            if (response.status === 401 || response.status === 403) {
                console.warn('[restoreConversation] auth error:', response.status);
                clearChatSession();
                return;
            }

            if (!response.ok) {
                throw new Error(`대화 복원 실패 (HTTP ${response.status})`);
            }

            const result = await response.json();
            console.log('[restoreConversation] result:', result);

            const msgs = result?.data ?? [];
            if (!Array.isArray(msgs)) throw new Error('메시지 data가 배열이 아닙니다.');

            showChatView();
            currentCategorySpan.textContent = selectedCategoryName || '이전 대화';
            messagesContainer.innerHTML = '';

            msgs.forEach(msg => {
                if (msg.role === 'USER') addUserMessage(msg.content);
                else addBotMessage(msg.content);
            });

            scrollToBottom();
        } catch (error) {
            console.error('[restoreConversation] Error:', error);
            clearChatSession();
        }
    }

    // === 4. 메시지 전송 (대화방 기반) ===
    async function sendMessage() {
        const content = inputField.value.trim();
        if (!content || !currentConversationId) return;

        addUserMessage(content);
        inputField.value = '';
        sendBtn.disabled = true;

        const loadingId = addBotMessage('답변을 생성하고 있습니다...', true);

        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${currentConversationId}/messages`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: content })
            });

            removeMessage(loadingId);

            if (response.status === 401 || response.status === 403) {
                addBotMessage(`권한이 없거나 로그인 정보가 만료되었습니다. (HTTP ${response.status})`);
                return;
            }

            if (!response.ok) {
                throw new Error(`메시지 전송 실패 (HTTP ${response.status})`);
            }

            const result = await response.json();
            console.log('[sendMessage] result:', result);

            // ChatMessageResponseDto: data.assistant.content
            const answer = result?.data?.assistant?.content;
            if (answer) addBotMessage(answer);
            else addBotMessage('답변을 생성할 수 없습니다.');

        } catch (error) {
            console.error('[sendMessage] Error:', error);
            removeMessage(loadingId);
            addBotMessage('오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            sendBtn.disabled = false;
        }
    }

    // === UI 유틸리티 ===
    function showCategoryView(reload) {
        categoryView.style.display = 'block';
        chatView.style.display = 'none';
        if (reload) loadCategories();
    }

    function showChatView() {
        categoryView.style.display = 'none';
        chatView.style.display = 'flex';
        inputField.focus();
    }

    function clearChatSession() {
        sessionStorage.removeItem('activeChatId');
        sessionStorage.removeItem('activeCategoryName');
        currentConversationId = null;
        selectedCategoryName = null;
        currentCategorySpan.textContent = '카테고리';
        messagesContainer.innerHTML = '';
    }

    function addUserMessage(text) {
        const msgEl = createMessageElement('user', text);
        messagesContainer.appendChild(msgEl);
        scrollToBottom();
    }

    function addBotMessage(text, isLoading = false) {
        const id = 'bot-' + Date.now();
        const msgEl = createMessageElement('bot', text, id, isLoading);
        messagesContainer.appendChild(msgEl);
        scrollToBottom();
        return id;
    }

    function createMessageElement(type, text, id, isLoading = false) {
        const div = document.createElement('div');
        div.className = `chatbot-message ${type}`;
        if (id) div.id = id;

        const bubble = document.createElement('div');
        bubble.className = 'chatbot-message-bubble';
        bubble.textContent = text;
        if (isLoading) bubble.style.fontStyle = 'italic';

        const time = document.createElement('div');
        time.className = 'chatbot-message-time';
        const now = new Date();
        time.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

        div.appendChild(bubble);
        div.appendChild(time);
        return div;
    }

    function removeMessage(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // ✅ 공통 fetch: 세션스토리지 토큰 + 쿠키(세션) 포함
    async function apiFetch(url, options = {}) {
        const token = sessionStorage.getItem('accessToken');
        const headers = { ...(options.headers || {}) };
        if (token && !headers['Authorization']) headers['Authorization'] = `Bearer ${token}`;

        return fetch(url, {
            ...options,
            headers,
            credentials: 'include'
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, m => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[m]));
    }
});
