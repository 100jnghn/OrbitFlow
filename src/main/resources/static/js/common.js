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


/* =========================
   API Fetch + Token Refresh (기존 로직 유지)
========================= */
async function apiFetch(url, options = {}) {
    const accessToken = sessionStorage.getItem('accessToken');
    options.headers = {
        ...(options.headers || {}),
        ...(accessToken && { 'Authorization': `Bearer ${accessToken}` })
    };

    let res = await fetch(url, options);
    if (res.status !== 401) return res;

    if (isRefreshing) {
        return new Promise((resolve, reject) => {
            refreshSubscribers.push(async () => {
                options.headers.Authorization = `Bearer ${sessionStorage.getItem('accessToken')}`;
                try { resolve(await fetch(url, options)); } catch (e) { reject(e); }
            });
        });
    }

    isRefreshing = true;
    const refreshed = await refreshAccessToken();
    isRefreshing = false;

    if (!refreshed) {
        sessionExpired = true;
        refreshSubscribers = [];
        handleSessionExpired();
        throw new Error('SESSION_EXPIRED');
    }

    refreshSubscribers.forEach(cb => cb());
    refreshSubscribers = [];
    options.headers.Authorization = `Bearer ${sessionStorage.getItem('accessToken')}`;
    return fetch(url, options);
}

async function refreshAccessToken() {
    const res = await fetch('/api/auth/refresh', { method: 'POST', credentials: 'include' });
    if (!res.ok) return false;
    const result = await res.json();
    sessionStorage.setItem('accessToken', result.data.accessToken);
    saveSessionExpiry(result.data.refreshExpiresAt);
    scheduleSessionWarning();

    return true;
}

function showSessionExtendModal() {
    if (isExtendModalOpen) return;

    const modal = document.getElementById('extendSessionModal');
    if (!modal) return;

    isExtendModalOpen = true;
    if (modal) modal.style.display = 'flex';

    startSessionCountdown();
}

function closeExtendSessionModal() {
    const modal = document.getElementById('extendSessionModal');
    if (modal) modal.style.display = 'none';

    isExtendModalOpen = false;
    clearInterval(countdownTimer);
}


function handleSessionExpired() {
    if (sessionExpired) return; // 중복 방지
    sessionExpired = true;

    sessionStorage.clear();

    const modal = document.getElementById('sessionModal');
    if (modal) modal.style.display = 'block';
}

function confirmSessionExpired() { location.href = '/login'; }

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

        closeExtendSessionModal();
        scheduleSessionWarning();
    } catch (e) {
        handleSessionExpired();
    }
}


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

        const userNameEl = document.getElementById('userName');
        if (userNameEl) userNameEl.innerText = `${me.name} (${me.role})`;


        const adminMenuLink = document.getElementById('adminMenuLink');
        if (adminMenuLink) {
            adminMenuLink.style.display =
                (me.role === 'ADMIN' || me.role === 'COMPANY_ADMIN')
                    ? 'inline-flex'
                    : 'none';
        }

        scheduleSessionWarning(); // 여기서만 호출

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
});

function saveSessionExpiry(refreshExpiresAt) {
    sessionStorage.setItem(
        'refreshExpiresAt',
        new Date(refreshExpiresAt).getTime()
    );
}


/** 세션 만료 경고 타이머 추가 **/
function scheduleSessionWarning() {
    const expiresAt = Number(sessionStorage.getItem('refreshExpiresAt'));
    if (!expiresAt) return;

    const now = Date.now();

    // 이미 만료된 세션이면 경고 띄우지 말고 종료
    if (expiresAt <= now) {
        return;
    }

    if (sessionStorage.getItem('sessionWarningShown') === 'true') {
        return;
    }

    const WARNING_BEFORE_MS = 10 * 1000;
    const delay = expiresAt - now - WARNING_BEFORE_MS;

    clearTimeout(sessionWarningTimer);

    if (delay <= 0) {
        showSessionExtendModalOnce();
        return;
    }

    sessionWarningTimer = setTimeout(showSessionExtendModalOnce, delay);

}

/** 카운트다운 시작 함수 **/
function startSessionCountdown() {
    const countdownEl = document.getElementById('sessionCountdown');
    if (!countdownEl) return;

    clearInterval(countdownTimer);

    countdownTimer = setInterval(() => {
        const expiresAt = Number(sessionStorage.getItem('refreshExpiresAt'));
        if (!expiresAt) return;

        const remainingMs = expiresAt - Date.now();

        if (remainingMs <= 0) {
            clearInterval(countdownTimer);
            handleSessionExpired();
            return;
        }

        const sec = Math.floor(remainingMs / 1000);
        const mm = String(Math.floor(sec / 60)).padStart(2, '0');
        const ss = String(sec % 60).padStart(2, '0');

        countdownEl.textContent = `${mm}:${ss}`;
    }, 1000);
}

function showSessionExtendModalOnce() {
    if (isExtendModalOpen) return;

    sessionStorage.setItem('sessionWarningShown', 'true');
    showSessionExtendModal();
}
