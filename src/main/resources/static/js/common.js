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

    // 이미 refresh 중이면 대기
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
async function refreshAccessToken() {
    const refreshToken = sessionStorage.getItem('refreshToken');
    if (!refreshToken) return false;

    const res = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
            'Refresh-Token': refreshToken
        }
    });

    if (!res.ok) return false;

    const data = await res.json();
    sessionStorage.setItem('accessToken', data.accessToken);
    sessionStorage.setItem('refreshToken', data.refreshToken);

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
    const refreshToken = sessionStorage.getItem('refreshToken');

    if (refreshToken) {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Refresh-Token': refreshToken
            }
        });
    }

    sessionStorage.clear();
    location.href = '/login';
}

/** 헤더 사용자 정보 표시 */
async function loadMe() {
    try {
        const res = await apiFetch('/api/auth/me');

        if (!res.ok) {
            location.href = '/login';
            return;
        }

        const me = await res.json();

        const userNameEl = document.getElementById('userName');
        if (userNameEl) {
            userNameEl.innerText = `${me.name} (${me.role})`;
        }

    } catch (e) {
        location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', loadMe);
