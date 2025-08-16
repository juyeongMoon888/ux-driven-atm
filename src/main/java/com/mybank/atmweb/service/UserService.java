package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Role;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.domain.UserProfile;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.UserProfileRepository;
import com.mybank.atmweb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;

    public void signup(SignupForm form) {
        //아이디 중복 확인 버튼과 별개로 아이디 중복 체크
        if (userRepository.existsByLoginId(form.getLoginId())) {
            throw new CustomException(ErrorCode.USER_EXIST);
        }

        //비밀번호 암호화 및 저장
        User user = new User();
        user.setLoginId(form.getLoginId());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setName(form.getName());
        user.setResidentNumber(formatResidentNumber(form.getResidentNumber()));
        user.setGender(form.getGender());
        user.setPhoneNumber(form.getPhoneNumber());
        user.setRole(Role.USER);

        userRepository.save(user);

        //UserProfile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setName(user.getName());
        profile.setBirth(extractBirthFromResidentNumber(user.getResidentNumber()));
        profile.setPhone(user.getPhoneNumber());
        userProfileRepository.save(profile);
    }
    public boolean isDuplicatedLoginId(String loginId) {
        return userRepository.findByLoginId(loginId).isPresent();
    }

    private String formatResidentNumber(String rn) {
        if (rn != null && rn.length() == 13) {
            return rn.substring(0, 6) + "-" + rn.substring(6);
        }
        throw new IllegalArgumentException("주민번호 형식 오류");

    }
    private String extractBirthFromResidentNumber(String rn) {
        String birth = rn.substring(0, 6);
        return birth.substring(0, 2) + "-" + birth.substring(2, 4) + "-" + birth.substring(4);
    }
}
