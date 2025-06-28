package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    //첫 메인 페이지
    @GetMapping("/")
    public String mainPage() {
        return "main"; //인증 성공 후 보여줄 메인 페이지
    }

    //로그인
    @GetMapping("/login")
    public String loginForm() {
        return "login"; //커스텀 로그인 페이지
    }

    //회원가입 폼
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    //입출금 페이지
    @GetMapping("/banking")
    public String bankingPage() {
        return "banking";
    }
}
