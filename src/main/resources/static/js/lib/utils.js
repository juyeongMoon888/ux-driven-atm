export function showValidationMessages(details) {
    for (const field in details) {
        const message = details[field];
        const errorEl = document.getElementById(`${field}-error`);
        if (errorEl) {
            errorEl.textContent = message;
        }
    }
}