import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";
document.addEventListener("DOMContentLoaded", main);
let historyDetail, date, type, after, memoInput, saveBtn, transactionId;

async function main() {
    const ok = await checkTokenBeforeEnteringBank();
    if (!ok) return;
    initElement();
    accountHistoryDetail();
    bindEvents();
}

function initElement() {
    historyDetail = document.getElementById("historyDetail");
    date = document.getElementById("tx-date");
    type = document.getElementById("tx-type");
    after = document.getElementById("tx-after");
    memoInput = document.getElementById("tx-memo");
    saveBtn = document.getElementById("btn-memo-save");
}

function bindEvents() {
    saveBtn.addEventListener("click", saveTransactionMemo);
}

async function accountHistoryDetail() {
    let res, parsed;
    transactionId = historyDetail.dataset.transactionId;
    try {
        res = await fetchWithAuth(`/api/bank/account-history/${transactionId}`, {
            method: "GET",
            credentials: "include"
        });
        parsed = await fetchJsonSafe(res);
        const transaction = parsed.data;

        const transferMap = {
            DEPOSIT: "입금",
            WITHDRAW: "출금"
        };

        if (res.ok) {
            date.textContent = transaction.createdAt;
            type.textContent = transferMap[transaction.transfer] || "알 수 없음";
            after.textContent = formatKRW(transaction.balanceAfter);
            memoInput.value = transaction.memo ?? "";
        } else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}

function formatKRW(value) {
  if (value == null) return "-";
  const n = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(n)) return "-";
  return n.toLocaleString("ko-KR");
}

async function saveTransactionMemo(e) {
    e.preventDefault();
    let res, parsed;
    const memo = memoInput.value.trim();
    if (memo.length > 100) {
        alert("메모는 최대 100자입니다.")
        return;
    }

    try {
        res = await fetchWithAuth(`/api/bank/account-history/${transactionId}/memo`, {
            method: "PATCH",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ memo })
        });
        parsed = await fetchJsonSafe(res);

        if (res.ok) {
            location.href = `/bank/account-history?accountNumber=${parsed.data.accountNumber}`
            return true;
        } else {
            handleApiFailure(res, parsed);
        }
    } catch(err) {
        handleNetworkOrApiError(err);
    }
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