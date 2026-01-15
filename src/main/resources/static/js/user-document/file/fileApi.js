// /js/common/fileApi.js

export async function fetchPresignedDownloadUrlByFileId(fileId) {
    const res = await apiFetch(`/api/files/${fileId}/presigned`);

    if (!res.ok) {
        throw new Error("다운로드 URL 생성 실패");
    }

    const json = await res.json();
    return json.data?.url;
}