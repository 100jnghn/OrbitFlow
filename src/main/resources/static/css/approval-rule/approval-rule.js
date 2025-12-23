document.addEventListener('DOMContentLoaded', async () => {

    /* ===============================
       DOM Guard
    ================================ */
    const form = document.getElementById('approvalRuleForm');
    const approvalStepsContainer = document.getElementById('approvalStepsContainer');
    const addStepBtn = document.getElementById('addStepBtn');
    const prevBtn = document.querySelector('.prev-btn');
    const activateBtn = document.querySelector('.activate-btn');

    if (!form || !approvalStepsContainer) return;

    /* ===============================
       State
    ================================ */
    let steps = [];
    let organizations = []; // 조직 카테고리

    const positionsByOrg = new Map();
    const employeesByKey = new Map();

    /* ===============================
       API
    ================================ */

    // 조직 카테고리
    async function fetchOrganizations() {
        try {
            const res = await apiFetch('/api/admin/org-categories');
            if (!res.ok) return [];
            const result = await res.json();
            return (result?.data || [])
                .filter(o => o.isActive)
                .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
                .map(o => ({id: o.id, name: o.name}));
        } catch {
            return [];
        }
    }

    // 카테고리 → 조직
    async function fetchOrganizationsByCategory(categoryId) {
        if (!categoryId) return [];
        const res = await apiFetch(`/api/admin/organizations/by-category/${categoryId}`);
        if (!res.ok) return [];
        const result = await res.json();
        return Array.isArray(result?.data) ? result.data : [];
    }

    // 조직 → 직책
    async function fetchPositionsByOrg(orgCategoryId) {
        if (!orgCategoryId) return [];

        // 캐시 체크
        if (positionsByOrg.has(orgCategoryId)) {
            return positionsByOrg.get(orgCategoryId);
        }

        try {
            const res = await apiFetch(
                `/api/admin/org-position-policies/${orgCategoryId}`
            );

            if (!res.ok) {
                console.warn('fetchPositionsByOrg failed', res.status);
                return [];
            }

            const result = await res.json();
            const list = Array.isArray(result?.data)
                ? result.data.map(p => ({
                    id: p.positionCategoryId,
                    name: p.positionCategoryName
                }))
                : [];

            // 캐싱
            positionsByOrg.set(orgCategoryId, list);
            return list;

        } catch (e) {
            console.error('fetchPositionsByOrg error', e);
            return [];
        }
    }


    // 조직 + 직책 → 사원
    async function fetchEmployees(orgId, positionId) {
        if (!orgId || !positionId) return [];
        const key = `${orgId}_${positionId}`;
        if (employeesByKey.has(key)) return employeesByKey.get(key);

        // TODO 실제 API로 교체
        const list = [
            {id: 101, name: '김철수'},
            {id: 102, name: '이영희'}
        ];
        employeesByKey.set(key, list);
        return list;
    }

    // 기존 결재선 조회
    async function fetchApprovalRuleDetail() {
        const templateId = form.dataset.templateId;
        if (!templateId) return null;

        try {
            const res = await apiFetch(`/api/form-templates/${templateId}`);
            if (!res.ok) return null;

            const text = await res.text();
            if (!text) return null;

            const result = JSON.parse(text);
            let rule =
                result?.data?.approvalRuleJson ??
                result?.data?.approvalRule;

            if (!rule) return null;
            if (typeof rule === 'string') rule = JSON.parse(rule);
            if (!Array.isArray(rule)) return null;

            return rule.sort((a, b) => a.step - b.step);
        } catch {
            return null;
        }
    }

    /* ===============================
       Render
    ================================ */

    function renderSteps() {
        approvalStepsContainer.innerHTML = '';

        steps.forEach((step, idx) => {
            const isFirst = idx === 0;

            const categoryOptions = organizations.map(
                c => `<option value="${c.id}" ${step.organizationCategoryId === c.id ? 'selected' : ''}>${c.name}</option>`
            ).join('');

            const orgOptions = (step.organizations || []).map(
                o => `<option value="${o.id}" ${step.organizationId === o.id ? 'selected' : ''}>${o.name}</option>`
            ).join('');

            const posOptions = (step.positions || []).map(
                p => `<option value="${p.id}" ${step.positionId === p.id ? 'selected' : ''}>${p.name}</option>`
            ).join('');

            const empOptions = (step.employees || []).map(
                e => `<option value="${e.id}" ${step.employeeId === e.id ? 'selected' : ''}>${e.name}</option>`
            ).join('');

            const html = `
                <div class="approval-step">
                    <div class="approval-step-label">Step ${idx + 1}</div>

                    <div class="approval-select-group">
                        ${!isFirst ? `
                            <select class="select-org-category" data-idx="${idx}">
                                <option value="">조직 카테고리 *</option>
                                ${categoryOptions}
                            </select>

                            <select class="select-org" data-idx="${idx}" ${!step.organizations.length ? 'disabled' : ''}>
                                <option value="">조직 *</option>
                                ${orgOptions}
                            </select>

                            <select class="select-pos" data-idx="${idx}" ${!step.positions.length ? 'disabled' : ''}>
                                <option value="">직책 *</option>
                                ${posOptions}
                            </select>

                            <select class="select-employee" data-idx="${idx}" ${!step.positionId ? 'disabled' : ''}>
                                <option value="">사원 (선택)</option>
                                ${empOptions}
                            </select>
                        ` : `
                            <select class="select-org-category" data-idx="${idx}">
                                <option value="">조직 카테고리 *</option>
                                ${categoryOptions}
                            </select>
                        `}
                    </div>

                    ${!isFirst ? `<button class="remove-step-btn" data-idx="${idx}"></button>` : `<div></div>`}
                </div>
            `;

            approvalStepsContainer.insertAdjacentHTML('beforeend', html);
        });

        bindEvents();
    }

    /* ===============================
       Events
    ================================ */

    function bindEvents() {

        // 조직 카테고리
        document.querySelectorAll('.select-org-category').forEach(sel => {
            sel.onchange = async e => {
                const i = +e.target.dataset.idx;
                const categoryId = +e.target.value || null;

                steps[i] = {
                    ...steps[i],
                    organizationCategoryId: categoryId,
                    organizationId: null,
                    positionId: null,
                    employeeId: null,
                    organizations: [],
                    positions: [],
                    employees: []
                };

                if (categoryId) {
                    steps[i].organizations = await fetchOrganizationsByCategory(categoryId);
                }

                renderSteps();
            };
        });

        // 조직
        document.querySelectorAll('.select-org').forEach(sel => {
            sel.onchange = async e => {
                const i = +e.target.dataset.idx;
                const orgId = +e.target.value || null;

                steps[i].organizationId = orgId;
                steps[i].positionId = null;
                steps[i].employeeId = null;
                steps[i].positions = [];
                steps[i].employees = [];

                if (orgId) {
                    steps[i].positions = await fetchPositionsByOrg(orgId);
                }

                renderSteps();
            };
        });

        // 직책
        document.querySelectorAll('.select-pos').forEach(sel => {
            sel.onchange = async e => {
                const i = +e.target.dataset.idx;
                const posId = +e.target.value || null;

                steps[i].positionId = posId;
                steps[i].employeeId = null;
                steps[i].employees = [];

                if (posId) {
                    steps[i].employees = await fetchEmployees(
                        steps[i].organizationId,
                        posId
                    );
                }

                renderSteps();
            };
        });

        // 사원
        document.querySelectorAll('.select-employee').forEach(sel => {
            sel.onchange = e => {
                const i = +e.target.dataset.idx;
                steps[i].employeeId = +e.target.value || null;
            };
        });

        // step 삭제
        document.querySelectorAll('.remove-step-btn').forEach(btn => {
            btn.onclick = e => {
                const i = +e.target.dataset.idx;
                steps.splice(i, 1);
                renderSteps();
            };
        });
    }

    /* ===============================
       Step Add
    ================================ */
    if (addStepBtn) {
        addStepBtn.onclick = () => {
            steps.push({
                organizationCategoryId: null,
                organizationId: null,
                positionId: null,
                employeeId: null,
                organizations: [],
                positions: [],
                employees: []
            });
            renderSteps();
        };
    }

    /* ===============================
       Prev / Activate
    ================================ */

    if (prevBtn) {
        prevBtn.onclick = () => history.back();
    }

    if (activateBtn) {
        activateBtn.onclick = async () => {
            const formTemplateId = form.dataset.templateId;
            if (!formTemplateId) return;

            if (!confirm('이 결재 양식을 활성화하시겠습니까?')) return;

            const res = await apiFetch(
                `/api/admin/form-templates/${formTemplateId}/publish`,
                {method: 'POST'}
            );

            if (!res.ok) {
                alert('결재 양식 활성화에 실패했습니다.');
                return;
            }

            alert('결재 양식이 활성화되었습니다.');
            window.location.href = '/view/admin/approval';
        };
    }

    /* ===============================
       Save
    ================================ */
    form.onsubmit = async e => {
        e.preventDefault();

        const formTemplateId = form.dataset.templateId;
        if (!formTemplateId) return;

        for (let i = 0; i < steps.length; i++) {
            const s = steps[i];

            if (!s.organizationCategoryId) {
                alert(`Step ${i + 1}의 조직 카테고리를 선택해주세요.`);
                return;
            }
            if (i > 0 && !s.organizationId) {
                alert(`Step ${i + 1}의 조직을 선택해주세요.`);
                return;
            }
            if (i > 0 && !s.positionId) {
                alert(`Step ${i + 1}의 직책을 선택해주세요.`);
                return;
            }
        }

        const approvalRuleJson = steps.map((s, idx) => ({
            step: idx + 1,
            organizationCategoryId: s.organizationCategoryId,
            organizationId: s.organizationId,
            positionId: idx === 0 ? null : s.positionId,
            employeeId: idx === 0 ? null : s.employeeId
        }));

        await apiFetch(
            `/api/admin/form-templates/${formTemplateId}/approval-rule`,
            {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({approvalRuleJson})
            }
        );

        alert('결재선 규칙이 저장되었습니다.');
    };

    /* ===============================
       Init
    ================================ */

    async function init() {
        organizations = await fetchOrganizations();
        const detail = await fetchApprovalRuleDetail();

        if (detail?.length) {
            for (const s of detail) {
                const step = {
                    organizationCategoryId: s.organizationCategoryId ?? null,
                    organizationId: s.organizationId ?? null,
                    positionId: s.positionId ?? null,
                    employeeId: s.employeeId ?? null,
                    organizations: [],
                    positions: [],
                    employees: []
                };

                if (step.organizationCategoryId) {
                    step.organizations = await fetchOrganizationsByCategory(step.organizationCategoryId);
                }
                if (step.organizationId) {
                    step.positions = await fetchPositionsByOrg(step.organizationId);
                }
                if (step.organizationId && step.positionId) {
                    step.employees = await fetchEmployees(step.organizationId, step.positionId);
                }

                steps.push(step);
            }
        } else {
            steps = [{
                organizationCategoryId: null,
                organizationId: null,
                positionId: null,
                employeeId: null,
                organizations: [],
                positions: [],
                employees: []
            }];
        }

        renderSteps();
    }

    await init();
});
