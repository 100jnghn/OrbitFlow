document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('exceptionRuleModal');
    const editModal = document.getElementById('editExceptionRuleModal');
    const form = document.getElementById('exceptionRuleForm');
    const editForm = document.getElementById('editExceptionRuleForm');

    // 예외 규칙 목록 저장 (중복 체크용)
    let exceptionRulesList = [];

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
                exceptionRulesList = rules; // 전역 변수에 저장 (중복 체크용)
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
        clearAllErrors();
        openModal(modal);
    });

    document.querySelector('#exceptionRuleModal .close').addEventListener('click', () => {
        closeModal(modal);
    });

    document.getElementById('cancelBtn').addEventListener('click', () => {
        clearAllErrors();
        closeModal(modal);
    });

    // 실시간 유효성 검사 이벤트 리스너
    document.getElementById('validFrom').addEventListener('change', function() {
        clearFieldError('validFromError', 'validFrom');
        // 시작일이 변경되면 종료일 에러도 함께 검증
        clearFieldError('validToError', 'validTo');
        const validTo = document.getElementById('validTo').value;
        if (validTo) {
            validateDateRange();
        }
    });

    document.getElementById('validTo').addEventListener('change', function() {
        clearFieldError('validToError', 'validTo');
        validateDateRange();
    });

    document.getElementById('exceptionStartTime').addEventListener('change', function() {
        clearFieldError('startTimeError', 'exceptionStartTime');
        clearFieldError('endTimeError', 'exceptionEndTime');
        const endTime = document.getElementById('exceptionEndTime').value;
        if (endTime) {
            validateTimeRange();
        }
    });

    document.getElementById('exceptionEndTime').addEventListener('change', function() {
        clearFieldError('endTimeError', 'exceptionEndTime');
        validateTimeRange();
    });

    // 휴게시간 숫자만 입력 허용
    const breakMinutesInput = document.getElementById('exceptionBreakMinutes');
    if (breakMinutesInput) {
        // 키 입력 시 숫자만 허용
        breakMinutesInput.addEventListener('keypress', function(e) {
            const char = String.fromCharCode(e.which);
            if (!/[0-9]/.test(char) && !e.ctrlKey && !e.metaKey) {
                e.preventDefault();
            }
        });
        
        // 붙여넣기 시 숫자만 허용
        breakMinutesInput.addEventListener('paste', function(e) {
            e.preventDefault();
            const paste = (e.clipboardData || window.clipboardData).getData('text');
            const numbersOnly = paste.replace(/[^0-9]/g, '');
            if (numbersOnly) {
                this.value = numbersOnly;
                this.dispatchEvent(new Event('input'));
            }
        });
        
        // input 이벤트에서도 필터링 (안전장치)
        breakMinutesInput.addEventListener('input', function() {
            const value = this.value.replace(/[^0-9]/g, '');
            if (this.value !== value) {
                this.value = value;
            }
            clearFieldError('breakMinutesError', 'exceptionBreakMinutes');
            if (value) {
                validateBreakMinutes();
            }
        });
    }

    document.getElementById('reason').addEventListener('input', function() {
        clearFieldError('reasonError', 'reason');
        validateReason();
    });

    // 개별 필드 에러 초기화 함수
    function clearFieldError(errorElementId, inputElementId) {
        const errorElement = document.getElementById(errorElementId);
        const inputElement = document.getElementById(inputElementId);
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
        if (inputElement) {
            inputElement.classList.remove('error');
        }
    }

    // 날짜 범위 검증
    function validateDateRange() {
        const validFrom = document.getElementById('validFrom').value;
        const validTo = document.getElementById('validTo').value;
        
        if (validFrom && validTo) {
            const validFromDate = new Date(validFrom);
            const validToDate = new Date(validTo);
            
            if (validToDate < validFromDate) {
                showError('validToError', '적용 종료일은 적용 시작일 이후여야 합니다.');
                return false;
            } else {
                // 날짜 범위가 올바르면 에러 메시지 클리어
                clearFieldError('validToError', 'validTo');
            }
        }
        return true;
    }

    // 시간 범위 검증
    function validateTimeRange() {
        const startTime = document.getElementById('exceptionStartTime').value;
        const endTime = document.getElementById('exceptionEndTime').value;
        
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            
            if (start >= end) {
                showError('endTimeError', '퇴근 시간은 출근 시간보다 늦어야 합니다.');
                return false;
            } else {
                // 시간 범위가 올바르면 에러 메시지 클리어
                clearFieldError('endTimeError', 'exceptionEndTime');
            }
        }
        return true;
    }

    // 휴게 시간 검증
    function validateBreakMinutes() {
        const breakMinutes = document.getElementById('exceptionBreakMinutes').value;
        if (breakMinutes) {
            const breakMin = parseInt(breakMinutes);
            if (isNaN(breakMin) || breakMin < 0) {
                showError('breakMinutesError', '휴게 시간은 0 이상의 숫자여야 합니다.');
                return false;
            } else if (breakMin > 480) {
                showError('breakMinutesError', '휴게 시간은 480분(8시간) 이하여야 합니다.');
                return false;
            }
        }
        return true;
    }

    // 사유 검증
    function validateReason() {
        const reason = document.getElementById('reason').value.trim();
        if (reason && reason.length > 40) {
            showError('reasonError', '규칙 적용 사유는 40자 이하여야 합니다.');
            return false;
        }
        return true;
    }

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
        // 동일 직원의 예외 규칙이 이미 존재하는지 확인
        const existingRule = exceptionRulesList.find(rule => rule.employeeId === employee.id);
        if (existingRule) {
            clearFieldError('employeeError', 'selectedEmployeeId');
            showError('employeeError', '해당 직원의 예외 규칙이 이미 존재합니다. 기존 규칙을 수정하거나 삭제한 후 다시 추가해주세요.');
            return;
        }

        // 에러 메시지 클리어
        clearFieldError('employeeError', 'selectedEmployeeId');
        
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

    // 에러 메시지 초기화 함수
    function clearAllErrors() {
        const errorElements = document.querySelectorAll('.error-message');
        errorElements.forEach(el => {
            el.textContent = '';
            el.style.display = 'none';
        });
        
        const errorInputs = document.querySelectorAll('.error');
        errorInputs.forEach(el => {
            el.classList.remove('error');
        });
    }

    // 에러 메시지 표시 함수
    function showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        const inputElement = document.getElementById(elementId.replace('Error', ''));
        
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
        
        if (inputElement) {
            inputElement.classList.add('error');
            inputElement.focus();
        }
    }

    // 유효성 검사 함수
    function validateExceptionRuleForm() {
        clearAllErrors();
        let isValid = true;

        // 1. 대상 사원 선택 검증
        const employeeId = document.getElementById('selectedEmployeeId').value;
        if (!employeeId) {
            showError('employeeError', '대상 사원을 선택해주세요.');
            isValid = false;
        } else {
            // 동일 직원의 예외 규칙이 이미 존재하는지 확인
            const existingRule = exceptionRulesList.find(rule => rule.employeeId === parseInt(employeeId));
            if (existingRule) {
                showError('employeeError', '해당 직원의 예외 규칙이 이미 존재합니다. 기존 규칙을 수정하거나 삭제한 후 다시 추가해주세요.');
                isValid = false;
            }
        }

        // 2. 적용 시작일 검증
        const validFrom = document.getElementById('validFrom').value;
        if (!validFrom) {
            showError('validFromError', '적용 시작일을 선택해주세요.');
            isValid = false;
        } else {
            const validFromDate = new Date(validFrom);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (validFromDate < today) {
                showError('validFromError', '적용 시작일은 오늘 날짜 이후여야 합니다.');
                isValid = false;
            }
        }

        // 3. 적용 종료일 검증
        const validTo = document.getElementById('validTo').value;
        if (validTo) {
            if (!validFrom) {
                showError('validToError', '적용 시작일을 먼저 선택해주세요.');
                isValid = false;
            } else {
                const validFromDate = new Date(validFrom);
                const validToDate = new Date(validTo);
                
                if (validToDate < validFromDate) {
                    showError('validToError', '적용 종료일은 적용 시작일 이후여야 합니다.');
                    isValid = false;
                }
            }
        }

        // 4. 오버라이드 필드 검증 (최소 하나는 입력되어야 함)
        const startTime = document.getElementById('exceptionStartTime').value;
        const endTime = document.getElementById('exceptionEndTime').value;
        const breakMinutes = document.getElementById('exceptionBreakMinutes').value;
        
        if (!startTime && !endTime && !breakMinutes) {
            showError('overrideFieldsError', '출근 시간, 퇴근 시간, 휴게 시간 중 최소 하나는 입력해주세요.');
            isValid = false;
        }

        // 5. 출근/퇴근 시간 검증
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            
            if (start >= end) {
                showError('endTimeError', '퇴근 시간은 출근 시간보다 늦어야 합니다.');
                isValid = false;
            }
        }

        // 6. 휴게 시간 검증
        if (breakMinutes) {
            const breakMin = parseInt(breakMinutes);
            if (isNaN(breakMin) || breakMin < 0) {
                showError('breakMinutesError', '휴게 시간은 0 이상의 숫자여야 합니다.');
                isValid = false;
            } else if (breakMin > 480) {
                showError('breakMinutesError', '휴게 시간은 480분(8시간) 이하여야 합니다.');
                isValid = false;
            }
        }

        // 7. 규칙 적용 사유 검증
        const reason = document.getElementById('reason').value.trim();
        if (!reason) {
            showError('reasonError', '규칙 적용 사유를 입력해주세요.');
            isValid = false;
        } else if (reason.length > 40) {
            showError('reasonError', '규칙 적용 사유는 40자 이하여야 합니다.');
            isValid = false;
        }

        return isValid;
    }

    // 예외 규칙 추가
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 유효성 검사
        if (!validateExceptionRuleForm()) {
            return;
        }

        const employeeId = document.getElementById('selectedEmployeeId').value;
        const startTime = document.getElementById('exceptionStartTime').value;
        const endTime = document.getElementById('exceptionEndTime').value;
        const breakMinutes = document.getElementById('exceptionBreakMinutes').value;
        const reason = document.getElementById('reason').value.trim();
        const validFrom = document.getElementById('validFrom').value;
        const validTo = document.getElementById('validTo').value || null;

        try {
            const token = sessionStorage.getItem('accessToken');
            const data = {
                employeeId: parseInt(employeeId),
                startTime: startTime ? startTime + ":00" : null,
                endTime: endTime ? endTime + ":00" : null,
                breakMinutes: breakMinutes ? parseInt(breakMinutes) : null,
                reason: reason,
                validFrom: validFrom,
                validTo: validTo
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
                form.reset();
                clearAllErrors();
                document.getElementById('selectedEmployeeId').value = '';
                document.getElementById('selectedEmployeeInfo').style.display = 'none';
                await loadExceptionRules(); // 규칙 목록 새로고침하여 중복 체크 업데이트
            } else {
                const error = await res.json();
                const errorMessage = error.message || '알 수 없는 오류';
                
                // 서버 에러 메시지에 따라 적절한 필드에 표시
                if (errorMessage.includes('사원')) {
                    showError('employeeError', errorMessage);
                } else if (errorMessage.includes('시작일') || errorMessage.includes('기간')) {
                    showError('validFromError', errorMessage);
                } else if (errorMessage.includes('종료일')) {
                    showError('validToError', errorMessage);
                } else if (errorMessage.includes('시간') || errorMessage.includes('출근') || errorMessage.includes('퇴근')) {
                    showError('startTimeError', errorMessage);
                } else if (errorMessage.includes('사유') || errorMessage.includes('reason')) {
                    showError('reasonError', errorMessage);
                } else {
                    alert('추가 실패: ' + errorMessage);
                }
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
        clearAllErrors();
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
        clearAllErrors();
        closeModal(editModal);
    });

    // 수정 폼 유효성 검사 함수
    function validateEditExceptionRuleForm() {
        clearAllErrors();
        let isValid = true;

        // 1. 적용 종료일 검증
        const validFrom = document.getElementById('editValidFrom').value;
        const validTo = document.getElementById('editValidTo').value;
        if (validTo) {
            if (!validFrom) {
                showError('editValidToError', '적용 시작일이 없습니다.');
                isValid = false;
            } else {
                const validFromDate = new Date(validFrom);
                const validToDate = new Date(validTo);
                
                if (validToDate < validFromDate) {
                    showError('editValidToError', '적용 종료일은 적용 시작일 이후여야 합니다.');
                    isValid = false;
                }
            }
        }

        // 2. 출근/퇴근 시간 검증
        const startTime = document.getElementById('editExceptionStartTime').value;
        const endTime = document.getElementById('editExceptionEndTime').value;
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            
            if (start >= end) {
                showError('editEndTimeError', '퇴근 시간은 출근 시간보다 늦어야 합니다.');
                isValid = false;
            }
        }

        // 3. 휴게 시간 검증
        const breakMinutes = document.getElementById('editExceptionBreakMinutes').value;
        if (breakMinutes) {
            const breakMin = parseInt(breakMinutes);
            if (isNaN(breakMin) || breakMin < 0) {
                showError('editBreakMinutesError', '휴게 시간은 0 이상의 숫자여야 합니다.');
                isValid = false;
            } else if (breakMin > 480) {
                showError('editBreakMinutesError', '휴게 시간은 480분(8시간) 이하여야 합니다.');
                isValid = false;
            }
        }

        // 4. 규칙 수정 사유 검증
        const reason = document.getElementById('editReason').value.trim();
        if (!reason) {
            showError('editReasonError', '규칙 수정 사유를 입력해주세요.');
            isValid = false;
        } else if (reason.length > 40) {
            showError('editReasonError', '규칙 수정 사유는 40자 이하여야 합니다.');
            isValid = false;
        }

        return isValid;
    }

    // 수정 모달 실시간 검증
    document.getElementById('editValidTo')?.addEventListener('change', function() {
        clearFieldError('editValidToError', 'editValidTo');
        const validFrom = document.getElementById('editValidFrom').value;
        const validTo = this.value;
        if (validFrom && validTo) {
            const validFromDate = new Date(validFrom);
            const validToDate = new Date(validTo);
            if (validToDate < validFromDate) {
                showError('editValidToError', '적용 종료일은 적용 시작일 이후여야 합니다.');
            } else {
                // 날짜 범위가 올바르면 에러 메시지 클리어
                clearFieldError('editValidToError', 'editValidTo');
            }
        }
    });

    document.getElementById('editExceptionStartTime')?.addEventListener('change', function() {
        clearFieldError('editStartTimeError', 'editExceptionStartTime');
        clearFieldError('editEndTimeError', 'editExceptionEndTime');
        const endTime = document.getElementById('editExceptionEndTime').value;
        if (endTime) {
            validateEditTimeRange();
        }
    });

    document.getElementById('editExceptionEndTime')?.addEventListener('change', function() {
        clearFieldError('editEndTimeError', 'editExceptionEndTime');
        validateEditTimeRange();
    });

    // 수정 모달 휴게시간 숫자만 입력 허용
    const editBreakMinutesInput = document.getElementById('editExceptionBreakMinutes');
    if (editBreakMinutesInput) {
        // 키 입력 시 숫자만 허용
        editBreakMinutesInput.addEventListener('keypress', function(e) {
            const char = String.fromCharCode(e.which);
            if (!/[0-9]/.test(char) && !e.ctrlKey && !e.metaKey) {
                e.preventDefault();
            }
        });
        
        // 붙여넣기 시 숫자만 허용
        editBreakMinutesInput.addEventListener('paste', function(e) {
            e.preventDefault();
            const paste = (e.clipboardData || window.clipboardData).getData('text');
            const numbersOnly = paste.replace(/[^0-9]/g, '');
            if (numbersOnly) {
                this.value = numbersOnly;
                this.dispatchEvent(new Event('input'));
            }
        });
        
        // input 이벤트에서도 필터링 (안전장치)
        editBreakMinutesInput.addEventListener('input', function() {
            const value = this.value.replace(/[^0-9]/g, '');
            if (this.value !== value) {
                this.value = value;
            }
            clearFieldError('editBreakMinutesError', 'editExceptionBreakMinutes');
            if (value) {
                const breakMin = parseInt(value);
                if (isNaN(breakMin) || breakMin < 0) {
                    showError('editBreakMinutesError', '휴게 시간은 0 이상의 숫자여야 합니다.');
                } else if (breakMin > 480) {
                    showError('editBreakMinutesError', '휴게 시간은 480분(8시간) 이하여야 합니다.');
                }
            }
        });
    }

    document.getElementById('editReason')?.addEventListener('input', function() {
        clearFieldError('editReasonError', 'editReason');
        const reason = this.value.trim();
        if (reason && reason.length > 40) {
            showError('editReasonError', '규칙 수정 사유는 40자 이하여야 합니다.');
        }
    });

    function validateEditTimeRange() {
        const startTime = document.getElementById('editExceptionStartTime').value;
        const endTime = document.getElementById('editExceptionEndTime').value;
        
        if (startTime && endTime) {
            const start = new Date(`2000-01-01T${startTime}:00`);
            const end = new Date(`2000-01-01T${endTime}:00`);
            
            if (start >= end) {
                showError('editEndTimeError', '퇴근 시간은 출근 시간보다 늦어야 합니다.');
                return false;
            } else {
                // 시간 범위가 올바르면 에러 메시지 클리어
                clearFieldError('editEndTimeError', 'editExceptionEndTime');
            }
        }
        return true;
    }

    // 예외 규칙 수정
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 유효성 검사
        if (!validateEditExceptionRuleForm()) {
            return;
        }

        try {
            const token = sessionStorage.getItem('accessToken');
            const ruleId = document.getElementById('editRuleId').value;
            const startTime = document.getElementById('editExceptionStartTime').value;
            const endTime = document.getElementById('editExceptionEndTime').value;
            const breakMinutes = document.getElementById('editExceptionBreakMinutes').value;
            const reason = document.getElementById('editReason').value.trim();
            const validFrom = document.getElementById('editValidFrom').value;
            const validTo = document.getElementById('editValidTo').value || null;

            const data = {
                startTime: startTime ? startTime + ":00" : null,
                endTime: endTime ? endTime + ":00" : null,
                breakMinutes: breakMinutes ? parseInt(breakMinutes) : null,
                reason: reason,
                validFrom: validFrom,
                validTo: validTo,
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
                clearAllErrors();
                await loadExceptionRules(); // 규칙 목록 새로고침
            } else {
                const error = await res.json();
                const errorMessage = error.message || '알 수 없는 오류';
                
                // 서버 에러 메시지에 따라 적절한 필드에 표시
                if (errorMessage.includes('종료일') || errorMessage.includes('기간')) {
                    showError('editValidToError', errorMessage);
                } else if (errorMessage.includes('시간') || errorMessage.includes('출근') || errorMessage.includes('퇴근')) {
                    showError('editEndTimeError', errorMessage);
                } else if (errorMessage.includes('사유') || errorMessage.includes('reason')) {
                    showError('editReasonError', errorMessage);
                } else {
                    alert('수정 실패: ' + errorMessage);
                }
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
                await loadExceptionRules(); // 규칙 목록 새로고침하여 중복 체크 업데이트
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

