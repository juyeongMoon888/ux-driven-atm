package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.service.ATMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/atm")
public class ATMController {

    private final ATMService atmService;


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest dto) {
        User user = atmService.login(dto.getResidentNumber(), dto.getPassword());
        return ResponseEntity.ok("로그인 성공" + user.getName());
    }
}
