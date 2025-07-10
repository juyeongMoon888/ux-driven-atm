import ApiError from "./errors/ApiError.js";
import { showValidationMessages } from "./lib/utils.js";

document.addEventListener("DOMContentListener", () => {
    const form = document.getElementById("loginForm");
    form.addEventListener("submit", login);
});

async function login(e) {
    e.preventDefault();

    const user = {
        loginId: document.getElementById("loginId").value,
        password: document.getElementById("password").value
    }
    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(user)
        });

        const errorData = await response.json();

        if (!response.ok) {
            if (response.status === 400) {
                throw new ApiError (errorData.code, errorData.message);
            } else if (response.status === 401) {
                throw new ApiError (errorData.code, errorData.message);
            } else if (response.status === 500) {
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } else {
                throw new Error ("알 수 없는 오류 (code: ${response.status});
            }
        }
        alert("로그인 성공!");
        localStorage.setItem("token");
        window.location.href = "/";
    }
    .catch (err) {
        if (err instanceOf ApiError && err.code === "VALIDATION_FAILED") {
            showValidationMessages(errorData.details);
        } else if (err instanceOf ApiError && err.code == "UNAUTHORIZED") {
            alert(err.message);
            location.href = "/login";
        } else if (err instanceOf TypeError && error.message === "Failed to fetch") {
            alert("서버와 연결할 수 없습니다. 인터넷 연결을 확인해주세요.");
            location.href = "/login";
            tryOnceToDetectRecovery();
        }
    };
}

// 서버 복구 확인
function tryOnceToDetectRecovery() {
    setTimeout(() => {
        fetch("/api/ping")
            .then(response => {
                if (response.ok) {
                localStorage.removeItem("token");
                location.reload();
                }
            })
            .catch(() => {
                console.warn("서버 여전히 꺼져 있음");
            });
    }, 5000);
}