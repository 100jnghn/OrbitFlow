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
        const params = new URLSearchParams({
            page,
            size: 10
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
        totalPages = pageData.totalPages;
        currentPage = pageData.number;

        renderApprovalTable(approvals);
        renderPagination(currentPage);

    } catch (e) {
        console.error(e);
        alert('결재 문서를 불러오지 못했습니다.');
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
                <td colspan="7" class="approval-empty-row">
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
        const statusText = formatApprovalStatus(item.myApprovalStatus);

        row.innerHTML = `
            <td>${number}</td>
            <td class="approval-title">${escapeHTML(item.documentTitle)}</td>
            <td>${escapeHTML(item.writerName)}</td>
            <td>${escapeHTML(item.templateName)}</td>
            <td>${escapeHTML(item.displayApproverName)}</td>
            <td>${statusText}</td>
            <td>${createdAt}</td>
        `;

        row.addEventListener('click', () => {
            location.href = `/view/document/${item.documentId}`;
        });

        tbody.appendChild(row);
    });
}

/* =========================
   페이지네이션
========================= */

function renderPagination(page) {
    const pagination = document.getElementById('approvalPagination');
    pagination.innerHTML = '';

    // 이전
    const prev = document.createElement('button');
    prev.className = 'page-btn';
    prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prev.disabled = page === 0;
    prev.onclick = () => loadApprovalList(page - 1);
    pagination.appendChild(prev);

    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);

    if (end - start < maxVisible - 1) {
        start = Math.max(0, end - maxVisible + 1);
    }

    if (start > 0) {
        addPageBtn(pagination, 0);
        if (start > 1) addEllipsis(pagination);
    }

    for (let i = start; i <= end; i++) {
        addPageBtn(pagination, i, i === page);
    }

    if (end < totalPages - 1) {
        if (end < totalPages - 2) addEllipsis(pagination);
        addPageBtn(pagination, totalPages - 1);
    }

    // 다음
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

    if (!dateFilter) return;

    dateFilter.addEventListener('change', () => {
        const today = new Date();

        if (dateFilter.value === 'custom') {
            start.style.display = 'inline-block';
            end.style.display = 'inline-block';
            return;
        }

        start.style.display = 'none';
        end.style.display = 'none';

        let from = new Date(today);
        if (dateFilter.value === 'week') from.setDate(today.getDate() - 7);
        if (dateFilter.value === 'month') from.setMonth(today.getMonth() - 1);

        start.value = toDateString(from);
        end.value = toDateString(today);
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


function formatApprovalStatus(status) {
    switch (status) {
        case 'IN_PROGRESS':
            return '처리 필요(me)';
        case 'WAITING':
            return '대기중';
        case 'APPROVED':
            return '승인';
        case 'REJECTED':
            return '반려';
        default:
            return status || '';
    }
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
