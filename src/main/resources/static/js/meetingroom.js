function loadMeetingRooms() {
    // 1. API 호출
    fetch('/api/admin/meetingrooms', {
        method: 'GET',
        headers: {
            // [중요] JWT 토큰이 있다면 헤더에 포함해야 합니다.
            // 로그인 시 저장해둔 토큰을 가져오세요 (예: localStorage)
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken'),
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            return response.json(); // JSON 변환
        })
        .then(data => {
            // 2. 받아온 데이터로 화면 그리기
            const list = data.data; // ResponseDto 구조상 data 필드에 리스트가 있음
            const tbody = document.querySelector('.resource-table tbody');
            tbody.innerHTML = ''; // 기존 내용 초기화

            if (list && list.length > 0) {
                list.forEach((room, index) => {
                    const row = `
                        <tr>
                            <td>${index + 1}</td>
                            <td>${room.name || ''}</td>
                            <td>${room.position || ''}</td>
                            <td>${room.description || ''}</td>
                            <td>
                                <div class="action-btns">
                                    <button type="button" class="btn-edit" onclick="editMeetingRoom(${room.meetingroomId})">수정</button>
                                    <button type="button" class="btn-delete" onclick="deleteMeetingRoom(${room.meetingroomId})">삭제</button>
                                </div>
                            </td>
                        </tr>
                    `;
                    tbody.insertAdjacentHTML('beforeend', row);
                });
            } else {
                // 데이터가 없을 경우
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5">
                            <div class="empty-state">
                                <i class="fas fa-inbox"></i>
                                <p>등록된 리소스가 없습니다.</p>
                            </div>
                        </td>
                    </tr>
                `;
            }
        })
        .catch(error => {
            console.error('데이터 로드 실패:', error);
            alert('회의실 목록을 불러오는데 실패했습니다.');
        });
}

// 수정 기능
function editMeetingRoom(meetingroomId) {
    console.log('수정할 회의실 ID:', meetingroomId);
    // TODO: 수정 모달 열기 또는 수정 페이지로 이동
    alert(`회의실 ID ${meetingroomId} 수정 기능을 구현하세요.`);
}

// 삭제 기능
function deleteMeetingRoom(meetingroomId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    fetch(`/api/admin/meetingrooms/${meetingroomId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken'),
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('삭제에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            alert('삭제되었습니다.');
            loadMeetingRooms(); // 목록 다시 로드
        })
        .catch(error => {
            console.error('삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        });
}

// 페이지 로드 시 자동 실행
document.addEventListener('DOMContentLoaded', function() {
    loadMeetingRooms();
});