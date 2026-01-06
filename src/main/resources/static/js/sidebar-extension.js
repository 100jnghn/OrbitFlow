/* =========================
   Work Status Dot
========================= */
function createWorkStatusDot(workStatus) {
    const dot = document.createElement('span');
    dot.className = `work-dot ${workStatus?.toLowerCase() ?? 'unknown'}`;
    dot.textContent = '●';
    return dot;
}

document.addEventListener('DOMContentLoaded', () => {

    const toggle = document.getElementById('extension-toggle');
    const treeEl = document.getElementById('extension-tree');

    // 사이드바 없는 페이지 보호
    if (!toggle || !treeEl) return;

    /* =========================
       메인 / 조직도 페이지 기본 OPEN
    ========================= */
    const isOrganizationPage =
        location.pathname.startsWith('/view/organizations');

    const isMainPage =
        location.pathname === '/';

    if (isOrganizationPage || isMainPage) {
        openExtensionTree();
    }

    function openExtensionTree() {
        treeEl.classList.remove('hidden');

        const icon = toggle.querySelector('i');
        if (icon) {
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        }
    }

    /* =========================
       내선번호 토글
    ========================= */
    toggle.addEventListener('click', () => {
        treeEl.classList.toggle('hidden');

        const icon = toggle.querySelector('i');
        if (!icon) return;

        icon.classList.toggle('fa-chevron-up');
        icon.classList.toggle('fa-chevron-down');
    });

    /* =========================
       내선번호 트리 로딩
    ========================= */
    function loadExtensionTree() {
        apiFetch('/api/sidebar/extensions')
            .then(res => {
                if (!res.ok) throw new Error('내선번호 API 실패');
                return res.json();
            })
            .then(res => {
                treeEl.innerHTML = '';
                renderTree(res.data);
            })
            .catch(err => console.error('내선번호 조회 실패', err));
    }

    // 🔹 외부에서도 호출 가능 (출근/퇴근 직후 즉시 반영용)
    window.reloadExtensionTree = loadExtensionTree;

    /* =========================
       Polling (10초)
    ========================= */
    loadExtensionTree(); // 최초 1회

    setInterval(() => {
        loadExtensionTree();
    }, 10000);

    /* =========================
       트리 렌더링
    ========================= */
    function renderTree(nodes, depth = 0) {
        nodes.forEach(org => {

            /* ---- 조직 ---- */
            const orgDiv = document.createElement('div');
            orgDiv.className = 'extension-org';
            orgDiv.style.paddingLeft = `${14 + depth * 12}px`;
            orgDiv.textContent = org.orgName;
            treeEl.appendChild(orgDiv);

            /* ---- 사원 ---- */
            (org.employees || []).forEach(emp => {
                const empDiv = document.createElement('div');
                empDiv.className = 'extension-emp';
                empDiv.style.paddingLeft = `${28 + depth * 12}px`;

                /* 왼쪽: 이름 + 내선번호 */
                const leftWrap = document.createElement('div');
                leftWrap.className = 'emp-left';

                const nameSpan = document.createElement('span');
                nameSpan.className = 'name';
                nameSpan.textContent = emp.name;

                const phoneSpan = document.createElement('span');
                phoneSpan.className = 'phone';
                phoneSpan.textContent = emp.internalPhone ?? '-';

                leftWrap.appendChild(nameSpan);
                leftWrap.appendChild(phoneSpan);

                /* 오른쪽: 근무 상태 점 */
                const workDot = createWorkStatusDot(emp.workStatus);

                empDiv.appendChild(leftWrap);
                empDiv.appendChild(workDot);

                empDiv.addEventListener('click', () => {
                    document.querySelectorAll('.extension-emp')
                        .forEach(el => el.classList.remove('active'));
                    empDiv.classList.add('active');

                    // 조직도 페이지면 → 우측 패널만 갱신
                    if (typeof window.loadEmployeeDetail === 'function') {
                        window.loadEmployeeDetail(emp.employeeId);
                    }
                    // 그 외 페이지면 → 조직도 페이지로 이동
                    else {
                        location.href = `/view/organizations?employeeId=${emp.employeeId}`;
                    }
                });

                treeEl.appendChild(empDiv);
            });

            /* ---- 하위 조직 ---- */
            if (org.children && org.children.length > 0) {
                renderTree(org.children, depth + 1);
            }
        });
    }
});
