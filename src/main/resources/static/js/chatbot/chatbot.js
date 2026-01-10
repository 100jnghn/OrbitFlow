/**
 * Orbitflow Chatbot JS (RAG & 대화 저장 지원 및 드래그 이동 기능 추가 버전)
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
    let currentConversationId = sessionStorage.getItem('activeChatId');
    let selectedCategoryName = sessionStorage.getItem('activeCategoryName');
    let categories = [];

    // 🚀 드래그 관련 변수
    let isDragging = false;
    let dragStartX, dragStartY;
    let initialIconX, initialIconY;
    const DRAG_THRESHOLD = 5; // 드래그로 판단할 최소 이동 거리(px)

    init();

    function init() {
        setupEventListeners();
        loadCategories();
        if (currentConversationId) {
            restoreConversation(currentConversationId);
        }
    }

    // === 이벤트 리스너 ===
    function setupEventListeners() {

        // 🚀 아이콘 드래그 및 클릭 핸들러
        floatIcon.addEventListener('mousedown', startDragging);

        // 모바일 터치 대응
        floatIcon.addEventListener('touchstart', (e) => {
            const touch = e.touches[0];
            startDragging({
                clientX: touch.clientX,
                clientY: touch.clientY,
                preventDefault: () => {}
            });
        }, { passive: false });

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

    // 🚀 드래그 시작 함수
    function startDragging(e) {
        isDragging = false;
        dragStartX = e.clientX;
        dragStartY = e.clientY;

        const rect = floatIcon.getBoundingClientRect();
        initialIconX = rect.left;
        initialIconY = rect.top;

        floatIcon.style.transition = 'none'; // 드래그 중 애니메이션 끄기
        floatIcon.style.cursor = 'grabbing';

        const moveHandler = (moveEvent) => {
            const clientX = moveEvent.touches ? moveEvent.touches[0].clientX : moveEvent.clientX;
            const clientY = moveEvent.touches ? moveEvent.touches[0].clientY : moveEvent.clientY;

            const dx = clientX - dragStartX;
            const dy = clientY - dragStartY;

            // 이동 거리가 임계값보다 크면 드래그로 판정
            if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                isDragging = true;
            }

            if (isDragging) {
                // 화면 경계 제한
                let nextX = initialIconX + dx;
                let nextY = initialIconY + dy;

                nextX = Math.max(0, Math.min(window.innerWidth - rect.width, nextX));
                nextY = Math.max(0, Math.min(window.innerHeight - rect.height, nextY));

                floatIcon.style.left = nextX + 'px';
                floatIcon.style.top = nextY + 'px';
                floatIcon.style.right = 'auto';
                floatIcon.style.bottom = 'auto';
            }
        };

        const stopHandler = () => {
            document.removeEventListener('mousemove', moveHandler);
            document.removeEventListener('mouseup', stopHandler);
            document.removeEventListener('touchmove', moveHandler);
            document.removeEventListener('touchend', stopHandler);

            floatIcon.style.transition = 'all 0.3s ease'; // 애니메이션 복구
            floatIcon.style.cursor = 'pointer';

            // 🚀 드래그가 아니었을 때만 챗봇 창 열기
            if (!isDragging) {
                openChatbot();
            }
        };

        document.addEventListener('mousemove', moveHandler);
        document.addEventListener('mouseup', stopHandler);
        document.addEventListener('touchmove', moveHandler, { passive: false });
        document.addEventListener('touchend', stopHandler);
    }

    // 🚀 챗봇 창 열기 함수 분리
    function openChatbot() {
        chatbotWindow.style.display = 'flex';
        loadCategories();
        if (!currentConversationId) showCategoryView(false);
        else showChatView();
    }

    // === 이하 기존 API 및 UI 로직 (동일) ===
    async function loadCategories() {
        try {
            categoryButtons.innerHTML = `<div style="padding:10px;font-size:13px;">카테고리를 불러오는 중...</div>`;
            const response = await apiFetch('/api/manual/categories', { method: 'GET' });

            if (!response.ok) {
                categoryButtons.innerHTML = `<div style="padding:10px;font-size:13px;">카테고리 로드 실패</div>`;
                return;
            }

            const result = await response.json();
            const data = result?.data ?? [];
            categories = Array.isArray(data) ? data : [];
            renderCategoryButtons();
        } catch (error) {
            console.error('[loadCategories] Error:', error);
        }
    }

    function renderCategoryButtons() {
        categoryButtons.innerHTML = '';
        if (!categories || categories.length === 0) {
            categoryButtons.innerHTML = `<div style="padding:10px;font-size:13px;">표시할 카테고리가 없습니다.</div>`;
            return;
        }

        categories.forEach(category => {
            const id = category.id ?? category.categoryId ?? category.category_id;
            const name = category.categoryName ?? category.category_name ?? category.name;
            const desc = category.description ?? category.desc ?? category.categoryDescription;

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

    async function createNewConversation(categoryId, categoryName) {
        try {
            const response = await apiFetch('/api/auth/chatbot/conversations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ manualCategoryId: categoryId })
            });

            if (!response.ok) throw new Error('대화방 생성 실패');

            const result = await response.json();
            const convId = result?.data?.conversationId;

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
        }
    }

    async function restoreConversation(convId) {
        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${convId}/messages`, { method: 'GET' });
            if (!response.ok) throw new Error('대화 복원 실패');

            const result = await response.json();
            const msgs = result?.data ?? [];

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
    /**
     * 🚀 실시간 글자 출력(타이핑) 효과 함수
     * @param {HTMLElement} element - 글자가 표시될 요소
     * @param {string} text - 전체 답변 텍스트
     * @param {number} speed - 출력 속도 (ms)
     */
    function typeWriter(element, text, speed = 25) {
        let i = 0;
        element.textContent = ""; // 초기화

        function type() {
            if (i < text.length) {
                element.textContent += text.charAt(i);
                i++;
                scrollToBottom(); // 글자가 추가될 때마다 스크롤 아래로
                setTimeout(type, speed);
            }
        }
        type();
    }


    async function sendMessage() {
        const content = inputField.value.trim();
        if (!content || !currentConversationId) return;

        addUserMessage(content);
        inputField.value = '';
        sendBtn.disabled = true;

        // "답변 생성 중" 로딩 표시
        const loadingId = addBotMessage('답변을 생성하고 있습니다...', true);

        try {
            const response = await apiFetch(`/api/auth/chatbot/conversations/${currentConversationId}/messages`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: content })
            });

            removeMessage(loadingId);
            if (!response.ok) throw new Error('전송 실패');

            const result = await response.json();
            const answer = result?.data?.assistant?.content;

            if (answer) {
                // 🚀 실시간 타이핑 효과를 위한 메시지 생성
                const botMsgId = 'bot-' + Date.now();
                const msgEl = createMessageElement('bot', '', botMsgId); // 빈 텍스트로 생성
                messagesContainer.appendChild(msgEl);

                const bubbleEl = msgEl.querySelector('.chatbot-message-bubble');
                typeWriter(bubbleEl, answer); // 타이핑 효과 시작
            } else {
                addBotMessage('답변을 생성할 수 없습니다.');
            }

        } catch (error) {
            removeMessage(loadingId);
            addBotMessage('오류가 발생했습니다.');
        } finally {
            sendBtn.disabled = false;
        }
    }

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

