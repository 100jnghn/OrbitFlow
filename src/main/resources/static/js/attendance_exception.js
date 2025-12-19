document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('exceptionRuleModal');
    const editModal = document.getElementById('editExceptionRuleModal');
    const form = document.getElementById('exceptionRuleForm');
    const editForm = document.getElementById('editExceptionRuleForm');

    // 초기화
    loadDefaultRule();
    loadExceptionRules();

    // ============================================
    // 기본 규칙 관련
    // ============================================
    async function loadDefaultRule() {
        try {
            const token = sessionStorage.getItem('accessToken');
            const res = await fetch('/api/admin/rules/default', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
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
            const token = sessionStorage.getItem('accessToken');
            const data = {
                defaultStartTime: document.getElementById('defaultStartTime').value + ":00",
                defaultEndTime: document.getElementById('defaultEndTime').value + ":00",
                lateThresholdMin: parseInt(document.getElementById('tardinessMinutes').value)
            };

            const res = await fetch('/api/admin/rules/default', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
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
    // 예외 규칙 목록 조회
    // ============================================
    async function loadExceptionRules() {
        try {
            const token = sessionStorage.getItem('accessToken');
            const res = await fetch('/api/admin/rules/exception', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                const rules = await res.json();
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

        // 수정/삭제 버튼 이벤트 리스너 추가
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', () => openEditModal(btn.dataset.id));
        });

        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', () => deleteExceptionRule(btn.dataset.id));
        });
    }

    // 모달 열기/닫기 헬퍼 함수
    function openModal(modalElement) {
        modalElement.style.display = 'block';
        document.body.style.overflow = 'hidden'; // 배경 스크롤 방지
    }

    function closeModal(modalElement) {
        modalElement.style.display = 'none';
        document.body.style.overflow = ''; // 배경 스크롤 복원
    }

    // ============================================
    // 예외 규칙 추가 모달
    // ============================================
    document.getElementById('addExceptionRuleBtn').addEventListener('click', () => {
        form.reset();
        document.getElementById('ruleId').value = '';
        document.getElementById('selectedEmployeeId').value = '';
        document.getElementById('selectedEmployeeInfo').style.display = 'none';
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('employeeSearchInput').value = '';
        document.getElementById('modalTitle').innerText = '사원별 근태 예외 규칙 추가';
        openModal(modal);
    });

    document.querySelector('#exceptionRuleModal .close').addEventListener('click', () => {
        closeModal(modal);
    });

    document.getElementById('cancelBtn').addEventListener('click', () => {
        closeModal(modal);
    });

    // 사원 검색
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
            const token = sessionStorage.getItem('accessToken');
            const res = await fetch(`/api/admin/rules/employees/search?keyword=${encodeURIComponent(keyword)}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                const employees = await res.json();
                displayEmployeeSearchResults(employees);
            } else {
                alert('사원 검색에 실패했습니다.');
            }
        } catch (error) {
            console.error('사원 검색 실패:', error);
            alert('사원 검색 중 오류가 발생했습니다.');
        }
    }

    function displayEmployeeSearchResults(employees) {
        const resultsDiv = document.getElementById('employeeSearchResults');
        resultsDiv.innerHTML = '';

        if (employees.length === 0) {
            resultsDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
            return;
        }

        const list = document.createElement('ul');
        list.className = 'employee-list';

        employees.forEach(emp => {
            const item = document.createElement('li');
            item.className = 'employee-item';
            item.innerHTML = `
                <div class="employee-name">${emp.name}</div>
                <div class="employee-details">${emp.employeeNo} | ${emp.organizationName} | ${emp.positionName}</div>
            `;
            item.addEventListener('click', () => selectEmployee(emp));
            list.appendChild(item);
        });

        resultsDiv.appendChild(list);
    }

    function selectEmployee(employee) {
        document.getElementById('selectedEmployeeId').value = employee.id;
        document.getElementById('selectedEmployeeInfo').innerHTML = `
            <div class="selected-employee">
                <strong>${employee.name}</strong> (${employee.organizationName}, ${employee.positionName})
            </div>
        `;
        document.getElementById('selectedEmployeeInfo').style.display = 'block';
        document.getElementById('employeeSearchResults').innerHTML = '';
        document.getElementById('employeeSearchInput').value = '';
    }

    // 예외 규칙 추가
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const employeeId = document.getElementById('selectedEmployeeId').value;
        if (!employeeId) {
            alert('대상 사원을 선택해주세요.');
            return;
        }

        try {
            const token = sessionStorage.getItem('accessToken');
            const data = {
                employeeId: parseInt(employeeId),
                startTime: document.getElementById('exceptionStartTime').value ? 
                    document.getElementById('exceptionStartTime').value + ":00" : null,
                endTime: document.getElementById('exceptionEndTime').value ? 
                    document.getElementById('exceptionEndTime').value + ":00" : null,
                breakMinutes: document.getElementById('exceptionBreakMinutes').value ? 
                    parseInt(document.getElementById('exceptionBreakMinutes').value) : null,
                reason: document.getElementById('reason').value,
                validFrom: document.getElementById('validFrom').value,
                validTo: document.getElementById('validTo').value || null
            };

            const res = await fetch('/api/admin/rules/exception', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('예외 규칙이 추가되었습니다.');
                closeModal(modal);
                loadExceptionRules();
            } else {
                const error = await res.json();
                alert('추가 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('예외 규칙 추가 실패:', error);
            alert('추가 중 오류가 발생했습니다.');
        }
    });

    // ============================================
    // 예외 규칙 수정 모달
    // ============================================
    async function openEditModal(ruleId) {
        try {
            const token = sessionStorage.getItem('accessToken');
            const res = await fetch(`/api/admin/rules/exception/${ruleId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                const rule = await res.json();
                populateEditForm(rule);
                openModal(editModal);
            } else {
                alert('규칙 정보를 불러오는데 실패했습니다.');
            }
        } catch (error) {
            console.error('규칙 정보 로드 실패:', error);
            alert('규칙 정보를 불러오는 중 오류가 발생했습니다.');
        }
    }

    function populateEditForm(rule) {
        document.getElementById('editRuleId').value = rule.overrideId;
        document.getElementById('editEmployeeInfo').textContent = 
            `${rule.employeeName || '알 수 없음'} (ID: ${rule.employeeId})`;
        
        if (rule.validTo) {
            document.getElementById('editOriginalValidTo').textContent = rule.validTo;
        } else {
            document.getElementById('editOriginalValidTo').textContent = '무기한';
        }

        document.getElementById('editValidFrom').value = rule.validFrom || '';
        document.getElementById('editValidTo').value = rule.validTo || '';
        
        if (rule.startTime) {
            document.getElementById('editExceptionStartTime').value = rule.startTime.substring(0, 5);
        } else {
            document.getElementById('editExceptionStartTime').value = '';
        }
        
        if (rule.endTime) {
            document.getElementById('editExceptionEndTime').value = rule.endTime.substring(0, 5);
        } else {
            document.getElementById('editExceptionEndTime').value = '';
        }
        
        document.getElementById('editExceptionBreakMinutes').value = rule.breakMinutes || '';
        document.getElementById('editReason').value = rule.reason || '';
    }

    document.querySelector('#editExceptionRuleModal .close').addEventListener('click', () => {
        closeModal(editModal);
    });

    document.getElementById('editCancelBtn').addEventListener('click', () => {
        closeModal(editModal);
    });

    // 예외 규칙 수정
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        try {
            const token = sessionStorage.getItem('accessToken');
            const ruleId = document.getElementById('editRuleId').value;
            const data = {
                startTime: document.getElementById('editExceptionStartTime').value ? 
                    document.getElementById('editExceptionStartTime').value + ":00" : null,
                endTime: document.getElementById('editExceptionEndTime').value ? 
                    document.getElementById('editExceptionEndTime').value + ":00" : null,
                breakMinutes: document.getElementById('editExceptionBreakMinutes').value ? 
                    parseInt(document.getElementById('editExceptionBreakMinutes').value) : null,
                reason: document.getElementById('editReason').value,
                validFrom: document.getElementById('editValidFrom').value,
                validTo: document.getElementById('editValidTo').value || null,
                isActive: true
            };

            const res = await fetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('예외 규칙이 수정되었습니다.');
                closeModal(editModal);
                loadExceptionRules();
            } else {
                const error = await res.json();
                alert('수정 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('예외 규칙 수정 실패:', error);
            alert('수정 중 오류가 발생했습니다.');
        }
    });

    // ============================================
    // 예외 규칙 삭제
    // ============================================
    async function deleteExceptionRule(ruleId) {
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        try {
            const token = sessionStorage.getItem('accessToken');
            const res = await fetch(`/api/admin/rules/exception/${ruleId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                alert('예외 규칙이 삭제되었습니다.');
                loadExceptionRules();
            } else {
                const error = await res.json();
                alert('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('예외 규칙 삭제 실패:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal(modal);
        }
        if (e.target === editModal) {
            closeModal(editModal);
        }
    });
});

