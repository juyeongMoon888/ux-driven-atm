import { showValidationMessages } from "./lib/utils.js";

let signupForm, loginInput, passwordInput, nameInput, residentNumberInput, genderInput, phoneNumberInput, idCheckResult;

document.addEventListener("DOMContentLoaded", main);

function main() {
    initElements();
    bindEvents();
}

function initElements() {
    signupForm = document.getElementById("signupForm");
    loginIdInput = document.getElementById("loginId");
    passwordInput = document.getElementById("password");
    nameInput = document.getElementById("name");
    residentNumberInput = document.getElementById("residentNumber");
    genderInput = document.getElementById("gender");
    phoneNumberInput = document.getElementById("phoneNumber");
    idCheckResult = document.getElementById("id-check-result");
}

function bindEvents() {
    signupForm.addEventListener("submit", handleSubmitSignup);
}

async function handleSubmitSignup(e) {
    e.preventDefault();

    if(signupForm.dataset.idChecked !== "true") {
        alert("아이디 중복 확인을 먼저 해주세요.");
        return;
    }

    const user = {
        loginId: loginIdInput.value,
        password: passwordInput.value,
        name: nameInput.value,
        residentNumber: residentNumberInput.value,
        gender: genderInput.value,
        phoneNumber: phoneNumberInput.value
    };

    try {
        const res = await fetch("/api/users/signup", {
            method: "POST",
            headers: {
            "Content-Type": "application/json"
            },
            body: JSON.stringify(user)
        });

        const data = await res.json();

        if (data.code === "SIGNUP_SUCCESS") {
            alert(data.message || "회원가입 성공");
            location.href="/login";
        } else if (data.code === "VALIDATION_FAILED"){
            showValidationMessages(data.details);
        } else {
            alert(data.message || "회원가입 실패");
        }
    } catch(err) {
        if (err instanceof TypeError && err.message === "Failed to fetch") {
            alert("서버와 연결할 수 없습니다. 인터넷 연결을 확인해주세요.");
            tryOnceToDetectRecovery();
        } else {
            alert("예상치 못한 오류가 발생했습니다.");
        }
    }
}

//아이디 중복 확인 메서드
async function checkLoginId() {
    const loginId = loginIdInput.value;

    if(!loginId) {
        setIdCheckResult("아이디를 입력해주세요", "red", "false");
        return;
    }

    try {
        const res = fetch(`/api/users/check-id?loginId=${encodeURIComponent(loginId)}`)

        if (!res.ok) throw new Error("서버 오류");

        const data = await res.json();

        if (data.duplicate) {
            setIdCheckResult("사용 가능한 아이디입니다.", "green", "true");
        } else {
            setIdCheckResult("이미 사용 중인 아이디입니다.", "red", "false");
        }
    } catch (err) {
        setIdCheckResult("서버 오류가 발생했습니다.", "red", "false");
    }
}

function setIdCheckResult(message, color, valid) {
    idCheckResult.innerText =  message;
    idCheckResult.style.color = color;
    signupForm.dataset.idChecked = valid ? "true" : "false";
}


