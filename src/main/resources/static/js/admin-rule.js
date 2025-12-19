document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('exceptionRuleModal');
    const form = document.getElementById('exceptionRuleForm');

    // 1. 기본 규칙 저장
    document.getElementById('saveDefaultRuleBtn').addEventListener('click', async () => {
        const data = {
            defaultStartTime: document.getElementById('defaultStartTime').value + ":00",
            defaultEndTime: document.getElementById('defaultEndTime').value + ":00",
            defaultBreakMinutes: document.getElementById('defaultBreakMinutes').value
        };

        const res = await fetch('/api/admin/rules/default', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` },
            body: JSON.stringify(data)
        });
        if (res.ok) alert('기본 규칙이 저장되었습니다.');
    });

    // 2. 모달 제어
    document.getElementById('addExceptionRuleBtn').onclick = () => {
        form.reset();
        document.getElementById('ruleId').value = '';
        document.getElementById('modalTitle').innerText = '예외 규칙 추가';
        modal.style.display = 'block';
    };

    document.getElementById('cancelBtn').onclick = () => modal.style.display = 'none';

    // 3. 예외 규칙 추가 및 수정
    form.onsubmit = async (e) => {
        e.preventDefault();
        const id = document.getElementById('ruleId').value;
        const data = {
            employeeId: document.getElementById('employeeSelect').value,
            startTime: document.getElementById('exceptionStartTime').value + ":00",
            endTime: document.getElementById('exceptionEndTime').value + ":00",
            reason: document.getElementById('reason').value,
            validFrom: document.getElementById('validFrom').value,
            validTo: document.getElementById('validTo').value || null
        };

        const url = id ? `/api/admin/rules/exception/${id}` : '/api/admin/rules/exception';
        const method = id ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` },
            body: JSON.stringify(data)
        });

        if (res.ok) location.reload();
    };

    // 4. 수정 버튼 클릭 시 데이터 로드
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.onclick = async () => {
            const id = btn.dataset.id;
            const res = await fetch(`/api/admin/rules/exception/${id}`, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
            });
            const rule = await res.json();

            document.getElementById('ruleId').value = id;
            document.getElementById('employeeSelect').value = rule.employeeId;
            document.getElementById('exceptionStartTime').value = rule.startTime.substring(0, 5);
            document.getElementById('exceptionEndTime').value = rule.endTime.substring(0, 5);
            document.getElementById('reason').value = rule.reason;
            document.getElementById('validFrom').value = rule.validFrom;
            document.getElementById('modalTitle').innerText = '예외 규칙 수정';
            modal.style.display = 'block';
        };
    });

    // 5. 삭제
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.onclick = async () => {
            if (!confirm('정말 삭제하시겠습니까?')) return;
            const id = btn.dataset.id;
            await fetch(`/api/admin/rules/exception/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
            });
            location.reload();
        };
    });
});