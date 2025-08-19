import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";

document.addEventListener("DOMContentLoaded", main);
let historyList;

async function main() {
    const ok = await checkTokenBeforeEnteringBank();
    if (!ok) return;
    initElement();
    bindEvents();
    accountHistory();
}

function initElement() {
    historyList = document.getElementById("historyList");
}

function bindEvents(){
    bindDynamicButtonEvents();
}

async function accountHistory() {
    let res, parsed;
    const accountNumber = historyList.dataset.accountNumber;

    try {
        res = await fetchWithAuth(`/api/bank/account-history?accountNumber=${accountNumber}`, {
            method: "GET",
            credentials: "include"
        });
        parsed = await fetchJsonSafe(res);

        const transactions = parsed.data;

        const transferMap = {
            DEPOSIT: "입금",
            WITHDRAW: "출금"
        };

        if (res.ok) {
            if (Array.isArray(transactions)) {
                for (const transaction of transactions) {
                   const txDiv = document.createElement("div")
                   txDiv.innerHTML = `
                        <p>내역번호: ${transaction.id}</p>
                        <p>거래시각: ${transaction.createdAt}</p>
                        <p>거래유형: ${transferMap[transaction.transfer] || "알 수 없음"}</p>
                        <p>금액: ${formatKRW(transaction.amount)}원</p>
                        <p>메모: ${transaction.memo}</p>
                        <button class="detail-btn" data-transaction-id="${transaction.id}">상세보기</button>
                        <hr/>
                   `;
                   historyList.appendChild(txDiv);
                }
                return true;
            }
        } else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}
function bindDynamicButtonEvents() {
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("detail-btn")) {
            const transactionId = e.target.dataset.transactionId;
            location.href = `/bank/account-history/${transactionId}`;
        }
    });
}

function formatKRW(value) {
  if (value == null) return "-";
  const n = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(n)) return "-";
  return n.toLocaleString("ko-KR");
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