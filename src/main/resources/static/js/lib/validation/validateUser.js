export function validateUser(user) {
    console.log("정적 검사 작동 확인");
    const errors = {};

    if (!user.loginId.trim()) errors.loginId = "NOTBLANK_LOGINID";

    if (!user.password.trim()) errors.password = "NOTBLANK_PASSWORD";
    else if (user.password.length < 8) errors.password = "SIZE_PASSWORD";

    if (!user.email.trim()) errors.email = "NOTBLANK_EMAIL";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(user.email)) errors.email = "EMAIL_EMAIL";

    if (!user.name.trim()) errors.name = "NOTBLANK_NAME";

    if (!user.residentNumber.trim()) errors.residentNumber = "NOTBLANK_RESIDENTNUMBER";
    else if (!/^\d{13}$/.test(user.residentNumber)) errors.residentNumber = "SIZE_RESIDENTNUMBER";

    if (!user.gender.trim()) errors.gender = "NOTBLANK_GENDER";

    if (!user.phoneNumber.trim()) errors.phoneNumber = "NOTBLANK_PHONENUMBER";
    else if (!/^\d{10,11}$/.test(user.phoneNumber)) errors.phoneNumber = "PATTERN_PHONENUMBER";

    return Object.keys(errors).length > 0 ? errors : null;
}