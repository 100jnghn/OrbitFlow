let businessChecked = false;

async function checkBusinessNumber() {
    const bn = document.getElementById('businessNumber').value;
    const msg = document.getElementById('businessMsg');

    const res = await fetch(`/api/companies/check-business-number?businessNumber=${bn}`);
    const json = await res.json();

    if (json.data.available) {
        msg.textContent = '사용 가능한 사업자번호입니다.';
        msg.className = 'hint success';
        businessChecked = true;
    } else {
        msg.textContent = '이미 등록된 사업자번호입니다.';
        msg.className = 'hint error';
        businessChecked = false;
    }
}

async function submitSignup() {
    if (!businessChecked) {
        alert('사업자번호 중복 확인이 필요합니다.');
        return;
    }

    const pwd = adminPassword.value;
    const pwd2 = adminPasswordConfirm.value;

    if (pwd !== pwd2) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    const body = {
        companyName: companyName.value,
        address: address.value,
        representativeName: representativeName.value,
        representativeContact: representativeContact.value,
        adminEmail: adminEmail.value,
        adminPassword: pwd,
        businessNumber: businessNumber.value
    };

    const res = await fetch('/api/companies', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (res.ok) {
        alert('회사 가입이 완료되었습니다.');
        location.href = '/login';
    } else {
        alert('가입 실패');
    }
}
