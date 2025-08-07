import { tryRefreshToken } from "/js/lib/tryRefreshToken.js";
import { ErrorCode } from "/js/lib/constants/errorMessages.js";

export async function fetchWithAuth(url, options = {}) {
    let token = localStorage.getItem("accessToken");

    let res = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            Authorization: `Bearer ${token}`
        },
        credentials: "include"
    });

    //서버 요청 후 accessToken 만료 여부 감지됨
    if (res.status === 401) {
        const newToken = await tryRefreshToken();

        if (newToken) {
        localStorage.setItem("accessToken", newToken);
            res = await fetch(url, {
                ...options,
                headers: {
                    ...(options.headers || {}),
                    Authorization: `Bearer ${newToken}`
                },
                credentials: "include"
            });
        } else {
            return { error: ErrorCode.TOKEN_EXPIRED};
        }
    }
    return res;
}