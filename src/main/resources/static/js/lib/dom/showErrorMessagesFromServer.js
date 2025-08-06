export function showErrorMessagesFromServer(data) {
    for (const field in data) {
        const code = data[field];

        const message = ErrorCode[code] || "입력값이 올바르지 않습니다.";
        const errorEl = document.getElementById(`${field}-error`);

        if (errorEl) {
            errorEl.textContent = message;
            errorEl.style.color = "red";
        }
    }
}