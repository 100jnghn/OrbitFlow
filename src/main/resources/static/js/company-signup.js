let businessChecked = false;

/* ======================
   공통 메시지
====================== */
function showMsg(el, message, type) {
    el.textContent = message;
    el.className = 'hint ' + type;
}

/* ======================
   버튼 상태
====================== */
function updateSignupButtonState() {
    signupBtn.disabled = !(
        validateCompanyName() &&
        validateAddress() &&
        validateRepresentativeName() &&
        validateEmail() &&
        validateContact() &&
        businessChecked &&
        validatePasswordLength() &&
        validatePasswordMatch()
    );
}

/* ======================
   회사명 / 주소
====================== */
function validateCompanyName() {
    const v = companyName.value.trim();
    if (!v) return companyNameMsg.textContent = '', false;
    showMsg(companyNameMsg, `${v.length}/100`, 'success');
    return true;
}

function validateAddress() {
    const v = address.value.trim();
    if (!v) return addressMsg.textContent = '', false;
    showMsg(addressMsg, `${v.length}/255`, 'success');
    return true;
}

/* ======================
   대표자명
====================== */
function validateRepresentativeName() {
    const v = representativeName.value.trim();
    if (!v) return representativeNameMsg.textContent = '', false;
    showMsg(representativeNameMsg, `${v.length}/50`, 'success');
    return true;
}

/* ======================
   대표자 연락처 (숫자만 + 글자수)
====================== */
representativeContact.addEventListener('input', () => {
    // 숫자 외 제거
    representativeContact.value =
        representativeContact.value.replace(/[^0-9]/g, '');

    validateContact();
    updateSignupButtonState();
});

function validateContact() {
    const v = representativeContact.value;
    if (!v) {
        showMsg(contactMsg, `숫자만 입력 가능합니다. (0/20)`, 'error');
        return false;
    }
    showMsg(contactMsg, `입력됨 (${v.length}/20)`, 'success');
    return true;
}

/* ======================
   이메일
====================== */
function validateEmail() {
    const v = adminEmail.value.trim();
    if (!v) return emailMsg.textContent = '', false;

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
        showMsg(emailMsg, '이메일 형식이 올바르지 않습니다.', 'error');
        return false;
    }

    showMsg(emailMsg, '사용 가능한 이메일 형식입니다.', 'success');
    return true;
}

/* ======================
   비밀번호 길이
====================== */
function validatePasswordLength() {
    const v = adminPassword.value;

    if (!v) {
        showMsg(passwordLengthMsg, '비밀번호는 8~15자입니다. (0/15)', 'error');
        return false;
    }

    if (v.length < 8 || v.length > 15) {
        showMsg(passwordLengthMsg, `비밀번호는 8~15자입니다. (${v.length}/15)`, 'error');
        return false;
    }

    showMsg(passwordLengthMsg, `사용 가능한 길이 (${v.length}/15)`, 'success');
    return true;
}

/* ======================
   비밀번호 일치 여부
====================== */
function validatePasswordMatch() {
    const v1 = adminPassword.value;
    const v2 = adminPasswordConfirm.value;

    if (!v1 || !v2) {
        showMsg(passwordMatchMsg, '비밀번호를 입력하면 일치 여부를 확인합니다.', 'error');
        return false;
    }

    if (v1 !== v2) {
        showMsg(passwordMatchMsg, '비밀번호가 일치하지 않습니다.', 'error');
        return false;
    }

    showMsg(passwordMatchMsg, '비밀번호가 일치합니다.', 'success');
    return true;
}

/* ======================
   비밀번호 이벤트
====================== */
adminPassword.addEventListener('input', () => {
    validatePasswordLength();
    validatePasswordMatch();
    updateSignupButtonState();
});

adminPasswordConfirm.addEventListener('input', () => {
    validatePasswordMatch();
    updateSignupButtonState();
});

/* ======================
   사업자번호 (숫자만)
====================== */
businessNumber.addEventListener('input', () => {
    businessNumber.value =
        businessNumber.value.replace(/[^0-9]/g, '');

    businessChecked = false;
    businessMsg.textContent = '';
    updateSignupButtonState();
});

async function checkBusinessNumber() {
    const v = businessNumber.value.trim();
    if (!v) return showMsg(businessMsg, '사업자번호를 입력해주세요.', 'error');

    const res = await fetch(
        `/api/companies/check-business-number?businessNumber=${encodeURIComponent(v)}`
    );
    const json = await res.json();

    if (json.data.available) {
        showMsg(businessMsg, '사용 가능한 사업자번호입니다.', 'success');
        businessChecked = true;
    } else {
        showMsg(businessMsg, '이미 등록된 사업자번호입니다.', 'error');
        businessChecked = false;
    }
    updateSignupButtonState();
}

/* ======================
   공통 입력 이벤트
====================== */
[
    companyName, address, representativeName,
    adminEmail
].forEach(el => el.addEventListener('input', updateSignupButtonState));

/* ======================
   제출
====================== */
async function submitSignup() {
    if (signupBtn.disabled) return;

    const body = {
        companyName: companyName.value.trim(),
        address: address.value.trim(),
        representativeName: representativeName.value.trim(),
        representativeContact: representativeContact.value.trim(),
        adminEmail: adminEmail.value.trim(),
        adminPassword: adminPassword.value,
        businessNumber: businessNumber.value.trim()
    };

    const res = await fetch('/api/companies', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (res.ok) {
        alert('회사 가입이 완료되었습니다.');
        location.href = '/login';
    }
}
