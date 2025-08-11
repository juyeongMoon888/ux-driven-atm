import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

document.addEventListener("DOMContentLoaded", main);
let transferForm, amountInput, memoInput;

function main() {
    initElement();
    bindEvents();
}

function initElement() {
    transferForm = document.getElementById("transferForm");
    amountInput = document.getElementById("amount");
    memoInput = document.getElementById("memo");
}

function bindEvents() {
    if (transferForm) {
        transferForm.addEventListener("submit", handleTransferProgress);
    } else {
        console.warn("❗ transferForm이 DOM에 없음");
    }
}

async function handleTransferProgress(e) {
    e.preventDefault();

    let res, parsed;
    const type = document.querySelector('input[name="type"]:checked').value;
    const accountNumber = transferForm.dataset.accountNumber;
    const amount = amountInput.value;
    const memo = memoInput.value;

    try {
        res = await fetchWithAuth("/api/bank/transfer", {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                type, accountNumber, amount, memo
            })
        });
        parsed = await fetchJsonSafe(res);

        if (res.ok) {
            location.href = "/bank/accounts";
            return true;
        } else {
            handleApiFailure(res, parsed);
        }

    } catch (err) {
        handleNetworkOrApiError(err);
    }
}