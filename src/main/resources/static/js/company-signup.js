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
        emailChecked &&
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
adminEmail.addEventListener('input', () => {
    // 한글 제거 (입력/붙여넣기 모두 차단)
    adminEmail.value = adminEmail.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '');

    validateEmail();
    updateSignupButtonState();

    adminEmail.addEventListener('blur', checkEmailDuplicate);

});


function validateEmail() {
    const v = adminEmail.value.trim();

    if (!v) {
        emailMsg.textContent = '';
        return false;
    }

    // 한글 포함 차단 (최종 방어)
    if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(v)) {
        showMsg(emailMsg, '이메일에는 한글을 사용할 수 없습니다.', 'error');
        return false;
    }

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

    if (!v) {
        showMsg(businessMsg, '사업자번호를 입력해주세요.', 'error');
        businessChecked = false;
        updateSignupButtonState();
        return;
    }

    try {
        const res = await apiFetch(
            `/api/companies/check-business-number?businessNumber=${encodeURIComponent(v)}`
        );

        const json = await res.json();

        if (!res.ok) {
            showMsg(businessMsg, json.message, 'error');
            businessChecked = false;
            return;
        }

        // 성공 (시연 모드 포함)
        showMsg(businessMsg, json.message, 'success');
        businessChecked = true;

    } catch (e) {
        showMsg(businessMsg, '사업자번호 검증 중 오류가 발생했습니다.', 'error');
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

    try {
        const res = await apiFetch('/api/companies', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        const result = await res.json();

        if (!res.ok) {
            alert(result.message);
            return;
        }

        alert(result.message);
        location.href = '/login';

    } catch (e) {
        alert('회사 가입 중 오류가 발생했습니다.');
    }
}

/* ======================
   이메일 중복 체크 (자동)
====================== */

let emailChecked = false;

async function checkEmailDuplicate() {
    const v = adminEmail.value.trim();
    if (!validateEmail()) {
        emailChecked = false;
        return;
    }

    try {
        const res = await apiFetch(
            `/api/companies/check-email?email=${encodeURIComponent(v)}`
        );

        const json = await res.json();

        if (json.data.available) {
            showMsg(emailMsg, '사용 가능한 이메일입니다.', 'success');
            emailChecked = true;
        } else {
            showMsg(emailMsg, '이미 사용 중인 이메일입니다.', 'error');
            emailChecked = false;
        }
    } catch (e) {
        showMsg(emailMsg, '이메일 검증 중 오류가 발생했습니다.', 'error');
        emailChecked = false;
    }

    updateSignupButtonState();
}
