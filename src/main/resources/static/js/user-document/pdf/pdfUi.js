// pdfUi.js
export function renderPdfGenerating(container) {
    const btn = document.createElement("button");
    btn.className = "action-btn secondary pdf-btn downloading";
    btn.disabled = true;
    btn.innerHTML = `
        <span class="spinner"></span>
        PDF 생성 중입니다…
    `;
    container.appendChild(btn);
}

export function renderPdfDownloadButton(container, onClick) {
    const btn = document.createElement("button");
    btn.className = "action-btn secondary pdf-btn";
    btn.textContent = "PDF 다운로드";
    btn.onclick = onClick;
    container.appendChild(btn);
}

// ✅ 추가
export function renderPdfDownloadingButton(container) {
    const btn = document.createElement("button");
    btn.className = "action-btn secondary pdf-btn downloading";
    btn.disabled = true;
    btn.innerHTML = `
        <span class="spinner"></span>
        PDF 다운로드 중…
    `;
    container.appendChild(btn);
}
