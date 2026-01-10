/**
 * 알림 모달 관련 함수
 */

// 현재 선택된 탭 (기본값: unread)
let currentTab = 'unread';

// 드롭다운 토글
function toggleNotificationDropdown() {
    const dropdown = document.getElementById('notificationDropdown');
    if (dropdown) {
        const isHidden = dropdown.classList.contains('hidden');
        if (isHidden) {
            dropdown.classList.remove('hidden');
            // 탭 초기화
            currentTab = 'unread';
            switchTab('unread');
            // 읽지 않은 알림 수 업데이트
            if (typeof refreshUnreadCount === 'function') {
                refreshUnreadCount();
            }
        } else {
            dropdown.classList.add('hidden');
            // 읽지 않은 알림 수 업데이트
            if (typeof refreshUnreadCount === 'function') {
                refreshUnreadCount();
            }
        }
    }
}

// 모달 열기 (하위 호환성)
function openNotificationModal() {
    toggleNotificationDropdown();
}

// 탭 전환
function switchTab(tab) {
    currentTab = tab;

    // 탭 버튼 활성화 상태 업데이트
    const unreadTab = document.getElementById('unreadTab');
    const readTab = document.getElementById('readTab');

    if (unreadTab && readTab) {
        if (tab === 'unread') {
            unreadTab.classList.add('active');
            readTab.classList.remove('active');
        } else {
            unreadTab.classList.remove('active');
            readTab.classList.add('active');
        }
    }

    // 알림 목록 로드
    loadNotifications(tab === 'read');
}

// 드롭다운 닫기
function closeNotificationModal() {
    toggleNotificationDropdown();
}

// 알림 목록 로드
async function loadNotifications(showRead = false) {
    const notificationList = document.getElementById('notificationList');
    if (!notificationList) return;

    // 로딩 표시
    notificationList.innerHTML = '<div class="notification-loading">알림을 불러오는 중...</div>';

    try {
        // 모든 알림을 가져온 후 필터링
        const response = await apiFetch('/api/notifications');

        if (!response.ok) {
            notificationList.innerHTML = '<div class="notification-empty">알림을 불러오는데 실패했습니다.</div>';
            return;
        }

        const result = await response.json();
        let notifications = result.data || [];

        // 읽음/읽지 않음 필터링
        if (showRead) {
            notifications = notifications.filter(n => n.isRead === true);
        } else {
            notifications = notifications.filter(n => n.isRead === false);
        }

        if (notifications.length === 0) {
            notificationList.innerHTML = '<div class="notification-empty">알림이 없습니다.</div>';
            return;
        }

        // 알림 목록 렌더링
        notificationList.innerHTML = notifications.map(notification => {
            const url = notification.url || '';
            const escapedUrl = url.replace(/'/g, "\\'").replace(/"/g, '&quot;');
            return `
            <div class="notification-item ${notification.isRead ? 'notification-item-read' : ''}" 
                 ${url ? `onclick="handleNotificationClick('${escapedUrl}', ${notification.notificationId}, event)"` : ''}
                 style="${url ? 'cursor: pointer;' : ''}">
                <div class="notification-item-time">${formatTimeAgo(notification.createdAt)}</div>
                <div class="notification-item-content-wrapper">
                    <div class="notification-item-header">
                        <div class="notification-item-title">${escapeHtml(notification.type)} 알림</div>
                    </div>
                    <div class="${notification.isRead ? 'notification-item-read-content' : 'notification-item-content'}">${escapeHtml(notification.content)}</div>
                </div>
                ${!notification.isRead ? `<button class="notification-check-btn" onclick="markAsRead(${notification.notificationId}, event)" title="확인">
                    <i class="fas fa-check"></i>
                </button>` : ''}
            </div>
        `;
        }).join('');
    } catch (error) {
        console.error('알림 로드 실패:', error);
        notificationList.innerHTML = '<div class="notification-empty">알림을 불러오는데 실패했습니다.</div>';
    }
}

// 시간 포맷팅 함수 (createdAt 기반)
function formatTimeAgo(createdAt) {
    if (!createdAt) return '';

    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now - created;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) {
        return '방금 전';
    } else if (diffMins < 60) {
        return `${diffMins}분 전`;
    } else if (diffHours < 24) {
        return `${diffHours}시간 전`;
    } else if (diffDays < 7) {
        return `${diffDays}일 전`;
    } else {
        const month = created.getMonth() + 1;
        const day = created.getDate();
        return `${month}월 ${day}일`;
    }
}


// 알림 클릭 처리
function handleNotificationClick(url, notificationId, event) {
    // 확인 버튼 클릭 시에는 이동하지 않음
    if (event && event.target.closest('.notification-check-btn')) {
        return;
    }

    // url이 있으면 이동
    if (url) {
        // 읽지 않은 알림이면 읽음 처리
        if (notificationId) {
            markAsRead(notificationId, event).then(() => {
                window.location.href = url;
            });
        } else {
            window.location.href = url;
        }
    }
}

// 알림 읽음 처리
async function markAsRead(notificationId, event) {
    // 이벤트 전파 중지
    if (event) {
        event.stopPropagation();
    }

    try {
        const response = await apiFetch(`/api/notifications/${notificationId}`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            console.error('알림 읽음 처리 실패');
            return;
        }

        // 성공 시 알림 목록 새로고침
        await loadNotifications(currentTab === 'read');

        // 알림 카운트도 새로고침
        if (typeof refreshUnreadCount === 'function') {
            refreshUnreadCount();
        }
    } catch (error) {
        console.error('알림 읽음 처리 실패:', error);
    }
}

// 모든 알림 읽음 처리
async function markAllAsRead() {
    try {
        const response = await apiFetch('/api/notifications/read-all', {
            method: 'PATCH'
        });

        if (!response.ok) {
            console.error('모든 알림 읽음 처리 실패');
            await sweetError('모든 알림을 읽음 처리하는데 실패했습니다.');
            return;
        }

        // 성공 시 알림 목록 새로고침
        await loadNotifications(currentTab === 'read');

        // 알림 카운트도 새로고침
        if (typeof refreshUnreadCount === 'function') {
            refreshUnreadCount();
        }

        // 성공 메시지 (선택사항)
        console.log('모든 알림이 읽음 처리되었습니다.');
    } catch (error) {
        console.error('모든 알림 읽음 처리 실패:', error);
        await sweetError('모든 알림을 읽음 처리하는데 실패했습니다.');
    }
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 외부 클릭 시 드롭다운 닫기
document.addEventListener('click', (e) => {
    const dropdown = document.getElementById('notificationDropdown');
    const bellIcon = document.querySelector('.fa-bell');

    if (dropdown && !dropdown.classList.contains('hidden')) {
        // 드롭다운 내부나 알림 아이콘을 클릭한 경우가 아니면 닫기
        if (!dropdown.contains(e.target) && !e.target.closest('a[onclick*="toggleNotificationDropdown"]')) {
            dropdown.classList.add('hidden');
            // 읽지 않은 알림 수 업데이트
            if (typeof refreshUnreadCount === 'function') {
                refreshUnreadCount();
            }
        }
    }
});

