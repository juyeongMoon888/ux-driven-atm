import ApiError from "./errors/ApiError.js";
import { getAccessToken, getUserFromLocalStorage, tryOnceToDetectRecovery } from "./lib/utils.js"
import { fetchWithAuth } from "./lib/fetchWithAuth.js"
import { fetchJsonSafe } from "./lib/fetchJsonSafe.js"


document.addEventListener("DOMContentLoaded", main);
let loginBtn, logoutBtn, greetingEl, accountCreateBtn;

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
    accountCreateBtn = document.getElementById("accountCreateBtn");
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

async function maybeFetchUserInfo() {
    try {
        await fetchMyInfo();
    } catch (err) {
        console.log("유저 정보 확인 실패", err);
    }
}

function setUIToLoggedIn() {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "block";
    accountCreateBtn.style.display = "block";
}

function setUIToLoggedOut() {
    loginBtn.style.display = "block";
    logoutBtn.style.display = "none";
    accountCreateBtn.style.display = "none";
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

async function fetchMyInfo() {
        let res;
        let parsed;
    try {
        res = await fetchWithAuth("/api/users/me");
        if (!res) {
            console.error("fetchWithAuth 응답이 null입니다.");
            return;
        }
        parsed = await fetchJsonSafe(res);
        const status = res.status;

        if (parsed === null) {
            return;
        }
        if (parsed.ok) {
            greetingEl.textContent=`안녕하세요,${parsed.name}님`;
            localStorage.setItem("user", JSON.stringify(parsed));
        } else {
            if (status === 401) {
                 throw new ApiError (parsed.code, parsed.message);
            } else if (status === 500) {
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } else {
                throw new Error(`알 수 없는 오류 (code: ${parsed.status})`);
            }
        }
    }
    catch (err) {
        resetUIToLoggedOut();

        if (err instanceof ApiError) {
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            setTimeout(() => {
            window.location.href = "/login";
            }, 5000); // 1.5초 후 이동

        } else if (err instanceof TypeError && err.message === "Failed to fetch") {
            alert("서버가 꺼졌습니다. 복구를 기다립니다...");
            tryOnceToDetectRecovery(); // 서버 꺼졌을 때만 감시 시작
        } else {
            alert(err.message);
        }
    };
}



