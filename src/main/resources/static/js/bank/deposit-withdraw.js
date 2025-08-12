import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

document.addEventListener("DOMContentLoaded", main);
let depositWithdrawForm, amountInput, memoInput;

function main() {
    initElement();
    bindEvents();
}

function initElement() {
    depositWithdrawForm = document.getElementById("depositWithdrawForm");
    amountInput = document.getElementById("amount");
    memoInput = document.getElementById("memo");
}

function bindEvents() {
    if (depositWithdrawForm) {
        depositWithdrawForm.addEventListener("submit", handleDepositWithdrawSubmit);
    } else {
        console.warn("❗ depositWithdrawForm DOM에 없음");
    }
}

async function handleDepositWithdrawSubmit(e) {
    e.preventDefault();

    let res, parsed;
    const type = document.querySelector('input[name="type"]:checked').value;
    const accountNumber = depositWithdrawForm.dataset.accountNumber;
    const amount = amountInput.value;
    const memo = memoInput.value;

    //deposit, withdraw 분리
    const url = type === "DEPOSIT"
        ? "/api/bank/deposit"
        : "/api/bank/withdraw"

    try {
        res = await fetchWithAuth(url, {
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