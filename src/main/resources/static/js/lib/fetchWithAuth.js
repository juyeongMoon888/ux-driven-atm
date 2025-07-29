import { tryRefreshToken } from "./tryRefreshToken.js";
import { handleErrorResponse } from "./utils.js";

export async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("accessToken");

    if (!token || token === "null" || token === "undefined") {
        const newToken = await tryRefreshToken();
        if (!newToken) {
            localStorage.removeItem("accessToken");
            window.location.href = "/login";
            return null;
        }
        return fetchWithAuth(url, options);
    }

    let res = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            Authorization: `Bearer ${token}`
        },
        credentials: "include"
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

    if (!res.ok) {
        await handleErrorResponse(res);
    }
    return res;
}