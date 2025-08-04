import { fetchWithAuth } from "./lib/fetchWithAuth.js"
import { fetchJsonSafe } from "./lib/fetchJsonSafe.js"

document.addEventListener("DOMContentLoaded", main);
let accountForm, bankSelect, passwordInput, accountNameInput;
function main() {
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
    console.log("✅ JS로 요청 시도 중");

    const account = {
        bank: bankSelect.value,
        password: passwordInput.value,
        accountName: accountNameInput.value
    }

    let res, parsed;
    try {
        res = await fetchWithAuth("/api/accounts", {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(account)
        });

        parsed = await fetchJsonSafe(res);

        if (!parsed.ok) {
            if (parsed.code == "BANK_INVALID") {
                const errors = {bankSelect: "BANK_INVALID"}
                showFieldErrors(errors, [bankSelect]);
            }
        } else {
            alert(parsed.message || "계좌생성이 완료되었습니다.")
        }

    } catch (err) {
        console.error("요청 실패", err);
    }
}
