import { tryRefreshToken } from "./tryRefreshToken.js";

export async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("accessToken");

    const res = await fetch(url, {
        ...options,
        headers: {
            (options.headers || {}),
            Authorization: `Bearer ${token}`
        }
    });

    if (res.status === 401) {
        const newToken = await tryRefreshToken();

        if (newToken) {
            res = await fetch(url, {
                ...options,
                headers: {
                    ...(options.headers || {}),
                    Authorization: `Bearer ${newToken}`
                }
            });
        } else {
            localStorage.removeItem("accessToken");
            window.location.href = "/login";
            return null;
        }
    }
    return res;
}