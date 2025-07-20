export function showFieldErrors(errors, fields = []) {
  // 에러 초기화
  for (const field of fields) {
    const el = document.getElementById(`${field}-error`);
    if (el) el.textContent = "";
  }

  // 에러 표시
  for (const [field, message] of Object.entries(errors)) {
    const el = document.getElementById(`${field}-error`);
    if (el) {
      el.textContent = message;
      el.style.color = "red";
    }
  }
}