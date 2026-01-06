let currentSearchParams = {
    page: 0,
    size: 10,
    startDate: '',
    endDate: '',
    status: 'ALL',
    keyword: ''
};

// 툴팁 전역 상태
let reasonPopoverEl = null;
let reasonPopoverAnchorEl = null;
let hoverCloseTimer = null;

document.addEventListener('DOMContentLoaded', function () {
    // 오늘 날짜 표시
    const today = new Date();
    const formattedToday = today.getFullYear() + '.' +
        String(today.getMonth() + 1).padStart(2, '0') + '.' +
        String(today.getDate()).padStart(2, '0');

    const todayLabel = document.getElementById('todayLabel');
    if (todayLabel) todayLabel.innerText = `금일 요약 현황 (${formattedToday})`;

    // 모달 글자수 카운터
    const modalReasonInput = document.getElementById('modalReason');
    const charCountElement = document.getElementById('charCount');

    if (modalReasonInput) {
        modalReasonInput.addEventListener('input', function () {
            clearFieldError('modalReasonError');

            const reason = this.value;
            const currentLength = reason.length;
            const maxLength = 40;

            if (charCountElement) {
                charCountElement.textContent = `${currentLength} / ${maxLength}`;
                if (currentLength >= maxLength) charCountElement.style.color = 'var(--danger-color)';
                else if (currentLength >= maxLength * 0.8) charCountElement.style.color = '#f59e0b';
                else charCountElement.style.color = 'var(--neutral-500)';
            }

            if (reason.trim() && reason.length > maxLength) {
                showError('modalReasonError', `정정 사유는 ${maxLength}자 이하여야 합니다.`);
            }
        });
    }

    // ✅ 테이블에 호버 이벤트 위임(정정됨 요소가 동적으로 생성되기 때문)
    const table = document.getElementById('attendanceTable');
    if (table) {
        table.addEventListener('mouseover', handleCorrectedMouseOver);
        table.addEventListener('mouseout', handleCorrectedMouseOut);
    }

    // ESC로 닫기(안전장치)
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeReasonPopover();
    });

    // 스크롤/리사이즈 시 위치 재계산
    window.addEventListener('scroll', () => {
        if (reasonPopoverEl && reasonPopoverAnchorEl) positionReasonPopover(reasonPopoverAnchorEl, reasonPopoverEl);
    }, true);

    window.addEventListener('resize', () => {
        if (reasonPopoverEl && reasonPopoverAnchorEl) positionReasonPopover(reasonPopoverAnchorEl, reasonPopoverEl);
    });

    loadSummaryData();
    loadAttendanceList();
});

/**
 * ✅ 정정됨 호버 처리
 */
function handleCorrectedMouseOver(e) {
    const target = e.target.closest('.badge-corrected');
    if (!target) return;

    // 같은 요소에 대해 중복 오픈 방지
    if (reasonPopoverEl && reasonPopoverAnchorEl === target) return;

    clearTimeout(hoverCloseTimer);

    const reason = target.getAttribute('data-reason') || '';
    openReasonPopover(target, reason);

    // 툴팁 위로 마우스가 가도 유지되게
    if (reasonPopoverEl) {
        reasonPopoverEl.addEventListener('mouseenter', () => clearTimeout(hoverCloseTimer));
        reasonPopoverEl.addEventListener('mouseleave', () => scheduleClosePopover());
    }
}

function handleCorrectedMouseOut(e) {
    const target = e.target.closest('.badge-corrected');
    if (!target) return;

    // badge에서 빠져나갔지만, 툴팁으로 이동한 경우는 닫지 않기
    const toEl = e.relatedTarget;
    const movedToPopover = reasonPopoverEl && toEl && reasonPopoverEl.contains(toEl);
    if (movedToPopover) return;

    scheduleClosePopover();
}

function scheduleClosePopover() {
    clearTimeout(hoverCloseTimer);
    hoverCloseTimer = setTimeout(() => closeReasonPopover(), 120);
}

/**
 * 상단 통계
 */
async function loadSummaryData() {
    try {
        const response = await fetch('/api/admin/attendance/summary', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        if (response.ok) {
            const data = await response.json();
            document.getElementById('totalEmployees').innerText = data.totalEmployees || 0;
            document.getElementById('onTimeCount').innerText = data.onTimeCount || 0;
            document.getElementById('lateCount').innerText = data.lateCount || 0;
            document.getElementById('notLeavingCount').innerText = data.notLeavingCount || 0;
        }
    } catch (error) {
        console.error("통계 데이터 로드 실패:", error);
    }
}

/**
 * 목록
 */
async function loadAttendanceList() {
    const { page, size, startDate, endDate, status, keyword } = currentSearchParams;
    const params = new URLSearchParams({ page, size, startDate, endDate, status, keyword });

    try {
        const response = await fetch(`/api/admin/attendance/list?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            const list = data.content || (Array.isArray(data) ? data : []);
            renderAttendanceTable(list);
            renderPagination(data);
        }
    } catch (error) {
        console.error("목록 로드 실패:", error);
    }
}

/**
 * ✅ 테이블 렌더링
 * - 정정 전: 정정 버튼
 * - 정정 후: 정정됨 버튼(호버 시 사유 툴팁)
 */
function renderAttendanceTable(list) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const commuteStyle = item.statusCode === 'LATE' ? 'color:#ff9800; font-weight:bold;' : '';

        let leaveTimeDisplay = item.leaveAt || '-';
        if (item.commuteAt && item.commuteAt !== '-' && (!item.leaveAt || item.leaveAt === '-')) {
            leaveTimeDisplay = '<span style="color:#2196F3; font-weight:bold;">근무 중</span>';
        }

        // 서버 필드명이 다를 가능성까지 커버
        const corrected =
            item.isCorrected === true ||
            item.corrected === true ||
            item.correctionYn === 'Y' ||
            item.correctionStatus === 'CORRECTED';

        const reason =
            item.correctionReason ??
            item.reason ??
            item.correctionMsg ??
            '';

        let actionButtons = '';
        if (corrected) {
            // ✅ 사유 버튼 없음 + ✅ 체크 아이콘 제거
            actionButtons = `
                <div class="action-group">
                    <span class="badge-corrected"
                          data-reason="${escapeHtml(String(reason))}">
                        정정됨
                    </span>
                </div>
            `;
        } else {
            actionButtons = `
                <button type="button"
                        class="btn-table-action"
                        onclick="openCorrectionModal(${item.attendanceId}, '${item.statusCode}')">
                    정정
                </button>
            `;
        }

        return `
            <tr>
                <td><strong>${item.employeeName}</strong><br><small>${item.employeeNum}</small></td>
                <td style="${commuteStyle}">${item.commuteAt || '-'}</td>
                <td>${leaveTimeDisplay}</td>
                <td>${item.workingTime || '-'}</td>
                <td><span class="status-badge ${item.statusCode}">${item.statusName}</span></td>
                <td>${item.workDate || '-'}</td>
                <td>${actionButtons}</td>
            </tr>
        `;
    }).join('');
}

/**
 * ✅ 호버 툴팁 열기
 */
function openReasonPopover(anchorEl, reason) {
    const msg = (reason || '').trim() || '등록된 정정 사유가 없습니다.';

    closeReasonPopover();
    reasonPopoverAnchorEl = anchorEl;

    const pop = document.createElement('div');
    pop.className = 'reason-popover';
    pop.setAttribute('role', 'tooltip');

    pop.innerHTML = `
        <div class="reason-popover-arrow"></div>
        <div class="reason-popover-title">
            <i class="fa-solid fa-circle-info"></i>
            정정 사유
        </div>
        <div class="reason-popover-body">${escapeHtml(msg)}</div>
    `;

    document.body.appendChild(pop);
    reasonPopoverEl = pop;

    applyReasonPopoverSizing(pop, msg);
    positionReasonPopover(anchorEl, pop);
}

/**
 * ✅ 메시지 길이에 따른 폭 보정
 */
function applyReasonPopoverSizing(popEl, msg) {
    const viewportMax = Math.min(520, window.innerWidth - 24);
    const minW = 200;

    const len = (msg || '').length;
    const target = Math.min(viewportMax, Math.max(minW, 220 + len * 6.2));

    popEl.style.width = `${target}px`;
    popEl.style.maxWidth = `${viewportMax}px`;
}

/**
 * ✅ 위치 계산(아래 우선, 공간 부족하면 위로)
 */
function positionReasonPopover(anchorEl, popEl) {
    const rect = anchorEl.getBoundingClientRect();
    const gap = 8;

    const popW = popEl.offsetWidth;
    const popH = popEl.offsetHeight;

    let top = rect.bottom + gap;
    let left = rect.left + rect.width / 2 - popW / 2;

    const minLeft = 12;
    const maxLeft = window.innerWidth - popW - 12;
    left = Math.max(minLeft, Math.min(left, maxLeft));

    if (top + popH > window.innerHeight - 12) {
        top = rect.top - popH - gap;
    }

    popEl.style.top = `${Math.max(12, top)}px`;
    popEl.style.left = `${left}px`;

    const arrow = popEl.querySelector('.reason-popover-arrow');
    if (arrow) {
        const anchorCenterX = rect.left + rect.width / 2;
        const arrowLeft = Math.max(18, Math.min(popW - 18, anchorCenterX - left));
        arrow.style.left = `${arrowLeft}px`;

        const isAbove = top < rect.top;
        if (isAbove) {
            arrow.style.top = 'auto';
            arrow.style.bottom = '-6px';
            arrow.style.transform = 'rotate(225deg)';
        } else {
            arrow.style.bottom = 'auto';
            arrow.style.top = '-6px';
            arrow.style.transform = 'rotate(45deg)';
        }
    }
}

function closeReasonPopover() {
    if (reasonPopoverEl && reasonPopoverEl.parentNode) {
        reasonPopoverEl.parentNode.removeChild(reasonPopoverEl);
    }
    reasonPopoverEl = null;
    reasonPopoverAnchorEl = null;
}

/**
 * 모달
 */
function openCorrectionModal(id, status) {
    document.getElementById('targetAttendanceId').value = id;
    document.getElementById('modalStatus').value = status;

    const modalReason = document.getElementById('modalReason');
    modalReason.value = '';
    clearFieldError('modalReasonError');

    const charCountElement = document.getElementById('charCount');
    if (charCountElement) {
        charCountElement.textContent = `0 / 40`;
        charCountElement.style.color = 'var(--neutral-500)';
    }

    document.getElementById('correctionModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    document.getElementById('correctionModal').style.display = 'none';
    document.body.style.overflow = '';
}

function handleModalBackdropClick(e) {
    if (e.target && e.target.id === 'correctionModal') closeModal();
}

/**
 * 저장
 */
async function submitCorrection() {
    const reason = document.getElementById('modalReason').value.trim();
    if (!reason) {
        showError('modalReasonError', '정정 사유를 입력해주세요.');
        return;
    }

    const id = document.getElementById('targetAttendanceId').value;
    const status = document.getElementById('modalStatus').value;

    try {
        const response = await fetch(`/api/admin/attendance/update/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            },
            body: JSON.stringify({ status: status, correctionReason: reason })
        });

        if (response.ok) {
            closeModal();
            closeReasonPopover();
            await loadAttendanceList();
            await loadSummaryData();
            return;
        }

        let errorMessage = '정정 처리에 실패했습니다.';
        try {
            const err = await response.json();
            if (err && err.message) errorMessage = err.message;
        } catch (_) {}
        alert(errorMessage);

    } catch (error) {
        console.error(error);
        alert("네트워크 오류로 정정 처리에 실패했습니다.");
    }
}

/**
 * 검색
 */
function handleSearch() {
    currentSearchParams.keyword = document.getElementById('searchKeyword').value.trim();
    currentSearchParams.startDate = document.getElementById('startDate').value;
    currentSearchParams.endDate = document.getElementById('endDate').value;
    currentSearchParams.status = document.getElementById('statusFilter').value;
    currentSearchParams.page = 0;

    closeReasonPopover();
    loadAttendanceList();
}

function resetFilters() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('statusFilter').value = 'ALL';
    document.getElementById('searchKeyword').value = '';
    currentSearchParams = { page: 0, size: 10, startDate: '', endDate: '', status: 'ALL', keyword: '' };

    closeReasonPopover();
    loadAttendanceList();
}

/**
 * XSS 방지
 */
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(text);
    return div.innerHTML;
}

/**
 * 에러
 */
function showError(id, msg) {
    const err = document.getElementById(id);
    if (err) {
        err.textContent = msg;
        err.style.display = 'block';
    }
}
function clearFieldError(errId) {
    const err = document.getElementById(errId);
    if (err) {
        err.textContent = '';
        err.style.display = 'none';
    }
}

/**
 * 페이지네이션
 */
function renderPagination(pageData) {
    const pagination = document.getElementById('boardPagination');
    if (!pagination || pageData.totalPages === undefined || pageData.totalPages === null) return;

    pagination.innerHTML = '';

    const currentPage = pageData.number ?? 0;
    const totalPages = pageData.totalPages ?? 0;

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = i === currentPage ? 'page-number active' : 'page-number';
        btn.textContent = i + 1;
        btn.type = 'button';
        btn.onclick = () => {
            currentSearchParams.page = i;
            closeReasonPopover();
            loadAttendanceList();
        };
        pagination.appendChild(btn);
    }
}
