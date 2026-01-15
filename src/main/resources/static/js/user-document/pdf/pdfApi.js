import {fetchPresignedDownloadUrlByFileId} from "/js/user-document/file/fileApi.js";


export async function fetchPdfStatus(documentId) {
    const res = await apiFetch(`/api/document-file/${documentId}/pdf`);

    if (!res.ok) {
        // 응답 바디가 JSON일 수도, 텍스트일 수도 있어서 둘 다 대비
        const contentType = res.headers.get("content-type") || "";
        let body = "";

        try {
            body = contentType.includes("application/json")
                ? JSON.stringify(await res.json())
                : await res.text();
        } catch (e) {
            body = "(failed to read response body)";
        }

        console.error("[PDF STATUS] request failed", {
            url: `/api/document-file/${documentId}/pdf`,
            status: res.status,
            statusText: res.statusText,
            body
        });

        throw new Error(`PDF 상태 조회 실패 (HTTP ${res.status})`);
    }

    const json = await res.json();
    return json.data; // { status, pdfFileId }
}


export async function downloadPdfByFileId(pdfFileId) {
    const url = await fetchPresignedDownloadUrlByFileId(pdfFileId);
    const a = document.createElement("a");
    a.href = url;
    a.download = "";
    document.body.appendChild(a);
    a.click();
    a.remove();
}
