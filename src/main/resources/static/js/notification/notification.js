/**
 * 알림 모달 관련 함수
 */

// 모달 열기
function openNotificationModal() {
    const modal = document.getElementById('notificationModal');
    if (modal) {
        modal.classList.remove('hidden');
        // 체크박스 초기화
        const checkbox = document.getElementById('showAllNotifications');
        if (checkbox) {
            checkbox.checked = false;
        }
        loadNotifications(false); // 기본값: 읽지 않은 알림만
    }
}

// 모달 닫기
function closeNotificationModal() {
    const modal = document.getElementById('notificationModal');
    if (modal) {
        modal.classList.add('hidden');
    }
}

// 알림 목록 로드
async function loadNotifications(showAll = false) {
    const notificationList = document.getElementById('notificationList');
    if (!notificationList) return;

    // 로딩 표시
    notificationList.innerHTML = '<div class="notification-loading">알림을 불러오는 중...</div>';

    try {
        const endpoint = showAll ? '/api/notifications' : '/api/notifications/unread';
        const response = await apiFetch(endpoint);
        
        if (!response.ok) {
            notificationList.innerHTML = '<div class="notification-empty">알림을 불러오는데 실패했습니다.</div>';
            return;
        }

        const result = await response.json();
        const notifications = result.data || [];

        if (notifications.length === 0) {
            notificationList.innerHTML = '<div class="notification-empty">알림이 없습니다.</div>';
            return;
        }

        // 알림 목록 렌더링
        notificationList.innerHTML = notifications.map(notification => `
            <div class="notification-item ${notification.isRead ? 'notification-item-read' : ''}">
                <div class="${notification.isRead ? 'notification-item-read-content' : 'notification-item-content'}">${escapeHtml(notification.content)}</div>
            </div>
        `).join('');
    } catch (error) {
        console.error('알림 로드 실패:', error);
        notificationList.innerHTML = '<div class="notification-empty">알림을 불러오는데 실패했습니다.</div>';
    }
}

// 모든 알림 토글
function toggleNotificationView() {
    const checkbox = document.getElementById('showAllNotifications');
    if (checkbox) {
        loadNotifications(checkbox.checked);
    }
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 모달 외부 클릭 시 닫기 (이미 HTML에서 처리됨)

