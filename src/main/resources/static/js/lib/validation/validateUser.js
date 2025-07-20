export function validateUser(user) {
    const errors = {};
    if (!user.loginId.trim()) return "아이디를 입력해주세요";

    if (!user.password.trim()) return "비밀번호를 입력해주세요";
    if (user.password.length < 8) return "비밀번호는 8자 이상이어야 합니다";

    if (!user.email.trim()) return "이메일을 입력해주세요";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(user.email)) return "이메일 형식이 올바르지 않습니다";

    if (!user.name.trim()) return "이름을 입력해주세요";

    if (!user.residentNumber.trim()) return "주민등록번호를 입력해주세요";
    if (!/^\d{13}$/.test(user.residentNumber)) return "주민등록번호는 13자리 숫자여야 합니다";

    if (!user.gender.trim()) return "성별을 입력해주세요";

    if (!user.phoneNumber.trim()) return "전화번호를 입력해주세요";
    if (!/^\d{10,11}$/.test(user.phoneNumber)) return "전화번호는 10~11자리 숫자로 입력해주세요";
    return null;
}