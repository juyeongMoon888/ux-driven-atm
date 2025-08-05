import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"

document.addEventListener("DOMContentLoaded", main);
let accountCreateBtn, bankPage;

async function main() {
    await checkTokenBeforeEnteringBank();
    initElement();
    bindEvents();
}

function initElement() {
    bankPage = document.getElementById("bankPage");
    accountCreateBtn = document.getElementById("accountCreateBtn");
    console.log("✅ accountCreateBtn:", accountCreateBtn);
}

function bindEvents() {
    if (accountCreateBtn) {
        console.log("✅ 이벤트 바인딩 시작됨");
        accountCreateBtn.addEventListener("click", handleAccountCreate);
    } else {
        console.warn("❗ accountCreateBtn이 DOM에 없음");
        console.log("accountCreateBtn이 생성되지 않았습니다.")
    }
}
async function handleAccountCreate() {
    location.href = "/bank/open-account";
}
async function checkTokenBeforeEnteringBank() {
    try {
        const res = await fetchWithAuth("/api/auth/check", {
            method: "GET",
            credentials: "include"
        })

        if (res.ok) {
            return true;
        } else {
            alert("로그인 후 이용 가능한 서비스입니다.")
            location.href = "/login";
            return;
        }
    } catch (err) {
        console.log("요청 실패", err);
        location.href = "/login";
        return false;
    }
}
