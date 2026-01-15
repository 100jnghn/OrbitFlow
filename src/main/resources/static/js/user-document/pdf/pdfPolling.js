import {fetchPdfStatus, downloadPdfByFileId} from "./pdfApi.js";
import {
    renderPdfGenerating,
    renderPdfDownloadButton,
    renderPdfDownloadingButton
} from "./pdfUi.js";

let timer = null;
let downloadingPdf = false;

export function stopPdfPolling() {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
}

export async function initPdfArea(documentId, container) {
    container.querySelectorAll(".pdf-btn, .pdf-generating").forEach(e => e.remove());

    const {status, pdfFileId} = await fetchPdfStatus(documentId);

    // 1️⃣ 승인 전
    if (status === "NONE") {
        stopPdfPolling();
        return;
    }

    // 2️⃣ PDF 생성 중
    if (status === "GENERATING") {
        renderPdfGenerating(container);

        if (!timer) {
            timer = setInterval(() => {
                initPdfArea(documentId, container).catch(console.error);
            }, 5000);
        }
        return;
    }

    // 3️⃣ PDF 생성 완료
    if (status === "READY") {
        stopPdfPolling();

        renderPdfDownloadButton(container, () =>
            downloadPdfWithUi(container, pdfFileId)
        );
    }
}

/* =========================================================
 * PDF 다운로드 + UI 제어 (첨부파일 패턴과 동일)
 * ========================================================= */
async function downloadPdfWithUi(container, pdfFileId) {
    if (downloadingPdf) return;
    downloadingPdf = true;

    // 기존 버튼 제거 → 다운로드 중 버튼
    container.querySelector(".pdf-btn")?.remove();
    renderPdfDownloadingButton(container);

    const startTime = Date.now();

    try {
        // 👉 실제 다운로드 시작
        await downloadPdfByFileId(pdfFileId);

        // UX 안정용 최소 표시 시간
        const elapsed = Date.now() - startTime;
        if (elapsed < 600) {
            await new Promise(r => setTimeout(r, 600 - elapsed));
        }

    } catch (e) {
        console.error(e);
        if (typeof sweetWarning === "function") {
            await sweetWarning("PDF 다운로드 중 오류가 발생했습니다.");
        }
    } finally {
        container.querySelector(".pdf-btn.downloading")?.remove();
        renderPdfDownloadButton(container, () =>
            downloadPdfWithUi(container, pdfFileId)
        );

        downloadingPdf = false;
    }
}
