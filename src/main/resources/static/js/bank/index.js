
import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

document.addEventListener("DOMContentLoaded", main);
let accountCreateBtn, bankPage, myAccountToggleBtn;

async function main() {
    initElement();
    bindEvents();
    await checkTokenBeforeEnteringBank();
}

function initElement() {
    bankPage = document.getElementById("bankPage");
    accountCreateBtn = document.getElementById("accountCreateBtn");
    myAccountToggleBtn = document.getElementById("myAccountToggleBtn");
}

function bindEvents() {
    if (accountCreateBtn) {
        accountCreateBtn.addEventListener("click", handleAccountCreate);
    } else {
        console.warn("❗ accountCreateBtn이 DOM에 없음");
    }

    if (myAccountToggleBtn) {
         myAccountToggleBtn.addEventListener("click", handleAccountList);
    } else {
        console.warn("❗ myAccountToggleBtn이 DOM에 없음");
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
async function handleAccountList() {
    console.log("handleAccountList 진입");
    location.href = "/bank/accounts";
}