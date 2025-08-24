import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"
import { getAccessToken } from "/js/lib/storage/getAccessToken.js"

document.addEventListener("DOMContentLoaded", main);
let accountCreateBtn, bankPage, myAccountToggleBtn, transferBtn;

async function main() {
    const ok = await checkTokenBeforeEnteringBank();
    if (!ok) return;
    initElement();
    bindEvents();
}

function initElement() {
    bankPage = document.getElementById("bankPage");
    accountCreateBtn = document.getElementById("accountCreateBtn");
    myAccountToggleBtn = document.getElementById("myAccountToggleBtn");
    transferBtn = document.getElementById("transferBtn");
}

function bindEvents() {
    if (accountCreateBtn) {
        accountCreateBtn.addEventListener("click", handleAccountCreate);
    } else {
        console.warn("accountCreateBtn이 DOM에 없음");
    }

    if (myAccountToggleBtn) {
         myAccountToggleBtn.addEventListener("click", handleAccountList);
    } else {
        console.warn("myAccountToggleBtn이 DOM에 없음");
    }

    if (transferBtn) {
        transferBtn.addEventListener("click", handleAccountTransfer);
    } else {
        console.warn("transferBtn이 DOM에 없음");
    }
}

async function handleAccountCreate() {
    location.href = "/bank/open-account";
}

async function handleAccountList() {
    location.href = "/bank/accounts";
}

async function handleAccountTransfer() {
    location.href = "/bank/transfer";
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
            location.replace("/login");
            return false;
        }
    } catch (err) {
        handleNetworkOrApiError(err);
        location.replace("/login");
        return false;
    }
}