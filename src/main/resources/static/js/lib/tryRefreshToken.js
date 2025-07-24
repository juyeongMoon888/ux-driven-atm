export async function tryRefreshToken() {
    try {
        const res = await fetch("/api/token/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!res.ok) {
            throw new Error("토큰 갱신 실패");
        }

        const data = await res.json();
        localStorage.setItem("accessToken", data.accessToken);
        return data.accessToken;
    } catch (e) {
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("accessToken");
        window.location.href = "/login";
        return null;
    }
}