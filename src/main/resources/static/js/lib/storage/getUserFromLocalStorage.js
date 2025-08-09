export function getUserFromLocalStorage() {
    const userJson = localStorage.getItem("user");
    try {
        return userJson ? JSON.parse(userJson) : null;
    } catch (err) {
        console.warn("로컬스토리지 파싱 실패, 제거합니다");
        localStorage.removeItem("user");
        return null;
    }
}