// ---------- sweet alert 공통 래퍼 ---------- //
window.sweetSuccess = function (message, type = 'success') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetError = function (message, type = 'error') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetWarning = function (message, type = 'warning') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetConfirm = function (title, message) {
    return Swal.fire({
        title: title,
        text: message,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '확인',
        cancelButtonText: '취소'
    });
};

window.sweetInfo = function (message, type = 'info') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

window.sweetQuestion = function (message, type = 'question') {
    return Swal.fire({
        text: message,
        icon: type,
        confirmButtonText: '확인'
    });
};

let businessChecked = false;
let emailChecked = false;
let companyNameChecked = false;

// 비밀번호 정책 (계정 활성화 화면과 동일)
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&]).{8,}$/;

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
    const enabled =
        validateCompanyName() &&
        companyNameChecked &&
        validateAddress() &&
        validateRepresentativeName() &&
        validateEmail() &&
        emailChecked &&
        validateContact() &&
        businessChecked &&
        validatePasswordLength() &&
        validatePasswordMatch();

    signupBtn.disabled = !enabled;
}

/* ======================
   회사명 / 주소
====================== */


companyName.addEventListener('input', () => {
    companyNameChecked = false;
    showMsg(
        companyNameMsg,
        '회사명 중복 확인이 필요합니다.',
        'error'
    );
    updateSignupButtonState();
});


function validateCompanyName() {
    const v = companyName.value.trim();
    return !!v;
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
   대표자 연락처
====================== */
representativeContact.addEventListener('input', () => {
    representativeContact.value =
        representativeContact.value.replace(/[^0-9]/g, '');

    validateContact();
    updateSignupButtonState();
});

function validateContact() {
    const v = representativeContact.value;
    if (!v) {
        showMsg(contactMsg, '숫자만 입력 가능합니다. (0/20)', 'error');
        return false;
    }
    showMsg(contactMsg, `입력됨 (${v.length}/20)`, 'success');
    return true;
}

/* ======================
   이메일
====================== */
adminEmail.addEventListener('input', () => {
    adminEmail.value = adminEmail.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '');
    emailChecked = false;

    showMsg(
        emailMsg,
        '이메일 변경 시 중복 확인이 필요합니다.',
        'error'
    );

    validateEmail();
    updateSignupButtonState();
});

adminEmail.addEventListener('blur', checkEmailDuplicate);

function validateEmail() {
    const v = adminEmail.value.trim();

    if (!v) {
        emailMsg.textContent = '';
        return false;
    }

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
   비밀번호 정책
====================== */
function validatePasswordLength() {
    const v = adminPassword.value;

    if (!v || !passwordRegex.test(v)) {
        showMsg(
            passwordLengthMsg,
            '비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.',
            'error'
        );
        return false;
    }

    showMsg(
        passwordLengthMsg,
        '사용 가능한 비밀번호입니다.',
        'success'
    );
    return true;
}

function validatePasswordMatch() {
    const v1 = adminPassword.value;
    const v2 = adminPasswordConfirm.value;

    if (!v1 || !v2) {
        showMsg(
            passwordMatchMsg,
            '비밀번호를 입력하면 일치 여부를 확인합니다.',
            'error'
        );
        return false;
    }

    // 정책 우선
    if (!passwordRegex.test(v1)) {
        showMsg(
            passwordMatchMsg,
            '비밀번호 정책을 먼저 만족해주세요.',
            'error'
        );
        return false;
    }

    if (v1 !== v2) {
        showMsg(
            passwordMatchMsg,
            '비밀번호가 일치하지 않습니다.',
            'error'
        );
        return false;
    }

    showMsg(
        passwordMatchMsg,
        '비밀번호가 일치합니다.',
        'success'
    );
    return true;
}

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
   사업자번호
====================== */
businessNumber.addEventListener('input', () => {
    businessNumber.value = businessNumber.value.replace(/[^0-9]/g, '');

    businessChecked = false;
    showMsg(
        businessMsg,
        '사업자번호 확인이 필요합니다.',
        'error'
    );

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

    // 1단계: 프론트 유효성
    if (!validateBusinessNumberFormat(v)) {
        businessChecked = false;
        updateSignupButtonState();
        return;
    }

    try {
        // 2단계: 서버 검증
        const json = await publicFetch(
            `/api/companies/check-business-number?businessNumber=${encodeURIComponent(v)}`
        );

        showMsg(businessMsg, json.message, 'success');
        businessChecked = true;

    } catch (e) {
        showMsg(
            businessMsg,
            e?.message || '유효하지 않은 사업자번호입니다.',
            'error'
        );
        businessChecked = false;
    }

    updateSignupButtonState();
}

/* ======================
   공통 입력 이벤트
====================== */
[
    companyName, address, representativeName, adminEmail
].forEach(el => el.addEventListener('input', updateSignupButtonState));

/* ======================
   제출 (SweetAlert 적용)
====================== */
async function submitSignup() {
    if (signupBtn.disabled) {

        if (!companyNameChecked) {
            showMsg(companyNameMsg, '회사명 중복 확인이 필요합니다.', 'error');
        }

        if (!emailChecked) {
            showMsg(emailMsg, '이메일 중복 확인이 필요합니다.', 'error');
        }

        validateAddress();
        validateRepresentativeName();
        validateEmail();
        validateContact();
        validatePasswordLength();
        validatePasswordMatch();

        sweetWarning('입력값을 다시 확인해주세요.');
        return;
    }


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
        const result = await publicFetch('/api/companies', {
            method: 'POST',
            body: JSON.stringify(body)
        });

        await sweetSuccess(
            result?.message || '회사 가입이 완료되었습니다.'
        );
        location.href = '/login';

    } catch (e) {
        await sweetError(
            e?.message || '회사 가입 중 오류가 발생했습니다.'
        );
    }
}

/* ======================
   이메일 중복 체크
====================== */
async function checkEmailDuplicate() {
    const v = adminEmail.value.trim();
    if (!validateEmail()) {
        emailChecked = false;
        return;
    }

    try {
        const json = await publicFetch(
            `/api/companies/check-email?email=${encodeURIComponent(v)}`
        );

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

/* ======================
   publicFetch
====================== */
async function publicFetch(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });

    const text = await res.text();
    const json = text ? JSON.parse(text) : null;

    if (!res.ok) {
        throw json || {message: '요청 처리 중 오류가 발생했습니다.'};
    }

    return json;
}

/* ======================
   비밀번호 보기 / 가리기 토글
====================== */
document.querySelectorAll('.pw-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
        const targetId = btn.dataset.target;
        const input = document.getElementById(targetId);
        if (!input) return;

        const isHidden = input.type === 'password';
        input.type = isHidden ? 'text' : 'password';

        btn.classList.toggle('active', isHidden);

        const icon = btn.querySelector('i');
        if (icon) {
            icon.classList.toggle('fa-eye', !isHidden);
            icon.classList.toggle('fa-eye-slash', isHidden);
        }
    });
});


function validateBusinessNumberFormat(v) {
    // 예: 10자리 사업자번호
    if (!/^\d{10}$/.test(v)) {
        showMsg(
            businessMsg,
            '사업자번호는 숫자 10자리여야 합니다.',
            'error'
        );
        return false;
    }
    return true;
}


async function checkCompanyName(event) {
    const btn = event.target;
    if (companyNameChecked) return;

    btn.disabled = true;

    try {
        const v = companyName.value.trim();

        if (!v) {
            showMsg(companyNameMsg, '회사명을 입력해주세요.', 'error');
            companyNameChecked = false;
            return;
        }

        const json = await publicFetch(
            `/api/companies/check-company-name?name=${encodeURIComponent(v)}`
        );

        showMsg(companyNameMsg, json.message, 'success');
        companyNameChecked = true;

    } catch (e) {
        showMsg(
            companyNameMsg,
            e?.message || '이미 사용 중인 회사명입니다.',
            'error'
        );
        companyNameChecked = false;
    } finally {
        btn.disabled = false;
        updateSignupButtonState();
    }
}
