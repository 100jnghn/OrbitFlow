/**
 * common.js
 * 인증이 필요한 모든 API 호출은 apiFetch 사용
 * - Access Token 자동 재발급
 * - Refresh Token 만료 시 세션 종료
 */

let isRefreshing = false;
let refreshSubscribers = [];

/** apiFetch() 함수 */
async function apiFetch(url, options = {}) {
    const accessToken = sessionStorage.getItem('accessToken');

    options.headers = {
        ...(options.headers || {}),
        ...(accessToken && {'Authorization': `Bearer ${accessToken}`})
    };

    let res = await fetch(url, options);

    if (res.status !== 401) {
        return res;
    }

    // refresh 로직
    if (isRefreshing) {
        return new Promise((resolve, reject) => {
            refreshSubscribers.push(async () => {
                options.headers.Authorization =
                    `Bearer ${sessionStorage.getItem('accessToken')}`;
                try {
                    resolve(await fetch(url, options));
                } catch (e) {
                    reject(e);
                }
            });
        });
    }

    isRefreshing = true;
    const refreshed = await refreshAccessToken();
    isRefreshing = false;

    if (!refreshed) {
        refreshSubscribers = []; // 대기 큐 정리
        handleSessionExpired();
        throw new Error('SESSION_EXPIRED');
    }

    // 대기 중이던 요청 재개
    refreshSubscribers.forEach(cb => cb());
    refreshSubscribers = [];

    options.headers.Authorization =
        `Bearer ${sessionStorage.getItem('accessToken')}`;

    return fetch(url, options);
}

/** Refresh 호출 함수 */
/** Access Token 재발급 (Refresh Token은 서버 DB에서 처리) */
async function refreshAccessToken() {
    const res = await fetch('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include'
    });

    if (!res.ok) return false;

    const result = await res.json();
    sessionStorage.setItem('accessToken', result.data.accessToken);
    return true;
}

/** 세션 만료 처리 함수 */
function handleSessionExpired() {
    sessionStorage.clear();
    document.getElementById('sessionModal').style.display = 'block';
}

function confirmSessionExpired() {
    location.href = '/login';
}

/** 로그아웃 함수 */
async function logout() {
    await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
    });

    sessionStorage.clear();
    location.href = '/login';
}

/** 헤더 사용자 정보 표시 */
async function loadMe() {
    try {
        const res = await apiFetch('/api/auth/me');

        if (!res.ok) {
            throw new Error('AUTH_FAILED');
        }

        const result = await res.json();
        const me = result.data;

        const userNameEl = document.getElementById('userName');
        if (userNameEl) {
            userNameEl.innerText = `${me.name} (${me.role})`;
        }

        // 관리자 메뉴 표시/숨김 처리
        const adminMenuLink = document.getElementById('adminMenuLink');
        if (adminMenuLink) {
            if (me.role === 'ADMIN' || me.role === 'COMPANY_ADMIN') {
                adminMenuLink.style.display = '';

                const currentPath = window.location.pathname;
                if (currentPath.startsWith('/view/admin')) {
                    adminMenuLink.classList.add('selected');
                } else {
                    adminMenuLink.classList.remove('selected');
                }
            } else {
                adminMenuLink.style.display = 'none';
            }
        }

        // 홈 링크 처리
        const homeLink = document.getElementById('homeLink');
        if (homeLink) {
            if (me.role === 'ADMIN' || me.role === 'COMPANY_ADMIN') {
                homeLink.href = '/view/admin';
            } else {
                homeLink.href = '/view/home';
            }
        }

    } catch (e) {
        location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', loadMe);

/** 관리자 사이드바 토글 함수 통합 */
function toggleAdminMenu(element) {
    const menuItem = element.closest('.menu-item');
    if (!menuItem) return;

    document.querySelectorAll('.sidebar-menu .menu-item').forEach(item => {
        if (item !== menuItem) item.classList.remove('active');
    });

    menuItem.classList.toggle('active');
}