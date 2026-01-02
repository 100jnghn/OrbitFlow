document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('exceptionRuleModal');
    const editModal = document.getElementById('editExceptionRuleModal');
    const form = document.getElementById('exceptionRuleForm');
    const editForm = document.getElementById('editExceptionRuleForm');

    // 예외 규칙 목록 저장 (중복 체크 및 화면 갱신용)
    let exceptionRulesList = [];

    // 초기화 함수 실행
    loadDefaultRule();
    loadExceptionRules();

    // ============================================
    // 1. 기본 규칙 관련 (Default Rule)
    // ============================================
    async function loadDefaultRule() {
        try {
            const res = await apiFetch('/api/admin/rules/default');
            if (res.ok) {
                const rule = await res.json();
                if (rule.defaultStartTime) {
                    document.getElementById('defaultStartTime').value = rule.defaultStartTime.substring(0, 5);
                }
                if (rule.defaultEndTime) {
                    document.getElementById('defaultEndTime').value = rule.defaultEndTime.substring(0, 5);
                }
                if (rule.lateThresholdMin !== null && rule.lateThresholdMin !== undefined) {
                    document.getElementById('tardinessMinutes').value = rule.lateThresholdMin;
                }
            }
        } catch (error) {
            console.error('기본 규칙 로드 실패:', error);
        }
    }

    document.getElementById('saveDefaultRuleBtn').addEventListener('click', async () => {
        try {
            const data = {
                defaultStartTime: document.getElementById('defaultStartTime').value + ":00",
                defaultEndTime: document.getElementById('defaultEndTime').value + ":00",
                lateThresholdMin: parseInt(document.getElementById('tardinessMinutes').value)
            };

            const res = await apiFetch('/api/admin/rules/default', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('기본 규칙이 저장되었습니다.');
                loadDefaultRule();
            } else {
                const error = await res.json();
                alert('저장 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('기본 규칙 저장 실패:', error);
            alert('저장 중 오류가 발생했습니다.');
        }
    });

    // ============================================
    // 2. 예외 규칙 목록 조회 및 렌더링
    // ============================================
    async function loadExceptionRules() {
        try {
            const res = await apiFetch('/api/admin/rules/exception');
            if (res.ok) {
                const rules = await res.json();
                exceptionRulesList = rules;
                renderExceptionRulesTable(rules);
            }
        } catch (error) {
            console.error('예외 규칙 목록 로드 실패:', error);
        }
    }

    function renderExceptionRulesTable(rules) {
        const tbody = document.getElementById('exceptionRulesTableBody');
        tbody.innerHTML = '';

        if (rules.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px;">등록된 예외 규칙이 없습니다.</td></tr>';
            return;
        }

        rules.forEach(rule => {
            const row = document.createElement('tr');
            const startTime = rule.startTime ? rule.startTime.substring(0, 5) : 'NULL';
            const endTime = rule.endTime ? rule.endTime.substring(0, 5) : 'NULL';
            const breakMinutes = rule.breakMinutes !== null ? rule.breakMinutes + '분' : 'NULL';
            const validFrom = rule.validFrom || '';
            const validTo = rule.validTo || '무기한';
            const appliedAt = rule.appliedAt ? new Date(rule.appliedAt).toLocaleDateString('ko-KR') : '';

            row.innerHTML = `
                <td>${rule.employeeName || '알 수 없음'}</td>
                <td>${validFrom} ~ ${validTo}</td>
                <td>${startTime}</td>
                <td>${endTime}</td>
                <td>${breakMinutes}</td>
                <td>${rule.reason || ''}</td>
                <td>${appliedAt}</td>
                <td>
                    <button class="btn-edit" data-id="${rule.overrideId}">수정</button>
                    <button class="btn-delete" data-id="${rule.overrideId}">삭제</button>
                </td>
            `;
            tbody.appendChild(row);
        });

        // 버튼 이벤트 바인딩
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.onclick = () => openEditModal(btn.dataset.id);
        });
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.onclick = () => deleteExceptionRule(btn.dataset.id);
        });
    }

    // ============================================
    // 3. 예외 규칙 추가 로직
    // ============================================
    const addBtn = document.getElementById('addExceptionRuleBtn');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            form.reset();
            document.getElementById('selectedEmployeeId').value = '';
            document.getElementById('selectedEmployeeInfo').style.display = 'none';
            document.getElementById('employeeSearchResults').innerHTML = '';
            document.getElementById('employeeSearchInput').value = '';
            clearAllErrors();
            updateCharCount('reason', 'reasonCharCount', 40);
            openModal(modal);
        });
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validateExceptionRuleForm()) return;

        try {
            const data = {
                employeeId: parseInt(document.getElementById('selectedEmployeeId').value),
                startTime: document.getElementById('exceptionStartTime').value ? document.getElementById('exceptionStartTime').value + ":00" : null,
                endTime: document.getElementById('exceptionEndTime').value ? document.getElementById('exceptionEndTime').value + ":00" : null,
                breakMinutes: document.getElementById('exceptionBreakMinutes').value ? parseInt(document.getElementById('exceptionBreakMinutes').value) : null,
                reason: document.getElementById('reason').value.trim(),
                validFrom: document.getElementById('validFrom').value,
                validTo: document.getElementById('validTo').value || null
            };

            const res = await apiFetch('/api/admin/rules/exception', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('예외 규칙이 추가되었습니다.');
                closeModal(modal);
                await loadExceptionRules();
            } else {
                const error = await res.json();
                alert('추가 실패: ' + (error.message || '오류 발생'));
            }
        } catch (error) {
            console.error('추가 실패:', error);
        }
    });

    // ============================================
    // 4. 예외 규칙 수정 로직 (핵심 수정 부분)
    // ============================================
    async function openEditModal(ruleId) {
        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`);
            if (res.ok) {
                const rule = await res.json();
                populateEditForm(rule);
                openModal(editModal);
            }
        } catch (error) {
            console.error('데이터 로드 실패:', error);
        }
    }

    function populateEditForm(rule) {
        clearAllErrors();
        document.getElementById('editRuleId').value = rule.overrideId;
        document.getElementById('editEmployeeInfo').textContent = `${rule.employeeName} (${rule.employeeNo})`;
        document.getElementById('editValidFrom').value = rule.validFrom || '';
        document.getElementById('editValidTo').value = rule.validTo || '';
        document.getElementById('editExceptionStartTime').value = rule.startTime ? rule.startTime.substring(0, 5) : '';
        document.getElementById('editExceptionEndTime').value = rule.endTime ? rule.endTime.substring(0, 5) : '';
        document.getElementById('editExceptionBreakMinutes').value = rule.breakMinutes || '';
        document.getElementById('editReason').value = rule.reason || '';
        updateCharCount('editReason', 'editReasonCharCount', 40);
    }

    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validateEditExceptionRuleForm()) return;

        try {
            const ruleId = document.getElementById('editRuleId').value;
            const data = {
                startTime: document.getElementById('editExceptionStartTime').value ? document.getElementById('editExceptionStartTime').value + ":00" : null,
                endTime: document.getElementById('editExceptionEndTime').value ? document.getElementById('editExceptionEndTime').value + ":00" : null,
                breakMinutes: document.getElementById('editExceptionBreakMinutes').value ? parseInt(document.getElementById('editExceptionBreakMinutes').value) : null,
                reason: document.getElementById('editReason').value.trim(),
                validFrom: document.getElementById('editValidFrom').value,
                validTo: document.getElementById('editValidTo').value || null,
                isActive: true
            };

            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('예외 규칙이 수정되었습니다.');
                closeModal(editModal);
                await loadExceptionRules(); // 서버에서 최신 목록 다시 로드하여 화면 갱신
            } else {
                const error = await res.json();
                alert('수정 실패: ' + (error.message || '오류 발생'));
            }
        } catch (error) {
            console.error('수정 실패:', error);
        }
    });

    // ============================================
    // 5. 삭제 및 공통 유틸리티
    // ============================================
    async function deleteExceptionRule(ruleId) {
        if (!confirm('정말 삭제하시겠습니까?')) return;
        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, { method: 'DELETE' });
            if (res.ok) {
                alert('삭제되었습니다.');
                await loadExceptionRules();
            }
        } catch (error) {
            console.error('삭제 실패:', error);
        }
    }

    // 사원 검색 로직
    document.getElementById('employeeSearchBtn')?.addEventListener('click', searchEmployee);
    async function searchEmployee() {
        const keyword = document.getElementById('employeeSearchInput').value.trim();
        if (!keyword) return alert('검색어를 입력하세요.');
        try {
            const res = await apiFetch(`/api/employees/search?keyword=${encodeURIComponent(keyword)}`);
            if (res.ok) {
                const emps = await res.json();
                const resultsDiv = document.getElementById('employeeSearchResults');
                resultsDiv.innerHTML = '';
                if (emps.length === 0) { resultsDiv.innerHTML = '<div>결과 없음</div>'; return; }
                const ul = document.createElement('ul');
                ul.className = 'employee-list';
                emps.forEach(emp => {
                    const li = document.createElement('li');
                    li.innerHTML = `${emp.name} (${emp.organizationName})`;
                    li.onclick = () => {
                        document.getElementById('selectedEmployeeId').value = emp.id;
                        document.getElementById('selectedEmployeeInfo').innerHTML = `선택됨: ${emp.name}`;
                        document.getElementById('selectedEmployeeInfo').style.display = 'block';
                        resultsDiv.innerHTML = '';
                    };
                    ul.appendChild(li);
                });
                resultsDiv.appendChild(ul);
            }
        } catch (e) { console.error(e); }
    }

    // 모달 및 UI 유틸리티
    function openModal(m) { m.style.display = 'block'; document.body.style.overflow = 'hidden'; }
    function closeModal(m) { m.style.display = 'none'; document.body.style.overflow = ''; }

    function updateCharCount(inputId, countId, max) {
        const input = document.getElementById(inputId);
        const count = document.getElementById(countId);
        if (input && count) count.textContent = `${input.value.length} / ${max}`;
    }

    function clearAllErrors() {
        document.querySelectorAll('.error-message').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.error').forEach(el => el.classList.remove('error'));
    }

    function showError(id, msg) {
        const errEl = document.getElementById(id);
        if (errEl) { errEl.textContent = msg; errEl.style.display = 'block'; }
    }

    // 유효성 검사 (기본/수정용)
    function validateExceptionRuleForm() {
        clearAllErrors();
        let valid = true;
        if (!document.getElementById('selectedEmployeeId').value) { showError('employeeError', '사원을 선택하세요.'); valid = false; }
        if (!document.getElementById('validFrom').value) { showError('validFromError', '시작일을 선택하세요.'); valid = false; }
        if (!document.getElementById('reason').value.trim()) { showError('reasonError', '사유를 입력하세요.'); valid = false; }
        return valid;
    }

    function validateEditExceptionRuleForm() {
        clearAllErrors();
        let valid = true;
        const reason = document.getElementById('editReason').value.trim();
        if (!reason) { showError('editReasonError', '수정 사유를 입력하세요.'); valid = false; }
        if (reason.length > 40) { showError('editReasonError', '사유는 40자 이내입니다.'); valid = false; }
        return valid;
    }

    // 닫기 버튼 이벤트
    document.querySelectorAll('.close, .cancel-btn').forEach(btn => {
        btn.onclick = () => { closeModal(modal); closeModal(editModal); };
    });

    window.onclick = (e) => { if (e.target == modal) closeModal(modal); if (e.target == editModal) closeModal(editModal); };
});