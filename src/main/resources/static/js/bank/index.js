import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

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
}

function bindEvents() {
    if (accountCreateBtn) {
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
        const parsed = await fetchJsonSafe(res);

        if (res.ok) {
            return true;
        } else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}
