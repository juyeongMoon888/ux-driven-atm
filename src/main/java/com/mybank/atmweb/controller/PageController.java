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

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @GetMapping("/bank")
    public String bankPage() {
        return "bank";
    }

    @GetMapping("/bank/open-account")
    public String openAccountForm(Model model) {
        model.addAttribute("bankTypes", BankType.values());
        return "bank/open-account";
    }

    //입출금
    @GetMapping("/transactions")
    public String transactionForm() {
        return "transactions";
    }
}
