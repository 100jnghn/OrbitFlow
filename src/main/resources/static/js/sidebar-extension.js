document.addEventListener('DOMContentLoaded', () => {

    const toggle = document.getElementById('extension-toggle');
    const treeEl = document.getElementById('extension-tree');

    // 사이드바가 없는 페이지에서는 실행 안 함
    if (!toggle || !treeEl) return;

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
    apiFetch('/api/sidebar/extensions')
        .then(res => {
            if (!res.ok) throw new Error('내선번호 API 실패');
            return res.json();
        })
        .then(res => {
            treeEl.innerHTML = ''; // 재렌더링 대비
            renderTree(res.data);
        })
        .catch(err => console.error('내선번호 조회 실패', err));


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
                empDiv.innerHTML = `
                    <span class="name">${emp.name}</span>
                    <span class="phone">${emp.internalPhone ?? '-'}</span>
                `;

                empDiv.addEventListener('click', () => {
                    // 선택 강조
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
