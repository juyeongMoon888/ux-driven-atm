import ApiError from "./errors/ApiError.js";

async function fetchMyInfo() {
    try {
        const response = await fetch("/api/users/me", {
            headers: {
                Authorization: "Bearer: " + token
            }
        });

        if (!response.ok) {
            const errorData = await response.json();

            if (response.status === 401) {
                throw new ApiError (errorData.code, errorData.message);
            } else if (response.status === 500) {
                throw new Error ("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } else {
                throw new Error ("알 수 없는 오류 (code: ${response.status});
            }
        }

        const data = await response.json();
        greetingEl.textContent=`안녕하세요,${data.name}님`;
    }
    //🔄 수정 대기
    catch (err) {
        resetUIToLoggedOut();

        if (err.instanceOf ApiError && err.code === "SESSION_EXPIRED") {
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            location.href = "/login";
        } else if (err instanceOf TypeError && err.message === "Failed to fetch") {
            alert("서버가 꺼졌습니다. 복구를 기다립니다...");
            tryOnceToDetectRecovery(); // 서버 꺼졌을 때만 감시 시작
        } else {
            alert(err.message);
        }
        }
    }
};
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const loginBtn = document.getElementById("loginBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const greetingEl = document.getElementById("greeting");

    // UI 초기화 함수
    function resetUIToLoggedOut() {
        loginBtn.style.display = "block";
        logoutBtn.style.display = "none";
        if (greetingEl) greetingEl.textContent = "";
        localStorage.removeItem("token");
    }

    // 인증된 상태일 때 사용자 정보 요청
    if (token) {
        loginBtn.style.display = "none";
        logoutBtn.style.display = "block";

        fetchMyInfo();

    } else {
        // 로그인 안 된 상태
        loginBtn.style.display = "block";
        logoutBtn.style.display = "none";
    }

    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token");
        location.reload();
    });
});