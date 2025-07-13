import ApiError from "./errors/ApiError.js";
import { getAccessToken, getUserFromLocalStorage, tryOnceToDetectRecovery } from "./lib/utils.js"

document.addEventListener("DOMContentLoaded", main);
let loginBtn, logoutBtn, greetingEl;

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
}

function initUI() {
    const accessToken = getAccessToken();
    const user = getUserFromLocalStorage();

    if (accessToken && user) {
        setUIToLoggedIn();
        greetingEl.textContent=`안녕하세요,${user.name}님`;
    }
}

function bindEvents() {
    logoutBtn.addEventListener("click", handleLogout);
}

function maybeFetchUserInfo() {
    const accessToken = getAccessToken();
    if (accessToken) fetchMyInfo(accessToken);
}

function setUIToLoggedIn() {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "block";
}

function setUIToLoggedOut() {
    loginBtn.style.display = "block";
    logoutBtn.style.display = "none";
    if (greetingEl) {
        greetingEl.textContent = "";
    }
}
function handleLogout() {
    localStorage.removeItem("accessToken");
    location.reload();
}

function resetUIToLoggedOut() {
    setUIToLoggedOut();
    localStorage.removeItem("accessToken")
}

async function fetchMyInfo(accessToken) {
    try {
        const response = await fetch("/api/users/me", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            const errorData = await response.json();

            if (response.status === 401) {
                throw new ApiError (errorData.code, errorData.message);
            } else if (response.status === 500) {
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } else {
                throw new Error (`알 수 없는 오류 (code: ${response.status})`);
            }
        }

        const data = await response.json();
        greetingEl.textContent=`안녕하세요,${data.name}님`;
    }
    catch (err) {
        resetUIToLoggedOut();

        if (err instanceof ApiError && err.code === "SESSION_EXPIRED") {
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            location.href = "/login";
        } else if (err instanceof TypeError && err.message === "Failed to fetch") {
            alert("서버가 꺼졌습니다. 복구를 기다립니다...");
            tryOnceToDetectRecovery(); // 서버 꺼졌을 때만 감시 시작
        } else {
            alert(err.message);
        }
    };
}




