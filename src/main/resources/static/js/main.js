import ApiError from "/js/errors/ApiError.js";
import { getAccessToken, getUserFromLocalStorage, tryOnceToDetectRecovery } from "/js/lib/utils.js"
import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"


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
    const cachedUser = getUserFromLocalStorage();

    if (!accessToken) return;

    if (cachedUser) {
        setUIToLoggedIn();
        greetingEl.textContent=`안녕하세요,${cachedUser.name}님`;
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
    try {
        await fetchMyInfo();
    } catch (err) {
        console.log("유저 정보 확인 실패", err);
    }
}

function setUIToLoggedIn() {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "block";
    bankBtn.style.display = "block";
}

function setUIToLoggedOut() {
    loginBtn.style.display = "block";
    logoutBtn.style.display = "none";
    if (greetingEl) {
        greetingEl.textContent = "";
    }
    bankBtn.style.display = "none";
}

async function handleLogout() {
    try {
        await fetchWithAuth("/api/auth/logout", {
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
        let res;
        let parsed;
    try {
        res = await fetchWithAuth("/api/users/me");
        if (!res) {
            console.error("fetchWithAuth 응답이 null입니다.");
            return;
        }
        if (res?.error === ERROR_MESSAGES.TOKEN_EXPIRED) {
          alert(ERROR_MESSAGES.TOKEN_EXPIRED);
          location.href = "/login";
          return;
        }

        parsed = await fetchJsonSafe(res);
        const status = res.status;

        if (parsed.ok) {
            greetingEl.textContent=`안녕하세요,${parsed.name}님`;
            localStorage.setItem("user", JSON.stringify(parsed));
        } else {
            switch (status) {
                case 401:
                resetUIToLoggedOut();
                alert(ERROR_MESSAGES.TOKEN_EXPIRED);
                localStorage.removeItm("accessToken");
                location.href = "/login";
                break;

                case 500:
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

                default:
                throw new Error(`알 수 없는 오류 (code: ${status})`);
            }
        }
    }
    catch (err) {
        resetUIToLoggedOut();
        if (err instanceof TypeError && err.message === "Failed to fetch") {
            alert(ERROR_MESSAGES.NETWORK_DOWN);
            tryOnceToDetectRecovery();
        } else {
            alert(err.message || ERROR_MESSAGES.UNKNOWN);
        }
    };
}
function goToBank() {
    location.href = "/bank";
}
function goToLogin() {
    location.href = "/login";
}




