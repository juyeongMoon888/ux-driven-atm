export async function tryRefreshToken() {
let accessToken;
    try {
        const res = await fetch("/api/auth/token/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!res.ok) {
        console.warn("❌ 토큰 재발급 실패", res.status);
            throw new Error("토큰 갱신 실패");
        }
        const parsed = await res.json();

        accessToken = parsed.data?.accessToken;
        if (accessToken) {
            localStorage.setItem("accessToken", accessToken);
                    console.log("✅ 토큰 재발급 성공:", localStorage.getItem("accessToken"));
        } else {
            console.warn("⚠️ accessToken이 응답에 없습니다:", parsed);
        }
        return accessToken;
    } catch (e) {
        console.log("alert 전에 도달");
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");

        return null;
    }
}