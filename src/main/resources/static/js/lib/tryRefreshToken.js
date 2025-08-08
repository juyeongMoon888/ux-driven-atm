export async function tryRefreshToken() {
console.log("tryRefreshToken 진입");
let accessToken;
    try {
        const res = await fetch("/api/auth/token/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!res.ok) {
            throw new Error("토큰 갱신 실패");
        }
        const parsed = await res.json();

        accessToken = parsed.data?.accessToken;
        if (!accessToken) {
            console.warn("⚠️ accessToken이 응답에 없습니다:", parsed);
            return null;
        }
        localStorage.setItem("accessToken", accessToken);
        return accessToken;
    } catch (err) {
        console.error("토큰 재발급 중 예외 발생:", err);
        return null;
    }
}