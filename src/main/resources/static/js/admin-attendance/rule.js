document.addEventListener('DOMContentLoaded', () => {
    // 모달 및 폼 엘리먼트
    const modal = document.getElementById('exceptionRuleModal');
    const editModal = document.getElementById('editExceptionRuleModal');
    const form = document.getElementById('exceptionRuleForm');
    const editForm = document.getElementById('editExceptionRuleForm');

    // 예외규칙 목록 전역 변수 (중복 체크용)
    let exceptionRulesList = [];

    // 초기 로드
    loadDefaultRule();
    loadExceptionRules();

    // ============================================
    // 이벤트 위임: 목록의 버튼 클릭 감지
    // ============================================
    const exceptionTableBody = document.getElementById('exceptionRulesTableBody');
    if (exceptionTableBody) {
        exceptionTableBody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.btn-edit');
            const deleteBtn = e.target.closest('.btn-delete');

            if (editBtn) {
                const ruleId = editBtn.getAttribute('data-id');
                if (ruleId) openEditModal(ruleId);
            }

            if (deleteBtn) {
                const ruleId = deleteBtn.getAttribute('data-id');
                if (ruleId) deleteExceptionRule(ruleId);
            }
        });
    }

    // ============================================
    // 입력 제한 및 글자 수 카운터 설정
    // ============================================
    const setupCharCount = (textareaId, countId) => {
        const textarea = document.getElementById(textareaId);
        const countDisplay = document.getElementById(countId);
        if (textarea && countDisplay) {
            textarea.addEventListener('input', () => {
                const count = textarea.value.length;
                countDisplay.textContent = `${count} / 40`;
                countDisplay.classList.toggle('error', count >= 40);
            });
        }
    };
    setupCharCount('reason', 'reasonCharCount');
    setupCharCount('editReason', 'editReasonCharCount');

    const setupNumericLimit = (id, max) => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('input', function () {
                this.value = this.value.replace(/[^0-9]/g, '');
                if (max && this.value && parseInt(this.value) > max) {
                    this.value = max.toString();
                }
            });
        }
    };

    setupNumericLimit('exceptionBreakMinutes', 480);
    setupNumericLimit('editExceptionBreakMinutes', 480);
    setupNumericLimit('defaultBreakMinutes', 480);

    // [추가] 예외 규칙 추가 모달: 시작일 변경 시 종료일 최소값 설정
    const validFromInput = document.getElementById('validFrom');
    const validToInput = document.getElementById('validTo');

    if (validFromInput && validToInput) {
        validFromInput.addEventListener('change', function () {
            validToInput.min = this.value;
            if (validToInput.value && validToInput.value < this.value) {
                validToInput.value = ''; // 시작일보다 이전 날짜 선택 시 초기화
            }
        });
    }

    // ============================================
    // 1. 기본 규칙 관련 (Default Rule)
    // ============================================
    async function loadDefaultRule() {
        try {
            const res = await apiFetch('/api/admin/rules/default');
            if (res.ok) {
                const response = await res.json();
                const rule = response.data;
                if (rule) {
                    if (rule.defaultStartTime) document.getElementById('defaultStartTime').value = rule.defaultStartTime.substring(0, 5);
                    if (rule.defaultEndTime) document.getElementById('defaultEndTime').value = rule.defaultEndTime.substring(0, 5);

                    document.getElementById('defaultBreakMinutes').value = rule.defaultBreakMinutes ?? 60;
                }
            }
        } catch (error) { console.error('기본 규칙 로드 실패:', error); }
    }

    document.getElementById('saveDefaultRuleBtn')?.addEventListener('click', async () => {
        const startTime = document.getElementById('defaultStartTime').value;
        const endTime = document.getElementById('defaultEndTime').value;
        if (!startTime || !endTime) {
            sweetWarning('시간을 입력해주세요.');
            return;
        }

        if (startTime >= endTime) {
            sweetWarning('퇴근 시간은 출근 시간보다 늦어야 합니다.');
            return;
        }

        const data = {
            defaultStartTime: startTime + ':00',
            defaultEndTime: endTime + ':00',

            defaultBreakMinutes: parseInt(document.getElementById('defaultBreakMinutes').value) || 60
        };

        try {
            const res = await apiFetch('/api/admin/rules/default', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (res.ok) {
                sweetSuccess('기본 규칙이 저장되었습니다.');
                await loadDefaultRule();
            } else {
                sweetError('기본 규칙 저장에 실패했습니다.');
            }
        } catch (error) { sweetError('통신 중 오류가 발생했습니다.'); }
    });

    // ============================================
    // 2. 예외 규칙 목록 (Exception Rule)
    // ============================================
    async function loadExceptionRules() {
        try {
            const res = await apiFetch('/api/admin/rules/exception');
            if (res.ok) {
                const response = await res.json();
                exceptionRulesList = response.data || [];
                renderExceptionRulesTable(exceptionRulesList);
            }
        } catch (error) { console.error('목록 로드 실패:', error); }
    }

    function renderExceptionRulesTable(rules) {
        const tbody = document.getElementById('exceptionRulesTableBody');
        if (!tbody) return;

        tbody.innerHTML = rules.length === 0
            ? '<tr><td colspan="8" style="text-align:center; padding: 20px;">등록된 예외 규칙이 없습니다.</td></tr>'
            : '';

        rules.forEach(rule => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${rule.employeeName}</strong><br><small>${rule.employeeNo || ''}</small></td>
                <td>${rule.validFrom} ~ ${rule.validTo || '무기한'}</td>
                <td>${rule.startTime?.substring(0, 5) || '-'}</td>
                <td>${rule.endTime?.substring(0, 5) || '-'}</td>
                <td>${rule.breakMinutes ? rule.breakMinutes + '분' : '-'}</td>
                <td>${rule.reason || ''}</td>
                <td>${rule.appliedAt?.substring(0, 10) || '-'}</td>
                <td>
                    <button type="button" class="btn-edit" data-id="${rule.overrideId}">수정</button>
                    <button type="button" class="btn-delete" data-id="${rule.overrideId}">삭제</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    async function openEditModal(ruleId) {
        if (!ruleId) return;
        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`);
            if (res.ok) {
                const response = await res.json();
                const rule = response.data;
                if (!rule) return;

                document.getElementById('editRuleId').value = rule.overrideId;
                document.getElementById('editEmployeeInfo').textContent = `${rule.employeeName} (${rule.employeeNo || ''})`;
                document.getElementById('editOriginalValidTo').textContent = rule.validTo || '무기한';
                document.getElementById('editValidFrom').value = rule.validFrom;
                document.getElementById('editValidTo').value = rule.validTo || '';
                // [추가] 수정 모달: 종료일 최소값 설정 (시작일 기준)
                const editValidToInput = document.getElementById('editValidTo');
                if (editValidToInput) editValidToInput.min = rule.validFrom;
                document.getElementById('editExceptionStartTime').value = rule.startTime?.substring(0, 5) || '';
                document.getElementById('editExceptionEndTime').value = rule.endTime?.substring(0, 5) || '';
                document.getElementById('editExceptionBreakMinutes').value = rule.breakMinutes || '';
                document.getElementById('editReason').value = rule.reason || '';

                if (document.getElementById('editReasonCharCount')) {
                    document.getElementById('editReasonCharCount').textContent = `${(rule.reason || '').length} / 40`;
                }

                clearAllErrors();
                if (editModal) openModal(editModal);
            }
        } catch (error) { console.error('수정 모달 로드 실패:', error); }
    }

    // ============================================
    // 추가/수정 폼 제출 및 삭제 로직
    // ============================================
    document.getElementById('addExceptionRuleBtn')?.addEventListener('click', () => {
        form.reset();
        document.getElementById('selectedEmployeeId').value = '';
        document.getElementById('selectedEmployeeInfo').style.display = 'none';
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('employeeSearchInput').value = '';
        document.getElementById('reasonCharCount').textContent = '0 / 40';
        clearAllErrors();
        openModal(modal);
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const employeeId = document.getElementById('selectedEmployeeId').value;
        const validFrom = document.getElementById('validFrom').value;
        const startTime = document.getElementById('exceptionStartTime').value;
        const endTime = document.getElementById('exceptionEndTime').value;
        const reason = document.getElementById('reason').value.trim();

        if (!employeeId || !validFrom || !startTime || !endTime || !reason) {
            sweetWarning('필수 항목을 모두 입력해주세요.');
            return;
        }

        const isDuplicate = exceptionRulesList.some(rule =>
            String(rule.employeeId) === String(employeeId)
        );

        if (isDuplicate) {
            const empInfo = document.getElementById('selectedEmployeeInfo').innerText;
            sweetWarning(`${empInfo} 사원은 이미 예외 규칙이 등록되어 있습니다.`);
            return;
        }


        const data = {
            employeeId: parseInt(employeeId),
            startTime: startTime + ':00',
            endTime: endTime + ':00',
            breakMinutes: parseInt(document.getElementById('exceptionBreakMinutes').value) || null,
            reason: reason,
            validFrom: validFrom,
            validTo: document.getElementById('validTo').value || null
        };

        try {
            const res = await apiFetch('/api/admin/rules/exception', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (res.ok) {
                sweetSuccess('예외 규칙이 추가되었습니다.');
                closeModal(modal);
                await loadExceptionRules();
            } else {
                const result = await res.json();
                sweetError(result.message || '규칙 추가에 실패했습니다.');
            }
        } catch (error) { sweetError('통신 오류가 발생했습니다.'); }
    });

    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const ruleId = document.getElementById('editRuleId').value;
        const startTime = document.getElementById('editExceptionStartTime').value;
        const endTime = document.getElementById('editExceptionEndTime').value;
        const reason = document.getElementById('editReason').value.trim();

        if (!startTime || !endTime || !reason) {
            sweetWarning('필수 항목을 모두 입력해주세요.');
            return;
        }

        const data = {
            startTime: startTime + ':00',
            endTime: endTime + ':00',
            breakMinutes: parseInt(document.getElementById('editExceptionBreakMinutes').value) || null,
            reason: reason,
            validFrom: document.getElementById('editValidFrom').value,
            validTo: document.getElementById('editValidTo').value || null,
            isActive: true
        };

        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (res.ok) {
                sweetSuccess('수정되었습니다.');
                closeModal(editModal);
                await loadExceptionRules();
            } else {
                sweetError('수정에 실패했습니다.');
            }
        } catch (error) { sweetError('통신 오류가 발생했습니다.'); }
    });

    async function deleteExceptionRule(ruleId) {
        // sweetConfirm 적용
        const resultConfirm = await sweetConfirm('삭제 확인', '정말 삭제하시겠습니까?');
        if (!resultConfirm.isConfirmed) return;

        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, { method: 'DELETE' });
            if (res.ok) {
                sweetSuccess('삭제되었습니다.');
                await loadExceptionRules();
            } else {
                sweetError('삭제에 실패했습니다.');
            }
        } catch (error) { sweetError('통신 중 오류가 발생했습니다.'); }
    }

    document.getElementById('employeeSearchBtn')?.addEventListener('click', async () => {
        const keyword = document.getElementById('employeeSearchInput').value.trim();
        if (!keyword) return;
        const res = await apiFetch(`/api/admin/employees?keyword=${encodeURIComponent(keyword)}&page=0&size=100`);
        if (res.ok) {
            const response = await res.json();
            displayEmployeeResults(response.data?.content || []);
        }
    });

    function displayEmployeeResults(employees) {
        const resultsDiv = document.getElementById('employeeSearchResults');
        resultsDiv.innerHTML = '';
        const ul = document.createElement('ul');
        ul.className = 'employee-list';
        employees.forEach(emp => {
            const li = document.createElement('li');
            li.className = 'employee-item';
            li.innerHTML = `<div class=\"employee-name\">${emp.name}</div><div class=\"employee-details\">${emp.orgPath || ''}</div>`;
            li.onclick = () => {
                document.getElementById('selectedEmployeeId').value = emp.id;
                document.getElementById('selectedEmployeeInfo').innerHTML = `<strong>${emp.name}</strong> (${emp.orgPath || ''})`;
                document.getElementById('selectedEmployeeInfo').style.display = 'block';
                resultsDiv.innerHTML = '';
            };
            ul.appendChild(li);
        });
        resultsDiv.appendChild(ul);
    }

    function openModal(m) { if (m) { m.style.display = 'flex'; document.body.style.overflow = 'hidden'; } }
    function closeModal(m) { if (m) { m.style.display = 'none'; document.body.style.overflow = ''; } }
    function clearAllErrors() { document.querySelectorAll('.error-message').forEach(el => { el.style.display = 'none'; }); }

    document.querySelectorAll('.close').forEach(btn => btn.onclick = () => { closeModal(modal); closeModal(editModal); });
    document.querySelectorAll('.close-modal').forEach(btn => btn.addEventListener('click', () => { closeModal(modal); closeModal(editModal); }));
    window.onclick = (event) => { if (event.target == modal) closeModal(modal); if (event.target == editModal) closeModal(editModal); };
});