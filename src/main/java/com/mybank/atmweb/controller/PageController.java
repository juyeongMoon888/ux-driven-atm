package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

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

    //계좌 개설
    @GetMapping("/accounts")
    public String accountsForm(Model model) {
        model.addAttribute("bankTypes", BankType.values());
        return "accounts";
    }

    //입출금
    @GetMapping("/transactions")
    public String transactionForm() {
        return "transactions";
    }
}
