/* ==========================
   Tooltip (singleton)
========================== */
let tooltipEl = null;

function ensureTooltip() {
    if (!tooltipEl) {
        tooltipEl = document.createElement('div');
        tooltipEl.className = 'tooltip';
        document.body.appendChild(tooltipEl);
    }
}

function showTooltip(e) {
    const text = e.currentTarget.dataset.fulltext;
    if (!text) return;

    ensureTooltip();
    tooltipEl.textContent = text;
    tooltipEl.style.display = 'block';
    moveTooltip(e);
}

function moveTooltip(e) {
    if (!tooltipEl) return;
    tooltipEl.style.left = e.pageX + 12 + 'px';
    tooltipEl.style.top = e.pageY + 12 + 'px';
}

function hideTooltip() {
    if (tooltipEl) {
        tooltipEl.style.display = 'none';
    }
}

/* ==========================
   Helper Functions
========================== */

// 날짜 포맷 (yyyy-MM-dd)
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// 시간 포맷 (int → HH:00)
function formatHour(hour) {
    if (hour === null || hour === undefined) return '-';
    return `${String(hour).padStart(2, '0')}:00`;
}

/* ==========================
   Table Cell Helpers
========================== */
function createCell(value = '-', tooltip = false) {
    const td = document.createElement('td');
    const text = (value ?? '').toString();
    
    td.textContent = text;
    
    if (tooltip && text.length > 0 && text !== '-') {
        td.dataset.fulltext = text;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }
    
    return td;
}

function createCategoryCell(typeCode, typeName) {
    const td = document.createElement('td');
    td.textContent = typeName || '-';
    
    // hover 시 전체 텍스트 보기 기능
    if (typeName && typeName !== '-') {
        td.dataset.fulltext = typeName;
        td.addEventListener('mouseenter', showTooltip);
        td.addEventListener('mousemove', moveTooltip);
        td.addEventListener('mouseleave', hideTooltip);
    }
    
    return td;
}

function createStatusCell(reservation) {
    const td = document.createElement('td');
    td.className = 'status-cell';
    
    const badge = document.createElement('span');
    badge.className = 'status-badge status-badge-clickable';
    badge.textContent = reservation.reservationStatusName;
    badge.dataset.reservationId = reservation.reservationId;
    badge.dataset.currentStatusId = reservation.reservationStatusId;
    
    // 상태별 클래스 추가
    const statusName = reservation.reservationStatusName;
    if (statusName === '예약 확정') {
        badge.classList.add('status-confirmed');
    } else if (statusName === '승인 대기') {
        badge.classList.add('status-pending');
    } else if (statusName === '예약 반려') {
        badge.classList.add('status-rejected');
    } else if (statusName === '예약 취소') {
        badge.classList.add('status-cancelled');
    }
    
    badge.addEventListener('click', (e) => {
        e.stopPropagation();
        // tooltip 숨기기
        hideTooltip();
        showStatusDropdown(badge, reservation);
    });
    
    // '예약 반려' 또는 '예약 취소'일 때 rejectReason tooltip 추가
    if ((reservation.reservationStatusName === '예약 반려' || reservation.reservationStatusName === '예약 취소') 
        && reservation.rejectReason) {
        td.dataset.fulltext = reservation.rejectReason;
        
        // td에 tooltip 이벤트 추가 (badge를 제외한 영역)
        td.addEventListener('mouseenter', (e) => {
            // badge 위가 아닐 때만 tooltip 표시
            if (e.target !== badge && !badge.contains(e.target)) {
                showTooltip(e);
            }
        });
        td.addEventListener('mousemove', (e) => {
            if (e.target !== badge && !badge.contains(e.target)) {
                moveTooltip(e);
            }
        });
        td.addEventListener('mouseleave', hideTooltip);
        
        // badge 위에서도 tooltip 표시 (클릭 전에)
        badge.addEventListener('mouseenter', (e) => {
            e.stopPropagation();
            showTooltip(e);
        });
        badge.addEventListener('mousemove', (e) => {
            e.stopPropagation();
            moveTooltip(e);
        });
        badge.addEventListener('mouseleave', (e) => {
            e.stopPropagation();
            hideTooltip();
        });
    }
    
    td.appendChild(badge);
    return td;
}

function createActionCell(reservation) {
    const td = document.createElement('td');

    // 승인 대기(1)만 승인 가능
    if (reservation.reservationStatusId === 1) {
        const btn = document.createElement('button');
        btn.className = 'btn-approve';
        btn.textContent = '승인';
        btn.onclick = () => approveReservation(reservation.reservationId);
        td.appendChild(btn);
    } else {
        td.textContent = '-';
    }

    return td;
}

/* ==========================
   Pagination State & Filters
========================== */
let currentPage = 0;
let totalPages = 0;
let pageSize = 10;
let currentFilters = {
    showPast: false,
    statusId: null,
    typeCode: null
};
let statusList = []; // 상태 목록 저장
let activeDropdown = null; // 현재 활성화된 드롭다운

/* ==========================
   Data Load
========================== */
async function loadStatuses() {
    try {
        const res = await apiFetch(
            '/api/reservation/status',
            {method: 'GET'}
        );

        if(!res.ok) throw new Error();

        const {data} = await res.json();

        // 테스트용 status는 불러오지 않기
        statusList = data.filter(status => status.id < 5);
        
        const select = document.getElementById("status-filter")

        select.innerHTML = '<option value="">전체</option>';

        statusList.forEach(status => {
            const option = document.createElement('option');
            option.value = status.id;
            option.textContent = status.statusName;
            select.appendChild(option);
        });
    } catch(e) {
        console.error(e);
        alert("상태 목록 조회 실패");
    }
}

async function loadReservations(page = 0) {
    try {
        const params = new URLSearchParams({
            page: page,
            size: pageSize
        });

        params.append('showPast', currentFilters.showPast);

        if (currentFilters.statusId !== null) {
            params.append('statusId', currentFilters.statusId);
        }

        if (currentFilters.typeCode !== null) {
            params.append('typeCode', currentFilters.typeCode);
        }

        // 관리자용 API 엔드포인트 (실제 엔드포인트에 맞게 수정 필요)
        const res = await apiFetch(`/api/admin/reservations?${params.toString()}`, {
            method: 'GET'
        });

        if (!res.ok) throw new Error();

        const {data} = await res.json();
        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" class="empty-state">
                        예약 내역이 없습니다.
                    </td>
                </tr>
            `;
            document.getElementById('pagination-container').style.display = 'none';
            return;
        }

        document.getElementById('pagination-container').style.display = 'flex';

        const startNumber = data.number * pageSize;

        data.content.forEach((r, i) => {
            const tr = document.createElement('tr');
            
            // 신청자 이름 (employeeName 또는 applicantName 등 API 응답에 맞게 수정 필요)
            const applicantName = r.employeeName || r.applicantName || r.name || '-';
            
            // 자원 이름
            const resourceName = r.resourceName || '-';
            
            // 예약 사유
            const reservationReason = r.reservationReason || '-';
            
            tr.append(
                createCell(startNumber + i + 1),
                createCategoryCell(r.typeCode, r.typeName), // 카테고리 (tooltip 적용)
                createCell(applicantName, true), // 신청자 이름 (tooltip 적용)
                createCell(resourceName, true), // 자원 이름 (tooltip 적용)
                createCell(reservationReason, true), // 예약 사유 (tooltip 적용)
                createCell(formatDate(r.reservationDate)),
                createCell(formatDate(r.endDate)),
                createCell(r.typeCode === 'CAR' ? '-' : formatHour(r.startTime)),
                createCell(r.typeCode === 'CAR' ? '-' : formatHour(r.endTime)),
                createStatusCell(r),
                createActionCell(r)
            );
            tbody.appendChild(tr);
        });

        renderPagination(data);

    } catch (e) {
        console.error(e);
        alert('예약 목록을 불러오지 못했습니다.');
    }
}


/* ==========================
   Pagination Render
========================== */
function renderPagination(pageData) {
    const container = document.querySelector('.pagination');
    container.innerHTML = '';

    const {number, totalPages, first, last} = pageData;

    const prev = document.createElement('button');
    prev.textContent = '<';
    prev.disabled = first;
    prev.onclick = () => loadReservations(number - 1);
    container.appendChild(prev);

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        if (i === number) btn.className = 'active';
        btn.onclick = () => loadReservations(i);
        container.appendChild(btn);
    }

    const next = document.createElement('button');
    next.textContent = '>';
    next.disabled = last;
    next.onclick = () => loadReservations(number + 1);
    container.appendChild(next);
}

/* ==========================
   Status Dropdown
========================== */
function showStatusDropdown(badge, reservation) {
    // 기존 드롭다운이 있으면 제거
    if (activeDropdown) {
        activeDropdown.remove();
        activeDropdown = null;
    }

    // 드롭다운 생성
    const dropdown = document.createElement('div');
    dropdown.className = 'status-dropdown';
    dropdown.dataset.reservationId = reservation.reservationId;
    
    // 회의실인 경우 필터링된 상태 목록 사용
    let filteredStatusList = statusList;
    if (reservation.typeCode === 'MEETING') {
        // 회의실은 '예약 확정', '예약 취소'만 표시
        filteredStatusList = statusList.filter(status => 
            status.statusName === '예약 확정' || status.statusName === '예약 취소'
        );
    }
    
    // 상태 목록 추가
    filteredStatusList.forEach(status => {
        const item = document.createElement('div');
        item.className = 'status-dropdown-item';
        if (status.id === reservation.reservationStatusId) {
            item.classList.add('active');
        }
        item.textContent = status.statusName;
        item.onclick = () => {
            // '예약 반려' 또는 '예약 취소'인 경우 모달 표시
            if (status.statusName === '예약 반려' || status.statusName === '예약 취소') {
                dropdown.remove();
                activeDropdown = null;
                showStatusReasonModal(reservation.reservationId, status.id, status.statusName);
            } else {
                updateReservationStatus(reservation.reservationId, status.id, status.statusName);
                dropdown.remove();
                activeDropdown = null;
            }
        };
        dropdown.appendChild(item);
    });

    // 배지 위치 기준으로 드롭다운 위치 설정
    const rect = badge.getBoundingClientRect();
    dropdown.style.position = 'fixed';
    dropdown.style.top = `${rect.bottom + 4}px`;
    dropdown.style.left = `${rect.left}px`;
    dropdown.style.zIndex = '1000';

    document.body.appendChild(dropdown);
    activeDropdown = dropdown;

    // 외부 클릭 시 드롭다운 닫기
    setTimeout(() => {
        document.addEventListener('click', function closeDropdown(e) {
            if (!dropdown.contains(e.target) && e.target !== badge) {
                dropdown.remove();
                activeDropdown = null;
                document.removeEventListener('click', closeDropdown);
            }
        }, { once: true });
    }, 0);
}

/* ==========================
   Status Reason Modal
========================== */
let pendingStatusUpdate = null; // 모달에서 처리할 상태 변경 정보

function showStatusReasonModal(reservationId, statusId, statusName) {
    const modal = document.getElementById('status-reason-modal');
    const title = document.getElementById('status-reason-modal-title');
    const label = document.getElementById('status-reason-label');
    const input = document.getElementById('status-reason-input');
    const hint = document.getElementById('status-reason-hint');
    const cancelBtn = document.getElementById('status-reason-modal-cancel');
    const submitBtn = document.getElementById('status-reason-modal-submit');

    // 모달 제목과 라벨 설정
    if (statusName === '예약 반려') {
        title.textContent = '예약 반려 사유';
        label.textContent = '반려 사유';
    } else if (statusName === '예약 취소') {
        title.textContent = '예약 취소 사유';
        label.textContent = '취소 사유';
    }

    // 입력 필드 초기화
    input.value = '';
    hint.textContent = '';
    hint.className = 'status-reason-hint';

    // 상태 변경 정보 저장
    pendingStatusUpdate = {
        reservationId,
        statusId,
        statusName
    };

    // 모달 표시
    modal.style.display = 'flex';

    // 취소 버튼 이벤트
    cancelBtn.onclick = () => {
        closeStatusReasonModal();
    };

    // 확인 버튼 이벤트
    submitBtn.onclick = () => {
        const reason = input.value.trim();
        if (!reason) {
            hint.textContent = '사유를 입력해주세요.';
            hint.style.color = '#c62828';
            return;
        }

        if (reason.length > 255) {
            hint.textContent = `최대 255자까지 입력 가능합니다. (${reason.length}/255)`;
            hint.style.color = '#c62828';
            return;
        }

        // 상태 변경 API 호출
        updateReservationStatusWithReason(reservationId, statusId, statusName, reason);
    };

    // Enter 키 처리
    input.onkeydown = (e) => {
        if (e.key === 'Enter' && e.ctrlKey) {
            submitBtn.click();
        }
    };

    // 입력 시 힌트 업데이트
    input.oninput = () => {
        const value = input.value.trim();
        if (value.length > 255) {
            hint.textContent = `최대 255자까지 입력 가능합니다. (${value.length}/255)`;
            hint.style.color = '#c62828';
        } else if (value.length > 0) {
            hint.textContent = `${value.length}/255`;
            hint.style.color = '#666';
        } else {
            hint.textContent = '';
        }
    };

    // 모달 외부 클릭 시 닫기
    modal.onclick = (e) => {
        if (e.target === modal) {
            closeStatusReasonModal();
        }
    };

    // 포커스 설정
    setTimeout(() => {
        input.focus();
    }, 100);
}

function closeStatusReasonModal() {
    const modal = document.getElementById('status-reason-modal');
    modal.style.display = 'none';
    pendingStatusUpdate = null;
}

/* ==========================
   Actions
========================== */
async function updateReservationStatusWithReason(reservationId, statusId, statusName, reason) {
    try {
        const res = await apiFetch(`/api/admin/reservations/${reservationId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                statusId,
                rejectReason: reason
            })
        });

        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '상태 변경에 실패했습니다.');
        }

        closeStatusReasonModal();
        alert(`예약 상태가 "${statusName}"으로 변경되었습니다.`);
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert(e.message || '상태 변경에 실패했습니다.');
    }
}

async function updateReservationStatus(reservationId, statusId, statusName) {
    try {
        const res = await apiFetch(`/api/admin/reservations/${reservationId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ statusId })
        });

        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '상태 변경에 실패했습니다.');
        }

        alert(`예약 상태가 "${statusName}"으로 변경되었습니다.`);
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert(e.message || '상태 변경에 실패했습니다.');
    }
}

async function approveReservation(id) {
    if (!confirm('예약을 승인하시겠습니까?')) return;

    try {
        const res = await apiFetch(`/api/admin/reservations/${id}/approve`, {
            method: 'PATCH'
        });

        if (!res.ok) throw new Error();

        alert('예약이 승인되었습니다.');
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert('예약 승인에 실패했습니다.');
    }
}

/* ==========================
   Batch Approve
========================== */
async function batchApproveReservations() {
    const typeSelect = document.getElementById('resource-category-filter');
    const statusSelect = document.getElementById('status-filter');
    
    const typeCode = typeSelect?.value || null;
    const statusId = statusSelect?.value ? Number(statusSelect.value) : null;

    // '승인 대기' 상태인지 확인
    const selectedOption = statusSelect?.options[statusSelect?.selectedIndex];
    if (selectedOption?.textContent !== '승인 대기') {
        alert('승인 대기 상태를 선택해주세요.');
        return;
    }

    if (!confirm(`승인 대기 상태의 예약을 일괄 승인하시겠습니까?`)) {
        return;
    }

    try {
        const params = new URLSearchParams();

        // resource-category-filter에서 선택한 카테고리 추가
        if (typeCode) {
            params.append('typeCode', typeCode);
        }

        console.log("타입코드 : " + typeCode)

        const res = await apiFetch(`/api/admin/reservations/batch-approve?${params.toString()}`, {
            method: 'PATCH'
        });


        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '일괄 승인에 실패했습니다.');
        }

        const result = await res.json();

        alert(`${result.data}개 예약이 일괄 승인되었습니다.`);
        loadReservations(currentPage);

    } catch (e) {
        console.error(e);
        alert(e.message || '일괄 승인에 실패했습니다.');
    }
}

/* ==========================
   Filter Functions
========================== */
function applyFilters() {
    const typeSelect = document.getElementById('resource-category-filter');
    const statusSelect = document.getElementById('status-filter');
    const pastToggle = document.getElementById('past-reservations-toggle');

    currentFilters.typeCode = typeSelect?.value || null;

    currentFilters.statusId = statusSelect?.value
        ? Number(statusSelect.value)
        : null;

    currentFilters.showPast = pastToggle?.checked ?? false;

    // 일괄 승인 버튼 표시/숨김 제어
    updateBatchApproveButton();

    // 필터 변경 시 항상 첫 페이지부터
    loadReservations(0);
}

function updateBatchApproveButton() {
    const batchApproveWrapper = document.getElementById('batch-approve-wrapper');
    const statusSelect = document.getElementById('status-filter');

    // '승인 대기' 상태 ID는 1 (일반적으로)
    // statusSelect의 선택된 옵션 텍스트가 '승인 대기'인지 확인
    const selectedOption = statusSelect?.options[statusSelect?.selectedIndex];
    const isPendingStatus = selectedOption?.textContent === '승인 대기';

    if (isPendingStatus) {
        batchApproveWrapper.style.display = 'block';
    } else {
        batchApproveWrapper.style.display = 'none';
    }
}

/* ==========================
   Event Listeners
========================== */
function initFilters() {
    const typeSelect = document.getElementById('resource-category-filter');
    const statusSelect = document.getElementById('status-filter');
    const pastToggle = document.getElementById('past-reservations-toggle');
    const batchApproveBtn = document.getElementById('btn-batch-approve');

    // 자원 타입 변경 시 자동 검색
    if (typeSelect) {
        typeSelect.addEventListener('change', applyFilters);
    }

    // 상태 변경 시 자동 검색
    if (statusSelect) {
        statusSelect.addEventListener('change', applyFilters);
    }

    // 과거 예약 토글 변경 시 자동 검색
    if (pastToggle) {
        pastToggle.addEventListener('change', applyFilters);
    }

    // 일괄 승인 버튼 클릭
    if (batchApproveBtn) {
        batchApproveBtn.addEventListener('click', batchApproveReservations);
    }
}

/* ==========================
   Init
========================== */
document.addEventListener('DOMContentLoaded', () => {
    initFilters();
    loadReservations();
    loadStatuses();
});

