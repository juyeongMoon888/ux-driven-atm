import { validateUser } from "/js/lib/validation/validateUser.js";
import { showFieldErrors } from "/js/lib/validation/renderFieldError.js";
import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js"
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js"

document.addEventListener("DOMContentLoaded", main);
let externalTransferFormEl, verifyBtnEl, bankSelectEl, fromAccountSelectEl, toAccountNumberEl, amountEl, memoEl, checkResultEl, transferBtnEl, amountSectionEl;

function main() {
    initElement();
    handleFromAccountSelect();
    bindEvents();
}

function initElement() {
    externalTransferFormEl = document.getElementById("externalTransferForm");
    verifyBtnEl = document.getElementById("verifyBtn");
    bankSelectEl = document.getElementById("bankSelect");
    fromAccountSelectEl = document.getElementById("fromAccountNumber");
    toAccountNumberEl = document.getElementById("toAccountNumber");
    amountEl = document.getElementById("amount");
    memoEl = document.getElementById("memo");
    transferBtnEl = document.getElementById("transferBtn");
    amountSectionEl = document.getElementById("amountSection");
}

function bindEvents() {
    if (verifyBtn) {
        verifyBtn.addEventListener("click", handleAccountVerify);
    } else {
        console.warn("verifyBtn DOM에 없음");
    }

    if (externalTransferFormEl) {
        externalTransferFormEl.addEventListener("submit", handleExternalTransfer);
    } else {
        console.warn("externalTransferFormEl DOM에 없음");
    }

    if (transferBtnEl) {
        transferBtnEl.addEventListener("click", handleExternalTransfer);
    } else {
        console.warn("transferBtnEl DOM에 없음");
    }
}

async function handleFromAccountSelect() {
    try {
        const res = await fetchWithAuth("/api/bank/account-options", {
                method: "GET",
                credentials: "include"
        });
        const parsed = await fetchJsonSafe(res);
        const accounts = parsed.data;
        if (res.ok) {
            const optionsHtml = (Array.isArray(accounts) ? accounts : []).map (acc =>
            `<option value="${acc.accountNumber}">
                [${acc.bank}] ${acc.accountNumber}
            </option>`
            ).join("");
            fromAccountSelectEl.innerHTML = optionsHtml;
        } else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}

async function handleAccountVerify(e) {
    e.preventDefault();
    const payload = {
        bank: bankSelectEl.value,
        toAccountNumber: toAccountNumberEl.value
    }

    const errors = validateUser(payload);
    if (errors) {
        showFieldErrors(errors, ["bank", "toAccountNumber"]);
        return;
    }

    let res, parsed;
    try {
        res = await fetchWithAuth("/api/bank/transfer/verify", {
            method: "POST",
            headers: {
                "Content-Type":"application/json"
            },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert("계좌검증이 완료되었습니다.");
            verifyBtn.disabled = true;

            amountSectionEl.style.display="block";
            return true;
        } else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}
//이체 + 사용자가 현재 선택한 계좌(번호) (문자열)
async function handleExternalTransfer(e) {
    e.preventDefault();

    //통제 변수
    /*if(form명.dataset.idChecked !== "true") {
        alert("계좌 검증 확인을 먼저 해주세요.");
        return;
    }*/

    try {
        let res, parsed
        const fromAccountNumber = fromAccountSelectEl.value;
        const bank = bankSelectEl.value;
        const toAccountNumber = toAccountNumberEl.value;
        const amount = amountEl.value;
        const memo = memoEl.value;
        const payload = { fromAccountNumber, bank, toAccountNumber, amount, memo };

        res = await fetchWithAuth("/api/bank/transfer", {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify(payload )
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
