import { ApiError } from "/js/errors/ApiError.js";
import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { ErrorCode } from "/js/lib/constants/errorMessages.js";
import { getAccessToken } from "/js/lib/storage/getAccessToken.js";
import { getUserFromLocalStorage } from "/js/lib/storage/getUserFromLocalStorage.js";
import { tryOnceToDetectRecovery } from "/js/lib/network/tryOnceToDetectRecovery.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { showFieldErrors } from "/js/lib/validation/renderFieldError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";

document.addEventListener("DOMContentLoaded", main);
let loginBtn, logoutBtn, greetingEl, bankBtn;

function main () {
    initElement();
    initUI();
    bindEvents();
    maybeFetchUserInfo();
}

function initElement() {
    loginBtn = document.getElementById("loginBtn");
    logoutBtn = document.getElementById("logoutBtn");
    greetingEl = document.getElementById("greeting");
    bankBtn = document.getElementById("bankBtn");
}

function initUI() {
    const accessToken = getAccessToken();
    const user = getUserFromLocalStorage();

    if (accessToken && user) {
        setUIToLoggedIn();
        greetingEl.textContent=`안녕하세요,${user.name}님`;
    } else {
        setUIToLoggedOut();
    }
}

function bindEvents() {
    if (loginBtn) {
        loginBtn.addEventListener("click", goToLogin);
    } else {
        console.warn("loginBtn이 존재하지 않음 - 이벤트 바인딩 실패");
    }

    if (logoutBtn) {
        console.log("logoutBtn 바인딩 시도함")
        logoutBtn.addEventListener("click", handleLogout);
    } else {
        console.warn("logoutBtn이 존재하지 않음 - 이벤트 바인딩 실패");
    }

    if (bankBtn) {
        console.log("bankBtn 바인딩 시도함")
        bankBtn.addEventListener("click", goToBank);
    } else {
        console.warn("bankBtn이 존재하지 않음 - 이벤트 바인딩 실패");
    }
}

async function maybeFetchUserInfo() {
    console.log("maybeFetchUserInfo 진입")

    const token = localStorage.getItem("accessToken");
    if (token) {
        await fetchMyInfo();
    }
}

function setUIToLoggedIn() {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "block";
    bankBtn.style.display = "block";
}

function setUIToLoggedOut() {
console.log("✅ setUIToLoggedOut 실행됨");
    loginBtn.style.display = "block";
    logoutBtn.style.display = "none";
    if (greetingEl) {
        greetingEl.textContent = "";
    }
    bankBtn.style.display = "none";
}

async function handleLogout() {
    try {
        await fetch("/api/auth/logout", {
           method: "POST",
           credentials: "include"
        });
    } catch (err) {
        console.warn("서버 로그아웃 실패", err);
    } finally {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");

        resetUIToLoggedOut();

        location.reload();
    }
}

function resetUIToLoggedOut() {
    setUIToLoggedOut();
    localStorage.removeItem("accessToken")
}

async function fetchMyInfo() {
    console.log("fetchMyInfo 진입")

    let res, parsed;
    try {
        res = await fetchWithAuth("/api/users/me");
        parsed = await fetchJsonSafe(res);
        if (res.ok) {
            greetingEl.textContent=`안녕하세요,${parsed.name}님`;
            localStorage.setItem("user", JSON.stringify(parsed));
        } else {
            handleApiFailure(res, parsed);
        }
    }
    catch (err) {
        resetUIToLoggedOut();
        handleNetworkOrApiError(err);
    };
}
function goToBank() {
    location.href = "/bank";
}

function goToLogin() {
    location.href = "/login";
}






