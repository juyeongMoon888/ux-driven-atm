export function handleNetworkOrApiError(err) {
    if (err instanceof ApiError) {
        if (err.code === "TOKEN_EXPIRED") {
            alert(ErrorCode.TOKEN_EXPIRED);
            location.href = "/login";
        } else if (err.code === "VALIDATION_FAILED") {
            showErrorMessagesFromServer(parsed.data);
        }
    } else if (err instanceof TypeError && err.message === "Failed to fetch") {
        alert(ErrorCode.NETWORK_DOWN);
        tryOnceToDetectRecovery();
    } else {
        alert(err.message || ErrorCode.UNKNOWN);
    }
}