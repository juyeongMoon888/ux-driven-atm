export function handleApiFailure(res, parsed) {
    switch (res.status) {
        case 400:
            throw new ApiError(ErrorCode.VALIDATION_FAILED, parsed.message);
        case 401:
            throw new ApiError(ErrorCode.TOKEN_EXPIRED, "세션이 만료되었습니다.");
        case 500:
            throw new Error(ErrorCode.SERVER_ERROR);
        default:
            throw new Error(`알 수 없는 오류 (code: ${res.status})`);
    }
}