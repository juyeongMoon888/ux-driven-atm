import { fetchWithAuth } from "/js/lib/fetchWithAuth.js";
import { fetchJsonSafe } from "/js/lib/fetchJsonSafe.js";
import { handleNetworkOrApiError } from "/js/lib/network/handleNetworkOrApiError.js";
import { handleApiFailure } from "/js/lib/api/handleApiFailure.js";

document.addEventListener("DOMContentLoaded", main);
let myAccountList, bankType, accountNumber, balance;
function main() {
    initElement();
    bindEvents();
    myAccount();
}

function initElement() {
    myAccountList = document.getElementById("myAccountList");
    bankType = document.getElementById("bankType");
    accountNumber = document.getElementById("accountNumber");
    balance = document.getElementById("balance");
}
function bindEvents() {
    bindDynamicButtonEvents();
}
async function myAccount() {
    let res, parsed;

    try {
        res = await fetchWithAuth("/api/bank/account-list", {
            method: "GET",
            credentials: "include"
        });
        parsed = await fetchJsonSafe(res);

        const accounts = parsed.data;
        if (res.ok) {
            if (Array.isArray(accounts)) {
                for (const account of accounts) {
                    const accountDiv = document.createElement("div");
                    accountDiv.innerHTML = `
                        <p>은행명: ${account.bankType}</p>
                        <p>계좌번호: ${account.accountNumber}</p>
                        <p>잔고: ${account.balance}</p>
                        <button class = "btn btn-primary transferBtn" data-account="${account.accountNumber}">입출금</button>
                        <button class = "btn btn-primary historyBtn" data-account="${account.accountNumber}">거래내역 보기</button>
                        <hr/>
                    `;
                    myAccountList.appendChild(accountDiv);
                }

                localStorage.setItem("account", JSON.stringify(accounts));
                return true;
            }
        }else {
            handleApiFailure(res, parsed);
        }
    } catch (err) {
        handleNetworkOrApiError(err);
    }
}

function bindDynamicButtonEvents() {
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("transferBtn")) {
            const accountNumber = e.target.dataset.account;

            location.href = `/bank/transfer?accountNumber=${accountNumber}`;
        }
    });
}