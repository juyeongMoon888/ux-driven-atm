package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Role;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //Spring Security 에서 제공하는 인터페이스

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(SignupForm form) {
        //아이디 중복 확인 버튼과 별개로 아이디 중복 체크
        if (userRepository.existsByLoginId(form.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        //비밀번호 암호화 및 저장
        User user = new User();
        user.setLoginId(form.getLoginId());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setName(form.getName());
        user.setResidentNumber(form.getResidentNumber());
        user.setGender(form.getGender());
        user.setPhoneNumber(form.getPhoneNumber());
        user.setRole(Role.USER); //일단 항상 USER

        userRepository.save(user);
    }
    //아이디 중복 확인
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }
}
