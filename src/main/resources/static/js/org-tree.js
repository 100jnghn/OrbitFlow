document.addEventListener('DOMContentLoaded', () => {

    apiFetch('/api/user/organizations/tree')
        .then(res => res.json())
        .then(res => renderOrgTree(res.data))
        .catch(console.error);

    if (targetEmployeeId) {
        loadEmployeeDetail(targetEmployeeId);
    }
});

/* =========================
   Org Tree
========================= */
function renderOrgTree(nodes) {
    const container = document.getElementById('org-tree-container');
    container.innerHTML = '';
    nodes.forEach(n => renderNode(n, container, 0));
}

function renderNode(node, parent, depth) {
    const org = document.createElement('div');
    org.className = 'org-node';
    org.style.paddingLeft = `${8 + depth * 14}px`;
    org.textContent = node.orgName;
    parent.appendChild(org);

    node.employees.forEach(emp => {
        const el = document.createElement('div');
        el.className = 'org-emp';
        el.style.paddingLeft = `${24 + depth * 14}px`;
        el.textContent = emp.name;
        el.onclick = () => {
            document.querySelectorAll('.org-emp')
                .forEach(e => e.classList.remove('active'));
            el.classList.add('active');
            loadEmployeeDetail(emp.employeeId);
        };
        parent.appendChild(el);
    });

    node.children.forEach(c => renderNode(c, parent, depth + 1));
}

/* =========================
   Employee Detail
========================= */
window.loadEmployeeDetail = async function (id) {
    const panel = document.getElementById('employee-panel');
    panel.classList.remove('hidden');

    const res = await apiFetch(`/api/employees/${id}`);
    const { data: e } = await res.json();

    document.getElementById('empAvatar').src =
        e.gender === 'FEMALE' ? '/images/female.png' : '/images/male.png';

    document.getElementById('empName').textContent = e.name;
    document.getElementById('empSummary').textContent =
        [e.orgPath, e.positionName, e.rankName].filter(Boolean).join(' · ');

    document.getElementById('empEmail').textContent = e.email ?? '-';
    document.getElementById('empInternalPhone').textContent = e.internalPhone ?? '-';

    document.getElementById('empOrg').textContent = e.orgPath ?? '-';
    document.getElementById('empRank').textContent = e.rankName ?? '-';
    document.getElementById('empPosition').textContent = e.positionName ?? '-';

    const status = document.getElementById('empStatus');
    status.textContent = e.status === 'ACTIVE' ? '근무중' : e.status;
    status.className = `emp-status ${e.status.toLowerCase()}`;
};
