let currentEmployeeId = null;
let loginEmployeeId = null;

/* =========================
   상수
========================= */
const EMPLOYMENT_LABEL = {
    REGULAR: '정규직',
    NON_REGULAR: '비정규직',
    CONTRACT: '계약직'
};

const WORK_STATUS_LABEL = {
    WORKING: '근무중',
    AWAY: '자리비움',
    VACATION: '휴가중',
    BUSINESS_TRIP: '출장',
    OUTWORK: '외근',
    OFF_WORK: '퇴근'
};

/* =========================
   초기 로딩
========================= */
document.addEventListener('DOMContentLoaded', async () => {

    // 로그인 사용자 정보
    try {
        const res = await apiFetch('/api/auth/me');
        const result = await res.json();
        loginEmployeeId = Number(result.data.employeeId);
    } catch {
        console.warn('로그인 사용자 정보 조회 실패');
    }

    // 조직 트리 로드
    apiFetch('/api/user/organizations/tree')
        .then(res => res.json())
        .then(res => renderOrgTree(res.data));

    // 버튼 이벤트 (한 번만 바인딩)
    const myBtn = document.getElementById('myActionBtn');
    if (myBtn) {
        myBtn.addEventListener('click', openSignatureModal);
    }

    // 초기 선택
    if (typeof targetEmployeeId !== 'undefined' && targetEmployeeId) {
        loadEmployeeDetail(targetEmployeeId);
    }
});

/* =========================
   Org Tree
========================= */
function renderOrgTree(nodes) {
    const container = document.getElementById('org-tree-container');
    container.innerHTML = '';
    nodes.forEach(n => renderNode(n, container, 0));
}

function renderNode(node, parent, depth) {
    const org = document.createElement('div');
    org.className = 'org-node';
    org.style.paddingLeft = `${8 + depth * 14}px`;
    org.textContent = node.orgName;
    parent.appendChild(org);

    node.employees.forEach(emp => {
        const el = document.createElement('div');
        el.className = 'org-emp';
        el.style.paddingLeft = `${24 + depth * 14}px`;
        el.textContent = emp.name;
        el.onclick = () => {
            document.querySelectorAll('.org-emp')
                .forEach(e => e.classList.remove('active'));
            el.classList.add('active');
            loadEmployeeDetail(emp.employeeId);
        };
        parent.appendChild(el);
    });

    node.children.forEach(c => renderNode(c, parent, depth + 1));
}

/* =========================
   Employee Detail
========================= */
async function loadEmployeeDetail(id) {
    currentEmployeeId = Number(id);

    const panel = document.getElementById('employee-panel');
    panel.classList.remove('hidden');

    const myBtn = document.getElementById('myActionBtn');
    if (myBtn) myBtn.classList.add('hidden');

    const res = await apiFetch(`/api/employees/${id}`);
    const {data: e} = await res.json();

    // 기본 정보
    document.getElementById('empAvatar').src =
        e.gender === 'FEMALE' ? '/images/female.png' : '/images/male.png';

    document.getElementById('empName').textContent = e.name;
    document.getElementById('empSummary').textContent =
        [e.orgPath, e.positionName, e.rankName].filter(Boolean).join(' · ');

    document.getElementById('empEmail').textContent = e.email ?? '-';
    document.getElementById('empInternalPhone').textContent = e.internalPhone ?? '-';
    document.getElementById('empEmployeeNo').textContent = e.employeeNo ?? '-';
    document.getElementById('empEmploymentType').textContent =
        EMPLOYMENT_LABEL[e.employmentType] ?? '-';
    document.getElementById('empHireDate').textContent =
        e.hireDate ? e.hireDate.replaceAll('-', '.') : '-';

    document.getElementById('empOrg').textContent = e.orgPath ?? '-';
    document.getElementById('empRank').textContent = e.rankName ?? '-';
    document.getElementById('empPosition').textContent = e.positionName ?? '-';

    // 본인 여부 판단
    if (myBtn && loginEmployeeId === currentEmployeeId) {
        myBtn.classList.remove('hidden');

        const hasSignature = await checkMySignature();
        myBtn.textContent = hasSignature
            ? '결재 서명 변경'
            : '결재 서명 등록';
    }

    await loadEmployeeWorkStatus(id);
}

/* =========================
   근무 상태
========================= */
async function loadEmployeeWorkStatus(employeeId) {
    const res = await apiFetch(`/api/attendance/work-status/${employeeId}`);
    const {data} = await res.json();

    if (Number(employeeId) !== currentEmployeeId) return;

    document.getElementById('empWorkStatus').textContent =
        WORK_STATUS_LABEL[data.workStatus] ?? '-';

    const dot = document.getElementById('empWorkDot');
    applyWorkStatus(dot, data.workStatus, 'hero-work-dot');

}

/* =========================
   Signature Modal
========================= */
async function checkMySignature() {
    try {
        const res = await apiFetch('/api/approval/signature/me');
        if (!res.ok) return false;

        const result = await res.json();
        return result.data.exists === true;
    } catch {
        return false;
    }
}

async function openSignatureModal() {

    const modal = document.getElementById('signatureModal');
    if (!modal) return;

    modal.classList.remove('hidden');


    // 항상 초기화
    signatureInput.value = '';
    previewImg.src = '';
    previewImg.style.display = 'none';
    placeholder.style.display = 'flex';
    removeBtn.style.display = 'none';


    try {
        const res = await apiFetch('/api/approval/signature/me');
        if (!res.ok) return;

        const {data} = await res.json();
        if (!data.exists) return;

        document.getElementById('signaturePreviewImage').src = data.imageUrl;
        document.getElementById('signaturePreviewImage').style.display = 'block';
        document.getElementById('signaturePlaceholder').style.display = 'none';
        document.getElementById('signatureRemoveBtn').style.display = 'inline-block';
    } catch {
    }
}

function closeSignatureModal() {
    document.getElementById('signatureModal')?.classList.add('hidden');
}

document.getElementById('signatureCancelBtn')
    ?.addEventListener('click', closeSignatureModal);


/* =========================
   Signature Upload Logic
========================= */

const signatureInput = document.getElementById('signatureInput');
const previewBox = document.getElementById('signaturePreviewBox');
const previewImg = document.getElementById('signaturePreviewImage');
const placeholder = document.getElementById('signaturePlaceholder');
const removeBtn = document.getElementById('signatureRemoveBtn');
const saveBtn = document.getElementById('signatureSaveBtn');

/* 클릭 → 파일 선택 */
previewBox?.addEventListener('click', () => {
    signatureInput.click();
});

/* 파일 선택 시 */
signatureInput?.addEventListener('change', () => {
    const file = signatureInput.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        sweetWarning('이미지 파일만 업로드 가능합니다.');
        signatureInput.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = e => {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
        placeholder.style.display = 'none';
        removeBtn.style.display = 'inline-block';
    };
    reader.readAsDataURL(file);
});


removeBtn?.addEventListener('click', () => {
    signatureInput.value = '';
    previewImg.src = '';
    previewImg.style.display = 'none';
    placeholder.style.display = 'flex';
    removeBtn.style.display = 'none';
});


saveBtn?.addEventListener('click', async () => {
    const file = signatureInput.files[0];

    if (!file) {
        sweetWarning('서명 이미지를 선택해주세요.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        saveBtn.disabled = true;
        saveBtn.textContent = '저장 중...';

        const res = await apiFetch('/api/approval/signature', {
            method: 'POST',
            body: formData
        });

        if (!res.ok) {
            sweetError('서명 저장에 실패했습니다.');
            return;
        }

        await sweetSuccess('서명이 저장되었습니다.');

        // 모달 닫기
        closeSignatureModal();

        // 버튼 텍스트 갱신
        const myBtn = document.getElementById('myActionBtn');
        if (myBtn) {
            myBtn.textContent = '결재 서명 변경';
        }

        // 입력 초기화
        signatureInput.value = '';
        previewImg.src = '';
        previewImg.style.display = 'none';
        placeholder.style.display = 'flex';
        removeBtn.style.display = 'none';

    } catch (e) {
        console.error(e);
        sweetError('서명 저장 중 오류가 발생했습니다.');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = '서명 등록';
    }
});

const WORK_STATUS_CLASS = {
    WORKING: 'working',
    AWAY: 'away',
    VACATION: 'vacation',
    BUSINESS_TRIP: 'business',
    OUTWORK: 'outwork',
    OFF_WORK: 'off'
};

function applyWorkStatus(dotEl, workStatus, baseClass) {
    if (!dotEl) return;

    dotEl.className = baseClass;
    const cls = WORK_STATUS_CLASS[workStatus];
    if (cls) {
        dotEl.classList.add(cls);
    }
}
