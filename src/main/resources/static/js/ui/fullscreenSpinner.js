let spinnerEl = null;

export function showFullscreenSpinner(message = "처리 중입니다...") {
    if (spinnerEl) return; // 중복 생성 방지

    spinnerEl = document.createElement("div");
    spinnerEl.id = "fullscreenSpinner";
    spinnerEl.innerHTML = `
        <div class="spinner-backdrop"></div>
        <div class="spinner-content">
            <div class="spinner"></div>
            <p class="spinner-text">${message}</p>
        </div>
    `;

    document.body.appendChild(spinnerEl);
    document.body.style.overflow = "hidden"; // 스크롤 잠금
}

export function hideFullscreenSpinner() {
    if (!spinnerEl) return;

    spinnerEl.remove();
    spinnerEl = null;
    document.body.style.overflow = "";
}
