import { isBlank } from "/js/lib/validation/isBlank.js"

export function validateUser(user) {
    console.log("정적 검사 작동 확인");
    const errors = {};

    if ("loginId" in user && isBlank(user.loginId)) errors.loginId = "NOTBLANK_LOGINID";

    if ("password" in user) {
        if (isBlank(user.password)) {
            errors.password = "NOTBLANK_PASSWORD";
        } else if (user.password.length < 8) {
            errors.password = "SIZE_PASSWORD";
        }
    }

    if ("email" in user) {
        if (isBlank(user.email)) {
            errors.email = "NOTBLANK_EMAIL";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(user.email)) {
            errors.email = "EMAIL_EMAIL";
        }
    }

    if ("name" in user && isBlank(user.name)) errors.name = "NOTBLANK_NAME";

    if ("residentNumber" in user) {
        if (isBlank(user.residentNumber)) {
            errors.residentNumber = "NOTBLANK_RESIDENTNUMBER";
        } else if (!/^\d{13}$/.test(user.residentNumber)) {
            errors.residentNumber = "SIZE_RESIDENTNUMBER";
        }
    }

    if ("gender" in user && isBlank(user.gender)) errors.gender = "NOTBLANK_GENDER";

    if ("phoneNumber" in user) {
        if (isBlank(user.phoneNumber)) {
            errors.phoneNumber = "NOTBLANK_PHONENUMBER";
        } else if (!/^\d{10,11}$/.test(user.phoneNumber)) {
            errors.phoneNumber = "PATTERN_PHONENUMBER";
        }
    }

    if ("bank" in user && isBlank(user.bank)) errors.bank = "은행을 선택하세요";
    if ("toAccountNumber" in user && isBlank(user.toAccountNumber)) errors.toAccountNumber = "계좌번호를 입력하세요";

    return Object.keys(errors).length > 0 ? errors : null;
}