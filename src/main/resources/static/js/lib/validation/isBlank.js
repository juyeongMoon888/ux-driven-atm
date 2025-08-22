export function isBlank(v) {
  return v == null || (typeof v === "string" && v.trim().length === 0);
}