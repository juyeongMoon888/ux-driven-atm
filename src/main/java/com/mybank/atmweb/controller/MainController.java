package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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



    //입출금 버튼 누르면

    //회원가입 폼
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    //회원가입 완료 후 대시보드
    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute SignupForm form, HttpSession httpSession) {
        userService.registerUser(form);
        return "redirect:/login";
    }


}
