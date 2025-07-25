import ApiError from "./errors/ApiError.js";
import { getAccessToken, getUserFromLocalStorage, tryOnceToDetectRecovery } from "./lib/utils.js"
import { fetchWithAuth } from "./lib/fetchWithAuth.js"


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
    const cachedUser = getUserFromLocalStorage();

    if (!accessToken) return;

    if (cachedUser) {
        setUIToLoggedIn();
        greetingEl.textContent=`안녕하세요,${cachedUser.name}님`;
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
async function handleLogout() {
    try {
        await fetchWithAuth("/api/auth/logout", {
           method: "POST"
        });
    } catch (err) {
        console.warn("서버 로그아웃 실패", err);
    } finally {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");
        location.reload();
    }
}

function resetUIToLoggedOut() {
    setUIToLoggedOut();
    localStorage.removeItem("accessToken")
}

async function fetchMyInfo(accessToken) {
    try {
        const res = await fetchWithAuth("/api/users/me");

        if (res === null) {
            return;
        }
        if (res.ok) {
            const user = await res.json();
            greetingEl.textContent=`안녕하세요,${user.name}님`;
            localStorage.setItem("user", JSON.stringify(user));
        } else {
            const errorData = await res.json();

            if (res.status === 401) {
                 throw new ApiError (errorData.code, errorData.message);
            } else if (res.status === 500) {
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } else {
                throw new Error(`알 수 없는 오류 (code: ${res.status})`);
            }
        }
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



