import { fetchWithAuth } from "/js/lib/fetchWithAuth.js"
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js"
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

document.addEventListener("DOMContentLoaded", main);
let accountForm, bankSelect, passwordInput, accountNameInput;

async function main() {
    initElements();
    bindEvents();
}
function initElements() {
    accountForm = document.getElementById("accountForm");
    bankSelect = document.getElementById("bankSelect");
    passwordInput = document.getElementById("password");
    accountNameInput = document.getElementById("accountName");
}
function bindEvents() {
    accountForm.addEventListener("submit", handleSubmitAccount);
}
async function handleSubmitAccount(e) {
    e.preventDefault();

    const account = {
        bank: bankSelect.value,
        password: passwordInput.value,
        accountName: accountNameInput.value
    }

    let res, parsed;
    try {
        res = await fetchWithAuth("/api/bank/open-account", {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(account)
        });
        parsed = await fetchJsonSafe(res);

        if (res.ok) {
            alert(parsed.message);
            location.href = "/bank";
        } else {
            handleApiFailure(res, parsed);
        }

    } catch (err) {
        handleNetworkOrApiError(err);
    }
}
