import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";

document.addEventListener("DOMContentLoaded", main);
let historyList;

function main() {
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
                for (const tx of transactions) {
                   const txDiv = document.createElement("div")
                   txDiv.innerHTML = `
                        <p>내역번호: ${tx.id}</p>
                        <p>거래시각: ${tx.createdAt}</p>
                        <p>거래유형: ${transferMap[tx.transfer] || "알 수 없음"}</p>
                        <p>금액: ${formatKRW(tx.amount)}원</p>
                        <button class="detail-btn" data-tx-id="${tx.id}">상세보기</button>
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
            const txId = e.target.dataset.txId;
            location.href = `/bank/account-history-detail?txId=${txId}`;
        }
    });
}
function formatKRW(value) {
  if (value == null) return "-";
  const n = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(n)) return "-";
  return n.toLocaleString("ko-KR");
}