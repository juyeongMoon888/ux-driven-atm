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
    } catch (err) {
        console.error("토큰 재발급 중 예외 발생:", err);
        return null;
    }
}