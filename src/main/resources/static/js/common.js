/**
 * common.js
 * 인증 + 관리자 사이드바 제어 (초기 하이라이트 제거 버전)
 */

let isRefreshing = false;
let refreshSubscribers = [];
let sessionWarningTimer = null;
let countdownTimer = null;
let isExtendModalOpen = false;
let sessionExpired = false;
const SESSION_WARNING_SHOWN_KEY = 'sessionWarningShown';
let sessionExpiredReason = null;
let sessionCountdownInterval = null;
let isSessionExtendPromptOpen = false;

/* =========================
   API Fetch + Token Refresh (기존 로직 유지)
========================= */
async function apiFetch(url, options = {}) {

    // 세션 연장 모달 떠 있으면 모든 API 중단
    if (isSessionExtendPromptOpen) {
        throw new Error('SESSION_EXTEND_PENDING');
    }

    const accessToken = sessionStorage.getItem('accessToken');

    // 토큰 없으면 즉시 로그인
    if (!accessToken) {
        location.href = '/login';
        throw new Error('NO_TOKEN');
    }

    const headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${accessToken}`
    };

    const fetchOptions = {
        ...options,
        headers,
        credentials: 'include' // refresh 쿠키 필수
    };

    let res = await fetch(url, fetchOptions);
    if (res.status !== 401) return res;

    /* ===== refresh 중이면 대기 ===== */
    if (isRefreshing) {
        return new Promise((resolve, reject) => {
            refreshSubscribers.push(async () => {
                try {
                    resolve(
                        await fetch(url, {
                            ...fetchOptions,
                            headers: {
                                ...headers,
                                Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                            }
                        })
                    );
                } catch (e) {
                    reject(e);
                }
            });
        });
    }

    /* ===== refresh 시작 ===== */
    isRefreshing = true;
    const refreshed = await refreshAccessToken();
    isRefreshing = false;

    if (!refreshed) {

        // 연장 모달 떠 있는 동안엔 종료시키지 않음
        if (isSessionExtendPromptOpen) {
            throw new Error('REFRESH_FAILED_DURING_EXTEND');
        }

        sessionExpired = true;
        refreshSubscribers = [];
        handleSessionExpired();
        throw new Error('SESSION_EXPIRED');
    }

    /* ===== 대기 중이던 요청 재시도 ===== */
    refreshSubscribers.forEach(cb => cb());
    refreshSubscribers = [];

    /* ===== 현재 요청 재시도 ===== */
    return fetch(url, {
        ...fetchOptions,
        headers: {
            ...headers,
            Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
        }
    });
}

async function refreshAccessToken() {
    const res = await fetch('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include'
    });
    if (!res.ok) return false;

    const result = await res.json();
    sessionStorage.setItem('accessToken', result.data.accessToken);
    saveSessionExpiry(result.data.refreshExpiresAt);
    scheduleSessionWarning();

    return true;
}

function showSessionExtendModal() {
    if (isExtendModalOpen) return;
    isExtendModalOpen = true;
    isSessionExtendPromptOpen = true;

    Swal.fire({
        title: '세션 만료 예정',
        html: `
            <div style="font-size:14px; color:#6b7280; margin-bottom:12px;">
                보안을 위해 세션이 곧 만료됩니다.<br>
                계속 사용하시겠습니까?
            </div>
            <div id="swalSessionCountdown"
                 style="font-size:26px; font-weight:700; color:#f59e0b;">
                00:00
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '세션 연장',
        cancelButtonText: '로그아웃',
        allowOutsideClick: false,
        allowEscapeKey: false,
        focusConfirm: false,
        didOpen: () => {
            startSwalSessionCountdown();
        },
        willClose: () => {
            stopSwalSessionCountdown();
            isExtendModalOpen = false;
            isSessionExtendPromptOpen = false;
        }
    }).then((result) => {
        if (result.isConfirmed) {
            extendSession();
        } else {
            confirmSessionExpired();
        }
    });
}

function startSwalSessionCountdown() {
    stopSwalSessionCountdown();

    sessionCountdownInterval = setInterval(() => {
        const expiresAt = Number(sessionStorage.getItem('refreshExpiresAt'));
        if (!expiresAt) return;

        const remainingMs = expiresAt - Date.now();

        if (remainingMs <= 0) {
            stopSwalSessionCountdown();
            Swal.close();
            handleSessionExpired();
            return;
        }

        const sec = Math.floor(remainingMs / 1000);
        const mm = String(Math.floor(sec / 60)).padStart(2, '0');
        const ss = String(sec % 60).padStart(2, '0');

        const el = document.getElementById('swalSessionCountdown');
        if (!el) return;

        el.textContent = `${mm}:${ss}`;

        // 1분 미만 색상 강조
        if (sec <= 60) {
            const ratio = sec / 60; // 1 → 0
            const red = 239;
            const green = Math.floor(68 + 120 * ratio); // 주황 → 빨강
            el.style.color = `rgb(${red}, ${green}, 68)`;
        } else {
            el.style.color = '#f59e0b'; // 기본 경고 색
        }
    }, 1000);
}

function stopSwalSessionCountdown() {
    if (sessionCountdownInterval) {
        clearInterval(sessionCountdownInterval);
        sessionCountdownInterval = null;
    }
}



function handleSessionExpired() {
    if (sessionExpired) return;
    sessionExpired = true;

    sessionStorage.clear();
    stopSwalSessionCountdown();
    Swal.close();

    let message = '세션이 만료되었습니다.\n다시 로그인해 주세요.';

    if (sessionExpiredReason === 'OTHER_EMPLOYEE_LOGIN') {
        message =
            '다른 계정으로 로그인되어\n현재 세션이 종료되었습니다.\n보안을 위해 다시 로그인해 주세요.';
    }

    Swal.fire({
        title: '세션 종료',
        text: message,
        icon: 'info',
        confirmButtonText: '로그인',
        allowOutsideClick: false,
        allowEscapeKey: false
    }).then(() => {
        location.href = '/login';
    });
}

async function confirmSessionExpired() {
    try {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (e) {
        // 무시
    }
    sessionStorage.clear();
    location.href = '/login';
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    sessionStorage.clear();
    location.href = '/login';
}

async function extendSession() {
    try {
        const res = await fetch('/api/auth/extend-session', {
            method: 'POST',
            credentials: 'include'
        });

        if (!res.ok) throw new Error('EXTEND_FAILED');

        const result = await res.json();

        sessionStorage.setItem('accessToken', result.data.accessToken);
        saveSessionExpiry(result.data.refreshExpiresAt);

        sessionStorage.removeItem('sessionWarningShown');

        // 상태 정리
        isSessionExtendPromptOpen = false;
        isExtendModalOpen = false;

        Swal.close();
        scheduleSessionWarning();

    } catch (e) {
        handleSessionExpired();
    }
}

/* =========================
   메시지 카운트 업데이트
========================= */
async function updateMessageCount() {
    if (isSessionExtendPromptOpen) return;
    try {
        const response = await apiFetch('/api/messages/unread/count');
        if (!response.ok) {
            // 에러 발생 시 배지 숨김
            const badge = document.getElementById('messageBadge');
            if (badge) badge.style.display = 'none';
            return;
        }

        const result = await response.json();
        const count = result.data || 0;
        const badge = document.getElementById('messageBadge');

        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count.toString();
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (e) {
        // 에러 발생 시 배지 숨김
        const badge = document.getElementById('messageBadge');
        if (badge) badge.style.display = 'none';
    }
}

// 페이지 로드 시 메시지 카운트 업데이트
document.addEventListener('DOMContentLoaded', function () {
    updateMessageCount();
    // 30초마다 메시지 카운트 업데이트
    setInterval(updateMessageCount, 30000);
});


/* =========================
   관리자 사이드바 제어 핵심
========================= */

/**
 * 관리자 사이드바 상태 초기화
 * - 처음 화면 접속 시 아무 메뉴도 선택되지 않게 처리
 */
function initAdminSidebar() {
    const sidebar = document.querySelector('.sidebar-menu');
    if (!sidebar) return;

    const currentPath = window.location.pathname;

    // ✅ [중요] 초기 화면 경로 등록 (이 경로들에서는 하이라이트가 나타나지 않음)
    const homePaths = ['/admin', '/admin/home', '/view/admin/main', '/admin/main'];

    // 메인 경로인 경우 모든 active/selected 제거 후 종료
    if (homePaths.includes(currentPath)) {
        sidebar.querySelectorAll('.menu-item').forEach(item => {
            item.classList.remove('active');
            item.querySelectorAll('li').forEach(li => li.classList.remove('selected'));
        });
        return;
    }

    // 상세 페이지일 경우 현재 URL과 일치하는 메뉴 활성화
    const activeLink = sidebar.querySelector(`a[href="${currentPath}"]`);
    if (activeLink) {
        const subLi = activeLink.closest('li');
        if (subLi && !subLi.classList.contains('menu-item')) {
            subLi.classList.add('selected');
        }

        const parentMenu = activeLink.closest('.menu-item');
        if (parentMenu) {
            parentMenu.classList.add('active');
            const title = parentMenu.querySelector('.menu-title');
            if (title) title.setAttribute('aria-expanded', 'true');
        }
    }
}

/**
 * 대분류 토글 함수
 */
function toggleAdminMenu(element) {
    const menuItem = element.closest('.menu-item');
    if (!menuItem || menuItem.classList.contains('no-sub')) return;

    const isOpen = menuItem.classList.contains('active');

    // 다른 메뉴 닫기
    document.querySelectorAll('.sidebar-menu .menu-item').forEach(item => {
        if (item !== menuItem) {
            item.classList.remove('active');
            const title = item.querySelector('.menu-title');
            if (title) title.setAttribute('aria-expanded', 'false');
        }
    });

    // 현재 메뉴 토글
    if (isOpen) {
        menuItem.classList.remove('active');
        element.setAttribute('aria-expanded', 'false');
    } else {
        menuItem.classList.add('active');
        element.setAttribute('aria-expanded', 'true');
    }
}

/* =========================
   사용자 정보 로드 및 초기화 실행
========================= */
async function loadMe() {


    window.addEventListener('storage', (e) => {
        if (e.key !== 'currentEmployeeId') return;

        const newEmployeeId = e.newValue;
        const myEmployeeId = sessionStorage.getItem('employeeId');

        if (!myEmployeeId || !newEmployeeId) return;

        // 다른 사원으로 로그인됨
        if (String(newEmployeeId) !== String(myEmployeeId)) {
            sessionExpiredReason = 'OTHER_EMPLOYEE_LOGIN';
            handleSessionExpired();
        }
    });


    const token = sessionStorage.getItem('accessToken');
    if (!token) {
        location.href = '/login';
        return;
    }

    try {
        const res = await apiFetch('/api/auth/me');
        if (!res.ok) throw new Error();

        const result = await res.json();
        const me = result.data;

        // 회사명 세팅
        const companyNameEl = document.getElementById('companyName');
        if (companyNameEl) {
            companyNameEl.innerText = me.companyName;
        }

        const userNameEl = document.getElementById('userName');
        if (userNameEl) {
            userNameEl.innerText = `${me.name} (${me.role})`;

            // 사원 상세 페이지로 이동
            userNameEl.href = `/view/organizations?employeeId=${me.employeeId}`;

            // http://localhost:8090/view/organizations?employeeId=?
        }


        const adminMenuLink = document.getElementById('adminMenuLink');
        if (adminMenuLink) {
            adminMenuLink.style.display =
                (me.role === 'ADMIN' || me.role === 'COMPANY_ADMIN')
                    ? 'inline-flex'
                    : 'none';
        }

        scheduleSessionWarning(); // 여기서만 호출

        // 현재 탭의 사원 ID 저장
        sessionStorage.setItem('employeeId', me.employeeId);

    } catch (e) {
        location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;

    // 로그인 페이지에서는 인증 체크 X
    if (path === '/login') return;

    loadMe();
    initAdminSidebar(); // 페이지 로드 시 실행
    initSidebarToggle(); // 사이드바 토글 초기화
});

/**
 * 사이드바 토글 기능 초기화
 */
function initSidebarToggle() {
    const toggleBtn = document.getElementById('sidebar-toggle');
    if (!toggleBtn) {
        console.warn('사이드바 토글 버튼을 찾을 수 없습니다.');
        return;
    }

    // 로컬 스토리지에서 상태 읽기
    const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    if (isCollapsed) {
        document.documentElement.classList.add('sidebar-collapsed');
    }

    toggleBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        const collapsed = document.documentElement.classList.toggle('sidebar-collapsed');
        localStorage.setItem('sidebarCollapsed', collapsed);
        console.log('사이드바 상태 변경:', collapsed ? '접힘' : '펼침');
    });

    console.log('사이드바 토글 초기화 완료');
}

function saveSessionExpiry(refreshExpiresAt) {
    sessionStorage.setItem(
        'refreshExpiresAt',
        new Date(refreshExpiresAt).getTime()
    );
}


/** 세션 만료 경고 타이머 추가 **/
function scheduleSessionWarning() {
    clearTimeout(sessionWarningTimer);

    const expiresAt = Number(sessionStorage.getItem('refreshExpiresAt'));
    if (!expiresAt) return;

    const now = Date.now();
    const WARNING_BEFORE_MS = 1 * 60 * 1000; // 30분 전
    const delay = expiresAt - now - WARNING_BEFORE_MS;

    if (delay <= 0) {
        showSessionExtendModal();
        return;
    }

    sessionWarningTimer = setTimeout(() => {
        showSessionExtendModal();
    }, delay);
}


function showSessionExtendModalOnce() {
    if (isExtendModalOpen) return;

    sessionStorage.setItem('sessionWarningShown', 'true');
    showSessionExtendModal();
}

/**
 * 알림 관련 코드
 */

// sse 연결 확인 변수
let eventSource = null;

// sse 연결 시도 & 안 읽은 알림 수 불러오기
document.addEventListener("DOMContentLoaded", () => {

    // 로그인 화면에서는 알림 관련 메소드 호출 X
    if (window.location.pathname === "/login") return;

    connectSse();
    refreshUnreadCount(); // 초기 상태 동기화
});

// sse 연결 함수
function connectSse() {
    if (eventSource) return;

    // access token 사용
    const accessToken = sessionStorage.getItem("accessToken");

    if (!accessToken) {
        console.warn("SSE 연결 중단: access token 없음");
        return;
    }

    // console.log("EventSourcePolyFill : " + window.EventSourcePolyfill);

    // access token 사용해서 sse 연결 요청
    eventSource = new EventSourcePolyfill(
        "/api/notifications/stream",
        {
            headers: {
                Authorization: `Bearer ${accessToken}`
            },
            withCredentials: true, // refresh token 쿠키 필요 시
        }
    );

    eventSource.addEventListener("notification", (event) => {
        const dto = JSON.parse(event.data);
        showToast(dto);
        refreshUnreadCount();
    });

    eventSource.onopen = () => {
        console.log("SSE 연결 성공");
    };

    eventSource.onerror = () => {
        eventSource.close();
        eventSource = null;

        // 세션 기준으로 판단 (SSE 전용)
        setTimeout(connectSse, 5000);
    };
}

// 알림 토스트 메시지 표시 함수
function showToast(dto) {
    const container = document.getElementById("notification-toast-container");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = "notification-toast";

    // 왼쪽 아이콘 영역
    const iconArea = document.createElement("div");
    iconArea.className = "notification-toast-icon";
    iconArea.innerHTML = '<i class="fas fa-bell"></i>';

    // 오른쪽 컨텐츠 영역
    const contentArea = document.createElement("div");
    contentArea.className = "notification-toast-content";

    // 텍스트
    const text = document.createElement("div");
    text.className = "notification-toast-text";
    text.textContent = '새로운 ' + dto.type + ' 알림';

    // 확인 버튼
    const checkBtn = document.createElement("button");
    checkBtn.className = "notification-toast-check-btn";
    checkBtn.title = "확인";
    checkBtn.innerHTML = '<i class="fas fa-check"></i>';
    checkBtn.onclick = (e) => {
        e.stopPropagation();
        if (dto.notificationId) {
            markAsRead(dto.notificationId);
        }
        toast.remove();
    };

    contentArea.appendChild(text);
    contentArea.appendChild(checkBtn);

    toast.appendChild(iconArea);
    toast.appendChild(contentArea);

    // 🔥 핵심 수정 부분
    toast.addEventListener('click', (e) => {
        if (e.target.closest('.notification-toast-check-btn')) return;

        e.stopPropagation(); // ⭐ document 클릭 방지

        // 1️⃣ 드롭다운 먼저 열기
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown) {
            dropdown.classList.remove('hidden');
            switchTab?.('unread');
        }

        // 2️⃣ 토스트 즉시 제거 (이벤트 루프 뒤)
        setTimeout(() => {
            toast.remove();
        }, 0);
    });

    container.appendChild(toast);

    // 자동 제거 함수
    const removeToast = () => {
        if (toast.parentNode) {
            toast.style.animation = 'slideOutToast 0.3s ease-out';
            setTimeout(() => toast.remove(), 300);
        }
    };

    let removeTimer = setTimeout(removeToast, 5000);

    toast.addEventListener('mouseenter', () => clearTimeout(removeTimer));
    toast.addEventListener('mouseleave', () => {
        removeTimer = setTimeout(removeToast, 5000);
    });
}


// 토스트 슬라이드 아웃 애니메이션 및 SweetAlert2 z-index 수정
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOutToast {
        from {
            opacity: 1;
            transform: translateY(0);
        }
        to {
            opacity: 0;
            transform: translateY(-10px);
        }
    }
    .swal2-container {
        z-index: 100000 !important;
    }
`;
document.head.appendChild(style);

// 읽지 않은 메시지 수 불러오는 함수
async function refreshUnreadCount() {
    if (isSessionExtendPromptOpen) return;
    try {
        const res = await apiFetch("/api/notifications/unread");
        if (!res.ok) return;

        const result = await res.json();
        const list = result.data;
        const badge = document.getElementById("notificationBadge");

        if (!badge) return;

        if (list.length > 0) {

            console.log("안 읽은 메시지 수 : " + list.length);

            badge.innerText = list.length >= 10 ? '9+' : list.length.toString();
            badge.classList.remove("hidden");
            badge.style.display = "flex";
        } else {
            badge.classList.add("hidden");
        }
    } catch (e) {
        console.error("읽지 않은 알림 불러오기 실패", e);
    }
}

window.addEventListener("beforeunload", () => {
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
});


// ---------- sweet alert 공통 래퍼 ---------- //
window.sweetSuccess = function (message, type = 'success') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetError = function (message, type = 'error') {
    if (message && message.includes('\n')) {
        return Swal.fire({
            html: message.replace(/\n/g, '<br>'),
            icon: type,
            confirmButtonText: '확인'
        });
    }
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetWarning = function (message, type = 'warning') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetConfirm = function (title, message) {
    return Swal.fire({
        title: title,
        text: message,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '확인',
        cancelButtonText: '취소'
    });
};

window.sweetInfo = function (message, type = 'info') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetQuestion = function (message, type = 'question') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};