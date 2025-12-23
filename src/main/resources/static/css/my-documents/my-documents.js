// ===============================
// 상태 관리 (page는 0-based)
// ===============================
let mydocState = {
    status: "ALL",
    searchType: "date",
    searchParams: {},
    page: 0,
    size: 10,
    totalPages: 1
};

// ===============================
// 초기화
// ===============================
function initMyDocumentsPage() {
    bindFilterEvents();
    renderSearchInputs("date");
    fetchAndRender();
}

// ===============================
// 이벤트 바인딩
// ===============================
function bindFilterEvents() {
    const statusEl = document.getElementById("statusFilter");
    const searchTypeEl = document.getElementById("searchType");
    const searchBtnEl = document.getElementById("searchBtn");

    if (statusEl) {
        statusEl.addEventListener("change", function () {
            mydocState.status = this.value;
            mydocState.page = 0;
            fetchAndRender();
        });
    }

    if (searchTypeEl) {
        searchTypeEl.addEventListener("change", function () {
            mydocState.searchType = this.value;
            renderSearchInputs(this.value);
        });
    }

    if (searchBtnEl) {
        searchBtnEl.addEventListener("click", function () {
            updateSearchParams();
            mydocState.page = 0;
            fetchAndRender();
        });
    }
}

// ===============================
// 검색 UI
// ===============================
function renderSearchInputs(type) {
    const area = document.getElementById("searchInputs");
    if (!area) return;

    if (type === "date") {
        area.innerHTML = `
            <input type="date" id="startDate">
            <span>~</span>
            <input type="date" id="endDate">
        `;
    } else {
        area.innerHTML = `
            <input type="text" id="textInput"
                   placeholder="${type === 'GROUP_NAME'
            ? '양식명을 입력하세요'
            : '문서 제목을 입력하세요'}">
        `;
    }
}

function updateSearchParams() {
    if (mydocState.searchType === "date") {
        mydocState.searchParams = {
            startDate: document.getElementById("startDate")?.value,
            endDate: document.getElementById("endDate")?.value
        };
    } else {
        mydocState.searchParams = {
            keyword: document.getElementById("textInput")?.value
        };
    }
}

// ===============================
// API 호출
// ===============================
function fetchMyDocuments(params) {
    const query = new URLSearchParams();

    if (params.status && params.status !== "ALL") {
        query.append("documentStatus", params.status);
    }

    query.append("page", String(params.page));
    query.append("size", String(params.size));

    if (params.searchType === "date") {
        if (params.startDate) query.append("startDate", params.startDate);
        if (params.endDate) query.append("endDate", params.endDate);
    } else {
        const keyword = (params.keyword || "").trim();
        if (keyword.length > 0) {
            query.append("keyword", keyword);
            query.append("searchType", params.searchType); // 서버 enum
        }
    }

    const url = `/api/documents/my-written?${query.toString()}`;
    console.log("REQUEST:", url);

    // ⭐ 핵심 수정: Response → JSON → data
    return apiFetch(url, {method: "GET"})
        .then(res => res.json())
        .then(json => json.data);
}


// ===============================
// 렌더링
// ===============================
function fetchAndRender() {
    const params = {
        status: mydocState.status,
        searchType: mydocState.searchType,
        ...mydocState.searchParams,
        page: mydocState.page,
        size: mydocState.size
    };

    fetchMyDocuments(params).then(res => {
        const pageData = res.data ?? res;
        console.log("PAGE DATA:", pageData);
        renderTable(pageData.content || []);
        mydocState.totalPages = pageData.totalPages ?? 1;
        renderPagination();
    });
}


function renderTable(docs) {
    const tbody = document.getElementById("mydocTbody");
    const nodata = document.getElementById("nodataMsg");

    if (!tbody || !nodata) return;

    tbody.innerHTML = "";

    if (!docs.length) {
        nodata.style.display = "block";
        return;
    }

    nodata.style.display = "none";

    docs.forEach(doc => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${doc.title}</td>
            <td>${doc.templateGroupName}</td>
            <td>${formatDate(doc.createdAt)}</td>
            <td>${getStatusText(doc.status)}</td>
            <td>${doc.approvalName ?? "-"}</td>
        `;

        tbody.appendChild(tr);
    });
}

// ===============================
// 페이지네이션 (0-based)
// ===============================
function renderPagination() {
    const pag = document.getElementById("pagination");
    if (!pag) return;

    const cur = mydocState.page;
    const total = mydocState.totalPages;

    pag.innerHTML = "";
    if (total <= 1) return;

    const prev = document.createElement("button");
    prev.textContent = "이전";
    prev.disabled = cur === 0;
    prev.onclick = () => {
        mydocState.page--;
        fetchAndRender();
    };
    pag.appendChild(prev);

    for (let i = 0; i < total; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === cur) btn.classList.add("active");
        btn.onclick = () => {
            mydocState.page = i;
            fetchAndRender();
        };
        pag.appendChild(btn);
    }

    const next = document.createElement("button");
    next.textContent = "다음";
    next.disabled = cur === total - 1;
    next.onclick = () => {
        mydocState.page++;
        fetchAndRender();
    };
    pag.appendChild(next);
}

// ===============================
// 유틸
// ===============================
function getStatusText(status) {
    switch (status) {
        case "DRAFT":
            return "작성중";
        case "SUBMITTED":
            return "상신";
        case "IN_PROGRESS":
            return "결재중";
        case "APPROVED":
            return "승인완료";
        case "REJECTED":
            return "반려";
        default:
            return status;
    }
}

function formatDate(isoString) {
    if (!isoString) return "-";
    const d = new Date(isoString);
    return d.toLocaleDateString("ko-KR");
}

// ===============================
// fragment 안전 실행
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    if (document.querySelector(".my-documents-page")) {
        initMyDocumentsPage();
    }
});
