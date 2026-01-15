document.addEventListener('DOMContentLoaded', () => {

    const toggle = document.getElementById('extension-toggle');
    const treeEl = document.getElementById('extension-tree');

    // 사이드바 없는 페이지 보호
    if (!toggle || !treeEl) return;

    /* =========================
       기본 OPEN 페이지
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

    // 외부에서도 호출 가능
    window.reloadExtensionTree = loadExtensionTree;

    /* =========================
       Polling
    ========================= */
    loadExtensionTree(); // 최초
    setInterval(loadExtensionTree, 10000);

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

                /* 오른쪽: 근무 상태 점 (※ 여기서 1번만 생성) */
                const workDot = document.createElement('span');
                workDot.className = 'work-dot';

                applyWorkStatus(workDot, emp.workStatus, 'work-dot');

                empDiv.appendChild(leftWrap);
                empDiv.appendChild(workDot);

                empDiv.addEventListener('click', () => {
                    document.querySelectorAll('.extension-emp')
                        .forEach(el => el.classList.remove('active'));
                    empDiv.classList.add('active');

                    if (typeof window.loadEmployeeDetail === 'function') {
                        window.loadEmployeeDetail(emp.employeeId);
                    } else {
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

    /* =========================
       근무 상태 class 적용 (중복 방지 핵심)
    ========================= */
    const WORK_STATUS_CLASS = {
        WORKING: 'working',
        AWAY: 'away',
        VACATION: 'vacation',
        BUSINESS_TRIP: 'business',
        OUTWORK: 'outwork',
        OFF_WORK: 'off'
    };

    function applyWorkStatus(dotEl, workStatus, baseClass) {
        if (!dotEl) return;

        dotEl.className = baseClass;

        const cls = WORK_STATUS_CLASS[workStatus];
        if (cls) {
            dotEl.classList.add(cls);
        }
    }
});
