import { ErrorCode } from "/js/lib/constants/errorMessages.js";
import { ApiError } from "/js/errors/ApiError.js";

export function handleApiFailure(res, parsed) {
    // JSON 파싱 실패한 응답인 경우
    if (!parsed || parsed.raw) {
        console.error("서버 응답이 JSON 형식이 아닙니다.");
        throw new Error (ErrorCode.RESPONSE_MALFORMED);
    }

    // 정상 JSON인 경우 상태 코드별 분기
    switch (res.status) {
        case 400:
            switch (parsed.code) {
                case "VALIDATION_FAILED":
                    throw new ApiError(ErrorCode.VALIDATION_FAILED, parsed.message, parsed.data);
                case "DATA_INTEGRITY_VIOLATION":
                    throw new ApiError(ErrorCode.DATA_INTEGRITY_VIOLATION, parsed.message);
                case "BANK_INVALID":
                    throw new ApiError(ErrorCode.BANK_INVALID, parsed.message);
            }
        case 401:
            switch (parsed.code) {
                case "INVALID_CREDENTIALS":
                    throw new ApiError(ErrorCode.INVALID_CREDENTIALS, parsed.message);
                case "TOKEN_NOT_FOUND":
                    throw new ApiError(ErrorCode.TOKEN_NOT_FOUND, "로그인 후 이용가능한 서비스입니다.");
                case "TOKEN_EXPIRED":
                    throw new ApiError(ErrorCode.TOKEN_EXPIRED, "세션이 만료되었습니다.");
            }
        case 500:
            throw new Error(ErrorCode.SERVER_ERROR);
        default:
            throw new Error(`알 수 없는 오류 (code: ${res.status})`);
    }
}