import {renderDocumentContent} from "/js/user-document/renderer.js";

/* =========================================================
 * API
 * ========================================================= */

async function fetchPreviewData() {
    const res = await apiFetch(
        `/api/admin/form-templates/${templateId}/preview`
    );

    if (!res.ok) {
        throw new Error("미리보기 데이터 조회 실패");
    }

    const json = await res.json();
    return json.data; // { meta, contentSchema }
}

/* =========================================================
 * Entry
 * ========================================================= */

document.addEventListener("DOMContentLoaded", async () => {
    try {
        const {contentSchema} = await fetchPreviewData();

        const container =
            document.getElementById("documentContentContainer");

        if (!contentSchema || !container) {
            throw new Error("미리보기 렌더링 대상 없음");
        }

        // ✅ detail과 동일한 렌더 파이프라인
        renderDocumentContent(contentSchema, container);

    } catch (e) {
        console.error(e);
        alert("미리보기 데이터를 불러오지 못했습니다.");
    }
});

/* =========================================================
 * Navigation
 * ========================================================= */

function handlePreviewBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        location.href = "/admin/form-templates";
    }
}

window.handlePreviewBack = handlePreviewBack;
