/**
 * 관리자 - 인사 감사 로그 목록
 */

/* ==========================
   Pagination State
========================== */
let currentPage = 0;
let pageSize = 10;

/* ==========================
   Init
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadAuditLogs();
});

/* ==========================
   Data Load
========================== */
async function loadAuditLogs(page = 0) {
    try {
        const res = await apiFetch(
            `/api/admin/audit-logs?page=${page}&size=${pageSize}&sort=createdAt,desc`,
            { method: 'GET' }
        );

        if (!res.ok) throw new Error();

        const result = await res.json();
        const pageData = result.data;

        const tbody = document.querySelector('.resource-table tbody');
        tbody.innerHTML = '';

        currentPage = pageData.number;
        const content = pageData.content;

        if (!content || content.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6">
                        <div class="empty-state">
                            <i class="fas fa-clipboard-list"></i>
                            <p>감사 로그가 없습니다.</p>
                        </div>
                    </td>
                </tr>
            `;
            document.getElementById('pagination-container').style.display = 'none';
            return;
        }

        document.getElementById('pagination-container').style.display = 'flex';

        // 번호 계산 (페이지 기준, 연속 번호)
        const startNumber = currentPage * pageSize;

        content.forEach((log, index) => {
            const tr = document.createElement('tr');

            tr.append(
                createCell(startNumber + index + 1),
                createCell(formatDateTime(log.createdAt)),
                createCell(log.actorName),
                createCell(log.entityDisplay),
                createCell(log.eventType),
                createDetailButton(log.id)
            );

            tbody.appendChild(tr);
        });

        renderPagination(pageData);

    } catch (e) {
        console.error(e);
        alert('인사 감사 로그 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Cell Helpers
========================== */
function createCell(value = '') {
    const td = document.createElement('td');
    td.textContent = value ?? '-';
    return td;
}

function createDetailButton(id) {
    const td = document.createElement('td');

    const btn = document.createElement('button');
    btn.className = 'btn-edit'; // 디자인 통일 (보기 버튼)
    btn.textContent = '보기';
    btn.onclick = () => openAuditLogModal(id);

    td.appendChild(btn);
    return td;
}

/* ==========================
   Pagination Render
========================== */
function renderPagination(pageData) {
    const container = document.querySelector('.pagination');
    container.innerHTML = '';

    const { number, totalPages, first, last } = pageData;

    // 이전 버튼
    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = first;
    prevBtn.onclick = () => loadAuditLogs(number - 1);
    container.appendChild(prevBtn);

    // 페이지 번호 (5개 단위)
    const startPage = Math.floor(number / 5) * 5;
    const endPage = Math.min(startPage + 5, totalPages);

    for (let i = startPage; i < endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i + 1;
        pageBtn.className = i === number ? 'active' : '';
        pageBtn.onclick = () => loadAuditLogs(i);
        container.appendChild(pageBtn);
    }

    // 다음 버튼
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = last;
    nextBtn.onclick = () => loadAuditLogs(number + 1);
    container.appendChild(nextBtn);
}

/* ==========================
   Modal (Detail)
========================== */
async function openAuditLogModal(id) {
    try {
        const res = await apiFetch(`/api/admin/audit-logs/${id}`);
        if (!res.ok) throw new Error();

        const { data: log } = await res.json();

        // 기본 정보
        setText('modal-created-at', formatDateTime(log.createdAt));
        setText('modal-actor', `${log.actorName} (${log.actorEmail})`);
        setText('modal-entity', log.entityDisplay);
        setText('modal-event', humanizeEvent(log.eventType));


        // BEFORE / AFTER
        document.getElementById('modal-before').textContent =
            formatDiff(log.beforeData, log.afterData);

        document.getElementById('modal-after').style.display = 'none';


        document.getElementById('audit-log-modal').style.display = 'flex';

    } catch (e) {
        console.error(e);
        alert('감사 로그 상세를 불러오지 못했습니다.');
    }
}

function closeAuditLogModal() {
    document.getElementById('audit-log-modal').style.display = 'none';
}

/* ==========================
   Utils
========================== */
function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value ?? '-';
}

function formatDiff(before, after) {
    if (!before && !after) return '-';

    const keys = new Set([
        ...Object.keys(before || {}),
        ...Object.keys(after || {})
    ]);

    if (keys.size === 0) return '-';

    const lines = [];

    keys.forEach(key => {
        const b = before?.[key];
        const a = after?.[key];

        if (b !== a) {
            lines.push(`${humanizeKey(key)}: ${humanizeData(b) ?? '-'} → ${humanizeData(a) ?? '-'}`);
        }
    });

    return lines.join('\n');
}

function humanizeKey(key) {
    const map = {
        name: '이름',
        isHead: '결재처리여부',
        employeeNo: '사번',
        email: '이메일',
        role: '권한',
        orgId: '조직',
        orgCategoryId: '조직카테고리',
        parentPositionId: '상위직책',
        rankId: '직급',
        positionCategoryId: '직책',
        parentRankId: '상위직급',
        orderIndex: '정렬순서',
        status: '상태',
        gender: '성별',
        hireDate: '입사일',
        employmentType: '고용형태'
    };
    return map[key] ?? key;
}


function formatDateTime(isoString) {
    if (!isoString) return '-';
    return isoString.replace('T', ' ').substring(0, 19);
}

function humanizeEvent(event) {
    const map = {
        ACTIVATE: '계정 활성화',
        DEACTIVATE: '비활성화',
        MOVE: '조직 이동',
        ASSIGN: '직급/직책 부여',
        UNASSIGN: '직급/직책 해제',
        STATUS_CHANGE: '상태 변경',
        CREATE: '생성',
        UPDATE: '정보 수정'
    };
    return map[event] ?? event;
}

function humanizeData(data) {
    const map = {
        ACTIVE: '재직',
        SUSPENDED: '휴직',
        RESIGNED: '퇴사',
        TEMP: '임시계정',

        REGULAR: '정규직',
        NON_REGULAR: '비정규직',
        CONTRACT: '계약직',


    MALE: '남성',
        FEMALE: '여성',


        ADMIN: '관리자',
        EMPLOYEE: '사원',
        COMPANY_ADMIN: '대표 관리자'
    };
    return map[data] ?? data;
}
