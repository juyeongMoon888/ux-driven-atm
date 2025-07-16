export function showValidationMessages(details) {
    for (const field in details) {
        const message = details[field];
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
    const contentType = res.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        const errorData = await res.json();

        if (res.status === 401) {
            throw new ApiError(errorData.code, errorData.message);
        } else if (res.status === 500) {
            throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        } else {
            throw new Error (`알 수 없는 오류 (code: ${status})`);
        }
    } else {
        const text = await res.text();

        if (res.status = 401) {
            throw new Error(`인증 오류: ${text}`);
        } else if (res.status === 500) {
            throw new Error("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        } else {
            throw new Error(`알 수 없는 오류 (code: $(res.status}) - ${text}`);
        }
    }
}
