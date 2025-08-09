export const ErrorCode = {
  NOTBLANK_LOGINID: "아이디를 입력해주세요",
  NOTBLANK_PASSWORD: "비밀번호를 입력해주세요",
  SIZE_PASSWORD: "비밀번호는 8자 이상이어야 합니다",
  NOTBLANK_EMAIL: "이메일을 입력해주세요",
  EMAIL_EMAIL: "이메일 형식이 올바르지 않습니다",
  NOTBLANK_NAME: "이름을 입력해주세요",
  NOTBLANK_RESIDENTNUMBER: "주민등록번호를 입력해주세요",
  SIZE_RESIDENTNUMBER: "주민등록번호는 13자리 숫자여야 합니다",
  NOTBLANK_GENDER: "성별을 입력해주세요",
  NOTBLANK_PHONENUMBER: "전화번호를 입력해주세요",
  PATTERN_PHONENUMBER: "전화번호는 10~11자리 숫자로 입력해주세요",

  VALIDATION_FAILED: "입력값이 올바르지 않습니다.",
  TOKEN_EXPIRED: "세션이 만료되었습니다. 다시 로그인해주세요.",
  SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
  NETWORK_DOWN: "서버가 꺼졌습니다. 복구를 기다립니다...",
  RESPONSE_MALFORMED: "서버 응답이 올바르지 않습니다. 잠시 후 다시 시도해 주세요.",
  DATA_INTEGRITY_VIOLATION: "이미 존재하는 아이디이거나 잘못된 데이터입니다.",
  INVALID_CREDENTIALS: "아이디 또는 비밀번호가 일치하지 않습니다.",
  TOKEN_NOT_FOUND: "로그인 후 이용가능한 서비스입니다.",
  BANK_INVALID: "유효하지 않은 은행입니다.",
  UNKNOWN_ERROR: "알 수 없는 오류가 발생했습니다"
};