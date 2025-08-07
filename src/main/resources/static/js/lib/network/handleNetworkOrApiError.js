import { showFieldErrors } from "/js/lib/validation/renderFieldError.js";
import { ApiError } from "/js/errors/ApiError.js";

export function handleNetworkOrApiError(err) {
    if (err instanceof ApiError) {
        if (err.code === "TOKEN_EXPIRED") {
            alert(ErrorCode.TOKEN_EXPIRED);
            location.href = "/login";
        } else if (err.code === "VALIDATION_FAILED") {
            showFieldErrors()
        } else if (err.code == "INVALID_CREDENTIALS") {
            alert(ErrorCode.INVALID_CREDENTIALS);
            location.href = "/login";
        } else if (err.code == "TOKEN_NOT_FOUND") {
            alert(ErrorCode.TOKEN_NOT_FOUND);
            location.href = "/login";
        } else if (err.code == "BANK_INVALID") {
            const errors = {bankSelect: "BANK_INVALID"};
            showFieldErrors(errors, [bankSelect]);
        }
    } else if (err instanceof TypeError && err.message === "Failed to fetch") {
        alert(ErrorCode.NETWORK_DOWN);
        tryOnceToDetectRecovery();
    } else {
        alert(err.message || ErrorCode.UNKNOWN);
    }
}