import { ErrorCode } from "/js/lib/constants/errorMessages.js";

export function showFieldErrors(errors, fields = []) {
  // 1. 에러 초기화 (필드 목록이 있으면 해당 필드만 초기화)
  for (const field of fields) {
    const el = document.getElementById(`${field}-error`);
    if (el) el.textContent = "";
  }

  // 2. 에러 표시
  for (const [field, code] of Object.entries(errors)) {
    const el = document.getElementById(`${field}-error`);
    const message = ErrorCode[code] || "입력값이 올바르지 않습니다.";
    if (el) {
      el.textContent = message;
      el.style.color = "red";
    }
  }
}