/**
 * inbox.js
 * - 내 결재함 목록 조회
 * - 검색 / 필터
 * - 페이지네이션
 */

/* =========================
   전역 상태
========================= */

const APPROVAL_API = '/api/documents/approvals';

let currentPage = 0;
let totalPages = 1;

/* =========================
   초기화
========================= */

function updateApprovalSidebarSelection() {
    // 모든 no-sub 메뉴 선택 해제
    document.querySelectorAll('.menu-item.no-sub').forEach(item => {
        item.classList.remove('selected');
    });

    // 결재 대기함 선택
    const inboxLink = document.getElementById('inboxLink');
    if (inboxLink) {
        const menuItem = inboxLink.closest('.menu-item.no-sub');
        if (menuItem) {
            menuItem.classList.add('selected');
        }
    }
}


document.addEventListener('DOMContentLoaded', () => {
    updateApprovalSidebarSelection(); // ⭐ 추가
    loadApprovalList(0);
    bindDateFilterEvents();
});

/* =========================
   결재 목록 조회
========================= */

async function loadApprovalList(page = 0) {
    try {
        const size = 10;

        const params = new URLSearchParams({
            page,
            size
        });

        // ===== 검색 조건 =====
        const keyword = document.getElementById('keyword')?.value;
        const searchType = document.getElementById('searchType')?.value;
        const documentStatus = document.getElementById('documentStatus')?.value;
        const approvalStatus = document.getElementById('approvalStatus')?.value;
        const startDate = document.getElementById('startDate')?.value;
        const endDate = document.getElementById('endDate')?.value;

        if (keyword) params.append('keyword', keyword);
        if (searchType && searchType !== 'ALL') params.append('searchType', searchType);
        if (documentStatus) params.append('documentStatus', documentStatus);
        if (approvalStatus) params.append('approvalStatus', approvalStatus);
        if (startDate && endDate) {
            params.append('startDate', startDate);
            params.append('endDate', endDate);
        }

        const res = await apiFetch(`${APPROVAL_API}?${params.toString()}`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!res.ok) {
            if (res.status === 401) {
                location.href = '/login';
                return;
            }
            throw new Error('결재 목록 조회 실패');
        }

        const result = await res.json();
        const pageData = result.data;

        const approvals = pageData.content || [];

        // ✅ 페이지네이션의 단일 기준
        currentPage = page;
        totalPages = pageData.totalPages;

        renderApprovalTable(approvals);
        renderPagination(currentPage);

    } catch (e) {
        console.error(e);
        await sweetWarning('결재 목록 조회 실패.');
    }
}

/* =========================
   테이블 렌더링
========================= */

function renderApprovalTable(list) {
    const tbody = document.getElementById('approvalTableBody');
    tbody.innerHTML = '';

    if (!list.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="approval-empty-row">
                    결재 문서가 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    list.forEach((item, index) => {
        const row = document.createElement('tr');

        const number = currentPage * 10 + index + 1;
        const createdAt = formatDateTime(item.createdAt);

        const documentStatusBadge =
            renderDocumentStatusBadge(item.documentDisplayStatus);

        const myStatusBadge =
            renderMyApprovalStatusBadge(item);

        const safeTitle = escapeHTML(item.documentTitle || '');

        row.innerHTML = `
            <td class="index-col">${number}</td>
        
            <!-- 제목 -->
            <td class="approval-title title-col cell-ellipsis"
                title="${safeTitle}">
                ${safeTitle}
            </td>
        
            <td>${documentStatusBadge}</td>
        
            <!-- 기안자 -->
            <td class="cell-ellipsis"
                title="${escapeHTML(item.writerName)}">
                ${escapeHTML(item.writerName)}
            </td>
        
            <!-- 양식 -->
            <td class="cell-ellipsis"
                title="${escapeHTML(item.templateName)}">
                ${escapeHTML(item.templateName)}
            </td>
        
            <!-- 현재 결재자 -->
            <td class="cell-ellipsis"
                title="${escapeHTML(item.displayApproverName)}">
                ${escapeHTML(item.displayApproverName)}
            </td>
        
            <td>${myStatusBadge}</td>
            <td>${createdAt}</td>
        `;


        row.addEventListener('click', () => {
            location.href = `/view/document/${item.documentId}`;
        });

        tbody.appendChild(row);
    });
}


function renderMyApprovalStatusBadge(item) {
    const status = item.myApprovalStatus;
    const remaining = item.remainingBeforeMyTurn;

    let text = '';
    let colorClass = 'status-gray';

    // ✅ 내 차례
    if (status === 'IN_PROGRESS' && remaining === 0) {
        text = '결재 필요';
        colorClass = 'status-blue';
    }
    // ✅ 아직 대기중
    else if (status === 'WAITING' && typeof remaining === 'number') {
        text = `대기중 (${remaining}명)`;
        colorClass = 'status-yellow';
    }
    // ✅ 완료
    else if (status === 'APPROVED') {
        text = '승인';
        colorClass = 'status-green';
    } else if (status === 'REJECTED') {
        text = '반려';
        colorClass = 'status-red';
    } else if (status === 'CANCELLED') {
        text = '취소';
        colorClass = 'status-gray';
    }

    return `<span class="status-badge ${colorClass}">${text}</span>`;
}


function renderDocumentStatusBadge(statusText) {
    if (!statusText) {
        return `<span class="status-badge status-gray">-</span>`;
    }

    let colorClass = 'status-gray';

    switch (statusText) {
        case '진행중':
            colorClass = 'status-blue';
            break;
        case '승인 완료':
            colorClass = 'status-green';
            break;
        case '반려 종료':
            colorClass = 'status-red';
            break;
    }

    return `<span class="status-badge ${colorClass}">${statusText}</span>`;
}


/* =========================
   페이지네이션
========================= */

function renderPagination(page) {
    const pagination = document.getElementById('approvalPagination');
    pagination.innerHTML = '';

    // 이전 버튼
    const prev = document.createElement('button');
    prev.className = 'page-btn';
    prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prev.disabled = page === 0;
    prev.onclick = () => loadApprovalList(page - 1);
    pagination.appendChild(prev);

    const maxVisible = 5;

    let start, end;

    // ✅ 핵심 분기
    if (totalPages <= maxVisible) {
        // 전체 페이지 수가 적으면 전부 노출
        start = 0;
        end = totalPages - 1;
    } else {
        start = Math.max(0, page - Math.floor(maxVisible / 2));
        end = start + maxVisible - 1;

        if (end >= totalPages - 1) {
            end = totalPages - 1;
            start = end - maxVisible + 1;
        }
    }

    // 첫 페이지 + ...
    if (start > 0) {
        addPageBtn(pagination, 0);
        if (start > 1) addEllipsis(pagination);
    }

    // 페이지 버튼
    for (let i = start; i <= end; i++) {
        addPageBtn(pagination, i, i === page);
    }

    // ... + 마지막 페이지
    if (end < totalPages - 1) {
        if (end < totalPages - 2) addEllipsis(pagination);
        addPageBtn(pagination, totalPages - 1);
    }

    // 다음 버튼
    const next = document.createElement('button');
    next.className = 'page-btn';
    next.innerHTML = '<i class="fas fa-chevron-right"></i>';
    next.disabled = page >= totalPages - 1;
    next.onclick = () => loadApprovalList(page + 1);
    pagination.appendChild(next);
}

function addPageBtn(container, page, active = false) {
    const btn = document.createElement('button');
    btn.className = 'page-number';
    if (active) btn.classList.add('active');
    btn.textContent = page + 1;
    btn.onclick = () => loadApprovalList(page);
    container.appendChild(btn);
}

function addEllipsis(container) {
    const span = document.createElement('span');
    span.className = 'ellipsis';
    span.textContent = '...';
    container.appendChild(span);
}

/* =========================
   검색 / 필터
========================= */

function searchApprovals() {
    currentPage = 0;
    loadApprovalList(0);
}

/* =========================
   날짜 필터
========================= */

function bindDateFilterEvents() {
    const dateFilter = document.getElementById('dateFilter');
    const start = document.getElementById('startDate');
    const end = document.getElementById('endDate');

    if (!dateFilter || !start || !end) return;

    /* =========================
       ✅ 시작일 → 종료일 제약 (공통)
    ========================= */

    // 초기 상태 반영 (뒤로가기 / 새로고침 대비)
    if (start.value) {
        end.min = start.value;
    }

    start.addEventListener('change', () => {
        if (start.value) {
            end.min = start.value;

            // 이미 선택된 종료일이 시작일보다 이전이면 초기화
            if (end.value && end.value < start.value) {
                end.value = '';
            }
        } else {
            end.removeAttribute('min');
        }
    });

    /* =========================
       날짜 프리셋 필터
    ========================= */

    dateFilter.addEventListener('change', () => {
        const today = new Date();

        // 사용자 지정
        if (dateFilter.value === 'custom') {
            start.style.display = 'inline-block';
            end.style.display = 'inline-block';

            // 현재 start 기준으로 min 재적용
            if (start.value) {
                end.min = start.value;
            }

            return;
        }

        // preset 선택 시
        start.style.display = 'none';
        end.style.display = 'none';

        let from = new Date(today);

        if (dateFilter.value === 'today') {
            // today 그대로
        } else if (dateFilter.value === 'week') {
            from.setDate(today.getDate() - 7);
        } else if (dateFilter.value === 'month') {
            from.setMonth(today.getMonth() - 1);
        } else {
            // 초기화
            start.value = '';
            end.value = '';
            end.removeAttribute('min');
            return;
        }

        const fromStr = toDateString(from);
        const todayStr = toDateString(today);

        start.value = fromStr;
        end.value = todayStr;

        // ✅ preset도 동일하게 min 적용
        end.min = fromStr;
    });
}


/* =========================
   유틸
========================= */

function formatDateTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);

    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    const ss = String(d.getSeconds()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
}


function toDateString(date) {
    return date.toISOString().split('T')[0];
}

function escapeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
