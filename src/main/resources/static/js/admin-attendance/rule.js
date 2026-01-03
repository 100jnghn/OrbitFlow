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
    // 예외규칙 추가 버튼 이벤트
    // ============================================
    document.getElementById('addExceptionRuleBtn').addEventListener('click', () => {
        // 폼 초기화
        form.reset();
        document.getElementById('selectedEmployeeId').value = '';
        document.getElementById('selectedEmployeeInfo').style.display = 'none';
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('employeeSearchInput').value = '';
        document.getElementById('reasonCharCount').textContent = '0 / 40';
        clearAllErrors();
        openModal(modal);
    });

    // ============================================
    // 사유 글자 수 카운터
    // ============================================
    const reasonTextarea = document.getElementById('reason');
    const reasonCharCount = document.getElementById('reasonCharCount');
    if (reasonTextarea && reasonCharCount) {
        reasonTextarea.addEventListener('input', () => {
            const count = reasonTextarea.value.length;
            reasonCharCount.textContent = `${count} / 40`;
            if (count >= 40) {
                reasonCharCount.classList.add('error');
            } else {
                reasonCharCount.classList.remove('error');
            }
        });
    }

    const editReasonTextarea = document.getElementById('editReason');
    const editReasonCharCount = document.getElementById('editReasonCharCount');
    if (editReasonTextarea && editReasonCharCount) {
        editReasonTextarea.addEventListener('input', () => {
            const count = editReasonTextarea.value.length;
            editReasonCharCount.textContent = `${count} / 40`;
            if (count >= 40) {
                editReasonCharCount.classList.add('error');
            } else {
                editReasonCharCount.classList.remove('error');
            }
        });
    }

    // ============================================
    // 휴게시간 숫자만 입력 제한
    // ============================================
    document.getElementById('exceptionBreakMinutes')?.addEventListener('input', function(e) {
        this.value = this.value.replace(/[^0-9]/g, '');
        if (this.value && parseInt(this.value) > 480) {
            this.value = '480';
        }
    });

    document.getElementById('editExceptionBreakMinutes')?.addEventListener('input', function(e) {
        this.value = this.value.replace(/[^0-9]/g, '');
        if (this.value && parseInt(this.value) > 480) {
            this.value = '480';
        }
    });

    // ============================================
    // 예외규칙 추가 폼 제출
    // ============================================
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAllErrors();
        
        const employeeId = document.getElementById('selectedEmployeeId').value;
        const validFrom = document.getElementById('validFrom').value;
        const validTo = document.getElementById('validTo').value;
        const startTime = document.getElementById('exceptionStartTime').value;
        const endTime = document.getElementById('exceptionEndTime').value;
        const breakMinutes = document.getElementById('exceptionBreakMinutes').value;
        const reason = document.getElementById('reason').value.trim();

        // 유효성 검사
        const errors = [];
        
        if (!employeeId) {
            errors.push('대상 사원을 선택해주세요.');
        } else {
            // 이미 등록된 사원인지 재확인
            const existingRule = exceptionRulesList.find(rule => rule.employeeId === parseInt(employeeId));
            if (existingRule) {
                errors.push(`해당 사원은 이미 예외규칙이 등록되어 있습니다.`);
            }
        }
        if (!validFrom) {
            errors.push('적용 시작일을 입력해주세요.');
        }
        if (!reason) {
            errors.push('규칙 적용 사유를 입력해주세요.');
        } else if (reason.length > 40) {
            errors.push('사유는 40자 이하여야 합니다.');
        }

        // 출근시간과 퇴근시간 유효성 검사
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            if (start >= end) {
                errors.push('출근 시간은 퇴근 시간보다 빨라야 합니다.');
            }
        }

        // 휴게시간 유효성 검사
        if (breakMinutes && (isNaN(breakMinutes) || parseInt(breakMinutes) < 0 || parseInt(breakMinutes) > 480)) {
            errors.push('휴게 시간은 0~480분 사이의 숫자만 입력 가능합니다.');
        }

        // 에러가 있으면 팝업으로 표시
        if (errors.length > 0) {
            alert(errors.join('\n'));
            return;
        }

        try {
            const data = {
                employeeId: parseInt(employeeId),
                startTime: startTime ? startTime + ':00' : null,
                endTime: endTime ? endTime + ':00' : null,
                breakMinutes: breakMinutes ? parseInt(breakMinutes) : null,
                reason: reason,
                validFrom: validFrom,
                validTo: validTo || null
            };

            const res = await apiFetch('/api/admin/rules/exception', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await res.json();
            if (res.ok) {
                alert(result.message || '예외 규칙이 추가되었습니다.');
                closeModal(modal);
                await loadExceptionRules(); // 목록 새로고침하여 전역 변수 업데이트
            } else {
                alert(result.message || '추가 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('추가 실패:', error);
            alert('추가 중 오류가 발생했습니다.');
        }
    });

    // ============================================
    // 1. 기본 규칙 관련 (Default Rule)
    // ============================================
    async function loadDefaultRule() {
        try {
            const res = await apiFetch('/api/admin/rules/default');
            if (res.ok) {
                const response = await res.json();
                const rule = response.data; // ResponseDto 구조

                if (rule && rule.defaultStartTime) {
                    document.getElementById('defaultStartTime').value = rule.defaultStartTime.substring(0, 5);
                }
                if (rule && rule.defaultEndTime) {
                    document.getElementById('defaultEndTime').value = rule.defaultEndTime.substring(0, 5);
                }
                if (rule && rule.defaultBreakMinutes !== null && rule.defaultBreakMinutes !== undefined) {
                    document.getElementById('tardinessMinutes').value = rule.defaultBreakMinutes;
                }
            }
        } catch (error) {
            console.error('기본 규칙 로드 실패:', error);
        }
    }

    // 기본 규칙 저장 버튼
    document.getElementById('saveDefaultRuleBtn').addEventListener('click', async () => {
        const startTime = document.getElementById('defaultStartTime').value;
        const endTime = document.getElementById('defaultEndTime').value;
        const breakMin = document.getElementById('tardinessMinutes').value;

        if (!startTime || !endTime) {
            alert('시간을 입력해주세요.');
            return;
        }

        const data = {
            defaultStartTime: startTime + ':00',
            defaultEndTime: endTime + ':00',
            defaultBreakMinutes: breakMin ? parseInt(breakMin) : null
        };

        const res = await apiFetch('/api/admin/rules/default', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const result = await res.json();
        if (res.ok) {
            alert(result.message || '기본 규칙이 저장되었습니다.');
            loadDefaultRule();
        } else {
            alert(result.message || '저장 실패');
        }
    });

    // ============================================
    // 2. 예외 규칙 목록 (Exception Rule)
    // ============================================
    async function loadExceptionRules() {
        try {
            const res = await apiFetch('/api/admin/rules/exception');
            if (res.ok) {
                const response = await res.json();
                const rules = response.data || [];
                exceptionRulesList = rules; // 전역 변수에 저장 (중복 체크용)
                renderExceptionRulesTable(rules);
            }
        } catch (error) {
            console.error('예외 규칙 로드 실패:', error);
        }
    }

    function renderExceptionRulesTable(rules) {
        const tbody = document.getElementById('exceptionRulesTableBody');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!rules || rules.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding: 20px;">등록된 예외 규칙이 없습니다.</td></tr>';
            return;
        }

        rules.forEach(rule => {
            const row = document.createElement('tr');
            const startTime = rule.startTime ? rule.startTime.substring(0, 5) : '-';
            const endTime = rule.endTime ? rule.endTime.substring(0, 5) : '-';
            const breakMin = rule.breakMinutes != null ? `${rule.breakMinutes}분` : '-';
            const dateRange = `${rule.validFrom} ~ ${rule.validTo || '무기한'}`;
            const regDate = rule.appliedAt ? rule.appliedAt.substring(0, 10) : '-';

            row.innerHTML = `
                <td><strong>${rule.employeeName || '사원'}</strong><br><small>${rule.employeeNo || ''}</small></td>
                <td>${dateRange}</td>
                <td>${startTime}</td>
                <td>${endTime}</td>
                <td>${breakMin}</td>
                <td>${rule.reason || ''}</td>
                <td>${regDate}</td>
                <td>
                    <button class="btn-edit" data-id="${rule.overrideId}">수정</button>
                    <button class="btn-delete" data-id="${rule.overrideId}">삭제</button>
                </td>
            `;
            tbody.appendChild(row);
        });

        // 버튼 이벤트 바인딩
        tbody.querySelectorAll('.btn-edit').forEach(btn => {
            btn.onclick = () => openEditModal(btn.dataset.id);
        });
        tbody.querySelectorAll('.btn-delete').forEach(btn => {
            btn.onclick = () => deleteExceptionRule(btn.dataset.id);
        });
    }

    // ============================================
    // 3. 예외 규칙 추가 및 수정 모달 제어
    // ============================================
    async function openEditModal(ruleId) {
        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`);
            if (res.ok) {
                const response = await res.json();
                const rule = response.data;

                if (!rule) {
                    alert('규칙을 찾을 수 없습니다.');
                    return;
                }

                document.getElementById('editRuleId').value = rule.overrideId;
                document.getElementById('editEmployeeInfo').textContent = `${rule.employeeName || ''}${rule.employeeNo ? ' (' + rule.employeeNo + ')' : ''}`;
                document.getElementById('editOriginalValidTo').textContent = rule.validTo || '무기한';
                document.getElementById('editValidFrom').value = rule.validFrom;
                document.getElementById('editValidTo').value = rule.validTo || '';
                document.getElementById('editExceptionStartTime').value = rule.startTime ? rule.startTime.substring(0, 5) : '';
                document.getElementById('editExceptionEndTime').value = rule.endTime ? rule.endTime.substring(0, 5) : '';
                document.getElementById('editExceptionBreakMinutes').value = rule.breakMinutes || '';
                document.getElementById('editReason').value = rule.reason || '';
                
                // 글자 수 카운터 업데이트
                const editReasonCharCount = document.getElementById('editReasonCharCount');
                if (editReasonCharCount) {
                    const count = (rule.reason || '').length;
                    editReasonCharCount.textContent = `${count} / 40`;
                }

                clearAllErrors();
                openModal(editModal);
            } else {
                const result = await res.json();
                alert(result.message || '데이터를 가져오는데 실패했습니다.');
            }
        } catch (error) {
            console.error('데이터 로드 실패:', error);
            alert('데이터를 가져오는데 실패했습니다.');
        }
    }

    // 예외 규칙 수정 제출
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAllErrors();
        
        const ruleId = document.getElementById('editRuleId').value;
        const startTime = document.getElementById('editExceptionStartTime').value;
        const endTime = document.getElementById('editExceptionEndTime').value;
        const breakMinutes = document.getElementById('editExceptionBreakMinutes').value;
        const reason = document.getElementById('editReason').value.trim();

        // 유효성 검사
        const errors = [];
        
        if (!reason) {
            errors.push('규칙 수정 사유를 입력해주세요.');
        } else if (reason.length > 40) {
            errors.push('사유는 40자 이하여야 합니다.');
        }

        // 출근시간과 퇴근시간 유효성 검사
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            if (start >= end) {
                errors.push('출근 시간은 퇴근 시간보다 빨라야 합니다.');
            }
        }

        // 휴게시간 유효성 검사
        if (breakMinutes && (isNaN(breakMinutes) || parseInt(breakMinutes) < 0 || parseInt(breakMinutes) > 480)) {
            errors.push('휴게 시간은 0~480분 사이의 숫자만 입력 가능합니다.');
        }

        // 에러가 있으면 팝업으로 표시
        if (errors.length > 0) {
            alert(errors.join('\n'));
            return;
        }

        try {
            const data = {
                startTime: startTime ? startTime + ':00' : null,
                endTime: endTime ? endTime + ':00' : null,
                breakMinutes: breakMinutes ? parseInt(breakMinutes) : null,
                reason: reason,
                validFrom: document.getElementById('editValidFrom').value,
                validTo: document.getElementById('editValidTo').value || null,
                isActive: true
            };

            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await res.json();
            if (res.ok) {
                alert(result.message || '수정되었습니다.');
                closeModal(editModal);
                await loadExceptionRules(); // 목록 새로고침하여 전역 변수 업데이트
            } else {
                alert(result.message || '수정 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('수정 실패:', error);
            alert('수정 중 오류가 발생했습니다.');
        }
    });

    // 예외 규칙 삭제
    async function deleteExceptionRule(ruleId) {
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        try {
            const res = await apiFetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'DELETE'
            });

            const result = await res.json();
            if (res.ok) {
                alert(result.message || '삭제되었습니다.');
                await loadExceptionRules(); // 목록 새로고침하여 전역 변수 업데이트
            } else {
                alert(result.message || '삭제 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('삭제 실패:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    }

    // ============================================
    // 4. 사원 검색 기능
    // ============================================
    document.getElementById('employeeSearchBtn').addEventListener('click', searchEmployee);
    document.getElementById('employeeSearchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchEmployee();
        }
    });

    async function searchEmployee() {
        const keyword = document.getElementById('employeeSearchInput').value.trim();
        if (!keyword) {
            alert('사원 이름 또는 사번을 입력해주세요.');
            return;
        }

        try {
            const res = await apiFetch(`/api/admin/employees?keyword=${encodeURIComponent(keyword)}&page=0&size=20`);
            if (res.ok) {
                const response = await res.json();
                const employees = response.data?.content || [];
                displayEmployeeResults(employees);
            } else {
                const result = await res.json();
                alert(result.message || '사원 검색에 실패했습니다.');
            }
        } catch (error) {
            console.error('사원 검색 실패:', error);
            alert('사원 검색 중 오류가 발생했습니다.');
        }
    }

    function displayEmployeeResults(employees) {
        const resultsDiv = document.getElementById('employeeSearchResults');
        if (!resultsDiv) return;
        
        resultsDiv.innerHTML = '';
        
        if (!employees || employees.length === 0) {
            resultsDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
            return;
        }

        const ul = document.createElement('ul');
        ul.className = 'employee-list';
        employees.forEach(emp => {
            const li = document.createElement('li');
            li.className = 'employee-item';
            li.innerHTML = `
                <div class="employee-name">${emp.name || ''}</div>
                <div class="employee-details">${emp.orgPath || ''} | ${emp.rankName || ''} | ${emp.positionName || ''}</div>
            `;
            li.onclick = () => selectEmployee(emp);
            ul.appendChild(li);
        });
        resultsDiv.appendChild(ul);
    }

    function selectEmployee(employee) {
        // 이미 등록된 사원인지 확인
        const existingRule = exceptionRulesList.find(rule => rule.employeeId === employee.id);
        if (existingRule) {
            alert(`해당 사원(${employee.name})은 이미 예외규칙이 등록되어 있습니다.\n기존 규칙을 수정하거나 삭제한 후 다시 등록해주세요.`);
            document.getElementById('employeeSearchResults').innerHTML = '';
            document.getElementById('employeeSearchInput').value = '';
            clearError('employeeError');
            return;
        }

        document.getElementById('selectedEmployeeId').value = employee.id;
        document.getElementById('selectedEmployeeInfo').innerHTML = `
            <div class="selected-employee">
                <strong>${employee.name}</strong> (${employee.orgPath || ''})
            </div>
        `;
        document.getElementById('selectedEmployeeInfo').style.display = 'block';
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('employeeSearchInput').value = '';
        clearError('employeeError');
    }

    // ============================================
    // 5. 유틸리티 함수
    // ============================================
    function openModal(m) {
        if (m) {
            m.style.display = 'flex';
            m.style.alignItems = 'center';
            m.style.justifyContent = 'center';
            document.body.style.overflow = 'hidden';
        }
    }
    
    function closeModal(m) {
        if (m) {
            m.style.display = 'none';
            document.body.style.overflow = '';
        }
    }

    function clearAllErrors() {
        document.querySelectorAll('.error-message').forEach(el => {
            el.textContent = '';
            el.style.display = 'none';
        });
        document.querySelectorAll('.error').forEach(el => {
            el.classList.remove('error');
        });
    }

    function clearError(errorId) {
        const errorEl = document.getElementById(errorId);
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.style.display = 'none';
        }
    }

    // 닫기 버튼 공통 처리
    document.querySelectorAll('.close').forEach(btn => {
        btn.onclick = () => {
            closeModal(modal);
            closeModal(editModal);
        };
    });

    document.getElementById('cancelBtn')?.addEventListener('click', () => closeModal(modal));
    document.getElementById('editCancelBtn')?.addEventListener('click', () => closeModal(editModal));

    // 모달 외부 클릭 시 닫기
    modal?.addEventListener('click', (e) => {
        if (e.target === modal) closeModal(modal);
    });
    editModal?.addEventListener('click', (e) => {
        if (e.target === editModal) closeModal(editModal);
    });
});