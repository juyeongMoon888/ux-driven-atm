import { ApiError } from "/js/errors/ApiError.js";
import { showErrorMessagesFromServer, tryOnceToDetectRecovery } from "/js/lib/utils.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { ErrorCode } from "/js/lib/constants/errorMessages.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";

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

        if (res.ok) {
            accessToken = parsed.accessToken;
            userInfo = parsed.user;
            alert("로그인 성공!");
        } else {
            handleApiFailure(res,parsed);
        }
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("user", JSON.stringify(userInfo));
        window.location.href = "/";
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}

