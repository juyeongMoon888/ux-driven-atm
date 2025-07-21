import { ERROR_MESSAGES } from "../constants/errorMessages.js";
export function showFieldErrors(errors, fields = []) {
  // 에러 초기화
  for (const field of fields) {
    const el = document.getElementById(`${field}-error`);
    if (el) el.textContent = "";
  }

  // 에러 표시
  for (const [field, code] of Object.entries(errors)) {
    const el = document.getElementById(`${field}-error`);
    const message = ERROR_MESSAGES[code] || "입력값이 올바르지 않습니다.";
    if (el) {
      el.textContent = message;
      el.style.color = "red";
    }
  }
}