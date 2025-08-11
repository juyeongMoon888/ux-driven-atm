package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.dto.AccountSummaryDto;
import com.mybank.atmweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PageController {

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
        return "bank/index";
    }

    @GetMapping("/bank/open-account")
    public String openAccountForm(Model model) {
        model.addAttribute("bankTypes", BankType.values());
        return "bank/open-account";
    }

    @GetMapping("/bank/accounts")
    public String accountList() {
        return "bank/account-list";
    }

    @GetMapping("/bank/transfer")
    public String transfer(@RequestParam String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        return "bank/transfer";
    }

    @GetMapping("/bank/account-history")
    public String accountHistoryList(@RequestParam String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        return "bank/account-history";
    }
}
