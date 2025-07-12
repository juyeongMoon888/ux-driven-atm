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
