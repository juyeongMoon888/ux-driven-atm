package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(SignupForm form) {
        User user = new User();
        user.setLoginId(form.getLoginId());
        user.setPassword(form.getPassword());
        user.setGender(form.getGender());
        user.setPhoneNumber(form.getPhoneNumber());
        user.setResidentNumber(form.getResidentNumber());
        userRepository.save(user);
    }
}
