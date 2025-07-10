import { showValidationMessages } from "./lib/utils.js";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");
    form.addEventListener("submit", submitSignup);
})

function submitSignup(e) {
    e.preventDefault();

    const formElement = document.getElementById("signupForm")

    if(formElement.dataset.idChecked !== "true") {
        alert("아이디 중복 확인을 먼저 해주세요.");
        return;
    }
    const user = {
        loginId: document.getElementById("loginId").value,
        password: document.getElementById("password").value,
        name: document.getElementById("name").value,
        residentNumber: document.getElementById("residentNumber").value,
        gender: document.getElementById("gender").value,
        phoneNumber: document.getElementById("phoneNumber").value
    };
    fetch("/api/users/signup", {
        method: "POST",
        headers: {
        "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    })
    .then(async res => {
        const data = await res.json();
        if (data.code === "SIGNUP_SUCCESS") {
            alert(data.message || "회원가입 성공");
            location.href="/login";
        } else if (data.code === "VALIDATION_FAILED"){
            showValidationMessages(data.details);
        } else {
            alert(data.message || "회원가입 실패");
        }
    })
    .catch(err => {
        if (err instanceOf TypeError && err.message === "Failed to fetch") {
            alert("서버와 연결할 수 없습니다. 인터넷 연결을 확인해주세요.");
            tryOnceToDetectRecovery();
        }
    });
}

//아이디 중복 확인 메서드
function checkLoginId() {
    const loginId = document.getElementById("loginId").value;
    const resultElement = document.getElementById("id-check-result");
    const formElement = document.getElementById("signupForm");//아이디 중복 확인 상태 저장

    if(!loginId) {
        resultElement.innerText="아이디를 입력해주세요.";
        resultElement.style.color="red";
        formElement.dataset.idChecked = "false";
        return;
    }

    fetch(`/api/users/check-id?loginId=${encodeURIComponent(loginId)}`)
    .then(async response => {
        if (!response.ok) throw new Error("서버 오류");
            return response.json();
    })
    .then (data => {
        if(data.duplicate) {
            resultElement.innerText = "사용 가능한 아이디입니다.";
            resultElement.style.color = "green";
            formElement.dataset.idChecked = "true";
        } else {
            resultElement.innerText = "이미 사용 중인 아이디입니다."
            resultElement.style.color = "red";
            formElement.dataset.idChecked = "false";
        }
    })
    .catch(error => {
        console.error("아이디 중복 확인 중 에러:", error);
        resultElement.innerText="서버 오류가 발생했습니다.";
        resultElement.style.color = "red";
        formElement.dataset.idChecked = "false";
    });
}
