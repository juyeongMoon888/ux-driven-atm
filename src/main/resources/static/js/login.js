import ApiError from "./errors/ApiError.js";
import { showErrorMessagesFromServer, tryOnceToDetectRecovery } from "./lib/utils.js";
import { fetchJsonSafe } from "./lib/fetchJsonSafe.js";

document.addEventListener("DOMContentLoaded", main);
let loginForm, loginIdInput, passwordInput;

function main() {
    initElements();
    bindEvents();
}

function initElements() {
    loginForm = document.getElementById("loginForm");
    loginIdInput = document.getElementById("loginId");
    passwordInput = document.getElementById("password");
}

function bindEvents() {
    loginForm.addEventListener("submit", handleLoginSubmit);
}

let res, parsed;
    let accessToken, userInfo;

async function handleLoginSubmit(e) {
    e.preventDefault();

    const user = {
        loginId: loginIdInput.value,
        password: passwordInput.value
    };

    try {
        res = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(user)
        });

        parsed = await fetchJsonSafe(res);

            if (parsed.ok) {
                accessToken = parsed.accessToken;
                userInfo = parsed.user;
                console.log("userInfo", userInfo);
                alert("로그인 성공!");
            } else {
                await handleErrorResponse(parsed);
            }

            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("user", JSON.stringify(userInfo));
            window.location.href = "/";
        } catch (err) {
            console.error("[로그인 예외 발생]=", err);
            handleNetworkOrApiError(err, parsed);
        }
}

function handleNetworkOrApiError(err, parsed = {}) {
    if (err instanceof ApiError && err.code === "VALIDATION_FAILED") {
        showValidationMessages(parsed.data);
    } else if (err instanceof ApiError && err.code == "UNAUTHORIZED") {
        alert(err.message);
        location.href = "/login";
    } else if (err instanceof TypeError && err.message === "Failed to fetch") {
        alert("서버와 연결할 수 없습니다. 인터넷 연결을 확인해주세요.");
        tryOnceToDetectRecovery();
        location.href = "/login";
    } else {
        alert(err.message);
    }
}
