import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";

document.addEventListener("DOMContentLoaded", main);
let historyDetail;

function main() {
    initElement();
    bindEvents();
    accountHistoryDetail();
}

function initElement() {
    historyDetail = document.getElementById("historyDetail");
}

function bindEvents(){
    bindDynamicButtonEvents();
}

async function accountHistoryDetail() {
    let res, parsed;
    const txId = historyDetail.dataset.transactionId;

    try {
        res = await fetchWithAuth(`/api/bank/account-history/${transactionId}`, {
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
                        <p>거래시각: ${transaction.createdAt}</p>
                        <p>거래유형: ${transferMap[transaction.transfer] || "알 수 없음"}</p>
                        <p>금액: ${formatKRW(transaction.amount)}원</p>
                        <p>거래 후 잔액: ${formatKRW(transaction.balanceAfter)}원</p>
                        <p>메모: ${transaction.memo}</p>
                        <hr/>
                   `;
                   historyDetail.appendChild(txDiv);
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

function formatKRW(value) {
  if (value == null) return "-";
  const n = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(n)) return "-";
  return n.toLocaleString("ko-KR");
}