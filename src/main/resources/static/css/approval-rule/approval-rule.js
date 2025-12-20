document.addEventListener('DOMContentLoaded', async () => {

    /* ===============================
       DOM 존재 여부 가드
    ================================ */
    const form = document.getElementById('approvalRuleForm');
    const approvalStepsContainer = document.getElementById('approvalStepsContainer');
    const addStepBtn = document.getElementById('addStepBtn');
    const prevBtn = document.querySelector('.prev-btn');          // ⭐ 추가
    const activateBtn = document.querySelector('.activate-btn');  // ⭐ 추가

    if (!form || !approvalStepsContainer) {
        return;
    }

    /* ===============================
       상태 변수
    ================================ */
    let steps = [];
    let organizations = [];

    const positionsByOrg = new Map();
    const employeesByKey = new Map();

    /* ===============================
       API 호출
    ================================ */

    async function fetchOrganizations() {
        try {
            const res = await apiFetch('/api/admin/org-categories');
            if (!res.ok) return [];

            const result = await res.json();
            const list = result?.data;

            if (!Array.isArray(list)) return [];

            return list
                .filter(o => o.isActive)
                .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
                .map(o => ({id: o.id, name: o.name}));

        } catch {
            return [];
        }
    }

    async function fetchPositionsByOrg(orgId) {
        if (!orgId) return [];
        if (positionsByOrg.has(orgId)) return positionsByOrg.get(orgId);

        const list = [
            {id: 1, name: '사원'},
            {id: 2, name: '대리'},
            {id: 3, name: '과장'}
        ];

        positionsByOrg.set(orgId, list);
        return list;
    }

    async function fetchEmployees(orgId, positionId) {
        if (!orgId || !positionId) return [];

        const key = `${orgId}_${positionId}`;
        if (employeesByKey.has(key)) return employeesByKey.get(key);

        const list = [
            {id: 101, name: '김철수'},
            {id: 102, name: '이영희'}
        ];

        employeesByKey.set(key, list);
        return list;
    }

    async function fetchApprovalRuleDetail() {
        const templateId = form.dataset.templateId;
        if (!templateId) return null;

        try {
            const res = await apiFetch(`/api/form-templates/${templateId}`);

            // ⭐ 1. 응답 OK 여부
            if (!res.ok) {
                console.warn('template fetch failed', res.status);
                return null;
            }

            // ⭐ 2. body 존재 여부 확인
            const text = await res.text();
            if (!text) {
                console.warn('empty response body');
                return null;
            }

            // ⭐ 3. JSON 파싱
            const result = JSON.parse(text);

            console.log('[form template response]', result);

            const template = result.data;
            if (!template) return null;

            let rule = template.approvalRuleJson ?? template.approvalRule;
            console.log('[approval rule raw]', rule);

            if (!rule) return null;

            if (typeof rule === 'string') {
                rule = JSON.parse(rule);
            }

            if (!Array.isArray(rule)) return null;

            return rule.sort((a, b) => a.step - b.step);

        } catch (e) {
            console.error('fetchApprovalRuleDetail error', e);
            return null;
        }
    }


    /* ===============================
       렌더링
    ================================ */

    function renderSteps() {
        approvalStepsContainer.innerHTML = '';

        steps.forEach((step, idx) => {
            const isFirst = idx === 0;

            const orgOptions = organizations.map(
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
                        <select class="select-org" data-idx="${idx}">
                            <option value="">조직 지정 *</option>
                            ${orgOptions}
                        </select>

                        ${!isFirst ? `
                            <select class="select-pos" data-idx="${idx}" ${!step.positions.length ? 'disabled' : ''}>
                                <option value="">직책 지정 *</option>
                                ${posOptions}
                            </select>

                            <select class="select-employee" data-idx="${idx}" ${!step.positionId ? 'disabled' : ''}>
                                <option value="">사원 지정 (선택)</option>
                                ${empOptions}
                            </select>
                        ` : ''}
                    </div>

                    ${!isFirst ? `<button class="remove-step-btn" data-idx="${idx}"></button>` : `<div></div>`}
                </div>
            `;

            approvalStepsContainer.insertAdjacentHTML('beforeend', html);
        });

        bindEvents();
    }

    /* ===============================
       이벤트 바인딩
    ================================ */

    function bindEvents() {
        document.querySelectorAll('.select-org').forEach(sel => {
            sel.onchange = async e => {
                const i = +e.target.dataset.idx;
                const orgId = +e.target.value || null;

                steps[i] = {
                    ...steps[i],
                    organizationId: orgId,
                    positionId: null,
                    employeeId: null,
                    positions: [],
                    employees: []
                };

                if (orgId) {
                    steps[i].positions = await fetchPositionsByOrg(orgId);
                }

                renderSteps();
            };
        });

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

        document.querySelectorAll('.select-employee').forEach(sel => {
            sel.onchange = e => {
                const i = +e.target.dataset.idx;
                steps[i].employeeId = +e.target.value || null;
            };
        });

        document.querySelectorAll('.remove-step-btn').forEach(btn => {
            btn.onclick = e => {
                const i = +e.target.dataset.idx;
                steps.splice(i, 1);
                renderSteps();
            };
        });
    }

    /* ===============================
       Step 추가
    ================================ */
    if (addStepBtn) {
        addStepBtn.onclick = () => {
            steps.push({
                organizationId: null,
                positionId: null,
                employeeId: null,
                positions: [],
                employees: []
            });
            renderSteps();
        };
    }

    /* ===============================
       이전 / 활성 버튼
    ================================ */
    if (prevBtn) {
        prevBtn.onclick = () => history.back();
    }

    if (activateBtn) {
        activateBtn.onclick = async () => {
            const formTemplateId = form.dataset.templateId;

            if (!formTemplateId) {
                alert('양식 ID가 없습니다.');
                return;
            }

            // 사용자 확인 (권장 UX)
            const confirmed = confirm('이 결재 양식을 활성화하시겠습니까?');
            if (!confirmed) return;

            try {
                const res = await apiFetch(
                    `/api/admin/form-templates/${formTemplateId}/publish`,
                    {
                        method: 'POST'
                    }
                );

                if (!res.ok) {
                    // body가 없을 수도 있으니 text로 안전 처리
                    const text = await res.text();
                    console.error('publish failed:', text);
                    alert('결재 양식 활성화에 실패했습니다.');
                    return;
                }

                // 성공 응답 처리
                let message = '결재 양식이 활성화되었습니다.';
                try {
                    const text = await res.text();
                    if (text) {
                        const result = JSON.parse(text);
                        message = result?.message || message;
                    }
                } catch {
                    // JSON 파싱 실패해도 무시
                }

                alert(message);

                // ✅ 성공 시 목록 페이지로 이동
                window.location.href = '/view/admin/approval';

            } catch (error) {
                console.error('publish error', error);
                alert('서버 통신 중 오류가 발생했습니다.');
            }
        };
    }


    /* ===============================
       저장
    ================================ */
    form.onsubmit = async (e) => {
        e.preventDefault();

        const formTemplateId = form.dataset.templateId;
        if (!formTemplateId) {
            alert('양식 ID가 없습니다.');
            return;
        }

        for (let i = 0; i < steps.length; i++) {
            const s = steps[i];

            if (!s.organizationId) {
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
            organizationId: s.organizationId,
            positionId: idx === 0 ? null : s.positionId,
            employeeId: idx === 0 ? null : s.employeeId
        }));

        try {
            const res = await apiFetch(
                `/api/admin/form-templates/${formTemplateId}/approval-rule`,
                {
                    method: 'PATCH',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({approvalRuleJson})
                }
            );

            if (!res.ok) {
                const err = await res.json();
                alert(err?.message || '결재선 저장에 실패했습니다.');
                return;
            }

            alert('결재선 규칙이 저장되었습니다.');

        } catch (err) {
            console.error(err);
            alert('서버 통신 중 오류가 발생했습니다.');
        }
    };

    /* ===============================
       초기화
    ================================ */

    async function init() {
        // 1️⃣ 조직 목록 먼저 로드
        organizations = await fetchOrganizations();

        // 2️⃣ templateId 기반 기존 결재선 조회
        const detail = await fetchApprovalRuleDetail();

        if (detail && detail.length) {
            steps = [];

            for (const s of detail) {
                const step = {
                    organizationId: s.organizationId ?? null,
                    positionId: s.positionId ?? null,
                    employeeId: s.employeeId ?? null,
                    positions: [],
                    employees: []
                };

                // 3️⃣ 조직이 있으면 직책 목록 로드
                if (step.organizationId) {
                    step.positions = await fetchPositionsByOrg(step.organizationId);
                }

                // 4️⃣ 직책이 있으면 사원 목록 로드
                if (step.organizationId && step.positionId) {
                    step.employees = await fetchEmployees(
                        step.organizationId,
                        step.positionId
                    );
                }

                steps.push(step);
            }
        } else {
            // 5️⃣ 기존 결재선이 없을 경우 기본 step
            steps = [{
                organizationId: null,
                positionId: null,
                employeeId: null,
                positions: [],
                employees: []
            }];
        }

        // 6️⃣ 화면 렌더링
        renderSteps();
    }

    await init();
});
