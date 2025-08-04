import { showErrorMessagesFromServer, tryOnceToDetectRecovery } from "./lib/utils.js";
import { validateUser } from "./lib/validation/validateUser.js";
import { showFieldErrors } from "./lib/validation/renderFieldError.js";
import { fetchJsonSafe } from "./lib/fetchJsonSafe.js";

let signupForm, loginIdInput, passwordInput, emailInput, emailError, nameInput, residentNumberInput, genderInput, phoneNumberInput, idCheckResult, checkLoginIdBtn;

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
    emailInput = document.getElementById("email");
    residentNumberInput = document.getElementById("residentNumber");
    genderInput = document.getElementById("gender");
    phoneNumberInput = document.getElementById("phoneNumber");
    idCheckResult = document.getElementById("id-check-result");
    checkLoginIdBtn = document.getElementById("checkLoginIdBtn");
}

function bindEvents() {
    signupForm.addEventListener("submit", handleSubmitSignup);
    checkLoginIdBtn.addEventListener("click", checkLoginId);
}

async function handleSubmitSignup(e) {
    e.preventDefault();

    //통제 변수 유지
    if(signupForm.dataset.idChecked !== "true") {
        alert("아이디 중복 확인을 먼저 해주세요.");
        return;
    }

    const user = {
        loginId: loginIdInput.value,
        password: passwordInput.value,
        email:emailInput.value,
        name: nameInput.value,
        residentNumber: residentNumberInput.value,
        gender: genderInput.value,
        phoneNumber: phoneNumberInput.value
    };

    const start = performance.now();
    const errors = validateUser(user);

    if (errors) {
        showFieldErrors(errors, ["loginId", "password", "email", "name", "residentNumber", "gender", "phoneNumber" ]);
        const end = performance.now();
        return;
    }

    try {
        const res = await fetch("/api/users/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(user)
        });
        const parsed = await fetchJsonSafe(res);

        if (parsed.raw) {
            console.error("서버 응답이 JSON 형식이 아닙니다.");
            alert("서버 응답이 올바르지 않습니다.");
            return;
        }

        if (parsed.ok){
            if (parsed.code === "SIGNUP_SUCCESS") {
                alert(parsed.message || "회원가입 성공");
                location.href="/login";
            } else {
                 alert(parsed.message || "회원가입 실패");
            }
        }
        else if (parsed.code ===  "DATA_INTEGRITY_VIOLATION") {
            alert(res.message);
        }
        else if (parsed.code === "VALIDATION_FAILED"){
            showErrorMessagesFromServer(parsed.data);
        } else {
              alert(parsed.message || "알 수 없는 오류가 발생했습니다.");
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
        const res = await fetch(`/api/users/check-id?loginId=${encodeURIComponent(loginId)}`);
        const parsed = await fetchJsonSafe(res);

        if (parsed.raw) {
            console.error("서버 응답이 JSON 형식이 아닙니다.");
            alert("서버 응답이 올바르지 않습니다.");
            return;
        }

        if (parsed.ok) {
            if (parsed.duplicate) {
                setIdCheckResult("이미 사용 중인 아이디입니다.", "red", "false");
            } else {
                setIdCheckResult("사용 가능한 아이디입니다.", "green", "true");
            }
        } else {
            alert(parsed.message || "알 수 없는 오류가 발생했습니다.");
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


