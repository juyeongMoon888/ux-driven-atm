import { ErrorCode } from "/js/lib/constants/errorMessages.js";

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

export function getAccessToken() {
    return localStorage.getItem("accessToken");
}

export function getUserFromLocalStorage() {
    const userJson = localStorage.getItem("user");
    return userJson ? JSON.parse(userJson) : null;
}

export function tryOnceToDetectRecovery() {
    setTimeout(() => {
        fetch("/api/ping")
            .then(response => {
                if (response.ok) {
                localStorage.removeItem("accessToken");
                location.reload();
                }
            })
            .catch(() => {
                console.warn("서버 여전히 꺼져 있음");
            });
    }, 5000);
}

export async function handleErrorResponse(res) {
    const code = res.code || "UNKNOWN";
    const message = res.message || "알 수 없는 오류가 발생했습니다.";

    if (res.status === 401) {
        throw new ApiError(code, message);
    } else if (res.status === 500) {
        throw new Error (ERROR_MESSAGES.SERVER_ERROR);
    } else {
        throw new Error (`알 수 없는 오류 (code: ${res.status}) - ${message}`);
    }
}
