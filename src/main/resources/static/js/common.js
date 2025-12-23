/**
 * common.js
 * 인증 + 관리자 사이드바 제어 (초기 하이라이트 제거 버전)
 */

let isRefreshing = false;
let refreshSubscribers = [];

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
    return true;
}

function handleSessionExpired() {
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
    try {
        const res = await apiFetch('/api/auth/me');
        if (!res.ok) throw new Error('AUTH_FAILED');
        const result = await res.json();
        const me = result.data;

        const userNameEl = document.getElementById('userName');
        if (userNameEl) userNameEl.innerText = `${me.name} (${me.role})`;

        const adminMenuLink = document.getElementById('adminMenuLink');
        if (adminMenuLink) {
            adminMenuLink.style.display = (me.role === 'ADMIN' || me.role === 'COMPANY_ADMIN') ? '' : 'none';
        }
    } catch (e) {
        location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadMe();
    initAdminSidebar(); // ✅ 페이지 로드 시 실행
});