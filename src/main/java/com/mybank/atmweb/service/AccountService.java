package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.AccountRequestDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static com.mybank.atmweb.global.code.ErrorCode.BANK_INVALID;
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public void createAccount(Long userId, AccountRequestDto dto) {
        BankType bankType;

        try {
            bankType = BankType.valueOf(dto.getBank());

        } catch (IllegalArgumentException e) {
            log.error("[BANK_INVALID] dto.getBank() = {}, BankType enum과 매핑 실패", dto.getBank());
            throw new CustomException(BANK_INVALID);
        }

        User user = findUserByIdOrThrow(userId);

        Account account = Account.builder()
                //계좌 소유자 정보 연결
                .owner(user)
                .accountName(dto.getAccountName())
                .bank(bankType)
                .accountNumber(generateAccountNumber(bankType.getPrefix()))
                .balance(0L)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Account saved = accountRepository.save(account);
        log.info("✅ 저장된 계좌 ID: {}", saved.getId());
    }
    //로직 UUID로 만든거 수정함. (너무 단순해서)
    public String generateAccountNumber(String prefix) {
        String randomDigits = String.format("%08d", new Random().nextInt(100_100_100));
        return prefix + randomDigits;
    }

    public User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }


}
