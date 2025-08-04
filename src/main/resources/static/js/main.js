import ApiError from "./errors/ApiError.js";
import { getAccessToken, getUserFromLocalStorage, tryOnceToDetectRecovery } from "./lib/utils.js"
import { fetchWithAuth } from "./lib/fetchWithAuth.js"
import { fetchJsonSafe } from "./lib/fetchJsonSafe.js"


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
        bankBtn.addEventListener("click", checkTokenBeforeEnteringBank);
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
    console.log("handleLogout 진입");
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

        //UI 초기화: greeting 메시지, 버튼 상태 등
        resetUIToLoggedOut();

        //페이지 새로고침 또는 리다이렉트
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
        parsed = await fetchJsonSafe(res);
        const status = res.status;

        //refactor: json이 안오더라도 null은 아니기 때문에 이 코드는 지워야함.
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
            }, 5000);

        } else if (err instanceof TypeError && err.message === "Failed to fetch") {
            alert("서버가 꺼졌습니다. 복구를 기다립니다...");
            tryOnceToDetectRecovery(); // 서버 꺼졌을 때만 감시 시작
        } else {
            alert(err.message);
        }
    };
}
async function checkTokenBeforeEnteringBank() {
    try {
        const res = await fetchWithAuth("/api/auth/check", {
            method: "GET",
            credentials: "include"
        })

        if (res.ok) {
            location.href = "/bank";
        } else {
            location.href = "/login";
        }
    } catch (err) {
        console.log("요청 실패", err);
    }
}
function goToLogin() {
    location.href = "/login";
}




